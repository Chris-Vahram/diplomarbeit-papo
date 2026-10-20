package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.hibernate.StaleStateException;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// "Mein Webshop" editor (shop-builder spec): page management, whole-page draft saves with
// version check, publishing, one-step restore and the theme draft (design.md Decision 1).
// Every operation resolves the shop from the authenticated partner; page ids of other shops
// are answered like unknown ones.
@ApplicationScoped
public class ShopEditorService {

    static final String PAGE_CONFLICT = "Diese Seite wurde inzwischen geändert. Bitte neu laden.";
    static final String THEME_CONFLICT = "Das Design wurde inzwischen geändert. Bitte neu laden.";

    // Free-page templates under shop-templates/default/; "leer" is the empty page.
    private static final Map<String, String> FREE_TEMPLATES = Map.of(
            "ueber-uns", "free-ueber-uns.json",
            "kontakt", "free-kontakt.json");

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopPageService pageService;

    public ShopPageListDto pages(Partner partner) {
        return pageList(shopService.findOrCreate(partner));
    }

    public ShopPageDetailDto page(Partner partner, long pageId) {
        return detail(designablePage(shopService.findOrCreate(partner), pageId));
    }

    @Transactional
    public ShopPageDetailDto createPage(Partner partner, ShopEditorRequests.CreatePage request) {
        PartnerShop shop = shopService.findOrCreate(partner);
        String template = request.template() == null || request.template().isBlank() ? "leer" : request.template();
        if (!template.equals("leer") && !FREE_TEMPLATES.containsKey(template)) {
            throw new ApiException(400, "Diese Vorlage gibt es nicht.");
        }
        ShopPage page = pageService.createFreePage(shop, request.menuName(), request.segment());
        if (!template.equals("leer")) {
            JsonNode content = pageService.withBlockIds(pageService.readTemplate(FREE_TEMPLATES.get(template)));
            pageService.validateContent(page, content, false);
            page.draftContent = content.toString();
        }
        flush(PAGE_CONFLICT);
        return detail(page);
    }

    // Mandatory pages keep their fixed address (the public routes rely on it); their menu
    // name and SEO fields are editable like those of free pages.
    @Transactional
    public ShopPageDto updateSettings(Partner partner, long pageId, ShopEditorRequests.PageSettings request) {
        ShopPage page = designablePage(shopService.findOrCreate(partner), pageId);
        requireVersion(page.version, request.version(), PAGE_CONFLICT);
        String menuName = request.menuName() == null ? "" : request.menuName().strip();
        if (menuName.isEmpty() || menuName.length() > ShopPage.MENU_NAME_MAX_LENGTH) {
            throw new ApiException(400, "Der Menüname muss zwischen 1 und "
                    + ShopPage.MENU_NAME_MAX_LENGTH + " Zeichen lang sein.");
        }
        String segment = request.segment() == null ? page.segment : request.segment().strip();
        if (!segment.equals(page.segment)) {
            if (page.kind != ShopPageKind.FREE) {
                throw new ApiException(409, "Die Adresse einer Pflichtseite kann nicht geändert werden.");
            }
            pageService.requireNotLinked(page);
            page.segment = pageService.requireFreeSegment(page.shop, segment, page);
        }
        page.menuName = menuName;
        page.seoTitle = optional(request.seoTitle(), ShopPage.SEO_TITLE_MAX_LENGTH, "Der SEO-Titel");
        page.seoDescription = optional(request.seoDescription(), ShopPage.SEO_DESCRIPTION_MAX_LENGTH,
                "Die SEO-Beschreibung");
        page.updatedAt = Instant.now();
        flush(PAGE_CONFLICT);
        return ShopPageDto.from(page);
    }

    // Autosave target: lenient validation, so half-filled blocks are kept (publish checks all).
    @Transactional
    public ShopPageDto saveDraft(Partner partner, long pageId, ShopEditorRequests.Draft request) {
        ShopPage page = designablePage(shopService.findOrCreate(partner), pageId);
        requireVersion(page.version, request.version(), PAGE_CONFLICT);
        pageService.validateContent(page, request.content(), false);
        // Compared as JSON: jsonb normalises key order, so the stored string differs from the request.
        if (!request.content().equals(pageService.parse(page.draftContent))) {
            page.draftContent = request.content().toString();
            page.updatedAt = Instant.now();
        }
        flush(PAGE_CONFLICT);
        return ShopPageDto.from(page);
    }

    @Transactional
    public ShopPageDto publish(Partner partner, long pageId, Long version) {
        ShopPage page = designablePage(shopService.findOrCreate(partner), pageId);
        requireVersion(page.version, version, PAGE_CONFLICT);
        pageService.publish(page);
        flush(PAGE_CONFLICT);
        return ShopPageDto.from(page);
    }

    @Transactional
    public ShopPageDto unpublish(Partner partner, long pageId) {
        ShopPage page = pageService.unpublish(designablePage(shopService.findOrCreate(partner), pageId));
        flush(PAGE_CONFLICT);
        return ShopPageDto.from(page);
    }

