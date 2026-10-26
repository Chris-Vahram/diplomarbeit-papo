package at.papo.partnerportal.shop;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

// Pages and theme of a shop (shop-pages, shop-theme specs): default creation, limits,
// per-page publishing and theme publishing. Editor operations live in ShopEditorService.
@ApplicationScoped
public class ShopPageService {

    public static final int MAX_ONLINE_PAGES = 6;
    public static final int MAX_PAGES = 20;

    private static final Pattern SEGMENT = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ShopCatalogService catalogService;

    @Inject
    ShopTemplateCatalog templateCatalog;

    @Inject
    ShopAssetService assetService;

    // Idempotent: creates the theme and every missing slot page. Mandatory pages start
    // published from their default catalogue template; system pages only reserve slot and segment.
    @Transactional
    public void ensureDefaults(PartnerShop shop) {
        Instant now = Instant.now();
        if (ShopTheme.count("shop", shop) == 0) {
            String theme = readTemplate("theme.json").toString();
            ShopThemeValidator.validate(parse(theme));
            ShopTheme entity = new ShopTheme();
            entity.shop = shop;
            entity.draftTheme = theme;
            entity.publishedTheme = theme;
            entity.updatedAt = now;
            entity.persist();
        }
        List<ShopPage> createdMandatory = new ArrayList<>();
        for (ShopPageSlot slot : ShopPageSlot.values()) {
            if (ShopPage.count("shop = ?1 and slot = ?2", shop, slot) > 0) {
                continue;
            }
            ShopPage page = new ShopPage();
            page.shop = shop;
            page.kind = slot.kind;
            page.slot = slot;
            page.segment = slot.segment;
            page.menuName = slot.menuName;
            page.position = slot.ordinal();
            page.updatedAt = now;
            if (slot.kind == ShopPageKind.MANDATORY) {
                String content = templateContent(shop, templateCatalog.find(slot.defaultTemplate, slot)
                        .orElseThrow(() -> new IllegalStateException("Shop template missing: " + slot.defaultTemplate)))
                        .toString();
                page.draftContent = content;
                page.publishedContent = content;
                page.publishedAt = now;
                page.online = true;
                createdMandatory.add(page);
            }
            page.persist();
        }
        // Templates link between pages, so they are checked once all slots exist. Existing
        // pages are left alone: a partner's draft may reference an article disabled since,
        // and that must never break startup or shop creation.
        createdMandatory.forEach(page -> validateContent(page, page.draftContent));
    }

    // Free page as an empty draft; the segment is checked like any page URL.
    @Transactional
    public ShopPage createFreePage(PartnerShop shop, String menuName, String segment) {
        if (designableCount(shop) >= MAX_PAGES) {
            throw new ApiException(409, "Ein Shop kann höchstens " + MAX_PAGES + " Seiten haben.");
        }
        String name = menuName == null ? "" : menuName.strip();
        if (name.isEmpty() || name.length() > ShopPage.MENU_NAME_MAX_LENGTH) {
            throw new ApiException(400, "Der Menüname muss zwischen 1 und "
                    + ShopPage.MENU_NAME_MAX_LENGTH + " Zeichen lang sein.");
        }
        ShopPage page = new ShopPage();
        page.shop = shop;
        page.kind = ShopPageKind.FREE;
        page.segment = requireFreeSegment(shop, segment, null);
        page.menuName = name;
        page.position = nextPosition(shop);
        page.draftContent = "{\"blocks\":[]}";
        page.updatedAt = Instant.now();
        page.persist();
        return page;
    }

    // Publishes the draft; keeps the replaced version for one-step restore.
    @Transactional
    public ShopPage publish(ShopPage page) {
        requireDesignable(page);
        if (!page.online && onlineCount(page.shop) >= MAX_ONLINE_PAGES) {
            throw new ApiException(409, "Es können höchstens " + MAX_ONLINE_PAGES + " Seiten gleichzeitig online sein.");
        }
        validateContent(page, page.draftContent);
        ShopDesignRules.requireReadablePage(parse(page.draftContent), parse(theme(page.shop).publishedTheme));
        // Republishing unchanged content must not replace the restore point with itself.
        if (page.publishedContent == null || !parse(page.publishedContent).equals(parse(page.draftContent))) {
            page.previousPublishedContent = page.publishedContent;
        }
        page.publishedContent = page.draftContent;
        page.publishedAt = Instant.now();
        page.updatedAt = page.publishedAt;
        page.online = true;
        return page;
    }

