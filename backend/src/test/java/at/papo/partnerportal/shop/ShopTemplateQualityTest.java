package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// shop-templates spec "Template quality", provisional until add-shop-design-assistant exists:
// every template is checked with its own theme suggestion and with the default theme against
// the assistant's contrast thresholds (Warnung below 4.5:1, Blocker below 3:1) and alt-text rule.
// Required fields and references are already enforced when the catalogue loads (see
// ShopTemplateCatalogTest). Switch this test to the assistant's rule units once they exist.
class ShopTemplateQualityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final double TEXT = 4.5;
    private static final double UI = 3.0;

    @Test
    void everyTemplatePassesContrastAndAltTextWithOwnAndDefaultTheme() throws Exception {
        JsonNode defaultTheme = MAPPER.readTree(getClass().getClassLoader()
                .getResourceAsStream(ShopTemplateCatalog.DIR + "theme.json"));
        List<String> findings = new ArrayList<>();
        for (ShopTemplate template : ShopTemplateCatalog.load(MAPPER, ShopTemplateCatalog.DIR)) {
            check(template, defaultTheme, "Standard-Theme", findings);
            if (template.theme() != null) {
                check(template, template.theme(), "eigenes Theme", findings);
            }
        }
        assertEquals(List.of(), findings);
    }

    @Test
    void contrastFormulaMatchesTheAssistantExamples() {
        // add-shop-design-assistant spec: #BBBBBB on #FFFFFF is about 1.9:1.
        assertEquals(1.9, contrast("#BBBBBB", "#FFFFFF"), 0.05);
        assertEquals(21.0, contrast("#000000", "#FFFFFF"), 0.01);
        // Mid-tone accent: the renderer must pick dark text, white would only reach about 3:1.
        assertEquals("#111111", onColor("#E07A2F"));
        assertTrue(contrast(onColor("#E07A2F"), "#E07A2F") >= TEXT);
    }

    private static void check(ShopTemplate template, JsonNode theme, String themeName, List<String> findings) {
        String where = template.id() + " (" + themeName + ")";
        ObjectNode colors = theme.get("colors").deepCopy();
        // Header and footer sit on the surface; the current navigation item is underlined in primary.
        require(findings, where + " Kopfzeile", colors.get("text").asText(), colors.get("surface").asText(), TEXT);
        require(findings, where + " Navigation", colors.get("primary").asText(), colors.get("surface").asText(), UI);
        int index = 0;
        for (JsonNode block : template.blocks()) {
            index++;
            String at = where + " Abschnitt " + index + " (" + block.get("type").asText() + ")";
            JsonNode config = block.get("config");
            ObjectNode effective = colors.deepCopy();
            if (config.has("colors")) {
                effective.setAll((ObjectNode) config.get("colors"));
            }
            checkBlock(findings, at, block.get("type").asText(), config, effective);
            if (config.hasNonNull("imageId") && config.path("alt").asText("").isBlank()) {
                findings.add(at + ": Bildbeschreibung fehlt");
            }
        }
    }

    // Mirrors shop.css: text colour per block background, muted text, cards, prices and buttons.
    private static void checkBlock(List<String> findings, String at, String type, JsonNode config, ObjectNode c) {
        String text = c.get("text").asText();
        String background = c.get("background").asText();
        String surface = c.get("surface").asText();
        String primary = c.get("primary").asText();
        String accent = c.get("accent").asText();
        String muted = mix(text, background, 0.68);
        switch (config.path("background").asText("none")) {
            case "primary" -> require(findings, at + " Text auf Hauptfarbe", onColor(primary), primary, TEXT);
            case "accent" -> require(findings, at + " Text auf Akzentfarbe", onColor(accent), accent, TEXT);
            case "surface" -> {
                require(findings, at + " Text auf Fläche", text, surface, TEXT);
                require(findings, at + " Nebentext auf Fläche", muted, surface, TEXT);
            }
            default -> {
                require(findings, at + " Text", text, background, TEXT);
                require(findings, at + " Nebentext", muted, background, TEXT);
            }
        }
        // Article cards and the detail view draw on the surface with muted category and primary price.
        if (List.of("article-grid", "featured-articles", "article-detail").contains(type)) {
            require(findings, at + " Artikelkarte", text, surface, TEXT);
            require(findings, at + " Kategorie", muted, surface, TEXT);
            require(findings, at + " Preis", primary, surface, TEXT);
        }
        if (config.hasNonNull("buttonLabel")) {
            require(findings, at + " Schaltfläche", onColor(accent), accent, TEXT);
        }
    }

    private static void require(List<String> findings, String what, String foreground, String background, double min) {
        double ratio = contrast(foreground, background);
        if (ratio < min) {
            String severity = ratio < UI ? "Blocker" : "Warnung";
            findings.add(String.format("%s: %s %s auf %s = %.2f:1", what, severity, foreground, background, ratio));
        }
    }

    // Same choice as onColor in frontend/src/shop/shop-theme.js.
    static String onColor(String hex) {
        return contrast("#111111", hex) > contrast("#FFFFFF", hex) ? "#111111" : "#ffffff";
    }

    static double contrast(String a, String b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(String hex) {
        double[] rgb = new double[3];
        for (int i = 0; i < 3; i++) {
            double value = Integer.parseInt(hex.substring(1 + 2 * i, 3 + 2 * i), 16) / 255.0;
            rgb[i] = value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }

    // color-mix(in srgb, a share%, b) as used for --shop-muted.
    private static String mix(String a, String b, double share) {
        StringBuilder out = new StringBuilder("#");
        for (int i = 0; i < 3; i++) {
            int x = Integer.parseInt(a.substring(1 + 2 * i, 3 + 2 * i), 16);
            int y = Integer.parseInt(b.substring(1 + 2 * i, 3 + 2 * i), 16);
            out.append(String.format("%02x", Math.round(x * share + y * (1 - share))));
        }
        return out.toString();
    }
}
