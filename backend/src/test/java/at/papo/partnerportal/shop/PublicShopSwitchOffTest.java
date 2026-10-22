package at.papo.partnerportal.shop;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

// shop-public-access spec "Public shop kill switch": the deployment default (switch off).
@QuarkusTest
@TestProfile(PublicShopSwitchOffTest.SwitchOff.class)
class PublicShopSwitchOffTest {

    public static class SwitchOff implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("papo.shop.public-enabled", "false");
        }
    }

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void releasedOnlineShopStaysUnreachableWhileManagementWorks() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
        String shopPath = "/api/company/partners/" + partnerId + "/shop";

        String kennung = given().auth().oauth2(companyToken)
                .when().post(shopPath + "/release")
                .then().statusCode(200)
                .body("status", is("ONLINE"))
                .body("publicEnabled", is(false))
                .extract().path("kennung");

        given().when().get("/api/shop/" + kennung)
                .then().statusCode(404)
                .header("X-Robots-Tag", "noindex")
                .body("message", is("Dieser Shop ist derzeit nicht erreichbar."));

        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/partner/shop")
                .then().statusCode(200)
                .body("status", is("ONLINE"))
                .body("publicEnabled", is(false));
    }
}
