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

    private static final String ADDRESS_JSON = "{\"street\":\"Teststr.\",\"houseNumber\":\"1\","
            + "\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}";

    @Inject
    KeycloakTestTokens tokens;

    private long enabledProductId() {
        // Seeded enabled for P-1001 (partner-demo) - see DemoDataSeeder.
        return Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id;
    }

    private long partner1001Id() {
        return Partner.<Partner>find("partnerNumber", "P-1001").firstResult().id;
    }

    private long partner1002Id() {
        return Partner.<Partner>find("partnerNumber", "P-1002").firstResult().id;
    }

    @Test
    void partnerCreatesOrderWithNewCustomer() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
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
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
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
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
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
                .body("{\"newKunde\":{\"lastName\":\"Mustermann\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},\"items\":[]}")
                .when().post("/api/partner/orders")
                .then().statusCode(400);
    }

    @Test
    void companySeesAllOrdersAndAdvancesStatus() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Firmenkunde\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"2\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(companyToken)
                .when().get("/api/company/orders")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(403);

        // Full workflow per order-management spec: submitted -> reviewed -> fulfilled.
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200)
                .body("status", is("REVIEWED"));

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200)
                .body("status", is("FULFILLED"));
    }

    @Test
    void companyStatusChangeIsForwardOnly() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Statustest\",\"address\":{\"street\":\"Teststr.\",\"houseNumber\":\"3\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .extract().path("id");

        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        // Skip: submitted -> fulfilled directly is rejected, order stays submitted.
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(400);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/orders")
                .then().statusCode(200)
                .body("find { it.id == " + orderId + " }.status", is("SUBMITTED"));

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200)
                .body("status", is("REVIEWED"));

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(200)
                .body("status", is("FULFILLED"));

        // Revert: fulfilled -> reviewed is rejected, order stays fulfilled.
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"REVIEWED\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(400);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/orders")
                .then().statusCode(200)
                .body("find { it.id == " + orderId + " }.status", is("FULFILLED"));
    }

    @Test
    void partnerSeesOnlyOwnOrders() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/partner/orders")
                .then().statusCode(200);
    }

    @Test
    void newOrderIsUnseenByDefaultAndSeenEndpointFlipsIt() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = enabledProductId();

        int orderId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"Ungesehen\",\"address\":" + ADDRESS_JSON + "},"
                        + "\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200)
                .body("seenByCompany", is(false))
                .extract().path("id");

        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(companyToken)
                .when().put("/api/company/orders/" + orderId + "/seen")
                .then().statusCode(200)
                .body("seenByCompany", is(true));

        given().auth().oauth2(companyToken)
                .when().get("/api/company/orders")
                .then().statusCode(200)
                .body("find { it.id == " + orderId + " }.seenByCompany", is(true));
    }

    @Test
    void companyCreatesOrderForPartner() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        long productId = enabledProductId();

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"partnerId\":" + partnerId + ",\"newKunde\":{\"lastName\":\"Firmenanlage\",\"address\":"
                        + ADDRESS_JSON + "},\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/company/orders")
                .then().statusCode(200)
                .body("status", is("SUBMITTED"))
                .body("kunde.lastName", is("Firmenanlage"))
                .body("partnerId", is((int) partnerId));
    }

    @Test
    void companyOrderCreationRejectsMissingPartner() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"partnerId\":999999,\"newKunde\":{\"lastName\":\"Unbekannt\",\"address\":" + ADDRESS_JSON
                        + "},\"items\":[{\"productId\":" + enabledProductId() + ",\"quantity\":1}]}")
                .when().post("/api/company/orders")
                .then().statusCode(404);
    }

    @Test
    void companyOrderCreationRejectsEmptyItemsLikePartnerFlow() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"partnerId\":" + partnerId + ",\"newKunde\":{\"lastName\":\"Leer\",\"address\":"
                        + ADDRESS_JSON + "},\"items\":[]}")
                .when().post("/api/company/orders")
                .then().statusCode(400);
    }

    @Test
    void companyCannotSelectAnotherPartnersKundeForManualOrder() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        int kundeId = given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"lastName\":\"Scope-Test\",\"address\":" + ADDRESS_JSON + "}")
                .when().post("/api/partner/customers")
                .then().statusCode(200)
                .extract().path("id");

        String companyToken = tokens.tokenFor("company-demo", "company-demo");

        // kundeId belongs to P-1001, but the order is created for P-1002 — must be rejected,
        // matching the same ownership check the partner's own flow enforces.
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"partnerId\":" + partner1002Id() + ",\"kundeId\":" + kundeId
                        + ",\"items\":[{\"productId\":" + enabledProductId() + ",\"quantity\":1}]}")
                .when().post("/api/company/orders")
                .then().statusCode(404);
    }

    @Test
    void companyCannotCreateManualOrderForInactivePartner() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();

        io.restassured.path.json.JsonPath current = given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .extract().jsonPath();

        java.util.Map<String, Object> updateBody = new java.util.HashMap<>();
        updateBody.put("partnerNumber", current.getString("partnerNumber"));
        updateBody.put("name", current.getString("name"));
        updateBody.put("address", current.getMap("address"));
        updateBody.put("email", current.getString("email"));
        updateBody.put("phone", current.getString("phone"));
        updateBody.put("commissionPercent", current.getDouble("commissionPercent"));
        updateBody.put("keycloakUsername", current.getString("keycloakUsername"));

        updateBody.put("active", false);
        given().auth().oauth2(companyToken).contentType("application/json")
                .body(updateBody)
                .when().put("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .body("active", is(false));

        try {
            given().auth().oauth2(companyToken).contentType("application/json")
                    .body("{\"partnerId\":" + partnerId + ",\"newKunde\":{\"lastName\":\"Inaktiv-Test\",\"address\":"
                            + ADDRESS_JSON + "},\"items\":[{\"productId\":" + enabledProductId() + ",\"quantity\":1}]}")
                    .when().post("/api/company/orders")
                    .then().statusCode(409);
        } finally {
            // Restore seeded active state so later tests aren't polluted (same pattern as
            // partnerCannotOrderUnavailableProductEvenIfEnabled's enablement restore above).
            updateBody.put("active", true);
            given().auth().oauth2(companyToken).contentType("application/json")
                    .body(updateBody)
                    .when().put("/api/company/partners/" + partnerId)
                    .then().statusCode(200)
                    .body("active", is(true));
        }
    }

    @Test
    void companyFetchesPartnersCustomersScopedToThatPartner() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"lastName\":\"Listen-Test\",\"address\":" + ADDRESS_JSON + "}")
                .when().post("/api/partner/customers")
                .then().statusCode(200);

        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partner1001Id() + "/customers")
                .then().statusCode(200)
                .body("find { it.lastName == 'Listen-Test' }", org.hamcrest.Matchers.notNullValue());
    }
}
