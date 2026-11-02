package at.papo.partnerportal.shop;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeOrigin;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderOrigin;
import at.papo.partnerportal.order.OrderStatus;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Guest checkout in partner-demo's shop (shop-checkout spec, add-shop-guest-checkout).
@QuarkusTest
class ShopOrderResourceTest {

    // Own client address per request, so neither rate limit interferes unless a test wants it.
    private static final AtomicInteger CLIENT = new AtomicInteger(1);

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    ShopOrderService shopOrderService;

    private static RequestSpecification visitor() {
        return given().header("X-Forwarded-For", "198.20.0." + (CLIENT.getAndIncrement() % 250 + 1))
                .contentType(ContentType.JSON);
    }

    private String demoShop() {
        long partnerId = QuarkusTransaction.requiringNew().call(
                () -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id);
        String company = tokens.tokenFor("company-demo", "company-demo");
        given().auth().oauth2(company).when().post("/api/company/partners/" + partnerId + "/shop/release")
                .then().statusCode(200);
        return given().auth().oauth2(company).when().get("/api/company/partners/" + partnerId + "/shop")
                .then().statusCode(200).extract().path("kennung");
    }

    private static Map<String, Object> checkout(String lastName, List<Map<String, Object>> items) {
        Map<String, Object> customer = new HashMap<>();
        customer.put("lastName", lastName);
        customer.put("firstName", "Erika");
        customer.put("street", "Hauptplatz");
        customer.put("houseNumber", "1");
        customer.put("postalCode", "4020");
        customer.put("city", "Linz");
        customer.put("country", "Österreich");
        customer.put("email", "erika@example.at");
        Map<String, Object> body = new HashMap<>();
        body.put("idempotencyKey", UUID.randomUUID().toString());
        body.put("customer", customer);
        body.put("items", items);
        return body;
    }

    private static Map<String, Object> line(String article, int quantity) {
        Map<String, Object> line = new HashMap<>();
        line.put("articleId", productId(article));
        line.put("quantity", quantity);
        return line;
    }

    private static long ordersOf(String lastName) {
        return QuarkusTransaction.requiringNew().call(() -> Order.count("kunde.lastName", lastName));
    }

