package at.papo.partnerportal.shop;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Shop statistics in partner-demo's shop (shop-statistics spec, add-shop-statistics). Other tests
// share the database, so assertions compare counts before and after.
@QuarkusTest
class ShopStatisticsResourceTest {

    private static final AtomicInteger CLIENT = new AtomicInteger(1);
    private static final String BROWSER = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 "
            + "(KHTML, like Gecko) Version/18.0 Safari/605.1.15";
    private static final String PHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 "
            + "(KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1";
    private static final String CLIENT_IP = "203.0.113.77";

    @Inject
    KeycloakTestTokens tokens;

    private static RequestSpecification visitor(String userAgent) {
        return given().header("X-Forwarded-For", "198.30.0." + (CLIENT.getAndIncrement() % 250 + 1))
                .header("User-Agent", userAgent);
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

    private static void send(String kennung, String userAgent, String body) {
        visitor(userAgent).contentType("text/plain").body(body)
                .when().post("/api/shop/" + kennung + "/events").then().statusCode(204);
    }

    private JsonPath stats(String user) {
        return given().auth().oauth2(tokens.tokenFor(user, user)).when().get("/api/partner/shop/statistics")
                .then().statusCode(200).extract().jsonPath();
    }

    private static long count(JsonPath stats, String metric) {
        return stats.getLong("current." + metric);
    }

    private static long rowCount(JsonPath stats, String list, String label) {
        Integer value = stats.get(list + ".find { it.label == '" + label + "' }.count");
        return value == null ? 0 : value;
    }

    private static long articleId(String name) {
        return QuarkusTransaction.requiringNew().call(() -> Product.<Product>find("name", name).firstResult().id);
    }

    @Test
    void eventsAreCountedAsDailyAggregates() {
        String kennung = demoShop();
        long article = articleId("Akkuschrauber 18V Set");
        JsonPath before = stats("partner-demo");

        send(kennung, PHONE, """
                {"events":[
                  {"type":"pageview","page":"startseite","entry":true,"referrer":"https://www.example.com/search?q=lampe"},
                  {"type":"pageview","page":"artikeldetail"},
                  {"type":"article","articleId":%d},
                  {"type":"cart","articleId":%d}
                ]}""".formatted(article, article));

        JsonPath after = stats("partner-demo");
        assertEquals(count(before, "visits") + 1, count(after, "visits"));
        assertEquals(count(before, "pageViews") + 2, count(after, "pageViews"));
        assertEquals(count(before, "articleViews") + 1, count(after, "articleViews"));
        assertEquals(count(before, "addToCart") + 1, count(after, "addToCart"));
        assertEquals(rowCount(before, "referrers", "example.com") + 1, rowCount(after, "referrers", "example.com"));
        assertEquals(rowCount(before, "devices", "Mobil") + 1, rowCount(after, "devices", "Mobil"));
        Integer views = after.get("topArticles.find { it.articleId == " + article + " }.views");
        assertTrue(views != null && views >= 1);
        assertEquals("Akkuschrauber 18V Set", after.get("topArticles.find { it.articleId == " + article + " }.name"));
    }

    @Test
    void invalidEventsAreIgnoredWithoutDetails() {
        String kennung = demoShop();
        long foreign = articleId("Wasserwaage 100cm");
        JsonPath before = stats("partner-demo");

        send(kennung, BROWSER, """
                {"events":[
                  {"type":"article","articleId":%d},
                  {"type":"cart","articleId":999999999},
                  {"type":"pageview","page":"gibt-es-nicht","entry":true},
                  {"type":"pageview","page":"../admin"},
                  {"type":"klick","page":"startseite"}
                ]}""".formatted(foreign));
        send(kennung, BROWSER, "kein json");
        send(kennung, BROWSER, "{\"events\":[]}");
        StringBuilder tooMany = new StringBuilder("{\"events\":[");
        for (int i = 0; i < ShopStatisticsService.MAX_EVENTS + 1; i++) {
            tooMany.append(i == 0 ? "" : ",").append("{\"type\":\"pageview\",\"page\":\"startseite\",\"entry\":true}");
        }
        send(kennung, BROWSER, tooMany.append("]}").toString());
        // Bots and requests without a user agent are not counted.
        send(kennung, "Mozilla/5.0 (compatible; Googlebot/2.1)", "{\"events\":[{\"type\":\"pageview\",\"page\":\"startseite\",\"entry\":true}]}");
        given().header("X-Forwarded-For", "198.31.0.1").header("User-Agent", "").contentType("text/plain")
                .body("{\"events\":[{\"type\":\"pageview\",\"page\":\"startseite\",\"entry\":true}]}")
                .when().post("/api/shop/" + kennung + "/events").then().statusCode(204);

        JsonPath after = stats("partner-demo");
        for (String metric : List.of("visits", "pageViews", "articleViews", "addToCart")) {
            assertEquals(count(before, metric), count(after, metric), metric);
        }
        // Unknown shops stay one shared 404, like every public shop route.
        visitor(BROWSER).contentType("text/plain").body("{\"events\":[]}")
                .when().post("/api/shop/gibt-es-nicht-xyz/events").then().statusCode(404);
    }

    @Test
    void visitorsAreCountedOnlyWithConsentAndOncePerDay() {
        String kennung = demoShop();
        JsonPath before = stats("partner-demo");
        String visitor = UUID.randomUUID().toString();
        String page = "{\"type\":\"pageview\",\"page\":\"startseite\",\"entry\":true}";

        // Without consent: no identifier, so no unique-visitor count.
        send(kennung, BROWSER, "{\"events\":[" + page + "]}");
        JsonPath anonymous = stats("partner-demo");
        assertEquals(before.getString("current.uniqueVisitors"), anonymous.getString("current.uniqueVisitors"));

        // With consent: the same random id twice on one day counts once; returning once.
        send(kennung, BROWSER, "{\"visitor\":\"" + visitor + "\",\"returning\":true,\"events\":[" + page + "]}");
        send(kennung, BROWSER, "{\"visitor\":\"" + visitor + "\",\"returning\":true,\"events\":[" + page + "]}");
        // An id that is not a random UUID is ignored.
        send(kennung, BROWSER, "{\"visitor\":\"max.mustermann@example.at\",\"events\":[" + page + "]}");

        JsonPath after = stats("partner-demo");
        long uniqueBefore = before.get("current.uniqueVisitors") == null ? 0 : before.getLong("current.uniqueVisitors");
        long returningBefore = before.get("current.returningVisitors") == null ? 0 : before.getLong("current.returningVisitors");
        assertEquals(uniqueBefore + 1, after.getLong("current.uniqueVisitors"));
        assertEquals(returningBefore + 1, after.getLong("current.returningVisitors"));
        assertEquals(count(before, "visits") + 4, count(after, "visits"));
    }

    @Test
    void noIpUserAgentOrVisitorIdIsStored() {
        String kennung = demoShop();
        String visitor = UUID.randomUUID().toString();
        given().header("X-Forwarded-For", CLIENT_IP).header("User-Agent", PHONE).contentType("text/plain")
                .body("{\"visitor\":\"" + visitor + "\",\"events\":[{\"type\":\"pageview\",\"page\":\"startseite\","
                        + "\"entry\":true,\"referrer\":\"http://" + CLIENT_IP + "/seite?ip=" + CLIENT_IP + "\"}]}")
                .when().post("/api/shop/" + kennung + "/events").then().statusCode(204);

        QuarkusTransaction.requiringNew().run(() -> {
            @SuppressWarnings("unchecked")
            List<String> columns = ShopStatDaily.getEntityManager().createNativeQuery(
                            "select column_name from information_schema.columns where table_name = 'shop_stat_daily'")
                    .getResultList();
            assertEquals(List.of("amount", "dimension", "id", "metric", "shop_id", "stat_day"),
                    columns.stream().map(String::valueOf).sorted().toList());
            @SuppressWarnings("unchecked")
            List<String> dimensions = ShopStatDaily.getEntityManager()
                    .createNativeQuery("select distinct dimension from shop_stat_daily").getResultList();
            for (String dimension : dimensions) {
                assertFalse(dimension.contains("203.0.113"), dimension);
                assertFalse(dimension.contains("iPhone"), dimension);
                assertFalse(dimension.contains(visitor), dimension);
                assertFalse(dimension.contains("http"), dimension);
            }
        });
    }

    @Test
    void shopOrdersAndRevenueComeFromOrders() {
        String kennung = demoShop();
        JsonPath before = stats("partner-demo");
        Map<String, Object> customer = new HashMap<>();
        customer.put("lastName", "Statistik-" + UUID.randomUUID());
        customer.put("street", "Hauptplatz");
        customer.put("houseNumber", "1");
        customer.put("postalCode", "4020");
        customer.put("city", "Linz");
        customer.put("country", "Österreich");
        customer.put("email", "statistik@example.at");
        Map<String, Object> body = new HashMap<>();
        body.put("idempotencyKey", UUID.randomUUID().toString());
        body.put("customer", customer);
        body.put("items", List.of(Map.of("articleId", articleId("Akkuschrauber 18V Set"), "quantity", 1)));
        BigDecimal value = new BigDecimal(String.valueOf(visitor(BROWSER).contentType(ContentType.JSON).body(body)
                .when().post("/api/shop/" + kennung + "/orders")
                .then().statusCode(201).extract().jsonPath().getString("grossTotal")));

        JsonPath after = stats("partner-demo");
        assertEquals(count(before, "orders") + 1, count(after, "orders"));
        assertEquals(0, new BigDecimal(before.getString("current.revenue")).add(value)
                .compareTo(new BigDecimal(after.getString("current.revenue"))));
    }

    @Test
    void defaultPeriodIsTheCurrentMonthWithPreviousPeriod() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Vienna"));
        LocalDate first = today.withDayOfMonth(1);
        JsonPath stats = stats("partner-demo");
        assertEquals(first.toString(), stats.getString("from"));
        assertEquals(today.toString(), stats.getString("to"));
        assertEquals(first.minusDays(1).toString(), stats.getString("previousTo"));
        assertEquals(first.minusDays(1).minusDays(today.getDayOfMonth() - 1).toString(), stats.getString("previousFrom"));
        assertEquals(today.getDayOfMonth(), stats.getList("days").size());
    }

