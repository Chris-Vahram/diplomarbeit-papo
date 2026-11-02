package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Server side of the design assistant (shop-design-assistant spec "Publish gating"): the
// contrast blockers (below 3:1) re-checked on publish. Required fields and the mandatory
// Artikeldetail block are ShopBlockValidator's; Warnungen and Tipps stay in the editor.
// The pairs mirror frontend/src/shop-editor/assistant/contrast-pairs.js and shop.css.
final class ShopDesignRules {

    static final double BLOCKER = 3.0;

    private ShopDesignRules() {
    }

    record Pair(String label, String foreground, String background) {

        double ratio() {
            return ShopContrast.contrast(foreground, background);
        }

        String identity() {
            return foreground.toLowerCase(Locale.ROOT) + "|" + background.toLowerCase(Locale.ROOT);
        }
    }

    // Design publish: the theme's own pairs, then every online page as it will look with it.
    static void requireReadableTheme(JsonNode theme, List<ShopPage> onlinePages, ShopPageService pages) {
        List<String> problems = new ArrayList<>(themeBlockers(theme));
        for (ShopPage page : onlinePages) {
            if (page.publishedContent != null) {
                blockBlockers(pages.parse(page.publishedContent), theme)
                        .forEach(problem -> problems.add("Seite „" + page.menuName + "“, " + problem));
            }
        }
        reject(problems, "das Design");
    }

    // Page publish: the blocks with the design visitors see (the published theme).
    static void requireReadablePage(JsonNode content, JsonNode theme) {
        reject(blockBlockers(content, theme), "diese Seite");
    }

    static List<String> themeBlockers(JsonNode theme) {
        return themePairs(theme.get("colors")).stream()
                .filter(pair -> pair.ratio() < BLOCKER)
                .map(ShopDesignRules::describe)
                .toList();
    }

    // Block pairs equal to a theme pair belong to the design and are reported there.
    static List<String> blockBlockers(JsonNode content, JsonNode theme) {
        JsonNode colors = theme.get("colors");
        Set<String> ofTheme = new HashSet<>();
        themePairs(colors).forEach(pair -> ofTheme.add(pair.identity()));
        String form = theme.path("form").asText("klar");
        List<String> problems = new ArrayList<>();
        int index = 0;
        for (JsonNode block : content.path("blocks")) {
            index++;
            ObjectNode effective = colors.deepCopy();
            JsonNode overrides = block.path("config").get("colors");
            if (overrides != null && overrides.isObject()) {
                effective.setAll((ObjectNode) overrides);
            }
            String label = ShopBlockType.byId(block.path("type").asText()).map(type -> type.label).orElse("");
            for (Pair pair : blockPairs(block.path("type").asText(), block.path("config"), effective, form)) {
                if (pair.ratio() < BLOCKER && !ofTheme.contains(pair.identity())) {
                    problems.add("Abschnitt " + index + " („" + label + "“): " + describe(pair));
                }
            }
        }
        return problems;
    }

    static List<Pair> themePairs(JsonNode c) {
        String text = c.get("text").asText();
        String background = c.get("background").asText();
        String surface = c.get("surface").asText();
        String primary = c.get("primary").asText();
        String accent = c.get("accent").asText();
        String muted = ShopContrast.mix(text, background, 0.68);
        return List.of(
                new Pair("Text auf dem Seitenhintergrund", text, background),
                new Pair("Nebentext auf dem Seitenhintergrund", muted, background),
                new Pair("Text auf Flächen (Kopfzeile, Fußzeile, Karten)", text, surface),
                new Pair("Nebentext auf Flächen", muted, surface),
                new Pair("Markierung des aktuellen Menüpunkts", primary, surface),
                new Pair("Preise auf Artikelkarten", primary, surface),
                new Pair("Text auf der Hauptfarbe", ShopContrast.onColor(primary), primary),
                new Pair("Text auf der Akzentfarbe und auf Schaltflächen", ShopContrast.onColor(accent), accent));
    }

    static List<Pair> blockPairs(String type, JsonNode config, JsonNode c, String form) {
        String text = c.get("text").asText();
        String background = c.get("background").asText();
        String surface = c.get("surface").asText();
        String primary = c.get("primary").asText();
        String accent = c.get("accent").asText();
        String muted = ShopContrast.mix(text, background, 0.68);
        List<Pair> pairs = new ArrayList<>();
        boolean button = !config.path("buttonLabel").asText("").isEmpty();
        // A hero photo changes the ground: "handwerk" sets the text on a surface panel,
        // "kraeftig" on a primary scrim over the photo; "klar" keeps it on the block background.
        if (type.equals("hero") && config.hasNonNull("imageId") && !form.equals("klar")) {
            if (form.equals("handwerk")) {
                pairs.add(new Pair("Text im Textfeld über dem Foto", text, surface));
            } else {
                pairs.add(new Pair("Text auf dem Foto", ShopContrast.onColor(primary), primary));
            }
            if (button) {
                pairs.add(new Pair("Schaltfläche", ShopContrast.onColor(accent), accent));
            }
            return pairs;
        }
        switch (config.path("background").asText("none")) {
            case "primary" -> pairs.add(new Pair("Text auf der Hauptfarbe", ShopContrast.onColor(primary), primary));
            case "accent" -> pairs.add(new Pair("Text auf der Akzentfarbe", ShopContrast.onColor(accent), accent));
            case "surface" -> {
                pairs.add(new Pair("Text auf der Fläche", text, surface));
                pairs.add(new Pair("Nebentext auf der Fläche", muted, surface));
            }
            default -> {
                pairs.add(new Pair("Text", text, background));
                pairs.add(new Pair("Nebentext", muted, background));
            }
        }
        if (List.of("article-grid", "featured-articles", "article-detail").contains(type)) {
            pairs.add(new Pair("Text auf Artikelkarten", text, surface));
            pairs.add(new Pair("Kategorie auf Artikelkarten", muted, surface));
            pairs.add(new Pair("Preise", primary, surface));
        }
        if (button) {
            pairs.add(new Pair("Schaltfläche", ShopContrast.onColor(accent), accent));
        }
        return pairs;
    }

    private static String describe(Pair pair) {
        return String.format(Locale.GERMAN, "%s: Kontrast %.1f:1, mindestens 3:1 nötig", pair.label(), pair.ratio());
    }

    private static void reject(List<String> problems, String subject) {
        if (!problems.isEmpty()) {
            throw new ApiException(400, "Text ist kaum lesbar, deshalb kann " + subject + " nicht veröffentlicht werden. "
                    + String.join("; ", problems) + ".");
        }
    }
}
