package at.papo.partnerportal.product;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class ProductResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyListsSeededProducts() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .when().get("/api/company/products")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(8));
    }

    @Test
    void companyCreatesAndUpdatesProduct() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        int id = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Testartikel\",\"price\":9.99,\"description\":\"Test\",\"available\":true}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .body("name", org.hamcrest.Matchers.is("Testartikel"))
                .extract().path("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Testartikel\",\"price\":12.50,\"description\":\"Geändert\",\"available\":false}")
                .when().put("/api/company/products/" + id)
                .then().statusCode(200)
                .body("price", org.hamcrest.Matchers.is(12.5f))
                .body("available", org.hamcrest.Matchers.is(false));
    }

    @Test
    void createWithoutNameIsRejected() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"price\":9.99}")
                .when().post("/api/company/products")
                .then().statusCode(400);
    }

    @Test
    void partnerCannotAccessProductManagement() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/company/products")
                .then().statusCode(403);
    }
}