    @Transactional
    public ShopPage unpublish(ShopPage page) {
        requireRemovable(page);
        requireNotLinked(page);
        page.online = false;
        page.updatedAt = Instant.now();
        return page;
    }

    @Transactional
    public void delete(ShopPage page) {
        requireRemovable(page);
        requireNotLinked(page);
        page.delete();
    }

    // Publishing the theme restyles every online page at once; block overrides live in the
    // page snapshots and are therefore untouched.
    @Transactional
    public ShopTheme publishTheme(PartnerShop shop) {
        ShopTheme theme = theme(shop);
        ShopThemeValidator.validate(parse(theme.draftTheme));
        ShopDesignRules.requireReadableTheme(parse(theme.draftTheme),
                ShopPage.list("shop = ?1 and online = true and kind <> ?2", shop, ShopPageKind.SYSTEM), this);
        theme.previousPublishedTheme = theme.publishedTheme;
        theme.publishedTheme = theme.draftTheme;
        theme.updatedAt = Instant.now();
        return theme;
    }

    public ShopTheme theme(PartnerShop shop) {
        ShopTheme theme = ShopTheme.find("shop", shop).firstResult();
        if (theme == null) {
            throw new ApiException(404, PartnerShopService.NOT_REACHABLE);
        }
        return theme;
    }

    // Navigation order; the article-detail page needs an article and is reached from the catalogue.
    public List<ShopPage> navigation(PartnerShop shop) {
        return ShopPage.<ShopPage>list("shop = ?1 and online = true and kind <> ?2 and (slot is null or slot <> ?3) "
                + "order by position, id", shop, ShopPageKind.SYSTEM, ShopPageSlot.ARTICLE_DETAIL);
    }

    public Optional<ShopPage> onlinePage(PartnerShop shop, String segment) {
        return ShopPage.<ShopPage>find("shop = ?1 and segment = ?2 and online = true and kind <> ?3",
                shop, segment, ShopPageKind.SYSTEM).firstResultOptional();
    }

    public List<ShopPage> legalPages(PartnerShop shop) {
        return ShopPage.<ShopPage>list("shop = ?1 and kind = ?2 and slot in ?3 order by position", shop,
                ShopPageKind.SYSTEM, List.of(ShopPageSlot.IMPRESSUM, ShopPageSlot.AGB,
                        ShopPageSlot.DATENSCHUTZ, ShopPageSlot.WIDERRUF));
    }

