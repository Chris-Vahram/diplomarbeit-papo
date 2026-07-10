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

    private static final String SAMPLE_ADDRESS_JSON = "{\"street\":\"Teststraße\",\"houseNumber\":\"1\","
            + "\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}";

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
                .body("{\"partnerNumber\":\"P-1001\",\"name\":\"Duplikat\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    @Test
    void companyCreateRejectsDuplicatePartnerNumberDifferingOnlyByCase() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"p-1001\",\"name\":\"Duplikat Kleinschreibung\",\"address\":"
                        + SAMPLE_ADDRESS_JSON + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    @Test
    void companyCreateRejectsCommissionAboveOneHundred() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9101\",\"name\":\"Zu hohe Provision\",\"address\":"
                        + SAMPLE_ADDRESS_JSON + ",\"commissionPercent\":150}")
                .when().post("/api/company/partners")
                .then().statusCode(400);
    }

    @Test
    void companyCanLinkAndUpdateKeycloakUsername() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        String created = given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9102\",\"name\":\"Login-Test\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":10,\"keycloakUsername\":\"future-login\"}")
                .when().post("/api/company/partners")
                .then().statusCode(200)
                .body("keycloakUsername", is("future-login"))
                .extract().asString();
        long id = io.restassured.path.json.JsonPath.from(created).getLong("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9102\",\"name\":\"Login-Test\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":10,\"keycloakUsername\":\"future-login-renamed\"}")
                .when().put("/api/company/partners/" + id)
                .then().statusCode(200)
                .body("keycloakUsername", is("future-login-renamed"));
    }

    @Test
    void companyCreateRejectsDuplicateKeycloakUsername() {
        String token = tokens.tokenFor("company-demo", "company-demo");
        // "partner-demo" is already linked to the seeded P-1001.

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9103\",\"name\":\"Konflikt\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":10,\"keycloakUsername\":\"partner-demo\"}")
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
        String addressJson = String.format(
                "{\"street\":\"%s\",\"houseNumber\":\"%s\",\"postalCode\":\"%s\",\"city\":\"%s\",\"country\":\"%s\"}",
                current.getString("address.street"), current.getString("address.houseNumber"),
                current.getString("address.postalCode"), current.getString("address.city"),
                current.getString("address.country"));
        // PUT is a full replace - every field the DTO carries (incl. keycloakUsername) must be
        // round-tripped here, or this restore-after-toggle helper silently wipes it.
        String body = String.format(
                "{\"partnerNumber\":\"%s\",\"name\":\"%s\",\"address\":%s,\"email\":%s,\"phone\":%s,"
                        + "\"commissionPercent\":%s,\"active\":%s,\"keycloakUsername\":%s}",
                current.getString("partnerNumber"), current.getString("name"),
                addressJson, quoteOrNull(current.getString("email")),
                quoteOrNull(current.getString("phone")), current.getString("commissionPercent"), active,
                quoteOrNull(current.getString("keycloakUsername")));

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
                .body("{\"name\":\"Elektro Huber GmbH\",\"address\":{\"street\":\"Neue Adresse\","
                        + "\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"},"
                        + "\"email\":\"partner-demo@example.at\",\"phone\":\"+43 660 1234567\"}")
                .when().put("/api/partner/profile")
                .then().statusCode(200)
                .body("address.street", is("Neue Adresse"))
                .body("commissionPercent", is(10.0f));
    }
}
