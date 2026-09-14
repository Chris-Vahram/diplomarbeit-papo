package at.papo.partnerportal.category;

import at.papo.partnerportal.product.Product;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class CategoryMigrationTest {

    @Inject
    CategoryMigrationService migrationService;

    @AfterEach
    void removeFixtures() {
        QuarkusTransaction.requiringNew().run(() -> Product.delete("name like 'Migrationstest-%'"));
    }

    @Test
    void identicalLegacyNamesShareCategoryAndBlankValuesStayUnassigned() {
        long[] productIds = QuarkusTransaction.requiringNew().call(() -> {
            Product first = legacyProduct("Übernahme Kategorie");
            Product second = legacyProduct(" Übernahme Kategorie ");
            Product blank = legacyProduct("   ");
            return new long[] { first.id, second.id, blank.id };
        });

        QuarkusTransaction.requiringNew().run(() -> migrationService.migrate());

        QuarkusTransaction.requiringNew().run(() -> {
            Product first = Product.findById(productIds[0]);
            Product second = Product.findById(productIds[1]);
            Product blank = Product.findById(productIds[2]);
            assertEquals(first.articleCategory.id, second.articleCategory.id);
            assertEquals("Übernahme Kategorie", first.category);
            assertNull(blank.articleCategory);
        });
    }

    @Test
    void repeatedMigrationPreservesCategoryIdAndRemovedAssignment() {
        long productId = QuarkusTransaction.requiringNew().call(() -> legacyProduct("Idempotente Kategorie").id);

        QuarkusTransaction.requiringNew().run(() -> migrationService.migrate());
        long categoryId = QuarkusTransaction.requiringNew().call(() -> Product.<Product>findById(productId).articleCategory.id);

        QuarkusTransaction.requiringNew().run(() -> migrationService.migrate());
        long repeatedCategoryId = QuarkusTransaction.requiringNew()
                .call(() -> Product.<Product>findById(productId).articleCategory.id);
        assertEquals(categoryId, repeatedCategoryId);

        QuarkusTransaction.requiringNew().run(() -> {
            Product product = Product.findById(productId);
            product.articleCategory = null;
            product.category = null;
        });
        QuarkusTransaction.requiringNew().run(() -> migrationService.migrate());

        QuarkusTransaction.requiringNew().run(() -> assertNull(Product.<Product>findById(productId).articleCategory));
    }

    @Test
    void normalizedCollisionStopsWithoutPartialAssignment() {
        long[] productIds = QuarkusTransaction.requiringNew().call(() -> {
            Product first = legacyProduct("Kollision Kategorie");
            Product second = legacyProduct("kollision kategorie");
            return new long[] { first.id, second.id };
        });

        assertThrows(IllegalStateException.class,
                () -> QuarkusTransaction.requiringNew().run(() -> migrationService.migrate()));

        QuarkusTransaction.requiringNew().run(() -> {
            assertNull(Product.<Product>findById(productIds[0]).articleCategory);
            assertNull(Product.<Product>findById(productIds[1]).articleCategory);
            assertEquals(0, Category.count("normalizedName", "kollision kategorie"));
        });
    }

    @Test
    void inconsistentExistingForeignKeyStopsMigration() {
        long productId = QuarkusTransaction.requiringNew().call(() -> {
            Category category = new Category();
            category.name = "FK Kategorie";
            category.normalizedName = "fk kategorie";
            category.active = true;
            category.persist();

            Product product = legacyProduct("Andere Kategorie");
            product.articleCategory = category;
            return product.id;
        });

        assertThrows(IllegalStateException.class,
                () -> QuarkusTransaction.requiringNew().run(() -> migrationService.migrate()));

        QuarkusTransaction.requiringNew().run(() -> {
            Product product = Product.findById(productId);
            assertEquals("Andere Kategorie", product.category);
            assertEquals("FK Kategorie", product.articleCategory.name);
        });
    }

    private Product legacyProduct(String category) {
        Product product = new Product();
        product.name = "Migrationstest-" + System.nanoTime();
        product.price = java.math.BigDecimal.ONE;
        product.category = category;
        product.persist();
        return product;
    }
}
