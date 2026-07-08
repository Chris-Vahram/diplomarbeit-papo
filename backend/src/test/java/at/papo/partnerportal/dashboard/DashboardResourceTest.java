package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class DashboardResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    // Delta-based, not exact-value: other test classes create orders/enablements
    // against the same shared Dev Services DB within a test run, so only the
    // change caused by this test's own order is a safe thing to assert on.
    @Test
    void fulfilledOrderRaisesDashboardTotalsByExactAmount() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long productId = Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id;

        BigDecimal before = given().auth().oauth2(partnerToken)
                .when().get("/api/partner/dashboard")
                .then().statusCode(200)
                .extract().jsonPath().getObject("totalRevenue", BigDecimal.class);
        long fulfilledCountBefore = given().auth().oauth2(partnerToken)
                .when().get("/api/partner/dashboard")
                .then().statusCode(200)
                .extract().jsonPath().getLong("fulfilledOrderCount");

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Dashboardtest\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"9\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        // Status changes are forward-only (order-management spec): submitted -> reviewed -> fulfilled.
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200);
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200);

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/dashboard")
                .then().statusCode(200)
                .body("totalRevenue", is(before.add(new BigDecimal("129.00")).floatValue()))
                .body("fulfilledOrderCount", is((int) fulfilledCountBefore + 1))
                .body("latestOrders.size()", org.hamcrest.Matchers.greaterThanOrEqualTo(1));
    }

    @Test
    void companyCannotAccessPartnerDashboard() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(companyToken)
                .when().get("/api/partner/dashboard")
                .then().statusCode(403);
    }
}
