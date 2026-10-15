package at.papo.partnerportal.category;

import at.papo.partnerportal.product.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class CategoryMigrationService {

    @Transactional
    public void migrate() {
        List<Category> categories = Category.<Category>find("order by id")
                .withLock(LockModeType.PESSIMISTIC_WRITE).list();
        // Every row that still carries legacy text: unlinked rows get linked, all rows lose the
        // text afterwards, so stale text can never re-link a category the company removed later
        // (derive-product-category-from-catalog). Linked rows keep their reference unchanged.
        List<Product> products = Product.<Product>find("legacyCategory is not null order by id")
                .withLock(LockModeType.PESSIMISTIC_WRITE).list();

        Map<String, Set<String>> legacyNames = collectLegacyNames(products);
        Map<String, Category> categoriesByName = indexCategories(categories);
        validateCategoryNames(categoriesByName);

        for (Map.Entry<String, Set<String>> entry : legacyNames.entrySet()) {
            if (entry.getValue().size() > 1) {
                throw migrationFailure("Mehrdeutige Artikelkategorie nach Normalisierung.");
            }
            Category category = categoriesByName.get(entry.getKey());
            if (category == null) {
                category = new Category();
                category.name = entry.getValue().iterator().next();
                category.normalizedName = entry.getKey();
                category.active = true;
                category.persist();
                categoriesByName.put(entry.getKey(), category);
            }
        }

        for (Product product : products) {
            String legacyName = trimmedOrNull(product.legacyCategory);
            if (product.articleCategory == null && legacyName != null) {
                product.articleCategory = categoriesByName.get(CategoryNames.normalized(legacyName));
            }
            product.legacyCategory = null;
        }
    }

    private Map<String, Set<String>> collectLegacyNames(List<Product> products) {
        Map<String, Set<String>> names = new LinkedHashMap<>();
        for (Product product : products) {
            String legacyName = trimmedOrNull(product.legacyCategory);
            if (legacyName == null || product.articleCategory != null) {
                continue;
            }
            String normalizedName;
            try {
                normalizedName = CategoryNames.normalized(legacyName);
            } catch (RuntimeException exception) {
                throw migrationFailure("Ungültiger Artikelkategoriename in Bestandsdaten.", exception);
            }
            names.computeIfAbsent(normalizedName, ignored -> new LinkedHashSet<>()).add(legacyName);
        }
        return names;
    }

    private Map<String, Category> indexCategories(List<Category> categories) {
        Map<String, Category> byName = new HashMap<>();
        for (Category category : categories) {
            if (category.normalizedName == null || category.name == null
                    || !category.normalizedName.equals(CategoryNames.normalized(category.name))) {
                throw migrationFailure("Inkonsistente Artikelkategorie im Katalog.");
            }
            if (byName.put(category.normalizedName, category) != null) {
                throw migrationFailure("Doppelte Artikelkategorie im Katalog.");
            }
        }
        return byName;
    }

    private void validateCategoryNames(Map<String, Category> categoriesByName) {
        for (Category category : categoriesByName.values()) {
            if (category.name.strip().isEmpty() || category.name.length() > 255) {
                throw migrationFailure("Ungültiger Artikelkategoriename im Katalog.");
            }
        }
    }

    private String trimmedOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private IllegalStateException migrationFailure(String message) {
        return new IllegalStateException("Artikelkategorien-Übernahme abgebrochen: " + message);
    }

    private IllegalStateException migrationFailure(String message, Throwable cause) {
        return new IllegalStateException("Artikelkategorien-Übernahme abgebrochen: " + message, cause);
    }
}
