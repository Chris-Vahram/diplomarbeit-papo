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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

// Pages and theme of a shop (shop-pages, shop-theme specs): default creation, limits,
// per-page publishing and theme publishing. Editing endpoints follow in add-shop-builder-editor.
@ApplicationScoped
public class ShopPageService {

    public static final int MAX_ONLINE_PAGES = 6;
    public static final int MAX_PAGES = 20;
    static final String TEMPLATE_DIR = "shop-templates/default/";

    private static final Pattern SEGMENT = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ShopCatalogService catalogService;

    // Idempotent: creates the theme and every missing slot page. Mandatory pages start
    // published from the default template; system pages only reserve slot and segment.
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
        boolean created = false;
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
                String content = withBlockIds(readTemplate(slot.templateFile)).toString();
                page.draftContent = content;
                page.publishedContent = content;
                page.publishedAt = now;
                page.online = true;
            }
            page.persist();
            created = true;
        }
        if (created) {
            // Templates link between pages, so they are checked once all slots exist.
            ShopPage.<ShopPage>list("shop = ?1 and kind = ?2", shop, ShopPageKind.MANDATORY)
                    .forEach(page -> validateContent(page, page.draftContent));
        }
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
        page.segment = requireFreeSegment(shop, segment);
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
        page.previousPublishedContent = page.publishedContent;
        page.publishedContent = page.draftContent;
        page.publishedAt = Instant.now();
        page.updatedAt = page.publishedAt;
        page.online = true;
        return page;
    }

    @Transactional
    public ShopPage unpublish(ShopPage page) {
        requireRemovable(page);
        page.online = false;
        page.updatedAt = Instant.now();
        return page;
    }

    @Transactional
    public void delete(ShopPage page) {
        requireRemovable(page);
        page.delete();
    }

    // Publishing the theme restyles every online page at once; block overrides live in the
    // page snapshots and are therefore untouched.
    @Transactional
    public ShopTheme publishTheme(PartnerShop shop) {
        ShopTheme theme = theme(shop);
        ShopThemeValidator.validate(parse(theme.draftTheme));
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
        ShopBlockValidator.validatePage(parse(content), page.slot, references(page.shop));
    }

    ShopBlockReferences references(PartnerShop shop) {
        return new ShopBlockReferences() {
            @Override
            public boolean isPublicImage(long imageId) {
                return catalogService.publicImage(shop.partner, imageId).isPresent();
            }

            @Override
            public boolean categoryExists(long categoryId) {
                return Category.findById(categoryId) != null;
            }

            @Override
            public boolean isCatalogueArticle(long articleId) {
                return catalogService.isCatalogueArticle(shop.partner, articleId);
            }

            @Override
            public boolean pageExists(String segment) {
                return ShopPage.count("shop = ?1 and segment = ?2", shop, segment) > 0;
            }
        };
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

    private String requireFreeSegment(PartnerShop shop, String input) {
        String segment = input == null ? "" : input.strip();
        if (segment.isEmpty() || segment.length() > ShopPage.SEGMENT_MAX_LENGTH || !SEGMENT.matcher(segment).matches()) {
            throw new ApiException(400, "Die Seitenadresse darf nur Kleinbuchstaben a–z, Ziffern und einzelne "
                    + "Bindestriche enthalten.");
        }
        if (ShopPage.count("shop = ?1 and segment = ?2", shop, segment) > 0) {
            throw new ApiException(409, "Diese Seitenadresse ist im Shop bereits vergeben.");
        }
        return segment;
    }

    private long designableCount(PartnerShop shop) {
        return ShopPage.count("shop = ?1 and kind <> ?2", shop, ShopPageKind.SYSTEM);
    }

    private long onlineCount(PartnerShop shop) {
        return ShopPage.count("shop = ?1 and kind <> ?2 and online = true", shop, ShopPageKind.SYSTEM);
    }

    private int nextPosition(PartnerShop shop) {
        Integer max = ShopPage.getEntityManager()
                .createQuery("select max(p.position) from ShopPage p where p.shop = ?1", Integer.class)
                .setParameter(1, shop).getSingleResult();
        return max == null ? 0 : max + 1;
    }

    private JsonNode readTemplate(String file) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_DIR + file)) {
            if (in == null) {
                throw new IllegalStateException("Shop template missing: " + file);
            }
            return objectMapper.readTree(in);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    // Template blocks carry no ids; every created page gets fresh ones for editor addressing.
    private JsonNode withBlockIds(JsonNode template) {
        JsonNode content = template.deepCopy();
        for (JsonNode block : content.path("blocks")) {
            ((ObjectNode) block).put("id", UUID.randomUUID().toString());
        }
        return content;
    }
}
