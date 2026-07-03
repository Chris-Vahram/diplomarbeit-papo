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
    void companyCreateRejectsMissingRequiredFields() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Ohne Nummer\",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(400);

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9999\",\"name\":\"Ohne Provision\"}")
                .when().post("/api/company/partners")
                .then().statusCode(400);
    }

    // Access-control edge case (auth-access-control spec, "Inactive partner
    // blocking"): deactivating via the same PUT the company UI uses must lock
    // the partner out of its own endpoints immediately - CurrentPartner.resolve()
    // is the single enforcement point for this, shared by every partner-scoped
    // resource.
    @Test
    void inactivePartnerIsBlockedFromPartnerEndpoints() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        String originalState = given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .extract().asString();

        setActive(companyToken, partnerId, originalState, false);

        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/profile")
                .then().statusCode(403);

        // Restore so later tests relying on partner-demo being active keep working.
        setActive(companyToken, partnerId, originalState, true);

        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/partner/profile")
                .then().statusCode(200);
    }

    private long partner1001Id() {
        return Partner.<Partner>find("partnerNumber", "P-1001").firstResult().id;
    }

    private void setActive(String companyToken, long partnerId, String currentStateJson, boolean active) {
        io.restassured.path.json.JsonPath current = io.restassured.path.json.JsonPath.from(currentStateJson);
        String body = String.format(
                "{\"partnerNumber\":\"%s\",\"name\":\"%s\",\"address\":%s,\"email\":%s,\"phone\":%s,"
                        + "\"commissionPercent\":%s,\"active\":%s}",
                current.getString("partnerNumber"), current.getString("name"),
                quoteOrNull(current.getString("address")), quoteOrNull(current.getString("email")),
                quoteOrNull(current.getString("phone")), current.getString("commissionPercent"), active);

        given().auth().oauth2(companyToken).contentType("application/json")
                .body(body)
                .when().put("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .body("active", is(active));
    }

    private String quoteOrNull(String value) {
        return value == null ? "null" : "\"" + value + "\"";
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
