package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ShopPageServiceTest {

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopPageService pageService;

    private long createShop() {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.partnerNumber = "PAGE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Seiten Test " + partner.partnerNumber;
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            return partner.id;
        });
    }

    private PartnerShop shop(long partnerId) {
        return shopService.findOrCreate(Partner.findById(partnerId));
    }

    private ShopPage page(long partnerId, ShopPageSlot slot) {
        return ShopPage.find("shop = ?1 and slot = ?2", shop(partnerId), slot).firstResult();
    }

    @Test
    void newShopGetsPublishedMandatoryPagesReservedSystemPagesAndTheme() {
        long partnerId = createShop();
        QuarkusTransaction.requiringNew().run(() -> {
            PartnerShop shop = shop(partnerId);
            List<ShopPage> pages = ShopPage.list("shop", shop);
            assertEquals(ShopPageSlot.values().length, pages.size());
            for (ShopPage page : pages) {
                if (page.kind == ShopPageKind.MANDATORY) {
                    assertTrue(page.online, page.segment);
                    assertNotNull(page.publishedContent);
                    assertEquals(page.draftContent, page.publishedContent);
                } else {
                    assertEquals(ShopPageKind.SYSTEM, page.kind);
                    assertNull(page.publishedContent);
                }
            }
            assertNotNull(pageService.theme(shop).publishedTheme);
            assertEquals(List.of("startseite", "artikel"),
                    pageService.navigation(shop).stream().map(page -> page.segment).toList());
        });
    }

    @Test
    void ensureDefaultsIsIdempotent() {
        long partnerId = createShop();
        QuarkusTransaction.requiringNew().run(() -> {
            PartnerShop shop = shop(partnerId);
            pageService.ensureDefaults(shop);
            pageService.ensureDefaults(shop);
            assertEquals(ShopPageSlot.values().length, ShopPage.count("shop", shop));
            assertEquals(1, ShopTheme.count("shop", shop));
        });
    }

    // Rejections roll their transaction back, so each one runs in its own.
    private static String rejection(Runnable action) {
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> QuarkusTransaction.requiringNew().run(action));
        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api) {
                return api.getMessage();
            }
        }
        throw new AssertionError("Expected ApiException", thrown);
    }

    private static void tx(Runnable action) {
        QuarkusTransaction.requiringNew().run(action);
    }

    private ShopPage segment(long partnerId, String segment) {
        return ShopPage.find("shop = ?1 and segment = ?2", shop(partnerId), segment).firstResult();
    }

    @Test
    void mandatoryAndSystemPagesCannotBeRemoved() {
        long partnerId = createShop();
        assertEquals("Pflichtseiten können nicht entfernt werden.",
                rejection(() -> pageService.unpublish(page(partnerId, ShopPageSlot.HOME))));
        assertEquals("Pflichtseiten können nicht entfernt werden.",
                rejection(() -> pageService.delete(page(partnerId, ShopPageSlot.HOME))));
        rejection(() -> pageService.delete(page(partnerId, ShopPageSlot.IMPRESSUM)));
        assertEquals("Systemseiten sind nicht gestaltbar.",
                rejection(() -> pageService.publish(page(partnerId, ShopPageSlot.CART))));
        tx(() -> assertTrue(page(partnerId, ShopPageSlot.HOME).online));
    }

    @Test
    void atMostSixPagesOnline() {
        long partnerId = createShop();
        tx(() -> {
            for (int i = 1; i <= 3; i++) {
                pageService.publish(pageService.createFreePage(shop(partnerId), "Seite " + i, "seite-" + i));
            }
            pageService.createFreePage(shop(partnerId), "Seite 4", "seite-4");
        });
        assertEquals("Es können höchstens 6 Seiten gleichzeitig online sein.",
                rejection(() -> pageService.publish(segment(partnerId, "seite-4"))));
        tx(() -> {
            // Republishing an online page is not a new online page.
            pageService.publish(page(partnerId, ShopPageSlot.HOME));
            // Taking a free page offline frees a place.
            pageService.unpublish(segment(partnerId, "seite-1"));
            assertTrue(pageService.publish(segment(partnerId, "seite-4")).online);
        });
    }

    @Test
    void atMostTwentyPagesIncludingDrafts() {
        long partnerId = createShop();
        tx(() -> {
            for (int i = 1; i <= 17; i++) {
                pageService.createFreePage(shop(partnerId), "Entwurf " + i, "entwurf-" + i);
            }
        });
        assertEquals("Ein Shop kann höchstens 20 Seiten haben.",
                rejection(() -> pageService.createFreePage(shop(partnerId), "Zu viel", "zu-viel")));
        // System pages do not count: 3 mandatory + 17 free = 20.
        tx(() -> assertEquals(20, ShopPage.count("shop = ?1 and kind <> ?2", shop(partnerId), ShopPageKind.SYSTEM)));
    }

    @Test
    void freePageSegmentMustBeValidAndUnique() {
        long partnerId = createShop();
        rejection(() -> pageService.createFreePage(shop(partnerId), "Über uns", "Über uns"));
        assertEquals("Diese Seitenadresse ist im Shop bereits vergeben.",
                rejection(() -> pageService.createFreePage(shop(partnerId), "Korb", "warenkorb")));
        rejection(() -> pageService.createFreePage(shop(partnerId), "Artikel 2", "artikel"));
    }

    @Test
    void publishKeepsPreviousVersionAndDraftStaysPrivate() {
        long partnerId = createShop();
        QuarkusTransaction.requiringNew().run(() -> {
            ShopPage home = page(partnerId, ShopPageSlot.HOME);
            String original = home.publishedContent;
            home.draftContent = "{\"blocks\":[{\"id\":\"" + UUID.randomUUID()
                    + "\",\"type\":\"heading-text\",\"config\":{\"heading\":\"Neu\"}}]}";
            assertEquals(original, home.publishedContent);
            pageService.publish(home);
            assertEquals(original, home.previousPublishedContent);
            assertEquals(home.draftContent, home.publishedContent);
        });
    }

    @Test
    void invalidDraftCannotBePublished() {
        long partnerId = createShop();
        tx(() -> page(partnerId, ShopPageSlot.ARTICLE_DETAIL).draftContent = "{\"blocks\":[]}");
        assertEquals("Die Seite Artikeldetail braucht genau einen Block „Artikeldetail“.",
                rejection(() -> pageService.publish(page(partnerId, ShopPageSlot.ARTICLE_DETAIL))));
    }

    @Test
    void themePublishKeepsPreviousAndRejectsUnknownFont() {
        long partnerId = createShop();
        String original = QuarkusTransaction.requiringNew().call(() -> {
            ShopTheme theme = pageService.theme(shop(partnerId));
            theme.draftTheme = theme.publishedTheme.replace("work-sans", "lora");
            return theme.publishedTheme;
        });
        tx(() -> {
            ShopTheme theme = pageService.publishTheme(shop(partnerId));
            assertEquals(original, theme.previousPublishedTheme);
            assertTrue(theme.publishedTheme.contains("lora"));
            theme.draftTheme = original.replace("work-sans", "comic-sans");
        });
        rejection(() -> pageService.publishTheme(shop(partnerId)));
    }

    private static String heroLinkingTo(String segment) {
        return "{\"blocks\":[{\"id\":\"" + UUID.randomUUID() + "\",\"type\":\"hero\","
                + "\"config\":{\"title\":\"x\",\"buttonLabel\":\"Los\",\"buttonPage\":\"" + segment + "\"}}]}";
    }

    @Test
    void buttonLinksOnlyToOnlineDesignablePages() {
        long partnerId = createShop();
        tx(() -> pageService.createFreePage(shop(partnerId), "Entwurf", "entwurf"));
        for (String target : List.of("kasse", "warenkorb", "artikeldetail", "entwurf", "gibt-es-nicht")) {
            tx(() -> page(partnerId, ShopPageSlot.HOME).draftContent = heroLinkingTo(target));
            assertTrue(rejection(() -> pageService.publish(page(partnerId, ShopPageSlot.HOME)))
                    .contains("verlinkte Seite"), target);
        }
        tx(() -> page(partnerId, ShopPageSlot.HOME).draftContent = heroLinkingTo("artikel"));
        tx(() -> pageService.publish(page(partnerId, ShopPageSlot.HOME)));
    }

    @Test
    void ensureDefaultsDoesNotRevalidateExistingPages() {
        long partnerId = createShop();
        tx(() -> {
            // Existing draft now invalid (e.g. article disabled since) and one slot missing.
            page(partnerId, ShopPageSlot.HOME).draftContent = heroLinkingTo("gibt-es-nicht");
            page(partnerId, ShopPageSlot.WIDERRUF).delete();
        });
        tx(() -> pageService.ensureDefaults(shop(partnerId)));
        tx(() -> assertNotNull(page(partnerId, ShopPageSlot.WIDERRUF)));
    }
}
