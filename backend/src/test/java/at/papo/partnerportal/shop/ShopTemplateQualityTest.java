package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// shop-templates spec "Template quality" (design.md Decision 3): every template is checked with
// its own theme suggestion and with the default theme against the server's blocker rules
// (ShopDesignRules, the same units that gate publishing). Warnungen and the other assistant
// rules run over the same templates in tools/check_shop_assistant.mjs. Required fields and
// references are already enforced when the catalogue loads (see ShopTemplateCatalogTest).
class ShopTemplateQualityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void everyTemplatePassesTheServerBlockerRulesWithOwnAndDefaultTheme() throws Exception {
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

    private static void check(ShopTemplate template, JsonNode theme, String themeName, List<String> findings) {
        String where = template.id() + " (" + themeName + "): ";
        ShopDesignRules.themeBlockers(theme).forEach(problem -> findings.add(where + problem));
        ShopDesignRules.blockBlockers(template.content(), theme).forEach(problem -> findings.add(where + problem));
    }
}
