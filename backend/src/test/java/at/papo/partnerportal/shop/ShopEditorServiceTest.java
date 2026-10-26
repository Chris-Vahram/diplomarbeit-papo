package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ShopEditorServiceTest {

    @Inject
    ShopEditorService editorService;

    @Inject
    ObjectMapper json;

    private long createPartner() {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.partnerNumber = "EDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Editor Test " + partner.partnerNumber;
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            return partner.id;
        });
    }

    private static <T> T tx(Callable<T> action) {
        return QuarkusTransaction.requiringNew().call(action);
    }

    // Rejections roll their transaction back, so each one runs in its own.
    private static String rejection(Callable<?> action) {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> tx(action));
        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api) {
                return api.getMessage();
            }
        }
        throw new AssertionError("Expected ApiException", thrown);
    }

    private static Partner partner(long id) {
        return Partner.findById(id);
    }

    private ShopPageDto pageBySegment(long partnerId, String segment) {
        return editorService.pages(partner(partnerId)).pages().stream()
                .filter(page -> page.segment().equals(segment)).findFirst().orElseThrow();
    }

    private JsonNode content(String blocks) throws Exception {
        return json.readTree("{\"blocks\":[" + blocks + "]}");
    }

    private static String block(String type, String config) {
        return "{\"id\":\"" + UUID.randomUUID() + "\",\"type\":\"" + type + "\",\"config\":" + config + "}";
    }

    private ShopPageDto saveDraft(long partnerId, ShopPageDto page, String blocks) throws Exception {
        JsonNode draft = content(blocks);
        return tx(() -> editorService.saveDraft(partner(partnerId), page.id(),
                new ShopEditorRequests.Draft(draft, page.version())));
    }

    @Test
    void firstSetupPublishesTheChosenStyleOnce() {
        long partnerId = createPartner();
        assertTrue(tx(() -> editorService.pages(partner(partnerId))).setupNeeded());
        assertEquals("Diesen Stil gibt es nicht.", rejection(() -> editorService.setup(partner(partnerId),
                new ShopEditorRequests.Setup("gibt-es-nicht"))));
        assertTrue(tx(() -> editorService.pages(partner(partnerId))).setupNeeded());

        ShopPageListDto list = tx(() -> editorService.setup(partner(partnerId), new ShopEditorRequests.Setup("handwerk")));
        assertFalse(list.setupNeeded());
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDetailDto detail = tx(() -> editorService.page(partner(partnerId), home.id()));
        assertEquals("Gutes Werkzeug, ehrliche Beratung", detail.published().at("/blocks/0/config/title").asText());
        assertEquals(detail.published(), detail.draft());
        assertTrue(home.restorable());
        ShopThemeDto theme = tx(() -> editorService.theme(partner(partnerId)));
        assertEquals("#2F4A3A", theme.published().at("/colors/primary").asText());
        assertEquals("handwerk", theme.published().at("/form").asText());
        assertFalse(theme.draftChanged());

        assertEquals("Der Shop ist bereits eingerichtet.", rejection(() -> editorService.setup(partner(partnerId),
                new ShopEditorRequests.Setup("kraeftig"))));
    }

    @Test
    void sampleImagesAreCopiedOncePerShop() {
        long partnerId = createPartner();
        ShopTemplateContentDto first = tx(() -> editorService.templateContent(partner(partnerId), "startseite-klar"));
        ShopTemplateContentDto second = tx(() -> editorService.templateContent(partner(partnerId), "startseite-klar"));
        long imageId = first.blocks().get(0).at("/config/imageId").asLong();
        assertEquals(imageId, second.blocks().get(0).at("/config/imageId").asLong());
        assertTrue(imageId > 0);
        assertEquals("klar", first.theme().at("/form").asText());
        // The new shop already copied the Klar photos for its default pages; nothing is duplicated.
        assertEquals(1, ShopAsset.count("shop.partner.id = ?1 and filename = ?2", partnerId, "Beispielbild messen.webp"));
        assertEquals("Diese Vorlage gibt es nicht.", rejection(() -> editorService.templateContent(partner(partnerId), "nein")));
    }

    @Test
    void skippingKeepsTheDefaultsAndEditedShopsNeedNoSetup() throws Exception {
        long skipping = createPartner();
        ShopPageDto before = tx(() -> pageBySegment(skipping, "startseite"));
        assertFalse(tx(() -> editorService.setup(partner(skipping), new ShopEditorRequests.Setup(null))).setupNeeded());
        assertEquals(before.version(), tx(() -> pageBySegment(skipping, "startseite")).version());

        // A shop edited before the setup existed is treated as set up.
        long edited = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(edited, "startseite"));
        saveDraft(edited, home, block("heading-text", "{\"heading\":\"Eigene Seite\"}"));
        assertFalse(tx(() -> editorService.pages(partner(edited))).setupNeeded());
    }

    @Test
    void pageListShowsDesignablePagesAndLimits() {
        long partnerId = createPartner();
        ShopPageListDto list = tx(() -> editorService.pages(partner(partnerId)));
        assertEquals(List.of("startseite", "artikel", "artikeldetail"),
                list.pages().stream().map(ShopPageDto::segment).toList());
        assertEquals(3, list.onlineCount());
        assertEquals(6, list.maxOnline());
        assertEquals(3, list.pageCount());
        assertEquals(20, list.maxPages());
    }

    @Test
    void freePageFromCatalogueTemplateStartsAsPublishableDraft() {
        long partnerId = createPartner();
        ShopPageDetailDto created = tx(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Kontakt", "kontakt", "seite-kontakt")));
        assertFalse(created.page().online());
        assertNull(created.published());
        assertEquals(2, created.draft().get("blocks").size());
        assertTrue(created.draft().at("/blocks/1/id").asText().matches("[0-9a-f-]{36}"));

        // Catalogue templates pass the publish checks as they are (shop-templates "Template quality").
        ShopPageDto published = tx(() -> editorService.publish(partner(partnerId), created.page().id(),
                created.page().version()));
        assertTrue(published.online());
        assertFalse(published.draftChanged());

        assertEquals("Diese Vorlage gibt es nicht.", rejection(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("X", "x-seite", "gibt-es-nicht"))));
        // A template of a mandatory page type is no free-page template.
        assertEquals("Diese Vorlage gibt es nicht.", rejection(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Y", "y-seite", "startseite-klar"))));
    }

    @Test
    void staleVersionIsRejectedForDraftsSettingsAndPublish() throws Exception {
        long partnerId = createPartner();
        ShopPageDto loaded = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDto first = saveDraft(partnerId, loaded, block("heading-text", "{\"heading\":\"Erster Tab\"}"));
        assertTrue(first.version() > loaded.version());
        assertTrue(first.draftChanged());

        // Second tab still holds the version it loaded.
        assertEquals(ShopEditorService.PAGE_CONFLICT,
                rejection(() -> saveDraft(partnerId, loaded, block("heading-text", "{\"heading\":\"Zweiter Tab\"}"))));
        assertEquals(ShopEditorService.PAGE_CONFLICT, rejection(() -> editorService.updateSettings(partner(partnerId),
                loaded.id(), new ShopEditorRequests.PageSettings("Start", null, null, null, loaded.version()))));
        assertEquals(ShopEditorService.PAGE_CONFLICT,
                rejection(() -> editorService.publish(partner(partnerId), loaded.id(), loaded.version())));

        // Saving unchanged content does not invalidate other editors.
        ShopPageDto same = saveDraft(partnerId, first, tx(() ->
                editorService.page(partner(partnerId), first.id()).draft().get("blocks").get(0).toString()));
        assertEquals(first.version(), same.version());
    }

    @Test
    void restoreSwapsInThePreviousPublishedVersion() throws Exception {
        long partnerId = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        String template = tx(() -> editorService.page(partner(partnerId), home.id()).published().toString());
        assertFalse(home.restorable());

        ShopPageDto saved = saveDraft(partnerId, home, block("heading-text", "{\"heading\":\"Neu\"}"));
        ShopPageDto published = tx(() -> editorService.publish(partner(partnerId), saved.id(), saved.version()));
        assertTrue(published.restorable());

        ShopPageDetailDto restored = tx(() -> editorService.restore(partner(partnerId), home.id(), published.version()));
        assertEquals(template, restored.published().toString());
        assertEquals(template, restored.draft().toString());
        assertTrue(restored.page().restorable());
        assertTrue(tx(() -> ShopPage.<ShopPage>findById(home.id()).previousPublishedContent.contains("Neu")));

        long freePartner = createPartner();
        ShopPageDetailDto free = tx(() -> editorService.createPage(partner(freePartner),
                new ShopEditorRequests.CreatePage("Neu", "neu", null)));
        assertTrue(rejection(() -> editorService.restore(partner(freePartner), free.page().id(), free.page().version()))
                .contains("keine vorherige"));
    }

    @Test
    void settingsRenameFreePagesButKeepMandatoryAddresses() {
        long partnerId = createPartner();
        ShopPageDetailDto free = tx(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Über uns", "ueber-uns", "seite-ueber-uns")));
        ShopPageDto renamed = tx(() -> editorService.updateSettings(partner(partnerId), free.page().id(),
                new ShopEditorRequests.PageSettings("Team", "unser-team", " Unser Team ", "Wer wir sind.",
                        free.page().version())));
        assertEquals("unser-team", renamed.segment());
        assertEquals("Team", renamed.menuName());
        assertEquals("Unser Team", renamed.seoTitle());

        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        assertEquals("Die Adresse einer Pflichtseite kann nicht geändert werden.",
                rejection(() -> editorService.updateSettings(partner(partnerId), home.id(),
                        new ShopEditorRequests.PageSettings("Start", "home", null, null, home.version()))));
        assertEquals("Diese Seitenadresse ist im Shop bereits vergeben.",
                rejection(() -> editorService.updateSettings(partner(partnerId), renamed.id(),
                        new ShopEditorRequests.PageSettings("Team", "artikel", null, null, renamed.version()))));
        assertEquals("Der SEO-Titel darf höchstens 60 Zeichen lang sein.",
                rejection(() -> editorService.updateSettings(partner(partnerId), home.id(),
                        new ShopEditorRequests.PageSettings("Start", null, "a".repeat(61), null, home.version()))));
        ShopPageDto start = tx(() -> editorService.updateSettings(partner(partnerId), home.id(),
                new ShopEditorRequests.PageSettings("Start", "startseite", null, "b".repeat(160), home.version())));
        assertEquals("Start", start.menuName());
    }

    @Test
    void reorderNeedsTheCompletePageListAndKeepsEditorsValid() {
        long partnerId = createPartner();
        ShopPageDetailDto free = tx(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Kontakt", "kontakt", null)));
        List<ShopPageDto> pages = tx(() -> editorService.pages(partner(partnerId)).pages());
        List<Long> reversed = pages.stream().map(ShopPageDto::id).toList().reversed();

        ShopPageListDto reordered = tx(() -> editorService.reorder(partner(partnerId),
                new ShopEditorRequests.PageOrder(reversed)));
        assertEquals(List.of("kontakt", "artikeldetail", "artikel", "startseite"),
                reordered.pages().stream().map(ShopPageDto::segment).toList());
        // Position is excluded from the optimistic lock: an open editor can keep saving.
        assertEquals(free.page().version(), reordered.pages().get(0).version());

        assertTrue(rejection(() -> editorService.reorder(partner(partnerId),
                new ShopEditorRequests.PageOrder(reversed.subList(0, 2)))).contains("nicht mehr aktuell"));
        long otherPartner = createPartner();
        assertEquals("Diese Seite gibt es nicht.", rejection(() -> editorService.page(partner(otherPartner), free.page().id())));
    }

    @Test
    void onlyFreePagesCanBeDeleted() {
        long partnerId = createPartner();
        ShopPageDetailDto free = tx(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Weg", "weg", null)));
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        assertEquals("Pflichtseiten können nicht entfernt werden.",
                rejection(() -> {
                    editorService.delete(partner(partnerId), home.id());
                    return null;
                }));
        long otherPartner = createPartner();
        rejection(() -> {
            editorService.delete(partner(otherPartner), free.page().id());
            return null;
        });
        tx(() -> {
            editorService.delete(partner(partnerId), free.page().id());
            return null;
        });
        assertEquals(3, tx(() -> editorService.pages(partner(partnerId)).pageCount()));
    }

    @Test
    void themeDraftStaysPrivateUntilPublishedAndOverridesSurvive() throws Exception {
        long partnerId = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDto saved = saveDraft(partnerId, home,
                block("heading-text", "{\"heading\":\"Grün\",\"colors\":{\"primary\":\"#0A5C36\"}}"));
        tx(() -> editorService.publish(partner(partnerId), saved.id(), saved.version()));
        String publishedPage = tx(() -> ShopPage.<ShopPage>findById(home.id()).publishedContent);

        ShopThemeDto theme = tx(() -> editorService.theme(partner(partnerId)));
        assertFalse(theme.fonts().isEmpty());
        ObjectNode draft = theme.draft().deepCopy();
        ((ObjectNode) draft.get("colors")).put("primary", "#AA0000");
        ShopThemeDto savedTheme = tx(() -> editorService.saveThemeDraft(partner(partnerId),
                new ShopEditorRequests.ThemeDraft(draft, theme.version())));
        assertTrue(savedTheme.draftChanged());
        assertEquals("#1B3A57", savedTheme.published().at("/colors/primary").asText());

        assertEquals(ShopEditorService.THEME_CONFLICT, rejection(() -> editorService.saveThemeDraft(partner(partnerId),
                new ShopEditorRequests.ThemeDraft(draft, theme.version()))));
        assertEquals(ShopEditorService.THEME_CONFLICT,
                rejection(() -> editorService.publishTheme(partner(partnerId), theme.version())));

        ShopThemeDto published = tx(() -> editorService.publishTheme(partner(partnerId), savedTheme.version()));
        assertEquals("#AA0000", published.published().at("/colors/primary").asText());
        assertFalse(published.draftChanged());
        // Block overrides live in the page snapshot and are untouched by theme publishing.
        assertEquals(publishedPage, tx(() -> ShopPage.<ShopPage>findById(home.id()).publishedContent));
        assertTrue(publishedPage.contains("#0A5C36"));
    }

    // shop-design-assistant "Publish gating": the server re-checks contrast blockers.
    @Test
    void unreadableTextStopsPageAndDesignPublishing() throws Exception {
        long partnerId = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDto grey = saveDraft(partnerId, home,
                block("heading-text", "{\"heading\":\"Hallo\",\"background\":\"surface\",\"colors\":{\"text\":\"#BBBBBB\"}}"));
        String pageRejection = rejection(() -> editorService.publish(partner(partnerId), grey.id(), grey.version()));
        assertTrue(pageRejection.startsWith("Text ist kaum lesbar, deshalb kann diese Seite nicht veröffentlicht werden."),
                pageRejection);
        assertFalse(tx(() -> ShopPage.<ShopPage>findById(home.id()).publishedContent).contains("#BBBBBB"));

        ShopThemeDto theme = tx(() -> editorService.theme(partner(partnerId)));
        ObjectNode draft = theme.draft().deepCopy();
        ((ObjectNode) draft.get("colors")).put("text", "#DDDDDD");
        ShopThemeDto savedTheme = tx(() -> editorService.saveThemeDraft(partner(partnerId),
                new ShopEditorRequests.ThemeDraft(draft, theme.version())));
        String themeRejection = rejection(() -> editorService.publishTheme(partner(partnerId), savedTheme.version()));
        assertTrue(themeRejection.contains("Text auf dem Seitenhintergrund: Kontrast"), themeRejection);
        assertEquals(theme.published(), tx(() -> editorService.theme(partner(partnerId))).published());
    }

    @Test
    void republishingUnchangedContentKeepsTheRestorePoint() throws Exception {
        long partnerId = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        String template = tx(() -> editorService.page(partner(partnerId), home.id()).published().toString());
        ShopPageDto saved = saveDraft(partnerId, home, block("heading-text", "{\"heading\":\"Neu\"}"));
        ShopPageDto first = tx(() -> editorService.publish(partner(partnerId), saved.id(), saved.version()));
        ShopPageDto second = tx(() -> editorService.publish(partner(partnerId), first.id(), first.version()));
        ShopPageDetailDto restored = tx(() -> editorService.restore(partner(partnerId), home.id(), second.version()));
        assertEquals(template, restored.published().toString());
    }

    @Test
    void linkedOnlinePageKeepsItsAddressAndStaysOnline() throws Exception {
        long partnerId = createPartner();
        ShopPageDetailDto team = tx(() -> editorService.createPage(partner(partnerId),
                new ShopEditorRequests.CreatePage("Team", "team", null)));
        ShopPageDto online = tx(() -> editorService.publish(partner(partnerId), team.page().id(), team.page().version()));
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDto linked = saveDraft(partnerId, home,
                block("hero", "{\"title\":\"Hallo\",\"buttonLabel\":\"Team\",\"buttonPage\":\"team\"}"));
        tx(() -> editorService.publish(partner(partnerId), linked.id(), linked.version()));

        String expected = "Auf diese Seite verlinkt noch: Startseite. Bitte dort zuerst den Link ändern und veröffentlichen.";
        assertEquals(expected, rejection(() -> editorService.updateSettings(partner(partnerId), online.id(),
                new ShopEditorRequests.PageSettings("Team", "wir", null, null, online.version()))));
        assertEquals(expected, rejection(() -> editorService.unpublish(partner(partnerId), online.id())));
        assertEquals(expected, rejection(() -> {
            editorService.delete(partner(partnerId), online.id());
            return null;
        }));
        // Renaming the menu entry only is fine.
        tx(() -> editorService.updateSettings(partner(partnerId), online.id(),
                new ShopEditorRequests.PageSettings("Unser Team", "team", null, null, online.version())));
    }

    @Test
    void missingVersionIsRefused() throws Exception {
        long partnerId = createPartner();
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        JsonNode draft = content(block("spacer", "{}"));
        assertEquals("Die Versionsangabe fehlt. Bitte die Seite neu laden.", rejection(() ->
                editorService.saveDraft(partner(partnerId), home.id(), new ShopEditorRequests.Draft(draft, null))));
    }

    @Test
    void draftMayLinkAnOfflinePageButPublishMayNot() throws Exception {
        long partnerId = createPartner();
        tx(() -> editorService.createPage(partner(partnerId), new ShopEditorRequests.CreatePage("Entwurf", "entwurf", null)));
        ShopPageDto home = tx(() -> pageBySegment(partnerId, "startseite"));
        ShopPageDto saved = saveDraft(partnerId, home,
                block("hero", "{\"title\":\"Hallo\",\"buttonLabel\":\"Los\",\"buttonPage\":\"entwurf\"}"));
        assertTrue(rejection(() -> editorService.publish(partner(partnerId), saved.id(), saved.version()))
                .contains("verlinkte Seite"));
    }
}
