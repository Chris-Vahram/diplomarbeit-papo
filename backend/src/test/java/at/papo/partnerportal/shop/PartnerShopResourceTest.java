package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class PartnerShopResourceTest {

    private static final String AVAILABLE_PRODUCT = "Kabeltrommel 25m";
    private static final String UNAVAILABLE_PRODUCT = "Wasserwaage 100cm";

    @Inject
    KeycloakTestTokens tokens;

    private String companyToken() {
        return tokens.tokenFor("company-demo", "company-demo");
    }

    private long createPartner(String name, String... enabledProducts) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.partnerNumber = "SHOP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = name;
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            for (String productName : enabledProducts) {
                ProductEnablement enablement = new ProductEnablement();
                enablement.partner = partner;
                enablement.product = Product.find("name", productName).firstResult();
                enablement.persist();
            }
            return partner.id;
        });
    }

    private void setActive(long partnerId, boolean active) {
        QuarkusTransaction.requiringNew().run(() -> Partner.<Partner>findById(partnerId).active = active);
    }

    private String shopPath(long partnerId) {
        return "/api/company/partners/" + partnerId + "/shop";
    }

    private void release(long partnerId) {
        given().auth().oauth2(companyToken())
                .when().post(shopPath(partnerId) + "/release")
                .then().statusCode(200);
    }

    @Test
    void companyReleasesShopAndPublicSeesMinimalData() {
        long partnerId = createPartner("Huemer Elektro GmbH", AVAILABLE_PRODUCT);

        given().auth().oauth2(companyToken())
                .when().get(shopPath(partnerId))
                .then().statusCode(200)
                .body("kennung", is("huemer-elektro-gmbh"))
                .body("publicPath", is("/shop/huemer-elektro-gmbh"))
                .body("status", is("NOT_RELEASED"));

        given().when().get("/api/shop/huemer-elektro-gmbh")
                .then().statusCode(404)
                .header("X-Robots-Tag", "noindex")
                .body("message", is("Dieser Shop ist derzeit nicht erreichbar."));

        release(partnerId);

        given().when().get("/api/shop/huemer-elektro-gmbh")
                .then().statusCode(200)
                .header("X-Robots-Tag", "noindex")
                .body("kennung", is("huemer-elektro-gmbh"))
                .body("displayName", is("Huemer Elektro GmbH"))
                .body("reachable", is(true))
                .body("size()", is(3));
    }

    @Test
    void collidingNameGetsNumericSuffix() {
        long first = createPartner("Kollision Shop KG");
        long second = createPartner("Kollision Shop KG");

        given().auth().oauth2(companyToken()).when().get(shopPath(first))
                .then().statusCode(200).body("kennung", is("kollision-shop-kg"));
        given().auth().oauth2(companyToken()).when().get(shopPath(second))
                .then().statusCode(200).body("kennung", is("kollision-shop-kg-2"));
        // Repeated access keeps the one record per partner.
        given().auth().oauth2(companyToken()).when().get(shopPath(second))
                .then().statusCode(200).body("kennung", is("kollision-shop-kg-2"));
    }

    @Test
    void takedownRequiresReasonAndReleaseClearsIt() {
        long partnerId = createPartner("Takedown Test OG", AVAILABLE_PRODUCT);

        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("reason", "Noch nicht freigegeben."))
                .when().post(shopPath(partnerId) + "/takedown")
                .then().statusCode(409);

        release(partnerId);

        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of())
                .when().post(shopPath(partnerId) + "/takedown")
                .then().statusCode(400);
        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("reason", "  zu kurz  "))
                .when().post(shopPath(partnerId) + "/takedown")
                .then().statusCode(400);
        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("reason", "x".repeat(501)))
                .when().post(shopPath(partnerId) + "/takedown")
                .then().statusCode(400);

        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("reason", "Preise auf der Startseite veraltet."))
                .when().post(shopPath(partnerId) + "/takedown")
                .then().statusCode(200)
                .body("status", is("OFFLINE_COMPANY"))
                .body("offlineReason", is("Preise auf der Startseite veraltet."));

        given().when().get("/api/shop/takedown-test-og").then().statusCode(404);

        given().auth().oauth2(companyToken())
                .when().post(shopPath(partnerId) + "/release")
                .then().statusCode(200)
                .body("status", is("ONLINE"))
                .body("offlineReason", nullValue());
    }

    @Test
    void inactivePartnerTakesShopOffline() {
        long partnerId = createPartner("Inaktiv Shop GmbH", AVAILABLE_PRODUCT);
        release(partnerId);
        setActive(partnerId, false);

        given().auth().oauth2(companyToken()).when().get(shopPath(partnerId))
                .then().statusCode(200).body("status", is("OFFLINE_INACTIVE"));
        given().when().get("/api/shop/inaktiv-shop-gmbh").then().statusCode(404);

        setActive(partnerId, true);
        given().when().get("/api/shop/inaktiv-shop-gmbh").then().statusCode(200);
    }

    @Test
    void shopWithoutAvailableArticlesIsOffline() {
        long partnerId = createPartner("Ohne Artikel KG", UNAVAILABLE_PRODUCT);
        release(partnerId);

        given().auth().oauth2(companyToken()).when().get(shopPath(partnerId))
                .then().statusCode(200).body("status", is("OFFLINE_NO_ARTICLES"));
        given().when().get("/api/shop/ohne-artikel-kg").then().statusCode(404);
    }

    @Test
    void companyChangesKennungWithValidation() {
        long partnerId = createPartner("Umbenennen Test GmbH", AVAILABLE_PRODUCT);
        long otherId = createPartner("Belegt Test GmbH");
        given().auth().oauth2(companyToken()).when().get(shopPath(otherId)).then().statusCode(200);
        release(partnerId);

        for (String invalid : new String[] {"Huemer Shop!", "api", "ab"}) {
            given().auth().oauth2(companyToken())
                    .contentType(ContentType.JSON).body(Map.of("kennung", invalid))
                    .when().put(shopPath(partnerId) + "/kennung")
                    .then().statusCode(400).body("message", notNullValue());
        }

        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("kennung", "belegt-test-gmbh"))
                .when().put(shopPath(partnerId) + "/kennung")
                .then().statusCode(409);

        given().auth().oauth2(companyToken())
                .contentType(ContentType.JSON).body(Map.of("kennung", "neuer-name"))
                .when().put(shopPath(partnerId) + "/kennung")
                .then().statusCode(200).body("kennung", is("neuer-name"));

        given().when().get("/api/shop/umbenennen-test-gmbh").then().statusCode(404);
        given().when().get("/api/shop/neuer-name").then().statusCode(200);
    }

    @Test
    void partnerSeesOwnShopButCannotChangeIdentifier() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/shop")
                .then().statusCode(200)
                .body("kennung", notNullValue())
                .body("status", notNullValue());

        long ownId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
        given().auth().oauth2(partnerToken)
                .contentType(ContentType.JSON).body(Map.of("kennung", "partner-eigene"))
                .when().put(shopPath(ownId) + "/kennung")
                .then().statusCode(403);
        given().auth().oauth2(partnerToken)
                .when().post(shopPath(ownId) + "/release")
                .then().statusCode(403);
    }

    @Test
    void companyOverviewListsStatusPerPartner() {
        long online = createPartner("Uebersicht Online KG", AVAILABLE_PRODUCT);
        long noArticles = createPartner("Uebersicht Leer KG", UNAVAILABLE_PRODUCT);
        release(online);
        release(noArticles);

        given().auth().oauth2(companyToken())
                .when().get("/api/company/shops")
                .then().statusCode(200)
                .body("find { it.partnerId == " + online + " }.status", is("ONLINE"))
                .body("find { it.partnerId == " + online + " }.kennung", is("uebersicht-online-kg"))
                .body("find { it.partnerId == " + noArticles + " }.status", is("OFFLINE_NO_ARTICLES"));

        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/company/shops")
                .then().statusCode(403);
    }

    @Test
    void internalApisStayProtected() {
        given().when().get("/api/company/shops").then().statusCode(401);
        given().when().get("/api/company/partners").then().statusCode(401);
        given().when().get("/api/company/partners/1/shop").then().statusCode(401);
        given().when().get("/api/partner/shop").then().statusCode(401);
        given().when().get("/api/partner/products").then().statusCode(401);
    }

    @Test
    void unknownAndInvalidIdentifiersShareTheNeutral404() {
        given().when().get("/api/shop/gibt-es-nicht")
                .then().statusCode(404)
                .body("message", is("Dieser Shop ist derzeit nicht erreichbar."));
        given().when().get("/api/shop/UNGUELTIG!")
                .then().statusCode(404)
                .body("message", is("Dieser Shop ist derzeit nicht erreichbar."));
    }

    @Test
    void rateLimitReturns429PerClientIp() {
        String ip = "203.0.113.77";
        for (int i = 0; i < 60; i++) {
            given().header("X-Forwarded-For", ip)
                    .when().get("/api/shop/gibt-es-nicht")
                    .then().statusCode(404);
        }
        given().header("X-Forwarded-For", ip)
                .when().get("/api/shop/gibt-es-nicht")
                .then().statusCode(429)
                .header("X-Robots-Tag", "noindex")
                .body("message", is("Zu viele Anfragen. Bitte kurz warten."));

        // Another client is unaffected; a spoofed left-most entry does not dodge the limit.
        given().header("X-Forwarded-For", "203.0.113.78")
                .when().get("/api/shop/gibt-es-nicht").then().statusCode(404);
        given().header("X-Forwarded-For", "198.51.100.1, " + ip)
                .when().get("/api/shop/gibt-es-nicht").then().statusCode(429);
    }
}
