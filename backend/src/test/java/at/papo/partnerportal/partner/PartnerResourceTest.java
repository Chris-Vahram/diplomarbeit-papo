package at.papo.partnerportal.partner;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class PartnerResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyListsSeededPartners() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .when().get("/api/company/partners")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(3));
    }

    @Test
    void companyCreateRejectsDuplicatePartnerNumber() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-1001\",\"name\":\"Duplikat\",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    @Test
    void partnerCannotAccessCompanyPartnerManagement() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/company/partners")
                .then().statusCode(403);
    }

    @Test
    void partnerSeesAndEditsOwnContactDataOnly() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/partner/profile")
                .then().statusCode(200)
                .body("partnerNumber", is("P-1001"))
                .body("commissionPercent", is(10.0f));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Elektro Huber GmbH\",\"address\":\"Neue Adresse 1, 4020 Linz\","
                        + "\"email\":\"partner-demo@example.at\",\"phone\":\"+43 660 1234567\"}")
                .when().put("/api/partner/profile")
                .then().statusCode(200)
                .body("address", is("Neue Adresse 1, 4020 Linz"))
                .body("commissionPercent", is(10.0f));
    }
}
