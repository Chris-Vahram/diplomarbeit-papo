package at.papo.partnerportal.shop;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

// shop-legal spec "Placeholder marking and release gate", production half.
@QuarkusTest
@TestProfile(ShopLegalGateProductionTest.Production.class)
class ShopLegalGateProductionTest {

    public static class Production implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("papo.environment", "production");
        }
    }

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void releaseNeedsAllTextsAndShopGoesOfflineWhenOneIsRemoved() {
        String company = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = ShopLegalTextsTest.createShopPartner(null);
        String shopPath = "/api/company/partners/" + partnerId + "/shop";
        try {
            given().auth().oauth2(company).when().post(shopPath + "/release")
                    .then().statusCode(409)
                    .body("message", is("Bitte zuerst alle Rechtstexte im Firmenprofil hinterlegen."));
            given().auth().oauth2(company).when().get(shopPath)
                    .then().statusCode(200)
                    .body("status", is("NOT_RELEASED"))
                    .body("missingLegalTexts", is(4))
                    .body("legalGate", is(true));

            ShopLegalTextsTest.putTexts(company, ShopLegalTextsTest.allTexts());
            String kennung = given().auth().oauth2(company).when().post(shopPath + "/release")
                    .then().statusCode(200)
                    .body("status", is("ONLINE"))
                    .extract().path("kennung");
            given().when().get("/api/shop/" + kennung).then().statusCode(200);

            Map<String, Object> withoutAgb = new HashMap<>(ShopLegalTextsTest.allTexts());
            withoutAgb.remove("agb");
            ShopLegalTextsTest.putTexts(company, withoutAgb);
            given().auth().oauth2(company).when().get(shopPath)
                    .then().statusCode(200).body("status", is("OFFLINE_LEGAL"));
            given().when().get("/api/shop/" + kennung)
                    .then().statusCode(404)
                    .body("message", is("Dieser Shop ist derzeit nicht erreichbar."));
            given().auth().oauth2(company).when().get("/api/company/shops")
                    .then().statusCode(200)
                    .body("find { it.partnerId == " + partnerId + " }.status", is("OFFLINE_LEGAL"));
        } finally {
            ShopLegalTextsTest.putTexts(company, Map.of());
        }
    }
}
