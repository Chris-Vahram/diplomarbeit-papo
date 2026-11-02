package at.papo.partnerportal.importer;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.category.CategoryNames;
import at.papo.partnerportal.category.CategoryService;
import at.papo.partnerportal.product.ArticleTax;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductImage;
import at.papo.partnerportal.security.CurrentCompany;
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

    @Inject
    CurrentCompany currentCompany;

    // Internal image reference as stored on products ("/product-images/{id}"); only images of
    // the importing company may be referenced (add-company-tenancy).
    private static final java.util.regex.Pattern INTERNAL_IMAGE_URL =
            java.util.regex.Pattern.compile("^/product-images/(\\d+)$");

    private static final List<ImportField> FIELDS = List.of(
            new ImportField("name", "Name", true, List.of("artikel", "artikelname", "bezeichnung", "produktname")),
            new ImportField("price", "Preis", true, List.of("price", "preis netto", "preis brutto", "verkaufspreis")),
            new ImportField("taxRate", "Steuersatz (%)", false,
                    List.of("ust", "ust satz", "mwst", "mwst satz", "steuersatz", "steuer", "tax rate")),
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
            Category category = Category.find("company = ?1 and normalizedName = ?2", currentCompany.resolve(), normalizedName).firstResult();
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

        String taxRate = mappedValues.get("taxRate");
        if (!isBlank(taxRate) && taxRate(taxRate) == null) {
            errors.add("Steuersatz \"" + taxRate.trim() + "\" ist ungültig (erlaubt: 20, 13, 10 oder 0 %).");
        }

        String available = mappedValues.get("available");
        if (!isBlank(available) && parseBoolean(available) == null) {
            errors.add("Verfügbar muss ja/nein bzw. true/false sein.");
        }

        return errors;
    }

    @Override
    public List<String> validatePreparedRow(Map<String, String> mappedValues) {
        java.util.regex.Matcher image = INTERNAL_IMAGE_URL.matcher(String.valueOf(blankToNull(mappedValues.get("imageUrl"))));
        if (image.matches() && ProductImage.count("id = ?1 and company = ?2",
                Long.valueOf(image.group(1)), currentCompany.resolve()) == 0) {
            return List.of("Bild nicht gefunden.");
        }
        String normalizedName = normalizedCategory(mappedValues.get("category"));
        if (normalizedName == null) {
            return isBlank(mappedValues.get("category")) ? List.of() : List.of("Artikelkategorie ist ungültig.");
        }
        Category category = Category.find("company = ?1 and normalizedName = ?2", currentCompany.resolve(), normalizedName).firstResult();
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
        return Product.count("company = ?1 and lower(name) = ?2", currentCompany.resolve(),
                identifierValue.toLowerCase()) > 0;
    }

    @Override
    public void persist(Map<String, String> mappedValues) {
        Product product = new Product();
        product.company = currentCompany.resolve();
        product.name = mappedValues.get("name").trim();
        product.price = parseDecimal(mappedValues.get("price"));
        product.taxRatePercent = ArticleTax.normalized(rateOrDefault(mappedValues.get("taxRate")));
        product.description = blankToNull(mappedValues.get("description"));
        Boolean available = parseBoolean(mappedValues.get("available"));
        product.available = available == null || available;
        String categoryName = blankToNull(mappedValues.get("category"));
        product.articleCategory = categoryName == null ? null
                : Category.find("company = ?1 and normalizedName = ?2", product.company,
                        CategoryNames.normalized(categoryName)).firstResult();
        product.imageUrl = blankToNull(mappedValues.get("imageUrl"));
        product.persist();
    }

    // Net price column: replaces the price with the gross value (row rate or 20 %) before validation.
    // Unparseable price or rate stays as it is, so validation reports it unchanged.
    public Map<String, String> withGrossPrice(Map<String, String> mappedValues) {
        String price = mappedValues.get("price");
        String taxRate = mappedValues.get("taxRate");
        if (isBlank(price) || (!isBlank(taxRate) && taxRate(taxRate) == null)) {
            return mappedValues;
        }
        try {
            BigDecimal net = parseDecimal(price);
            mappedValues.put("price", ArticleTax.grossFromNet(net, rateOrDefault(taxRate)).toPlainString());
        } catch (NumberFormatException e) {
            // reported by validateRow
        }
        return mappedValues;
    }

    private static BigDecimal rateOrDefault(String value) {
        return isBlank(value) ? ArticleTax.DEFAULT_RATE : taxRate(value);
    }

    // Allowed rate or null; accepts "20", "20 %", "20,0".
    private static BigDecimal taxRate(String value) {
        try {
            BigDecimal rate = parseDecimal(value.replace("%", ""));
            return ArticleTax.isAllowed(rate) ? rate : null;
        } catch (NumberFormatException e) {
            return null;
        }
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
