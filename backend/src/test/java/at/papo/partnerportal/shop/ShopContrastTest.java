package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Shared vectors with the frontend (tools/check_shop_assistant.mjs), so editor and server
// judge contrast identically (add-shop-design-assistant design.md Decision 3).
class ShopContrastTest {

    private static final Path VECTORS = Path.of("..", "tools", "fixtures", "shop-contrast-vectors.json");

    @Test
    void sharedVectorsMatch() throws Exception {
        JsonNode vectors = new ObjectMapper().readTree(VECTORS.toFile());
        for (JsonNode vector : vectors.get("contrast")) {
            String foreground = vector.get("foreground").asText();
            String background = vector.get("background").asText();
            double ratio = ShopContrast.contrast(foreground, background);
            assertEquals(vector.get("ratio").asDouble(), ratio, 0.01, foreground + " auf " + background);
            String severity = ratio < ShopDesignRules.BLOCKER ? "blocker" : ratio < 4.5 ? "warning" : null;
            assertEquals(vector.get("severity").isNull() ? null : vector.get("severity").asText(), severity,
                    foreground + " auf " + background);
        }
        for (JsonNode vector : vectors.get("onColor")) {
            assertEquals(vector.get("text").asText(), ShopContrast.onColor(vector.get("background").asText()));
        }
        for (JsonNode vector : vectors.get("mix")) {
            assertEquals(vector.get("result").asText(), ShopContrast.mix(vector.get("a").asText(),
                    vector.get("b").asText(), vector.get("share").asDouble()));
        }
    }
}
