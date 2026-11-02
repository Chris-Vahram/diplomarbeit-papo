package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// shop-templates spec "Template quality": every template is checked with its own theme
// suggestion and with the default theme against the design assistant's contrast thresholds
// (Warnung below 4.5:1, Blocker below 3:1, formula from ShopContrast) and alt-text rule.
// Templates must not even raise a Warnung, so this is stricter than ShopDesignRules, which only
// re-checks blockers on publish. Required fields and references are already enforced when the
// catalogue loads (see ShopTemplateCatalogTest).
class ShopTemplateQualityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final double TEXT = 4.5;
    private static final double UI = 3.0;

    @Test
    void everyTemplatePassesContrastAndAltTextWithOwnAndDefaultTheme() throws Exception {
        JsonNode defaultTheme = MAPPER.readTree(getClass().getClassLoader()
                .getResourceAsStream(ShopTemplateCatalog.DIR + "theme.json"));
        List<ShopTemplate> templates = ShopTemplateCatalog.load(MAPPER, ShopTemplateCatalog.DIR);
        // Free pages have no theme of their own and must work in every style's design.
        List<ShopTemplate> styles = templates.stream().filter(template -> template.theme() != null).toList();
        List<String> findings = new ArrayList<>();
        for (ShopTemplate template : templates) {
            check(template, defaultTheme, "Standard-Theme", findings);
            if (template.theme() != null) {
                check(template, template.theme(), "eigenes Theme", findings);
            } else {
                for (ShopTemplate style : styles) {
                    check(template, style.theme(), "Theme von " + style.id(), findings);
                }
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
            checkBlock(findings, at, block.get("type").asText(), config, effective, theme.path("form").asText("klar"));
            if (config.hasNonNull("imageId") && config.path("alt").asText("").isBlank()) {
                findings.add(at + ": Bildbeschreibung fehlt");
            }
        }
    }

    // Mirrors shop.css: text colour per block background, muted text, cards, prices and buttons.
    private static void checkBlock(List<String> findings, String at, String type, JsonNode config, ObjectNode c,
                                   String form) {
        String text = c.get("text").asText();
        String background = c.get("background").asText();
        String surface = c.get("surface").asText();
        String primary = c.get("primary").asText();
        String accent = c.get("accent").asText();
        String muted = mix(text, background, 0.68);
        // A hero photo changes the ground: "handwerk" sets the text on a surface panel,
        // "kraeftig" on a primary scrim over the photo; "klar" keeps it beside the photo.
        if (type.equals("hero") && config.hasNonNull("imageId") && !form.equals("klar")) {
            if (form.equals("handwerk")) {
                require(findings, at + " Text im Textfeld", text, surface, TEXT);
            } else {
                require(findings, at + " Text auf Foto", onColor(primary), primary, TEXT);
            }
            if (config.hasNonNull("buttonLabel")) {
                require(findings, at + " Schaltfläche", onColor(accent), accent, TEXT);
            }
            return;
        }
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

    private static String onColor(String hex) {
        return ShopContrast.onColor(hex);
    }

    private static double contrast(String a, String b) {
        return ShopContrast.contrast(a, b);
    }

    private static String mix(String a, String b, double share) {
        return ShopContrast.mix(a, b, share);
    }
}
