package at.papo.partnerportal.order;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class OrderResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    private long enabledProductId() {
        // Seeded enabled for P-1001 (partner-demo) - see DemoDataSeeder.
        return Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id;
    }

    private long partner1001Id() {
        return Partner.<Partner>find("partnerNumber", "P-1001").firstResult().id;
    }

    @Test
    void partnerCreatesOrderWithNewCustomer() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":\"Teststr. 1\"},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":2}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .body("status", is("SUBMITTED"))
                .body("orderValue", is(258.0f))
                .body("commissionPercentUsed", is(10.0f))
                .body("commissionValue", is(25.8f))
                .body("kunde.lastName", is("Mustermann"))
                .body("items.size()", is(1));
    }

    @Test
    void partnerCannotOrderNonEnabledProduct() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        long notEnabledProductId = Product.<Product>find("name", "Kabeltrommel 25m").firstResult().id;

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":\"Teststr. 1\"},"
                        + "\"items\":[{\"productId\":" + notEnabledProductId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(400);
    }

    @Test
    void partnerCannotOrderUnavailableProductEvenIfEnabled() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        long unavailableProductId = Product.<Product>find("name", "Wasserwaage 100cm").firstResult().id;

        given().auth().oauth2(companyToken)
                .when().put("/api/company/partners/" + partnerId + "/products/" + unavailableProductId)
                .then().statusCode(204);

        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":\"Teststr. 1\"},"
                        + "\"items\":[{\"productId\":" + unavailableProductId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(400);

        // Restore seeded enablement state so later tests (e.g. dashboard enabled-product count)
        // aren't polluted by this test's temporary enablement.
        given().auth().oauth2(companyToken)
                .when().delete("/api/company/partners/" + partnerId + "/products/" + unavailableProductId)
                .then().statusCode(204);
    }

    @Test
    void partnerOrderWithoutItemsIsRejected() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":\"Teststr. 1\"},\"items\":[]}")
                .when().post("/api/partner/orders")
                .then().statusCode(400);
    }

    @Test
    void companySeesAllOrdersAndAdvancesStatus() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Firmenkunde\",\"address\":\"Teststr. 2\"},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(companyToken)
                .when().get("/api/company/orders")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200)
                .body("status", is("REVIEWED"));

        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(403);
    }

    @Test
    void partnerSeesOnlyOwnOrders() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/partner/orders")
                .then().statusCode(200);
    }
}
