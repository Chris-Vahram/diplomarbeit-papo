package at.papo.partnerportal.shop;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// End-customer accounts in the separate realm papo-shop (shop-customer-accounts spec).
@QuarkusTest
class ShopCustomerAccountTest {

    private static final AtomicInteger CLIENT = new AtomicInteger(1);

    @Inject
    KeycloakTestTokens tokens;

    @ConfigProperty(name = "papo.shop.accounts.keycloak-url")
    String keycloakUrl;

    // ---- helpers ----------------------------------------------------------------------

    private RequestSpecification customer(String token) {
        return given().auth().oauth2(token)
                .header("X-Forwarded-For", "198.30.0." + (CLIENT.getAndIncrement() % 250 + 1))
                .contentType(ContentType.JSON);
    }

    private Response shopLogin(String username, String password) {
        return given().contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "papo-shop-frontend")
                .formParam("username", username)
                .formParam("password", password)
                .when().post(keycloakUrl + "/realms/papo-shop/protocol/openid-connect/token");
    }

    private String shopToken(String username, String password) {
        return shopLogin(username, password).then().statusCode(200).extract().path("access_token");
    }

    private String adminToken() {
        return given().contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", "papo-shop-admin")
                .formParam("client_secret", "test-shop-admin-secret")
                .when().post(keycloakUrl + "/realms/papo-shop/protocol/openid-connect/token")
                .then().statusCode(200).extract().path("access_token");
    }

    // Fresh customer through the admin API, so deleting it never disturbs other tests.
    private String createCustomer(String email, boolean verified) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", email);
        user.put("email", email);
        user.put("enabled", true);
        user.put("emailVerified", verified);
        user.put("firstName", "Test");
        user.put("lastName", "Kunde");
        user.put("credentials", List.of(Map.of("type", "password", "value", "geheim-12345", "temporary", false)));
        if (!verified) {
            user.put("requiredActions", List.of("VERIFY_EMAIL"));
        }
        String location = given().auth().oauth2(adminToken()).contentType(ContentType.JSON).body(user)
                .when().post(keycloakUrl + "/admin/realms/papo-shop/users")
                .then().statusCode(201).extract().header("Location");
        return location.substring(location.lastIndexOf('/') + 1);
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

    private static Map<String, Object> checkout(String lastName) {
        Map<String, Object> customer = new HashMap<>();
        customer.put("lastName", lastName);
        customer.put("street", "Hauptplatz");
        customer.put("houseNumber", "1");
        customer.put("postalCode", "4020");
        customer.put("city", "Linz");
        customer.put("email", "kundin@example.test");
        long productId = QuarkusTransaction.requiringNew().call(
                () -> Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id);
        Map<String, Object> body = new HashMap<>();
        body.put("idempotencyKey", UUID.randomUUID().toString());
        body.put("customer", customer);
        body.put("items", List.of(Map.of("articleId", productId, "quantity", 1)));
        return body;
    }

    private int placeOrder(String token, String kennung, String lastName) {
        return customer(token).body(checkout(lastName)).when().post("/api/shop-customer/shops/" + kennung + "/orders")
                .then().statusCode(201).extract().path("orderNumber");
    }

    // ---- realm separation ---------------------------------------------------------------

    @Test
    void customerTokenIsRejectedOnPortalApis() {
        String shopToken = shopToken("kundin-eins@example.test", "kundin-eins-pw");
        for (String path : List.of("/api/partner/orders", "/api/partner/customers", "/api/company/partners",
                "/api/company/orders")) {
            given().auth().oauth2(shopToken).when().get(path).then().statusCode(401);
        }
    }

    // A customer token must not reach portal resources through a path that only looks like the
    // customer namespace (dot segments, encoded dot segments).
    @Test
    void dotSegmentsDoNotSmuggleCustomerTokensIntoPortalApis() throws Exception {
        String shopToken = shopToken("kundin-eins@example.test", "kundin-eins-pw");
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        for (String path : List.of("/api/shop-customer/../partner/orders", "/api/shop-customer/%2e%2e/partner/orders",
                "/api/shop-customer/..%2fpartner/orders", "/api/shop-customer/../company/partners")) {
            java.net.http.HttpResponse<String> response = client.send(java.net.http.HttpRequest.newBuilder(
                            java.net.URI.create(io.restassured.RestAssured.baseURI + ":" + io.restassured.RestAssured.port + path))
                    .header("Authorization", "Bearer " + shopToken).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            assertTrue(response.statusCode() == 401 || response.statusCode() == 403 || response.statusCode() == 404,
                    path + " -> " + response.statusCode() + " " + response.body());
            assertTrue(!response.body().contains("\"orderValue\"") && !response.body().contains("\"partnerNumber\""), path);
        }
    }

    @Test
    void portalTokensAndMissingTokensAreRejectedOnCustomerApi() {
        given().when().get("/api/shop-customer/profile").then().statusCode(401);
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/shop-customer/profile").then().statusCode(401);
        given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo"))
                .when().get("/api/shop-customer/profile").then().statusCode(401);
        // The backend's own service account of the same realm is not a customer either (azp).
        given().auth().oauth2(adminToken()).when().get("/api/shop-customer/profile").then().statusCode(401);
    }

    @Test
    void unverifiedAccountCannotLogIn() {
        String email = "unbestaetigt-" + UUID.randomUUID() + "@example.test";
        createCustomer(email, false);
        shopLogin(email, "geheim-12345").then().statusCode(400)
                .body("error", is("invalid_grant"))
                .body("error_description", containsString("not fully set up"));
    }

    // ---- profile ------------------------------------------------------------------------

    @Test
    void profileStartsWithLoginEmailAndValidatesInput() {
        String email = "profil-" + UUID.randomUUID() + "@example.test";
        createCustomer(email, true);
        String token = shopToken(email, "geheim-12345");
        customer(token).when().get("/api/shop-customer/profile").then().statusCode(200)
                .body("loginEmail", is(email)).body("email", is(email)).body("lastName", nullValue());

        Map<String, Object> profile = new HashMap<>();
        profile.put("lastName", "  Muster ");
        profile.put("firstName", "Erika");
        profile.put("street", "Landstraße");
        profile.put("houseNumber", "5");
        profile.put("postalCode", "4020");
        profile.put("city", "Linz");
        profile.put("country", "Österreich");
        profile.put("email", "kontakt@example.test");
        profile.put("phone", "+43 732 123");
        customer(token).body(profile).when().put("/api/shop-customer/profile").then().statusCode(200)
                .body("lastName", is("Muster")).body("email", is("kontakt@example.test")).body("loginEmail", is(email));
        customer(token).when().get("/api/shop-customer/profile").then().statusCode(200)
                .body("city", is("Linz")).body("phone", is("+43 732 123"));

        profile.put("email", "kein-mail");
        customer(token).body(profile).when().put("/api/shop-customer/profile").then().statusCode(400)
                .body("message", is("Die E-Mail-Adresse ist ungültig."));
        profile.put("email", null);
        profile.put("lastName", "x".repeat(121));
        customer(token).body(profile).when().put("/api/shop-customer/profile").then().statusCode(400);
    }

    // ---- orders ---------------------------------------------------------------------------

    @Test
    void ordersAreLinkedToTheAccountAndOnlyVisibleToIt() {
        String kennung = demoShop();
        String eins = shopToken("kundin-eins@example.test", "kundin-eins-pw");
        String zwei = shopToken("kunde-zwei@example.test", "kunde-zwei-pw");
        String lastName = "Konto-" + UUID.randomUUID();
        int number = placeOrder(eins, kennung, lastName);

        // Guest order of the same shop never appears in an account.
        String guestName = "Gast-" + UUID.randomUUID();
        int guest = given().header("X-Forwarded-For", "198.31.0." + (CLIENT.getAndIncrement() % 250 + 1))
                .contentType(ContentType.JSON).body(checkout(guestName))
                .when().post("/api/shop/" + kennung + "/orders").then().statusCode(201).extract().path("orderNumber");

        customer(eins).when().get("/api/shop-customer/shops/" + kennung + "/orders").then().statusCode(200)
                .body("orderNumber", hasItem(number))
                .body("orderNumber", not(hasItem(guest)))
                .body("find { it.orderNumber == " + number + " }.status", is("SUBMITTED"))
                .body("find { it.orderNumber == " + number + " }.items[0].name", is("Akkuschrauber 18V Set"))
                .body("find { it.orderNumber == " + number + " }.delivery.lastName", is(lastName));
        customer(eins).when().get("/api/shop-customer/shops/" + kennung + "/orders/" + number).then().statusCode(200)
                .body("orderNumber", is(number));

        // Another customer: not in the list, 404 on direct access; guest order 404 as well.
        customer(zwei).when().get("/api/shop-customer/shops/" + kennung + "/orders").then().statusCode(200)
                .body("orderNumber", not(hasItem(number)));
        customer(zwei).when().get("/api/shop-customer/shops/" + kennung + "/orders/" + number).then().statusCode(404);
        customer(eins).when().get("/api/shop-customer/shops/" + kennung + "/orders/" + guest).then().statusCode(404);

        // The partner sees it like any shop order; the Kunde carries the account link.
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo")).when().get("/api/partner/orders")
                .then().statusCode(200).body("find { it.id == " + number + " }.origin", is("SHOP"));
        QuarkusTransaction.requiringNew().run(() -> {
            Order order = Order.findById((long) number);
            assertEquals("5a0b1c2d-0000-4000-8000-000000000001", order.kunde.accountSubject);
            assertNull(((Order) Order.findById((long) guest)).kunde.accountSubject);
        });
    }

    @Test
    void unknownShopIsNotFound() {
        String eins = shopToken("kundin-eins@example.test", "kundin-eins-pw");
        customer(eins).when().get("/api/shop-customer/shops/gibt-es-nicht/orders").then().statusCode(404);
    }

    // ---- export and deletion ------------------------------------------------------------------

    @Test
    void exportAndDeletionKeepBusinessRecords() {
        String kennung = demoShop();
        String email = "loeschen-" + UUID.randomUUID() + "@example.test";
        String subject = createCustomer(email, true);
        String token = shopToken(email, "geheim-12345");
        customer(token).body(Map.of("lastName", "Löschbar", "city", "Wels"))
                .when().put("/api/shop-customer/profile").then().statusCode(200);
        String lastName = "Loesch-" + UUID.randomUUID();
        int number = placeOrder(token, kennung, lastName);

        customer(token).when().get("/api/shop-customer/export").then().statusCode(200)
                .header("Content-Disposition", containsString("attachment"))
                .body("profile.loginEmail", is(email))
                .body("profile.lastName", is("Löschbar"))
                .body("orders.orderNumber", hasItem(number));

        customer(token).when().delete("/api/shop-customer/account").then().statusCode(204);

        // Login gone, profile gone, order and Kunde stay with the order-time data.
        shopLogin(email, "geheim-12345").then().statusCode(anyOf(is(400), is(401))).body("error", is("invalid_grant"));
        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals(0, ShopCustomerAccount.count("subject", subject));
            Order order = Order.findById((long) number);
            assertEquals(lastName, order.kunde.lastName);
            assertNull(order.kunde.accountSubject);
            assertEquals(0, Kunde.count("accountSubject", subject));
        });
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo")).when().get("/api/partner/orders")
                .then().statusCode(200).body("id", hasItem(number));
        given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo")).when().get("/api/company/orders")
                .then().statusCode(200).body("id", hasItem(number));
    }

    // ---- shop integration ---------------------------------------------------------------------

    @Test
    void shopAnnouncesAccountsAndReservesKontoSegment() {
        String kennung = demoShop();
        given().when().get("/api/shop/" + kennung).then().statusCode(200).body("accountsEnabled", is(true));
        String partner = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partner).contentType(ContentType.JSON)
                .body(Map.of("menuName", "Konto", "segment", "konto"))
                .when().post("/api/partner/shop/pages").then().statusCode(409);
        assertTrue(ShopPageService.RESERVED_SEGMENTS.contains("konto"));
    }
}
