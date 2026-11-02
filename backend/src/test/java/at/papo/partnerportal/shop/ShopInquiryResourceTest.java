package at.papo.partnerportal.shop;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Public submission and the partner inbox (shop-inquiries spec) with partner-demo's shop.
@QuarkusTest
class ShopInquiryResourceTest {

    // Own client address per request, so neither rate limit interferes unless a test wants it.
    private static final AtomicInteger CLIENT = new AtomicInteger(1);

    @Inject
    KeycloakTestTokens tokens;

    private static RequestSpecification visitor() {
        return given().header("X-Forwarded-For", "198.19.0." + (CLIENT.getAndIncrement() % 250 + 1))
                .contentType(ContentType.JSON);
    }

    private String partnerToken() {
        return tokens.tokenFor("partner-demo", "partner-demo");
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

    private static String formToken(String kennung) {
        return visitor().when().get("/api/shop/" + kennung + "/inquiries/token")
                .then().statusCode(200).extract().path("token");
    }

    private static Map<String, Object> inquiry(String token, String marker) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Erika Muster");
        body.put("email", "erika@example.at");
        body.put("message", "Bitte um ein Angebot. " + marker);
        body.put("consent", true);
        body.put("token", token);
        return body;
    }

    private static long count(String marker) {
        return QuarkusTransaction.requiringNew().call(
                () -> ShopInquiry.count("message like ?1", "%" + marker));
    }

    @Test
    void submissionRulesAndInboxLifecycle() throws InterruptedException {
        String kennung = demoShop();
        String token = formToken(kennung);
        String marker = UUID.randomUUID().toString();
        String partner = partnerToken();

        // Sent right after rendering: discarded with the generic retry message.
        visitor().body(inquiry(token, marker)).when().post("/api/shop/" + kennung + "/inquiries")
                .then().statusCode(400).body("message", is(ShopInquiryService.RETRY));
        assertEquals(0, count(marker));

        Thread.sleep(ShopInquiryToken.MIN_FILL_MILLIS + 200);

        Map<String, Object> honeypot = inquiry(token, marker);
        honeypot.put("website", "https://spam.example");
        visitor().body(honeypot).when().post("/api/shop/" + kennung + "/inquiries")
                .then().statusCode(400).body("message", is(ShopInquiryService.RETRY));

        Map<String, Object> noContact = inquiry(token, marker);
        noContact.remove("email");
        noContact.put("phone", "  ");
        visitor().body(noContact).when().post("/api/shop/" + kennung + "/inquiries")
                .then().statusCode(400).body("message", is("Bitte E-Mail oder Telefonnummer angeben."));

        Map<String, Object> noConsent = inquiry(token, marker);
        noConsent.put("consent", false);
        visitor().body(noConsent).when().post("/api/shop/" + kennung + "/inquiries").then().statusCode(400);

        Map<String, Object> tooLong = inquiry(token, marker);
        tooLong.put("message", "x".repeat(2001));
        visitor().body(tooLong).when().post("/api/shop/" + kennung + "/inquiries").then().statusCode(400);

        Map<String, Object> foreignArticle = inquiry(token, marker);
        foreignArticle.put("articleId", productId("Wasserwaage 100cm"));
        visitor().body(foreignArticle).when().post("/api/shop/" + kennung + "/inquiries").then().statusCode(400);
        assertEquals(0, count(marker));

        int unreadBefore = unread(partner);
        Map<String, Object> valid = inquiry(token, marker);
        valid.put("phone", "+43 732 123456");
        valid.put("articleId", productId("Akkuschrauber 18V Set"));
        visitor().body(valid).when().post("/api/shop/" + kennung + "/inquiries").then().statusCode(201);
        assertEquals(1, count(marker));
        assertEquals(unreadBefore + 1, unread(partner));

        long id = QuarkusTransaction.requiringNew().call(
                () -> ShopInquiry.<ShopInquiry>find("message like ?1", "%" + marker).firstResult().id);
        given().auth().oauth2(partner).when().get("/api/partner/shop/inquiries")
                .then().statusCode(200)
                .body("find { it.id == " + id + " }.name", is("Erika Muster"))
                .body("find { it.id == " + id + " }.phone", is("+43 732 123456"))
                .body("find { it.id == " + id + " }.articleName", is("Akkuschrauber 18V Set"))
                .body("find { it.id == " + id + " }.readAt", nullValue());

        given().auth().oauth2(partner).when().post("/api/partner/shop/inquiries/" + id + "/read")
                .then().statusCode(200).body("readAt", notNullValue());
        assertEquals(unreadBefore, unread(partner));
        given().auth().oauth2(partner).contentType(ContentType.JSON).body(Map.of("done", true))
                .when().put("/api/partner/shop/inquiries/" + id + "/done")
                .then().statusCode(200).body("doneAt", notNullValue());
        given().auth().oauth2(partner).contentType(ContentType.JSON).body(Map.of("done", false))
                .when().put("/api/partner/shop/inquiries/" + id + "/done")
                .then().statusCode(200).body("doneAt", nullValue()).body("readAt", notNullValue());

        given().auth().oauth2(partner).when().delete("/api/partner/shop/inquiries/" + id).then().statusCode(204);
        assertEquals(0, count(marker));
        given().auth().oauth2(partner).when().post("/api/partner/shop/inquiries/" + id + "/read").then().statusCode(404);
    }