    public JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new ApiException(400, "Ungültiges JSON.");
        }
    }

    void validateContent(ShopPage page, String content) {
        validateContent(page, parse(content), true);
    }

    void validateContent(ShopPage page, JsonNode content, boolean publishing) {
        ShopBlockValidator.validatePage(content, page.slot, references(page.shop), publishing);
    }

    ShopBlockReferences references(PartnerShop shop) {
        return new ShopBlockReferences() {
            @Override
            public boolean isOwnAsset(long assetId) {
                return ShopAsset.count("id = ?1 and shop = ?2", assetId, shop) > 0;
            }

            @Override
            public boolean categoryExists(long categoryId) {
                return Category.findById(categoryId) != null;
            }

            @Override
            public boolean isCatalogueArticle(long articleId) {
                return catalogService.isCatalogueArticle(shop.partner, articleId);
            }

            // Only online designable pages with their own URL; checked again on every publish.
            @Override
            public boolean isLinkablePage(String segment) {
                return ShopPage.count("shop = ?1 and segment = ?2 and online = true and kind <> ?3 "
                        + "and (slot is null or slot <> ?4)", shop, segment, ShopPageKind.SYSTEM,
                        ShopPageSlot.ARTICLE_DETAIL) > 0;
            }
        };
    }

    // A published button on another online page must never point to a page that disappears
    // or changes its address (shop-pages "Navigation and page URLs").
    void requireNotLinked(ShopPage page) {
        if (!page.online) {
            return;
        }
        List<String> linking = ShopPage.<ShopPage>list("shop = ?1 and online = true and kind <> ?2 and id <> ?3 "
                        + "order by position, id", page.shop, ShopPageKind.SYSTEM, page.id).stream()
                .filter(other -> other.publishedContent != null && ShopBlockContent
                        .values(parse(other.publishedContent), ShopBlockField.Kind.SEGMENT).stream()
                        .anyMatch(value -> value.asText().equals(page.segment)))
                .map(other -> other.menuName)
                .toList();
        if (!linking.isEmpty()) {
            throw new ApiException(409, "Auf diese Seite verlinkt noch: " + String.join(", ", linking)
                    + ". Bitte dort zuerst den Link ändern und veröffentlichen.");
        }
    }

    private void requireDesignable(ShopPage page) {
        if (!page.kind.designable()) {
            throw new ApiException(409, "Systemseiten sind nicht gestaltbar.");
        }
    }

    private void requireRemovable(ShopPage page) {
        if (page.kind != ShopPageKind.FREE) {
            throw new ApiException(409, "Pflichtseiten können nicht entfernt werden.");
        }
    }

    // self = the page being renamed, so keeping its own segment is not a collision.
    String requireFreeSegment(PartnerShop shop, String input, ShopPage self) {
        String segment = input == null ? "" : input.strip();
        if (segment.isEmpty() || segment.length() > ShopPage.SEGMENT_MAX_LENGTH || !SEGMENT.matcher(segment).matches()) {
            throw new ApiException(400, "Die Seitenadresse darf nur Kleinbuchstaben a–z, Ziffern und einzelne "
                    + "Bindestriche enthalten.");
        }
        if (ShopPage.count("shop = ?1 and segment = ?2 and id <> ?3", shop, segment,
                self == null ? -1L : self.id) > 0) {
            throw new ApiException(409, "Diese Seitenadresse ist im Shop bereits vergeben.");
        }
        return segment;
    }

    long designableCount(PartnerShop shop) {
        return ShopPage.count("shop = ?1 and kind <> ?2", shop, ShopPageKind.SYSTEM);
    }

    long onlineCount(PartnerShop shop) {
        return ShopPage.count("shop = ?1 and kind <> ?2 and online = true", shop, ShopPageKind.SYSTEM);
    }

    int nextPosition(PartnerShop shop) {
        Integer max = ShopPage.getEntityManager()
                .createQuery("select max(p.position) from ShopPage p where p.shop = ?1", Integer.class)
                .setParameter(1, shop).getSingleResult();
        return max == null ? 0 : max + 1;
    }

    JsonNode readTemplate(String file) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ShopTemplateCatalog.DIR + file)) {
            if (in == null) {
                throw new IllegalStateException("Shop template missing: " + file);
            }
            return objectMapper.readTree(in);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    // Template as page content of this shop: fresh block ids, and its bundled sample photos
    // copied into the shop's own images (once per shop), so the page only references own assets.
    JsonNode templateContent(PartnerShop shop, ShopTemplate template) {
        JsonNode content = withBlockIds(template.content());
        for (JsonNode block : content.path("blocks")) {
            JsonNode imageId = block.path("config").get("imageId");
            if (imageId != null && imageId.asText("").startsWith(ShopTemplateCatalog.IMAGE_PREFIX)) {
                ShopTemplateImage image = templateCatalog
                        .image(imageId.asText().substring(ShopTemplateCatalog.IMAGE_PREFIX.length())).orElseThrow();
                ((ObjectNode) block.get("config")).put("imageId",
                        assetService.sampleAsset(shop, image, templateCatalog.imageBytes(image)));
            }
        }
        return content;
    }

    // Template blocks carry no ids; every created page gets fresh ones for editor addressing.
    static JsonNode withBlockIds(JsonNode template) {
        JsonNode content = template.deepCopy();
        for (JsonNode block : content.path("blocks")) {
            ((ObjectNode) block).put("id", UUID.randomUUID().toString());
        }
        return content;
    }
}
