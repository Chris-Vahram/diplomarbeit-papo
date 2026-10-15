package at.papo.partnerportal.category;

import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// derive-product-category-from-catalog: the catalogue reference is the only category source.
@QuarkusTest
class ProductCategorySourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Test
    void renamingACategoryDoesNotWriteArticles() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        long categoryId = QuarkusTransaction.requiringNew().call(() -> {
            Category category = new Category();
            category.name = "Quelle Alt";
            category.normalizedName = "quelle alt";
            category.active = true;
            category.persist();
            Product product = new Product();
            product.name = "Quelle Artikel";
            product.price = new BigDecimal("1.00");
            product.articleCategory = category;
            product.persist();
            return category.id;
        });
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Quelle Neu\",\"active\":true}")
                .when().put("/api/company/categories/" + categoryId)
                .then().statusCode(200);

        assertEquals(0, statistics.getEntityStatistics(Product.class.getName()).getUpdateCount());
        given().auth().oauth2(token).when().get("/api/company/products")
                .then().statusCode(200).body("find { it.name == 'Quelle Artikel' }.category", is("Quelle Neu"));
    }

    @Test
    void seededArticlesReferenceTheCatalogue() {
        QuarkusTransaction.requiringNew().run(() -> {
            Product drill = Product.find("name", "Akkuschrauber 18V Set").firstResult();
            assertNotNull(drill.articleCategory);
            assertEquals("Werkzeug", drill.articleCategory.name);
            assertNull(drill.legacyCategory, "new data never writes the legacy text column");
        });
    }

    @Test
    void createdArticleStoresOnlyTheReference() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        long id = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Nur Referenz\",\"price\":2.5,\"available\":true,\"category\":\"Werkzeug\"}")
                .when().post("/api/company/products")
                .then().statusCode(200).body("category", is("Werkzeug")).extract().jsonPath().getLong("id");
        QuarkusTransaction.requiringNew().run(() -> assertNull(Product.<Product>findById(id).legacyCategory));
    }
}