    @Test
    void onlyTheOwningPartnerSeesInquiries() throws InterruptedException {
        String kennung = demoShop();
        String token = formToken(kennung);
        String marker = UUID.randomUUID().toString();
        Thread.sleep(ShopInquiryToken.MIN_FILL_MILLIS + 200);
        visitor().body(inquiry(token, marker)).when().post("/api/shop/" + kennung + "/inquiries").then().statusCode(201);
        long id = QuarkusTransaction.requiringNew().call(
                () -> ShopInquiry.<ShopInquiry>find("message like ?1", "%" + marker).firstResult().id);

        String other = tokens.tokenFor("partner-zwei", "partner-zwei");
        given().auth().oauth2(other).when().get("/api/partner/shop/inquiries")
                .then().statusCode(200).body("id", not(hasItem((int) id)));
        given().auth().oauth2(other).when().post("/api/partner/shop/inquiries/" + id + "/read").then().statusCode(404);
        given().auth().oauth2(other).contentType(ContentType.JSON).body(Map.of("done", true))
                .when().put("/api/partner/shop/inquiries/" + id + "/done").then().statusCode(404);
        given().auth().oauth2(other).when().delete("/api/partner/shop/inquiries/" + id).then().statusCode(404);

        String company = tokens.tokenFor("company-demo", "company-demo");
        for (String path : List.of("/api/partner/shop/inquiries", "/api/partner/shop/inquiries/unread-count")) {
            given().when().get(path).then().statusCode(401);
            given().auth().oauth2(company).when().get(path).then().statusCode(403);
        }
        given().auth().oauth2(company).when().delete("/api/partner/shop/inquiries/" + id).then().statusCode(403);
        assertEquals(1, count(marker));
        given().auth().oauth2(partnerToken()).when().delete("/api/partner/shop/inquiries/" + id).then().statusCode(204);
    }

    @Test
    void sendingIsRateLimitedPerClient() {
        String kennung = demoShop();
        RequestSpecification sameClient = given().header("X-Forwarded-For", "198.19.1.1").contentType(ContentType.JSON);
        for (int i = 0; i < 5; i++) {
            sameClient.body(Map.of("name", "Bot")).when().post("/api/shop/" + kennung + "/inquiries")
                    .then().statusCode(400);
        }
        sameClient.body(Map.of("name", "Bot")).when().post("/api/shop/" + kennung + "/inquiries")
                .then().statusCode(429);
    }

    @Test
    void unknownShopGetsNeutral404() {
        visitor().when().get("/api/shop/gibt-es-nicht/inquiries/token").then().statusCode(404);
        visitor().body(inquiry("x", "y")).when().post("/api/shop/gibt-es-nicht/inquiries").then().statusCode(404);
    }

    private int unread(String token) {
        return given().auth().oauth2(token).when().get("/api/partner/shop/inquiries/unread-count")
                .then().statusCode(200).extract().as(Integer.class);
    }

    private static long productId(String name) {
        return QuarkusTransaction.requiringNew().call(() -> Product.<Product>find("name", name).firstResult().id);
    }
}
