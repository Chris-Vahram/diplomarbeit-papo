package at.papo.partnerportal.export;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CsvExportResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void ordersExportHasHeaderRowUmlautRoundTripAndOneRowPerOrder() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long productId = Product.<Product>find("name", "Bohrmaschine Profi 750W").firstResult().id;

        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Müller\",\"address\":\"Teststr. 13\"},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200);

        byte[] csv = given().auth().oauth2(companyToken)
                .when().get("/api/company/orders/export")
                .then().statusCode(200)
                .contentType("text/csv;charset=UTF-8")
                .extract().asByteArray();

        List<String> lines = decodeCsvLines(csv);
        assertEquals("\"Datum\";\"Partner\";\"Kunde\";\"Wert\";\"Provision %\";\"Provision\";\"Status\"", lines.get(0));
        assertEquals(Order.count() + 1, lines.size());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Müller")),
                "erwartete Umlaut-Zeile (Müller) nicht in Export gefunden");
    }

    // The order-creation path always attaches a CommissionRecord (OrderService.createForPartner
    // - Partner.commissionPercent is non-nullable end to end), so a commission-less order is
    // structurally unreachable via the API. The export sources rows from CommissionRecord
    // itself (not Order minus a null-check), so this asserts the exclusion holds by construction:
    // row count always tracks CommissionRecord.count(), never Order.count().
    @Test
    void commissionsExportHasOneRowPerCommissionRecordOnly() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        byte[] csv = given().auth().oauth2(companyToken)
                .when().get("/api/company/commissions/export")
                .then().statusCode(200)
                .contentType("text/csv;charset=UTF-8")
                .extract().asByteArray();

        List<String> lines = decodeCsvLines(csv);
        assertEquals("\"Partner\";\"Bestell-Nr\";\"Basiswert\";\"Provision %\";\"Provision\"", lines.get(0));
        assertEquals(CommissionRecord.count() + 1, lines.size());
    }

    @Test
    void partnerCannotExportOrdersOrCommissions() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(partnerToken)
                .when().get("/api/company/orders/export")
                .then().statusCode(403);
        given().auth().oauth2(partnerToken)
                .when().get("/api/company/commissions/export")
                .then().statusCode(403);
    }

    // Strips the UTF-8 BOM before decoding, mirroring how Excel/a real CSV reader would.
    private static List<String> decodeCsvLines(byte[] csv) {
        assertEquals((byte) 0xEF, csv[0]);
        assertEquals((byte) 0xBB, csv[1]);
        assertEquals((byte) 0xBF, csv[2]);
        String text = new String(csv, 3, csv.length - 3, StandardCharsets.UTF_8);
        return text.lines().toList();
    }
}
