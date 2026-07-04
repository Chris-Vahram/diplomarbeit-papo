package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class CompanyDashboardResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    // Delta-based, not exact-value: other test classes create orders against the same
    // shared Dev Services DB within a test run (see DashboardResourceTest precedent).
    // "Sanitär Bauer KG" (P-1002) has no linked Keycloak login (DemoDataSeeder), so it
    // can never receive an order in any test - a stable zero-row settlement partner.
    @Test
    void fulfilledOrderRaisesCompanyDashboardTotalsAndSettlementByExactAmount() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long productId = Product.<Product>find("name", "Schutzhelm gelb").firstResult().id;

        JsonPath before = given().auth().oauth2(companyToken)
                .when().get("/api/company/dashboard")
                .then().statusCode(200)
                .extract().jsonPath();
        BigDecimal revenueBefore = before.getObject("totalRevenue", BigDecimal.class);
        BigDecimal commissionBefore = before.getObject("totalCommission", BigDecimal.class);
        long fulfilledCountBefore = before.getLong("fulfilledOrderCount");
        BigDecimal partnerRevenueBefore = before.getObject(
                "settlement.find { it.partnerName == 'Elektro Huber GmbH' }.revenue", BigDecimal.class);

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Firmendashboardtest\",\"address\":\"Teststr. 11\"},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200);
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/dashboard")
                .then().statusCode(200)
                .body("totalRevenue", is(revenueBefore.add(new BigDecimal("12.30")).floatValue()))
                .body("totalCommission", is(commissionBefore.add(new BigDecimal("1.23")).floatValue()))
                .body("fulfilledOrderCount", is((int) fulfilledCountBefore + 1))
                .body("settlement.find { it.partnerName == 'Elektro Huber GmbH' }.revenue",
                        is(partnerRevenueBefore.add(new BigDecimal("12.30")).floatValue()))
                .body("settlement.find { it.partnerName == 'Sanitär Bauer KG' }.fulfilledOrderCount", is(0))
                .body("settlement.find { it.partnerName == 'Sanitär Bauer KG' }.revenue", is(0))
                .body("settlement.find { it.partnerName == 'Sanitär Bauer KG' }.commission", is(0));
    }

    @Test
    void reviewedOrderDoesNotCountTowardCompanyDashboardTotals() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long productId = Product.<Product>find("name", "Bohrmaschine Profi 750W").firstResult().id;

        long fulfilledCountBefore = given().auth().oauth2(companyToken)
                .when().get("/api/company/dashboard")
                .then().statusCode(200)
                .extract().jsonPath().getLong("fulfilledOrderCount");

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Reviewedtest\",\"address\":\"Teststr. 12\"},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/dashboard")
                .then().statusCode(200)
                .body("fulfilledOrderCount", is((int) fulfilledCountBefore));
    }

    @Test
    void partnerCannotAccessCompanyDashboard() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(partnerToken)
                .when().get("/api/company/dashboard")
                .then().statusCode(403);
    }
}