    @Test
    void periodIsValidated() {
        String partner = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partner).queryParam("from", "2026-10-01").queryParam("to", "2026-09-01")
                .when().get("/api/partner/shop/statistics").then().statusCode(400);
        given().auth().oauth2(partner).queryParam("from", "2024-01-01").queryParam("to", "2026-01-01")
                .when().get("/api/partner/shop/statistics").then().statusCode(400);
        given().auth().oauth2(partner).queryParam("from", "gestern")
                .when().get("/api/partner/shop/statistics").then().statusCode(400);
        given().auth().oauth2(partner).queryParam("from", "2026-01-01").queryParam("to", "2026-01-07")
                .when().get("/api/partner/shop/statistics").then().statusCode(200)
                .body("previousFrom", is("2025-12-25")).body("previousTo", is("2025-12-31"));
    }

    @Test
    void partnerSeesOnlyOwnShop() {
        String kennung = demoShop();
        JsonPath otherBefore = stats("partner-zwei");
        send(kennung, BROWSER, "{\"events\":[{\"type\":\"pageview\",\"page\":\"startseite\",\"entry\":true}]}");
        JsonPath otherAfter = stats("partner-zwei");
        assertEquals(count(otherBefore, "visits"), count(otherAfter, "visits"));
        assertNull(otherAfter.get("topArticles.find { it.name == 'Akkuschrauber 18V Set' }"));

        given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo"))
                .when().get("/api/partner/shop/statistics").then().statusCode(403);
        given().when().get("/api/partner/shop/statistics").then().statusCode(401);
    }
}
