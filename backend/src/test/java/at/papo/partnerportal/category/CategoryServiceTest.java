package at.papo.partnerportal.category;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class CategoryServiceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyCreatesActiveCategory() {
        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"Testkategorie\"}")
                .when().post("/api/company/categories")
                .then().statusCode(200)
                .body("name", is("Testkategorie"))
                .body("active", is(true));
    }

    @Test
    void duplicateCategoryNameIsRejectedAfterTrimAndCaseNormalization() {
        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"Doppelte Kategorie\"}")
                .when().post("/api/company/categories")
                .then().statusCode(200);

        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"  doppelte kategorie  \"}")
                .when().post("/api/company/categories")
                .then().statusCode(409)
                .body("message", is("Artikelkategorie bereits vorhanden."));
    }

    @Test
    void blankAndTooLongCategoryNamesAreRejected() {
        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"   \"}")
                .when().post("/api/company/categories")
                .then().statusCode(400);

        String tooLong = "x".repeat(256);
        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"" + tooLong + "\"}")
                .when().post("/api/company/categories")
                .then().statusCode(400);
    }

    @Test
    void unicodeCaseNormalizationFitsExpandedStorageLength() {
        String name = "İ".repeat(255);

        given().auth().oauth2(companyToken()).contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when().post("/api/company/categories")
                .then().statusCode(200)
                .body("name", is(name));
    }

    private String companyToken() {
        return tokens.tokenFor("company-demo", "company-demo");
    }
}
