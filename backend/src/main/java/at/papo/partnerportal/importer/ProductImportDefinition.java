package at.papo.partnerportal.importer;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.category.CategoryNames;
import at.papo.partnerportal.category.CategoryService;
import at.papo.partnerportal.product.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static at.papo.partnerportal.importer.ImportValueParsing.blankToNull;
import static at.papo.partnerportal.importer.ImportValueParsing.isBlank;
import static at.papo.partnerportal.importer.ImportValueParsing.parseBoolean;
import static at.papo.partnerportal.importer.ImportValueParsing.parseDecimal;

// Product has no dedicated identifier field in the locked Phase 1 entity
// model (design.md) - name (case-insensitive) is used as the closest thing
// to one for import duplicate detection.
@ApplicationScoped
public class ProductImportDefinition implements ImportDefinition {

    @Inject
    CategoryService categoryService;

    private static final List<ImportField> FIELDS = List.of(
            new ImportField("name", "Name", true, List.of("artikel", "artikelname", "bezeichnung", "produktname")),
            new ImportField("price", "Preis", true, List.of("price", "preis netto", "verkaufspreis")),
            new ImportField("description", "Beschreibung", false, List.of("description", "text")),
            new ImportField("available", "Verfügbar", false, List.of("verfuegbar", "status", "aktiv")),
            new ImportField("category", "Kategorie", false, List.of("kategorie", "warengruppe", "category")),
            new ImportField("imageUrl", "Bild-URL", false, List.of("bild", "image", "foto"))
    );

    @Override
    public List<ImportField> fields() {
        return FIELDS;
    }

    @Override
    public void prepare(List<Map<String, String>> mappedRows) {
        java.util.Set<Long> categoryIds = new java.util.HashSet<>();
        for (Map<String, String> mappedValues : mappedRows) {
            String normalizedName = normalizedCategory(mappedValues.get("category"));
            if (normalizedName == null) {
                continue;
            }
            Category category = Category.find("normalizedName", normalizedName).firstResult();
            if (category != null) {
                categoryIds.add(category.id);
            }
        }
        categoryService.lockCategories(categoryIds);
    }

    @Override
    public List<String> validateRow(Map<String, String> mappedValues) {
        List<String> errors = new ArrayList<>();
        if (isBlank(mappedValues.get("name"))) {
            errors.add("Name fehlt.");
        }

        String price = mappedValues.get("price");
        if (isBlank(price)) {
            errors.add("Preis fehlt.");
        } else {
            try {
                if (parseDecimal(price).compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Preis darf nicht negativ sein.");
                }
            } catch (NumberFormatException e) {
                errors.add("Preis ist keine gültige Zahl.");
            }
        }

        String available = mappedValues.get("available");
        if (!isBlank(available) && parseBoolean(available) == null) {
            errors.add("Verfügbar muss ja/nein bzw. true/false sein.");
        }

        return errors;
    }

    @Override
    public List<String> validatePreparedRow(Map<String, String> mappedValues) {
        String normalizedName = normalizedCategory(mappedValues.get("category"));
        if (normalizedName == null) {
            return isBlank(mappedValues.get("category")) ? List.of() : List.of("Artikelkategorie ist ungültig.");
        }
        Category category = Category.find("normalizedName", normalizedName).firstResult();
        if (category == null) {
            return List.of("Artikelkategorie ist unbekannt.");
        }
        if (!category.active) {
            return List.of("Artikelkategorie ist deaktiviert.");
        }
        return List.of();
    }

    @Override
    public String identifierValue(Map<String, String> mappedValues) {
        return mappedValues.get("name") != null ? mappedValues.get("name").trim() : null;
    }

    @Override
    public boolean existsInDb(String identifierValue) {
        return Product.count("lower(name) = ?1", identifierValue.toLowerCase()) > 0;
    }

    @Override
    public void persist(Map<String, String> mappedValues) {
        Product product = new Product();
        product.name = mappedValues.get("name").trim();
        product.price = parseDecimal(mappedValues.get("price"));
        product.description = blankToNull(mappedValues.get("description"));
        Boolean available = parseBoolean(mappedValues.get("available"));
        product.available = available == null || available;
        String categoryName = blankToNull(mappedValues.get("category"));
        product.articleCategory = categoryName == null ? null
                : Category.find("normalizedName", CategoryNames.normalized(categoryName)).firstResult();
        product.imageUrl = blankToNull(mappedValues.get("imageUrl"));
        product.persist();
    }

    private String normalizedCategory(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return CategoryNames.normalized(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
