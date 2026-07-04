package at.papo.partnerportal.importer;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class PartnerImportResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void previewSuggestsMappingFromKnownHeaderNames() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] csv = "Partnernummer,Firma,Adresse,Provision\nP-9001,Testfirma,Teststr. 1,7.5\n"
                .getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(200)
                .body("suggestedMapping.partnerNumber", is("Partnernummer"))
                .body("suggestedMapping.name", is("Firma"))
                .body("suggestedMapping.address", is("Adresse"))
                .body("totalRows", is(1));
    }

    @Test
    void commitAcceptsValidRowsRejectsInvalidAndDuplicateRows() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // Row 1: valid new partner. Row 2: missing name. Row 3: duplicate of seeded P-1001.
        String csvText = "Nr,Name,Provision\nP-9002,Neupartner,8\n,Ohne Nummer,5\nP-1001,Duplikat,10\n";
        byte[] csv = csvText.getBytes(StandardCharsets.UTF_8);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\",\"commissionPercent\":\"Provision\"}";

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .multiPart("mapping", mapping)
                .when().post("/api/company/import/partners/commit")
                .then().statusCode(200)
                .body("totalRows", is(3))
                .body("acceptedCount", is(1))
                .body("rejectedCount", is(2));

        given().auth().oauth2(token)
                .when().get("/api/company/partners")
                .then().statusCode(200)
                .body("find { it.partnerNumber == 'P-9002' }.name", is("Neupartner"));
    }

    @Test
    void commitRejectsDuplicatePartnerNumberDifferingOnlyInCase() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // Seeded demo partner is "P-1001" — lowercase "p-1001" must still be caught as a duplicate.
        String csvText = "Nr,Name,Provision\np-1001,Kleinschreibung,5\n";
        byte[] csv = csvText.getBytes(StandardCharsets.UTF_8);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\",\"commissionPercent\":\"Provision\"}";

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .multiPart("mapping", mapping)
                .when().post("/api/company/import/partners/commit")
                .then().statusCode(200)
                .body("acceptedCount", is(0))
                .body("rejectedCount", is(1))
                .body("rows[0].errors[0]", is("Existiert bereits."));
    }

    @Test
    void previewSuggestsMappingForUmlautHeader() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // "Straße" must transliterate to "strasse" and match the address field's alias.
        byte[] csv = "Partnernummer,Firma,Straße,Provision\nP-9003,Testfirma,Teststr. 1,7.5\n"
                .getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(200)
                .body("suggestedMapping.address", is("Straße"));
    }

    @Test
    void commitDecodesIso88591FileWithUmlauts() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // File encoded as ISO-8859-1 (German-locale Excel default), not UTF-8.
        String csvText = "Nr,Name,Provision\nP-9004,Müller & Söhne,5\n";
        byte[] csv = csvText.getBytes(StandardCharsets.ISO_8859_1);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\",\"commissionPercent\":\"Provision\"}";

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .multiPart("mapping", mapping)
                .when().post("/api/company/import/partners/commit")
                .then().statusCode(200)
                .body("acceptedCount", is(1));

        given().auth().oauth2(token)
                .when().get("/api/company/partners")
                .then().statusCode(200)
                .body("find { it.partnerNumber == 'P-9004' }.name", is("Müller & Söhne"));
    }

    @Test
    void partnerCannotAccessImport() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", "a,b\n".getBytes(StandardCharsets.UTF_8), "text/csv")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(403);
    }
}
