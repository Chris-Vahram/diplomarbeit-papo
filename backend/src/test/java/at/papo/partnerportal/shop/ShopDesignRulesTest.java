package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// shop-design-assistant spec "Contrast rule" and "Publish gating", server side.
class ShopDesignRulesTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String THEME = "{\"colors\":{\"primary\":\"#1B3A57\",\"accent\":\"#E07A2F\","
            + "\"background\":\"#F4F5F7\",\"surface\":\"#FFFFFF\",\"text\":\"#15191E\"},"
            + "\"headingFont\":\"system\",\"bodyFont\":\"system\",\"form\":\"klar\"}";

    private static JsonNode page(String config) throws Exception {
        return JSON.readTree("{\"blocks\":[{\"id\":\"a\",\"type\":\"heading-text\",\"config\":" + config + "}]}");
    }

    @Test
    void lightGreyOnWhiteIsABlocker() throws Exception {
        JsonNode theme = JSON.readTree(THEME);
        List<String> problems = ShopDesignRules.blockBlockers(
                page("{\"background\":\"surface\",\"colors\":{\"text\":\"#BBBBBB\"}}"), theme);
        assertEquals(2, problems.size());
        assertTrue(problems.get(0).startsWith("Abschnitt 1 („Überschrift + Text“): Text auf der Fläche: Kontrast 1,9:1"),
                problems.get(0));
        ApiException rejected = assertThrows(ApiException.class, () -> ShopDesignRules.requireReadablePage(
                page("{\"background\":\"surface\",\"colors\":{\"text\":\"#BBBBBB\"}}"), theme));
        assertEquals(400, rejected.getStatus());
        assertTrue(rejected.getMessage().startsWith("Text ist kaum lesbar, deshalb kann diese Seite nicht veröffentlicht werden."));
    }

    @Test
    void warningsAndTheDefaultThemePass() throws Exception {
        JsonNode theme = JSON.readTree(THEME);
        // Secondary text at 3.0:1 is a Warnung in the editor, not a reason to refuse publishing.
        assertDoesNotThrow(() -> ShopDesignRules.requireReadablePage(
                page("{\"background\":\"surface\",\"colors\":{\"text\":\"#666666\"}}"), theme));
        assertEquals(List.of(), ShopDesignRules.themeBlockers(theme));
    }

    @Test
    void aBadThemeIsReportedForTheDesignOnly() throws Exception {
        JsonNode theme = JSON.readTree(THEME.replace("#15191E", "#DDDDDD"));
        assertTrue(ShopDesignRules.themeBlockers(theme).get(0).startsWith("Text auf dem Seitenhintergrund"));
        // Blocks without own colours share the theme's pairs and add nothing of their own.
        assertEquals(List.of(), ShopDesignRules.blockBlockers(page("{}"), theme));
    }
}
