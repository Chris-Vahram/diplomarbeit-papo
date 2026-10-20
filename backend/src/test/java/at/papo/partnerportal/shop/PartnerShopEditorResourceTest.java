package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.DemoPngGenerator;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// HTTP level of the editor with the demo partner login; the logic is covered by
// ShopEditorServiceTest and ShopAssetServiceTest.
@QuarkusTest
class PartnerShopEditorResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    PartnerShopService shopService;

    private String partnerToken() {
        return tokens.tokenFor("partner-demo", "partner-demo");
    }

    @Test
    void editorEndpointsArePartnerOnly() {
        for (String path : List.of("/api/partner/shop/pages", "/api/partner/shop/theme",
                "/api/partner/shop/assets", "/api/partner/shop/preview")) {
            given().when().get(path).then().statusCode(401);
            given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo")).when().get(path).then().statusCode(403);
        }
    }

    @Test
    void secondTabGetsConflictAndForeignPagesAreHidden() {
        String token = partnerToken();
        Map<String, Object> created = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("menuName", "REST Test", "segment", "rest-test-" + UUID.randomUUID().toString().substring(0, 8)))
                .when().post("/api/partner/shop/pages")
                .then().statusCode(200).extract().path("page");
        long pageId = ((Number) created.get("id")).longValue();
        long version = ((Number) created.get("version")).longValue();
        String draft = "{\"content\":{\"blocks\":[{\"id\":\"" + UUID.randomUUID()
                + "\",\"type\":\"heading-text\",\"config\":{\"heading\":\"%s\"}}]},\"version\":" + version + "}";

        given().auth().oauth2(token).contentType(ContentType.JSON).body(draft.formatted("Tab 1"))
                .when().put("/api/partner/shop/pages/" + pageId + "/draft")
                .then().statusCode(200).body("draftChanged", is(true));
        given().auth().oauth2(token).contentType(ContentType.JSON).body(draft.formatted("Tab 2"))
                .when().put("/api/partner/shop/pages/" + pageId + "/draft")
                .then().statusCode(409).body("message", is("Diese Seite wurde inzwischen geändert. Bitte neu laden."));
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .when().post("/api/partner/shop/pages/" + pageId + "/publish")
                .then().statusCode(400);

        // A page of another partner's shop is answered like an unknown one.
        long foreignPartner = QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.partnerNumber = "REST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Fremd " + partner.partnerNumber;
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            return partner.id;
        });
        long foreignPage = QuarkusTransaction.requiringNew().call(() -> ShopPage.<ShopPage>find("shop = ?1 and slot = ?2",
                shopService.findOrCreate(Partner.findById(foreignPartner)), ShopPageSlot.HOME).firstResult().id);
        given().auth().oauth2(token).when().get("/api/partner/shop/pages/" + foreignPage).then().statusCode(404);

        given().auth().oauth2(token).when().delete("/api/partner/shop/pages/" + pageId).then().statusCode(204);
    }

    @Test
    void uploadNeedsRightsConfirmationAndListsQuota() {
        String token = partnerToken();
        byte[] png = DemoPngGenerator.solidColorPng(3, 3, 200, 30, 30);
        given().auth().oauth2(token).multiPart("file", "rot.png", png, "image/png")
                .when().post("/api/partner/shop/assets")
                .then().statusCode(400);
        int assetId = given().auth().oauth2(token)
                .multiPart("file", "rot.png", png, "image/png").multiPart("rightsConfirmed", "true")
                .when().post("/api/partner/shop/assets")
                .then().statusCode(200).body("contentType", is("image/png")).extract().path("id");

        given().auth().oauth2(token).when().get("/api/partner/shop/assets")
                .then().statusCode(200)
                .body("quotaBytes", is(50 * 1024 * 1024))
                .body("assets.id", hasItem(assetId));
        given().auth().oauth2(token).when().get("/api/partner/shop/assets/" + assetId)
                .then().statusCode(200).contentType("image/png");
        given().auth().oauth2(token).when().get("/api/partner/shop/preview")
                .then().statusCode(200).body("shop.kennung", notNullValue()).body("navigation.items", notNullValue());
        given().auth().oauth2(token).when().delete("/api/partner/shop/assets/" + assetId).then().statusCode(204);
    }
}
