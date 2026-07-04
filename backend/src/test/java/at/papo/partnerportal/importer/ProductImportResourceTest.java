package at.papo.partnerportal.importer;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

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
}