    @Test
    void orderUsesServerPricesAndEntersThePipeline() {
        String kennung = demoShop();
        String lastName = "Shop-" + UUID.randomUUID();
        Map<String, Object> manipulated = line("Akkuschrauber 18V Set", 2);
        manipulated.put("price", "0.01");
        manipulated.put("grossPrice", "0.01");
        Map<String, Object> body = checkout(lastName, List.of(manipulated));
        body.put("grossTotal", "0.02");

        BigDecimal price = QuarkusTransaction.requiringNew().call(
                () -> Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().price);
        int number = visitor().body(body).when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(201)
                .body("items[0].name", is("Akkuschrauber 18V Set"))
                .body("customer.email", is("erika@example.at"))
                .body("contact.name", is(QuarkusTransaction.requiringNew().call(
                        () -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().name)))
                .extract().path("orderNumber");

        QuarkusTransaction.requiringNew().run(() -> {
            Order order = Order.findById((long) number);
            assertEquals(OrderOrigin.SHOP, order.origin);
            assertEquals(OrderStatus.SUBMITTED, order.status);
            assertEquals("PARTNER-DEMO", order.partner.partnerNumber);
            assertEquals(0, price.multiply(BigDecimal.valueOf(2)).compareTo(order.orderValue));
            assertEquals(0, price.compareTo(order.items.get(0).unitPriceSnapshot));
            assertEquals(KundeOrigin.SHOP, order.kunde.origin);
            assertEquals(order.partner.id, order.kunde.ownerPartner.id);
            CommissionRecord commission = CommissionRecord.find("order", order).firstResult();
            assertEquals(0, order.partner.commissionPercent.compareTo(commission.commissionPercentUsed));
        });

        // Partner sees it as a shop order; the company can advance it like any other order.
        String partner = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partner).when().get("/api/partner/orders")
                .then().statusCode(200)
                .body("find { it.id == " + number + " }.origin", is("SHOP"))
                .body("find { it.id == " + number + " }.status", is("SUBMITTED"));
        given().auth().oauth2(partner).when().get("/api/partner/customers")
                .then().statusCode(200).body("find { it.lastName == '" + lastName + "' }.origin", is("SHOP"));
        String company = tokens.tokenFor("company-demo", "company-demo");
        given().auth().oauth2(company).contentType(ContentType.JSON).body(Map.of("status", "REVIEWED"))
                .when().put("/api/company/orders/" + number + "/status")
                .then().statusCode(200).body("origin", is("SHOP"));
        // Partner of another company never sees the shop customer.
        given().auth().oauth2(tokens.tokenFor("partner-zwei", "partner-zwei")).when().get("/api/partner/customers")
                .then().statusCode(200).body("lastName", not(hasItem(lastName)));
    }

    @Test
    void repeatCustomerCreatesNewKundeEachTime() {
        String kennung = demoShop();
        String lastName = "Wiederholt-" + UUID.randomUUID();
        for (int i = 0; i < 2; i++) {
            visitor().body(checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1))))
                    .when().post("/api/shop/" + kennung + "/orders").then().statusCode(201);
        }
        assertEquals(2L, (long) QuarkusTransaction.requiringNew().call(
                () -> Kunde.count("lastName = ?1 and origin = ?2", lastName, KundeOrigin.SHOP)));
    }

    @Test
    void doubleSubmitCreatesOneOrder() {
        String kennung = demoShop();
        String lastName = "Doppelt-" + UUID.randomUUID();
        Map<String, Object> body = checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1)));
        int first = visitor().body(body).when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(201).extract().path("orderNumber");
        int second = visitor().body(body).when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(201).extract().path("orderNumber");
        assertEquals(first, second);
        assertEquals(1, ordersOf(lastName));
    }

    @Test
    void expiredKeysAreRemoved() {
        String kennung = demoShop();
        String lastName = "Alt-" + UUID.randomUUID();
        Map<String, Object> body = checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1)));
        visitor().body(body).when().post("/api/shop/" + kennung + "/orders").then().statusCode(201);
        String key = (String) body.get("idempotencyKey");
        QuarkusTransaction.requiringNew().run(() -> ShopOrderKey.update("createdAt = ?1 where requestKey = ?2",
                Instant.now().minus(ShopOrderService.KEY_LIFETIME).minusSeconds(60), key));
        assertTrue(shopOrderService.removeExpiredKeys() >= 1);
        assertEquals(0L, (long) QuarkusTransaction.requiringNew().call(() -> ShopOrderKey.count("requestKey", key)));
    }

    @Test
    void foreignOrUnavailableArticleIsRejected() {
        String kennung = demoShop();
        String lastName = "Fremd-" + UUID.randomUUID();
        visitor().body(checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1), line("Wasserwaage 100cm", 1))))
                .when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(409).body("message", is(ShopOrderService.UNAVAILABLE));
        Map<String, Object> missing = new HashMap<>();
        missing.put("articleId", 999_999_999L);
        missing.put("quantity", 1);
        visitor().body(checkout(lastName, List.of(missing))).when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(409);
        assertEquals(0, ordersOf(lastName));
        assertEquals(0L, (long) QuarkusTransaction.requiringNew().call(() -> Kunde.count("lastName", lastName)));
    }

    @Test
    void validationHoneypotAndQuantityBound() {
        String kennung = demoShop();
        String lastName = "Pruefung-" + UUID.randomUUID();
        String path = "/api/shop/" + kennung + "/orders";

        Map<String, Object> honeypot = checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1)));
        honeypot.put("website", "https://spam.example");
        visitor().body(honeypot).when().post(path).then().statusCode(400).body("message", is(ShopOrderService.RETRY));

        Map<String, Object> noEmail = checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1)));
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) noEmail.get("customer");
        customer.remove("email");
        visitor().body(noEmail).when().post(path).then().statusCode(400)
                .body("message", is("Bitte eine E-Mail-Adresse angeben."));

        visitor().body(checkout(lastName, List.of(line("Akkuschrauber 18V Set", 10_001)))).when().post(path)
                .then().statusCode(400);
        visitor().body(checkout(lastName, List.of())).when().post(path).then().statusCode(400);

        Map<String, Object> badKey = checkout(lastName, List.of(line("Akkuschrauber 18V Set", 1)));
        badKey.put("idempotencyKey", "kein-schluessel");
        visitor().body(badKey).when().post(path).then().statusCode(400);
        assertEquals(0, ordersOf(lastName));
    }

    @Test
    void unreachableShopGetsUniform404() {
        visitor().body(checkout("Niemand", List.of(line("Akkuschrauber 18V Set", 1))))
                .when().post("/api/shop/gibt-es-nicht/orders")
                .then().statusCode(404).body("message", is(PartnerShopService.NOT_REACHABLE));
    }

    @Test
    void orderingIsRateLimitedPerClient() {
        String kennung = demoShop();
        RequestSpecification sameClient = given().header("X-Forwarded-For", "198.20.1.1").contentType(ContentType.JSON);
        for (int i = 0; i < 5; i++) {
            sameClient.body(Map.of("website", "x")).when().post("/api/shop/" + kennung + "/orders").then().statusCode(400);
        }
        sameClient.body(Map.of("website", "x")).when().post("/api/shop/" + kennung + "/orders").then().statusCode(429);
    }

    private static long productId(String name) {
        return QuarkusTransaction.requiringNew().call(() -> Product.<Product>find("name", name).firstResult().id);
    }
}
