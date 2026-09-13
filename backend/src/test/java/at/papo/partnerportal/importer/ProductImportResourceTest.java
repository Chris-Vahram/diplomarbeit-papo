package at.papo.partnerportal.importer;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ProductImportResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void commitParsesGermanDecimalPriceAndRejectsDuplicateName() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // Semicolon-delimited (German-locale export) so the comma can be a decimal
        // separator in "Preis" without being mistaken for a column delimiter.
        // Row 1: valid, German-locale decimal price. Row 2: duplicate of a seeded product name.
        String csvText = "Artikel;Preis\nImporttestartikel;19,90\nBohrmaschine Profi 750W;50\n";
        byte[] csv = csvText.getBytes(StandardCharsets.UTF_8);
        String mapping = "{\"name\":\"Artikel\",\"price\":\"Preis\"}";

        given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv, "text/csv")
                .multiPart("mapping", mapping)
                .when().post("/api/company/import/products/commit")
                .then().statusCode(200)
                .body("totalRows", is(2))
                .body("acceptedCount", is(1))
                .body("rejectedCount", is(1));

        given().auth().oauth2(token)
                .when().get("/api/company/products")
                .then().statusCode(200)
                .body("find { it.name == 'Importtestartikel' }.price", is(19.90f));
    }

    @Test
    void previewSuggestsMappingForUmlautHeader() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // "Verfügbar" must transliterate to "verfuegbar" and match the available field's alias.
        byte[] csv = "Artikel;Preis;Verfügbar\nTestartikel;9,99;ja\n".getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv, "text/csv")
                .when().post("/api/company/import/products/preview")
                .then().statusCode(200)
                .body("suggestedMapping.available", is("Verfügbar"));
    }

    @Test
    void commitAcceptsKnownAndBlankCategoriesButRejectsUnknownAndInactiveRows() {
        String token = companyToken();
        int activeId = createCategory(token, "Import aktive Kategorie");
        int inactiveId = createCategory(token, "Import inaktive Kategorie");
        deactivateCategory(token, inactiveId, "Import inaktive Kategorie");
        String csv = "Artikel;Preis;Kategorie\n"
                + "Import Kategorie aktiv;1,00;Import aktive Kategorie\n"
                + "Import Kategorie leer;2,00;\n"
                + "Import Kategorie unbekannt;3,00;Import unbekannte Kategorie\n"
                + "Import Kategorie inaktiv;4,00;Import inaktive Kategorie\n";

        given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv.getBytes(StandardCharsets.UTF_8), "text/csv")
                .multiPart("mapping", "{\"name\":\"Artikel\",\"price\":\"Preis\",\"category\":\"Kategorie\"}")
                .when().post("/api/company/import/products/commit")
                .then().statusCode(200)
                .body("totalRows", is(4))
                .body("acceptedCount", is(2))
                .body("rejectedCount", is(2))
                .body("rows[2].errors[0]", is("Artikelkategorie ist unbekannt."))
                .body("rows[3].errors[0]", is("Artikelkategorie ist deaktiviert."));

        given().auth().oauth2(token).when().get("/api/company/products")
                .then().statusCode(200)
                .body("find { it.name == 'Import Kategorie aktiv' }.categoryId", is(activeId))
                .body("find { it.name == 'Import Kategorie leer' }.categoryId", is((Object) null));

        given().auth().oauth2(token).when().get("/api/company/categories")
                .then().statusCode(200)
                .body("find { it.name == 'Import unbekannte Kategorie' }", is((Object) null));
    }

    @Test
    void commitUsesCurrentCategoryAfterPreviewRenameAndDeactivate() {
        String token = companyToken();
        int renamedId = createCategory(token, "Import Vorschau alt");
        preview(token, "Import Vorschau alt", "Import Vorschau Rename Artikel");
        updateCategory(token, renamedId, "Import Vorschau neu", true);
        commitSingleCategoryRow(token, "Import Vorschau Rename Artikel", "Import Vorschau alt")
                .statusCode(200).body("acceptedCount", is(0)).body("rejectedCount", is(1));

        int deactivatedId = createCategory(token, "Import Vorschau deaktivieren");
        preview(token, "Import Vorschau deaktivieren", "Import Vorschau Deactivate Artikel");
        deactivateCategory(token, deactivatedId, "Import Vorschau deaktivieren");
        commitSingleCategoryRow(token, "Import Vorschau Deactivate Artikel", "Import Vorschau deaktivieren")
                .statusCode(200).body("acceptedCount", is(0)).body("rejectedCount", is(1));
    }

    @Test
    void failedPersistRollsBackEarlierAcceptedProducts() {
        String token = companyToken();
        String csv = "Artikel;Preis\n"
                + "Import Rollback zuerst;1,00\n"
                + "Import Rollback fehlerhaft;99999999999,99\n";

        given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv.getBytes(StandardCharsets.UTF_8), "text/csv")
                .multiPart("mapping", "{\"name\":\"Artikel\",\"price\":\"Preis\"}")
                .when().post("/api/company/import/products/commit")
                .then().statusCode(500);

        given().auth().oauth2(token).when().get("/api/company/products")
                .then().statusCode(200)
                .body("find { it.name == 'Import Rollback zuerst' }", is((Object) null));
    }

    @Test
    void parallelCommitsWithSameCategoryFinishWithoutDeadlock() throws Exception {
        String token = companyToken();
        createCategory(token, "Import Parallel Kategorie");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Response> first = executor.submit(() -> commitSingleCategoryRowAfterStart(token,
                    "Import parallel eins", start));
            Future<Response> second = executor.submit(() -> commitSingleCategoryRowAfterStart(token,
                    "Import parallel zwei", start));
            start.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS).statusCode() == 200);
            assertTrue(second.get(10, TimeUnit.SECONDS).statusCode() == 200);
        } finally {
            executor.shutdownNow();
        }
    }

    private String companyToken() {
        return tokens.tokenFor("company-demo", "company-demo");
    }

    private int createCategory(String token, String name) {
        return given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when().post("/api/company/categories").then().statusCode(200).extract().path("id");
    }

    private void updateCategory(String token, int id, String name, boolean active) {
        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"" + name + "\",\"active\":" + active + "}")
                .when().put("/api/company/categories/" + id).then().statusCode(200);
    }

    private void deactivateCategory(String token, int id, String name) {
        updateCategory(token, id, name, false);
    }

    private void preview(String token, String category, String productName) {
        String csv = "Artikel;Preis;Kategorie\n" + productName + ";1,00;" + category + "\n";
        given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv.getBytes(StandardCharsets.UTF_8), "text/csv")
                .when().post("/api/company/import/products/preview").then().statusCode(200);
    }

    private io.restassured.response.ValidatableResponse commitSingleCategoryRow(String token, String productName,
            String category) {
        String csv = "Artikel;Preis;Kategorie\n" + productName + ";1,00;" + category + "\n";
        return given().auth().oauth2(token)
                .multiPart("file", "products.csv", csv.getBytes(StandardCharsets.UTF_8), "text/csv")
                .multiPart("mapping", "{\"name\":\"Artikel\",\"price\":\"Preis\",\"category\":\"Kategorie\"}")
                .when().post("/api/company/import/products/commit").then();
    }

    private Response commitSingleCategoryRowAfterStart(String token, String productName, CountDownLatch start)
            throws InterruptedException {
        start.await();
        return commitSingleCategoryRow(token, productName, "Import Parallel Kategorie").extract().response();
    }
}
