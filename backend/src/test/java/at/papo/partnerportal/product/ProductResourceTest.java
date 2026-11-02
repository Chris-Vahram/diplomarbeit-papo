package at.papo.partnerportal.product;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
    void newProductDefaultsToTwentyPercentAndShowsNet() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        int id = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Steuertest Standard\",\"price\":12.00,\"available\":true}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .body("taxRatePercent", is(20.0f))
                .body("netPrice", is(10.0f))
                .extract().path("id");

        // Update without a rate keeps the stored one; a chosen rate is stored.
        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Steuertest Standard\",\"price\":11.30,\"available\":true,\"taxRatePercent\":13}")
                .when().put("/api/company/products/" + id)
                .then().statusCode(200)
                .body("taxRatePercent", is(13.0f))
                .body("netPrice", is(10.0f));
        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Steuertest Standard\",\"price\":11.30,\"available\":true}")
                .when().put("/api/company/products/" + id)
                .then().statusCode(200)
                .body("taxRatePercent", is(13.0f));
    }

    @Test
    void invalidTaxRateIsRejected() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Steuertest Ungültig\",\"price\":5.00,\"taxRatePercent\":19}")
                .when().post("/api/company/products")
                .then().statusCode(400)
                .body("message", is("Bitte einen gültigen Steuersatz wählen."));
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

    // Covers ProductService.applyImage: an internal "/api/product-images/{id}" reference
    // sets the image link and is echoed back verbatim; switching to an external URL clears
    // that link again (observable via the product picking up the new imageUrl unchanged).
    @Test
    void productImageUrlSwitchesBetweenUploadedAndExternal() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] png = DemoPngGenerator.solidColorPng(2, 2, 30, 30, 30);
        String uploadedUrl = given().auth().oauth2(token)
                .multiPart("file", "test.png", png, "image/png")
                .when().post("/api/product-images")
                .then().statusCode(200)
                .extract().path("url");

        int id = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Bildartikel\",\"price\":5.00,\"imageUrl\":\"" + uploadedUrl + "\"}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .body("imageUrl", org.hamcrest.Matchers.is(uploadedUrl))
                .extract().path("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Bildartikel\",\"price\":5.00,\"imageUrl\":\"https://example.at/bild.jpg\"}")
                .when().put("/api/company/products/" + id)
                .then().statusCode(200)
                .body("imageUrl", org.hamcrest.Matchers.is("https://example.at/bild.jpg"));
    }

    @Test
    void companyAssignsProductCategoryByIdAndReturnsCurrentName() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        int categoryId = createCategory(token, "ID Kategorie");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"ID Kategorieartikel\",\"price\":7.50,\"categoryId\":" + categoryId + "}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .body("categoryId", is(categoryId))
                .body("category", is("ID Kategorie"));
    }

    @Test
    void legacyCategoryNameResolvesWithoutCreatingCategory() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        int categoryId = createCategory(token, "Legacy Kategorie");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Legacy Kategorieartikel\",\"price\":8.50,\"category\":\" legacy kategorie \"}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .body("categoryId", is(categoryId))
                .body("category", is("Legacy Kategorie"));
    }

    @Test
    void unknownOrConflictingCategoryInputLeavesProductUnchanged() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        int productId = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Kategoriefehlerartikel\",\"price\":11.00}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Kategoriefehlerartikel geändert\",\"price\":12.00,\"categoryId\":999999}")
                .when().put("/api/company/products/" + productId)
                .then().statusCode(400);

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Kategoriefehlerartikel geändert\",\"price\":12.00,\"category\":\"Unbekannte Kategorie\"}")
                .when().put("/api/company/products/" + productId)
                .then().statusCode(400);

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Kategoriefehlerartikel geändert\",\"price\":12.00,\"categoryId\":1,\"category\":\"Werkzeug\"}")
                .when().put("/api/company/products/" + productId)
                .then().statusCode(400);

        given().auth().oauth2(token)
                .when().get("/api/company/products/" + productId)
                .then().statusCode(200)
                .body("name", is("Kategoriefehlerartikel"))
                .body("categoryId", nullValue())
                .body("category", nullValue());
    }

    @Test
    void categoryCanBeClearedByOmittingBothInputs() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        int categoryId = createCategory(token, "Löschbare Zuordnung");
        int productId = given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Zuordnungartikel\",\"price\":13.00,\"categoryId\":" + categoryId + "}")
                .when().post("/api/company/products")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Zuordnungartikel\",\"price\":14.00}")
                .when().put("/api/company/products/" + productId)
                .then().statusCode(200)
                .body("categoryId", nullValue())
                .body("category", nullValue());
    }

    private int createCategory(String token, String name) {
        return given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when().post("/api/company/categories")
                .then().statusCode(200)
                .extract().path("id");
    }
}
