package at.papo.partnerportal.enablement;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class EnablementResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    private long partner1001Id() {
        return Partner.<Partner>find("partnerNumber", "P-1001").firstResult().id;
    }

    // Not part of DemoDataSeeder's enablements for P-1001 - keeps this test's
    // enable/disable round-trip from disturbing the seeded enabled-products count
    // that partnerSeesOnlyOwnEnabledProducts asserts on.
    private long unseededProductId() {
        return Product.<Product>find("name", "Wasserwaage 100cm").firstResult().id;
    }

    @Test
    void companyEnablesAndDisablesProductForPartner() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        long productId = unseededProductId();

        given().auth().oauth2(token)
                .when().put("/api/company/partners/" + partnerId + "/products/" + productId)
                .then().statusCode(204);

        given().auth().oauth2(token)
                .when().get("/api/company/partners/" + partnerId + "/products")
                .then().statusCode(200)
                .body("find { it.productId == " + productId + " }.enabled", is(true));

        given().auth().oauth2(token)
                .when().delete("/api/company/partners/" + partnerId + "/products/" + productId)
                .then().statusCode(204);

        given().auth().oauth2(token)
                .when().get("/api/company/partners/" + partnerId + "/products")
                .then().statusCode(200)
                .body("find { it.productId == " + productId + " }.enabled", is(false));
    }

    @Test
    void partnerCannotAccessEnablementManagement() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/company/partners/1/products")
                .then().statusCode(403);
    }

    @Test
    void partnerSeesOnlyOwnEnabledProducts() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/partner/products")
                .then().statusCode(200)
                .body("size()", is(3));
    }

    // product-enablement spec, "No enabled products handling": the endpoint the
    // order-creation UI reads from must return an empty (not error) list so the
    // frontend can show its empty-state message.
    @Test
    void partnerWithNoEnabledProductsSeesEmptyList() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        java.util.List<Integer> enabledProductIds = given().auth().oauth2(partnerToken)
                .when().get("/api/partner/products")
                .then().statusCode(200)
                .extract().jsonPath().getList("id", Integer.class);

        for (int productId : enabledProductIds) {
            given().auth().oauth2(companyToken)
                    .when().delete("/api/company/partners/" + partnerId + "/products/" + productId)
                    .then().statusCode(204);
        }

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/products")
                .then().statusCode(200)
                .body("size()", is(0));

        // Restore, so partnerSeesOnlyOwnEnabledProducts and the dashboard tests
        // keep seeing the seeded 3 enabled products regardless of test order.
        for (int productId : enabledProductIds) {
            given().auth().oauth2(companyToken)
                    .when().put("/api/company/partners/" + partnerId + "/products/" + productId)
                    .then().statusCode(204);
        }
    }
}
