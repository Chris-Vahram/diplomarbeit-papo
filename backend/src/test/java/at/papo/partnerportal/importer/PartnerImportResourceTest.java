package at.papo.partnerportal.importer;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class PartnerImportResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    // ASCII-only header name here — the "mapping" multipart text part isn't
    // decoded as UTF-8, so a non-ASCII value like "Straße" wouldn't round-trip
    // and would break these commit-flow tests for a reason unrelated to what
    // they check (address umlaut decoding is covered by commitDecodesIso88591FileWithUmlauts).
    private static final String ADDRESS_MAPPING = "\"street\":\"Strasse\",\"houseNumber\":\"Hausnummer\","
            + "\"postalCode\":\"PLZ\",\"city\":\"Ort\",\"country\":\"Land\"";

    @Test
    void previewSuggestsMappingFromKnownHeaderNames() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        byte[] csv = ("Partnernummer,Firma,Straße,Hausnummer,PLZ,Ort,Land,Provision\n"
                + "P-9001,Testfirma,Teststr.,1,4020,Linz,Österreich,7.5\n")
                .getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(200)
                .body("suggestedMapping.partnerNumber", is("Partnernummer"))
                .body("suggestedMapping.name", is("Firma"))
                .body("suggestedMapping.street", is("Straße"))
                .body("suggestedMapping.houseNumber", is("Hausnummer"))
                .body("suggestedMapping.postalCode", is("PLZ"))
                .body("suggestedMapping.city", is("Ort"))
                .body("suggestedMapping.country", is("Land"))
                .body("totalRows", is(1));
    }

    @Test
    void commitAcceptsValidRowsRejectsInvalidAndDuplicateRows() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // Row 1: valid new partner. Row 2: missing name. Row 3: duplicate of seeded P-1001.
        String csvText = "Nr,Name,Strasse,Hausnummer,PLZ,Ort,Land,Provision\n"
                + "P-9002,Neupartner,Teststraße,1,4020,Linz,Österreich,8\n"
                + ",Ohne Nummer,Teststraße,1,4020,Linz,Österreich,5\n"
                + "P-1001,Duplikat,Teststraße,1,4020,Linz,Österreich,10\n";
        byte[] csv = csvText.getBytes(StandardCharsets.UTF_8);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\"," + ADDRESS_MAPPING
                + ",\"commissionPercent\":\"Provision\"}";

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
        String csvText = "Nr,Name,Strasse,Hausnummer,PLZ,Ort,Land,Provision\n"
                + "p-1001,Kleinschreibung,Teststraße,1,4020,Linz,Österreich,5\n";
        byte[] csv = csvText.getBytes(StandardCharsets.UTF_8);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\"," + ADDRESS_MAPPING
                + ",\"commissionPercent\":\"Provision\"}";

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
        // "Strasse" (no ß) must transliterate to match the "Straße" field's own label.
        byte[] csv = "Partnernummer,Firma,Strasse,Provision\nP-9003,Testfirma,Teststr. 1,7.5\n"
                .getBytes(StandardCharsets.UTF_8);

        given().auth().oauth2(token)
                .multiPart("file", "partner.csv", csv, "text/csv")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(200)
                .body("suggestedMapping.street", is("Strasse"));
    }

    @Test
    void commitDecodesIso88591FileWithUmlauts() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // File encoded as ISO-8859-1 (German-locale Excel default), not UTF-8.
        String csvText = "Nr,Name,Strasse,Hausnummer,PLZ,Ort,Land,Provision\n"
                + "P-9004,Müller & Söhne,Teststraße,1,4020,Linz,Österreich,5\n";
        byte[] csv = csvText.getBytes(StandardCharsets.ISO_8859_1);
        String mapping = "{\"partnerNumber\":\"Nr\",\"name\":\"Name\"," + ADDRESS_MAPPING
                + ",\"commissionPercent\":\"Provision\"}";

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
    void xlsxWithBlankHeaderCellDoesNotShiftLaterColumns() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // Column 1 (between "Partnernummer" and "Ort") has no header cell at all - a
        // genuinely blank/never-written cell, not an empty string - simulating a
        // manually-edited Excel file. Column 1's data cell still holds a value.
        byte[] xlsx = buildXlsx(
                new String[] { "Partnernummer", null, "Ort" },
                new String[] { "P-9005", "Zwischenspalte", "Testort" });

        given().auth().oauth2(token)
                .multiPart("file", "partner.xlsx", xlsx,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .when().post("/api/company/import/partners/preview")
                .then().statusCode(200)
                .body("headers.size()", is(3))
                .body("sampleRows[0].Ort", is("Testort"));
    }

    private static byte[] buildXlsx(String[] headerCells, String[] dataCells) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Partner");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headerCells.length; i++) {
                if (headerCells[i] != null) {
                    headerRow.createCell(i).setCellValue(headerCells[i]);
                }
            }
            Row dataRow = sheet.createRow(1);
            for (int i = 0; i < dataCells.length; i++) {
                dataRow.createCell(i).setCellValue(dataCells[i]);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