    // The previous published version becomes published version and draft; the replaced
    // version becomes the new previous one, so a restore can itself be undone once.
    @Transactional
    public ShopPageDetailDto restore(Partner partner, long pageId, Long version) {
        ShopPage page = designablePage(shopService.findOrCreate(partner), pageId);
        requireVersion(page.version, version, PAGE_CONFLICT);
        if (!page.online || page.previousPublishedContent == null) {
            throw new ApiException(409, "Für diese Seite gibt es keine vorherige veröffentlichte Version.");
        }
        String previous = page.previousPublishedContent;
        pageService.validateContent(page, previous);
        page.previousPublishedContent = page.publishedContent;
        page.publishedContent = previous;
        page.draftContent = previous;
        page.publishedAt = Instant.now();
        page.updatedAt = page.publishedAt;
        flush(PAGE_CONFLICT);
        return detail(page);
    }

    @Transactional
    public void delete(Partner partner, long pageId) {
        pageService.delete(designablePage(shopService.findOrCreate(partner), pageId));
    }

    // pageIds = every designable page of the shop in the new navigation order.
    @Transactional
    public ShopPageListDto reorder(Partner partner, ShopEditorRequests.PageOrder request) {
        PartnerShop shop = shopService.findOrCreate(partner);
        List<ShopPage> pages = designablePages(shop);
        List<Long> ids = request.pageIds() == null ? List.of() : request.pageIds();
        if (ids.size() != pages.size() || !new HashSet<>(ids).equals(new HashSet<>(pages.stream().map(page -> page.id).toList()))) {
            throw new ApiException(409, "Die Seitenliste ist nicht mehr aktuell. Bitte neu laden.");
        }
        for (ShopPage page : pages) {
            page.position = ids.indexOf(page.id);
        }
        flush(PAGE_CONFLICT);
        return pageList(shop);
    }

    public ShopThemeDto theme(Partner partner) {
        return themeDto(pageService.theme(shopService.findOrCreate(partner)));
    }

    @Transactional
    public ShopThemeDto saveThemeDraft(Partner partner, ShopEditorRequests.ThemeDraft request) {
        ShopTheme theme = pageService.theme(shopService.findOrCreate(partner));
        requireVersion(theme.version, request.version(), THEME_CONFLICT);
        ShopThemeValidator.validate(request.theme());
        if (!request.theme().equals(pageService.parse(theme.draftTheme))) {
            theme.draftTheme = request.theme().toString();
            theme.updatedAt = Instant.now();
        }
        flush(THEME_CONFLICT);
        return themeDto(theme);
    }

    @Transactional
    public ShopThemeDto publishTheme(Partner partner, Long version) {
        PartnerShop shop = shopService.findOrCreate(partner);
        requireVersion(pageService.theme(shop).version, version, THEME_CONFLICT);
        ShopTheme theme = pageService.publishTheme(shop);
        flush(THEME_CONFLICT);
        return themeDto(theme);
    }

    private ShopPageListDto pageList(PartnerShop shop) {
        return new ShopPageListDto(designablePages(shop).stream().map(ShopPageDto::from).toList(),
                pageService.onlineCount(shop), ShopPageService.MAX_ONLINE_PAGES,
                pageService.designableCount(shop), ShopPageService.MAX_PAGES);
    }

    private ShopPageDetailDto detail(ShopPage page) {
        return new ShopPageDetailDto(ShopPageDto.from(page), pageService.parse(page.draftContent),
                page.publishedContent == null ? null : pageService.parse(page.publishedContent));
    }

    private ShopThemeDto themeDto(ShopTheme theme) {
        List<ShopThemeDto.Font> fonts = Arrays.stream(ShopFont.values())
                .map(font -> new ShopThemeDto.Font(font.id, font.family)).toList();
        return new ShopThemeDto(pageService.parse(theme.draftTheme), pageService.parse(theme.publishedTheme),
                !Objects.equals(theme.draftTheme, theme.publishedTheme), theme.version, fonts);
    }

    private static List<ShopPage> designablePages(PartnerShop shop) {
        return ShopPage.list("shop = ?1 and kind <> ?2 order by position, id", shop, ShopPageKind.SYSTEM);
    }

    private static ShopPage designablePage(PartnerShop shop, long pageId) {
        ShopPage page = ShopPage.<ShopPage>find("id = ?1 and shop = ?2 and kind <> ?3", pageId, shop,
                ShopPageKind.SYSTEM).firstResult();
        if (page == null) {
            throw new ApiException(404, "Diese Seite gibt es nicht.");
        }
        return page;
    }

    // A missing version would silently match a never-saved page (version 0), so it is refused.
    private static void requireVersion(long stored, Long sent, String message) {
        if (sent == null) {
            throw new ApiException(400, "Die Versionsangabe fehlt. Bitte die Seite neu laden.");
        }
        if (stored != sent) {
            throw new ApiException(409, message);
        }
    }

    // Flushes now so the new version goes into the response, and so a save that raced past
    // requireVersion surfaces as the same 409 instead of a failed commit.
    private static void flush(String conflictMessage) {
        try {
            ShopPage.flush();
        } catch (OptimisticLockException | StaleStateException exception) {
            throw new ApiException(409, conflictMessage);
        }
    }

    private static String optional(String value, int maxLength, String label) {
        String text = value == null ? "" : value.strip();
        if (text.length() > maxLength) {
            throw new ApiException(400, label + " darf höchstens " + maxLength + " Zeichen lang sein.");
        }
        return text.isEmpty() ? null : text;
    }
}
