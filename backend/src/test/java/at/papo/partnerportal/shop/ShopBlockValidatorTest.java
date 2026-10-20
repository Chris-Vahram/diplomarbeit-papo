package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopBlockValidatorTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    // Asset 7, category 3, articles 1 and 2 and the page "artikel" exist for this shop.
    private static final ShopBlockReferences REFS = new ShopBlockReferences() {
        @Override
        public boolean isOwnAsset(long assetId) {
            return assetId == 7;
        }

        @Override
        public boolean categoryExists(long categoryId) {
            return categoryId == 3;
        }

        @Override
        public boolean isCatalogueArticle(long articleId) {
            return Set.of(1L, 2L).contains(articleId);
        }

        @Override
        public boolean isLinkablePage(String segment) {
            return segment.equals("artikel");
        }
    };

    private static JsonNode page(String... blocks) throws Exception {
        return JSON.readTree("{\"blocks\":[" + String.join(",", blocks) + "]}");
    }

    private static String block(String type, String config) {
        return "{\"id\":\"" + UUID.randomUUID() + "\",\"type\":\"" + type + "\",\"config\":" + config + "}";
    }

    private static String rejected(JsonNode content, ShopPageSlot slot) {
        return assertThrows(ApiException.class, () -> ShopBlockValidator.validatePage(content, slot, REFS)).getMessage();
    }

    @Test
    void validBlocksOfEveryTypePass() throws Exception {
        JsonNode content = page(
                block("hero", "{\"title\":\"Willkommen\",\"imageId\":7,\"buttonPage\":\"artikel\",\"align\":\"center\","
                        + "\"colors\":{\"primary\":\"#0A5C36\"}}"),
                block("heading-text", "{\"heading\":\"Über uns\",\"text\":\"Text\"}"),
                block("image", "{\"imageId\":7,\"alt\":\"Bild\",\"width\":\"wide\"}"),
                block("image-text", "{\"imageId\":7,\"text\":\"Text\",\"imagePosition\":\"right\"}"),
                block("article-grid", "{\"categoryId\":3,\"columns\":\"4\",\"background\":\"surface\"}"),
                block("featured-articles", "{\"articleIds\":[1,2]}"),
                block("contact-person", "{\"name\":\"Maria Muster\",\"email\":\"maria@example.at\"}"),
                block("spacer", "{\"size\":\"large\",\"divider\":true,\"spacing\":\"small\"}"));
        assertDoesNotThrow(() -> ShopBlockValidator.validatePage(content, ShopPageSlot.HOME, REFS));
    }

    @Test
    void unknownFieldIsRejectedByName() throws Exception {
        String message = rejected(page(block("heading-text", "{\"heading\":\"x\",\"html\":\"<b>x</b>\"}")), ShopPageSlot.HOME);
        assertEquals("Unbekanntes Feld „html“ im Block „Überschrift + Text“.", message);
    }

    @Test
    void scriptInTextIsAcceptedAsPlainText() throws Exception {
        // Stored verbatim; the renderer escapes it (frontend blocks use textContent/escapeHtml).
        assertDoesNotThrow(() -> ShopBlockValidator.validatePage(
                page(block("heading-text", "{\"text\":\"<script>alert(1)</script>\"}")), ShopPageSlot.HOME, REFS));
    }

    @Test
    void xssPayloadsInStructuredFieldsAreRejected() throws Exception {
        assertTrue(rejected(page(block("hero", "{\"title\":\"x\",\"align\":\"center\\\" onload=\\\"alert(1)\"}")),
                ShopPageSlot.HOME).startsWith("Ungültiger Wert"));
        assertTrue(rejected(page(block("hero", "{\"title\":\"x\",\"colors\":{\"primary\":\"red;}body{background:url(//evil)\"}}")),
                ShopPageSlot.HOME).contains("#RRGGBB"));
        assertTrue(rejected(page(block("hero", "{\"title\":\"x\",\"buttonPage\":\"javascript:alert(1)\"}")),
                ShopPageSlot.HOME).contains("verlinkte Seite"));
        assertTrue(rejected(page(block("image", "{\"imageId\":\"7 onerror=alert(1)\"}")),
                ShopPageSlot.HOME).contains("nicht verfügbar"));
        assertTrue(rejected(page(block("<img src=x>", "{}")), ShopPageSlot.HOME).startsWith("Unbekannter Blocktyp"));
    }

    @Test
    void lengthsAndRequiredFieldsAreChecked() throws Exception {
        assertEquals("Das Feld „title“ im Block „Hero/Banner“ darf höchstens 120 Zeichen lang sein.",
                rejected(page(block("hero", "{\"title\":\"" + "a".repeat(121) + "\"}")), ShopPageSlot.HOME));
        assertEquals("Das Feld „title“ im Block „Hero/Banner“ ist erforderlich.",
                rejected(page(block("hero", "{\"title\":\"  \"}")), ShopPageSlot.HOME));
    }

    @Test
    void referencesMustBelongToTheShop() throws Exception {
        assertTrue(rejected(page(block("image", "{\"imageId\":8}")), ShopPageSlot.HOME).contains("nicht verfügbar"));
        assertTrue(rejected(page(block("article-grid", "{\"categoryId\":99}")), ShopPageSlot.HOME).contains("Kategorie"));
        assertTrue(rejected(page(block("featured-articles", "{\"articleIds\":[1,5]}")), ShopPageSlot.HOME)
                .contains("nicht freigeschaltet"));
        assertTrue(rejected(page(block("featured-articles", "{\"articleIds\":[1,1,1,1,1,1,1,1,1,1,1,1,1]}")),
                ShopPageSlot.HOME).contains("höchstens 12"));
    }

    @Test
    void draftsKeepHalfFilledBlocksAndStaleReferences() throws Exception {
        JsonNode draft = page(block("hero", "{\"title\":\"\",\"buttonPage\":\"offline-seite\"}"),
                block("featured-articles", "{\"articleIds\":[1,5]}"),
                block("article-grid", "{\"categoryId\":99}"));
        assertDoesNotThrow(() -> ShopBlockValidator.validatePage(draft, ShopPageSlot.HOME, REFS, false));
        assertThrows(ApiException.class, () -> ShopBlockValidator.validatePage(draft, ShopPageSlot.HOME, REFS, true));
        // Structure, lengths and asset ownership are checked for drafts too.
        assertThrows(ApiException.class, () -> ShopBlockValidator.validatePage(
                page(block("image", "{\"imageId\":8}")), ShopPageSlot.HOME, REFS, false));
        assertThrows(ApiException.class, () -> ShopBlockValidator.validatePage(
                page(block("heading-text", "{\"html\":\"x\"}")), ShopPageSlot.HOME, REFS, false));
        assertThrows(ApiException.class, () -> ShopBlockValidator.validatePage(
                page(block("hero", "{\"buttonPage\":7}")), ShopPageSlot.HOME, REFS, false));
    }

    @Test
    void atMostFortyBlocksPerPage() throws Exception {
        String[] blocks = new String[41];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = block("spacer", "{}");
        }
        assertEquals("Eine Seite darf höchstens 40 Blöcke enthalten.", rejected(page(blocks), ShopPageSlot.HOME));
        assertDoesNotThrow(() -> ShopBlockValidator.validatePage(
                page(java.util.Arrays.copyOf(blocks, 40)), ShopPageSlot.HOME, REFS));
    }

    @Test
    void blockIdsMustBeUniqueUuids() throws Exception {
        String id = UUID.randomUUID().toString();
        String twice = "{\"id\":\"" + id + "\",\"type\":\"spacer\",\"config\":{}}";
        assertTrue(rejected(page(twice, twice), ShopPageSlot.HOME).contains("eindeutig"));
        assertTrue(rejected(page("{\"id\":\"1\",\"type\":\"spacer\",\"config\":{}}"), ShopPageSlot.HOME)
                .contains("gültige ID"));
    }

    @Test
    void articleDetailBlockOnlyAndAlwaysOnDetailPage() throws Exception {
        assertTrue(rejected(page(block("article-detail", "{}")), ShopPageSlot.HOME).contains("nur auf der Seite"));
        assertTrue(rejected(page(block("spacer", "{}")), ShopPageSlot.ARTICLE_DETAIL).contains("genau einen"));
        assertDoesNotThrow(() -> ShopBlockValidator.validatePage(
                page(block("article-detail", "{}"), block("spacer", "{}")), ShopPageSlot.ARTICLE_DETAIL, REFS));
    }

    @Test
    void themeNeedsAllColoursAndCuratedFonts() throws Exception {
        String colors = "{\"primary\":\"#0A5C36\",\"accent\":\"#E07A2F\",\"background\":\"#FFFFFF\","
                + "\"surface\":\"#F0F0F0\",\"text\":\"#111111\"}";
        assertDoesNotThrow(() -> ShopThemeValidator.validate(JSON.readTree(
                "{\"colors\":" + colors + ",\"headingFont\":\"lora\",\"bodyFont\":\"inter\"}")));
        assertThrows(ApiException.class, () -> ShopThemeValidator.validate(JSON.readTree(
                "{\"colors\":" + colors + ",\"headingFont\":\"comic-sans\",\"bodyFont\":\"inter\"}")));
        assertThrows(ApiException.class, () -> ShopThemeValidator.validate(JSON.readTree(
                "{\"colors\":{\"primary\":\"#0A5C36\"},\"headingFont\":\"lora\",\"bodyFont\":\"inter\"}")));
        assertThrows(ApiException.class, () -> ShopThemeValidator.validate(JSON.readTree(
                "{\"colors\":" + colors + ",\"headingFont\":\"lora\",\"bodyFont\":\"inter\",\"css\":\"x\"}")));
    }
}
