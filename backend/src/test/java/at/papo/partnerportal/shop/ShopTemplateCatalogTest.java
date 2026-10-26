package at.papo.partnerportal.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// shop-templates spec "Template catalogue" and "Templates as data". Plain unit test: the
// catalogue is loaded straight from the classpath, without Quarkus.
class ShopTemplateCatalogTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void everyPageTypeHasTwoOrThreeTemplatesWithNameDescriptionAndThumbnail() {
        List<ShopTemplate> templates = ShopTemplateCatalog.load(MAPPER, ShopTemplateCatalog.DIR);
        Map<String, Long> perPage = templates.stream()
                .collect(Collectors.groupingBy(ShopTemplate::page, Collectors.counting()));
        for (String page : List.of("startseite", "artikel", "artikeldetail")) {
            long count = perPage.getOrDefault(page, 0L);
            assertTrue(count >= 2 && count <= 3, page + ": " + count + " Vorlagen");
        }
        assertTrue(perPage.getOrDefault(ShopTemplateCatalog.FREE_PAGE, 0L) >= 2);
        for (ShopTemplate template : templates) {
            assertFalse(template.name().isBlank(), template.id());
            assertFalse(template.description().isBlank(), template.id());
            assertTrue(Files.isRegularFile(Path.of("../frontend/assets/shop-templates", template.thumbnail())),
                    "Vorschaubild fehlt: " + template.thumbnail());
        }
    }

    @Test
    void nothingInTheShippedCatalogueIsRejected() throws Exception {
        int listed = MAPPER.readTree(getClass().getClassLoader()
                .getResourceAsStream(ShopTemplateCatalog.DIR + "index.json")).size();
        assertEquals(listed, ShopTemplateCatalog.load(MAPPER, ShopTemplateCatalog.DIR).size());
    }

    @Test
    void everyMandatoryPageStartsFromACatalogueTemplate() {
        List<ShopTemplate> templates = ShopTemplateCatalog.load(MAPPER, ShopTemplateCatalog.DIR);
        for (ShopPageSlot slot : ShopPageSlot.values()) {
            if (slot.kind == ShopPageKind.MANDATORY) {
                assertTrue(templates.stream().anyMatch(template -> template.id().equals(slot.defaultTemplate)
                        && ShopTemplateCatalog.slot(template) == slot), slot.name());
            }
        }
    }

    @Test
    void brokenEntriesAreLeftOutAndTheRestStillLoads() {
        List<ShopTemplate> templates = ShopTemplateCatalog.load(MAPPER, "shop-templates-broken/");
        assertEquals(List.of("gut"), templates.stream().map(ShopTemplate::id).toList());
        assertEquals("Gut", templates.get(0).name());
    }

    @Test
    void missingIndexGivesAnEmptyCatalogue() {
        assertTrue(ShopTemplateCatalog.load(MAPPER, "gibt-es-nicht/").isEmpty());
    }
}
