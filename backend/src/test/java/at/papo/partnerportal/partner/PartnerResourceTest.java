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
                .body("{\"partnerNumber\":\"PARTNER-DEMO\",\"name\":\"Duplikat\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    @Test
    void companyCreateRejectsDuplicatePartnerNumberDifferingOnlyByCase() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"partner-demo\",\"name\":\"Duplikat Kleinschreibung\",\"address\":"
                        + SAMPLE_ADDRESS_JSON + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    // Guards the case-variant-duplicate hole (design.md Decision 2): storing the canonical
    // upper-case form means the login match resolves to at most one partner.
    @Test
    void companyCreatedPartnerNumberIsCanonicalizedToUpperCase() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"p-9104\",\"name\":\"Kleinschreibung\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(200)
                .body("partnerNumber", is("P-9104"));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9104\",\"name\":\"Zweiter Versuch\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409);
    }

    // The duplicate pre-check must compare canonical (trimmed+upper-cased) values, not the raw
    // request body - otherwise a whitespace-padded duplicate slips past the friendly 409 and
    // hits the DB unique constraint directly as an unhandled 500.
    @Test
    void companyCreateRejectsDuplicatePartnerNumberWithPaddedWhitespace() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"P-9105\",\"name\":\"Original\",\"address\":" + SAMPLE_ADDRESS_JSON
                        + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(200);

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"partnerNumber\":\"  P-9105  \",\"name\":\"Duplikat mit Leerzeichen\",\"address\":"
                        + SAMPLE_ADDRESS_JSON + ",\"commissionPercent\":5}")
                .when().post("/api/company/partners")
                .then().statusCode(409)
                .body("message", is("Partnernummer bereits vergeben."));
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
        return Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
    }

    private void setActive(String companyToken, long partnerId, String currentStateJson, boolean active) {
        io.restassured.path.json.JsonPath current = io.restassured.path.json.JsonPath.from(currentStateJson);
        String addressJson = String.format(
                "{\"street\":\"%s\",\"houseNumber\":\"%s\",\"postalCode\":\"%s\",\"city\":\"%s\",\"country\":\"%s\"}",
                current.getString("address.street"), current.getString("address.houseNumber"),
                current.getString("address.postalCode"), current.getString("address.city"),
                current.getString("address.country"));
        // PUT is a full replace - every field the DTO carries must be round-tripped here,
        // or this restore-after-toggle helper silently wipes it.
        String body = String.format(
                "{\"partnerNumber\":\"%s\",\"name\":\"%s\",\"address\":%s,\"email\":%s,\"phone\":%s,"
                        + "\"commissionPercent\":%s,\"active\":%s}",
                current.getString("partnerNumber"), current.getString("name"),
                addressJson, quoteOrNull(current.getString("email")),
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

    // Keycloak's username claim comes back lowercase ("partner-demo"); the seeded partner's
    // canonical partnerNumber is upper-case ("PARTNER-DEMO") - proves resolve() folds case
    // via toUpperCase, not just an exact-case match (partner-management spec scenario
    // "Lowercased Keycloak username still resolves").
    @Test
    void partnerLoginResolvesViaCaseFoldedPartnerNumber() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/api/partner/profile")
                .then().statusCode(200)
                .body("partnerNumber", is("PARTNER-DEMO"));
    }

    @Test
    void unlinkedPartnerLoginIsRejectedWithUnchangedGermanMessage() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = partner1001Id();
        String originalState = given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .extract().asString();

        // Rename the seeded partner's number away from PARTNER-DEMO so the partner-demo
        // login claim (upper-cased to PARTNER-DEMO) matches no partner record. try/finally so a
        // failed assertion below still restores the shared seeded fixture other tests rely on.
        try {
            renamePartnerNumber(companyToken, partnerId, originalState, "PARTNER-DEMO-RENAMED");

            String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
            given().auth().oauth2(partnerToken)
                    .when().get("/api/partner/profile")
                    .then().statusCode(403)
                    .body("message", is("Kein Partnerkonto mit diesem Login verknüpft."));
        } finally {
            renamePartnerNumber(companyToken, partnerId, originalState, "PARTNER-DEMO");
        }
    }

    private void renamePartnerNumber(String companyToken, long partnerId, String currentStateJson,
            String newPartnerNumber) {
        io.restassured.path.json.JsonPath current = io.restassured.path.json.JsonPath.from(currentStateJson);
        String addressJson = String.format(
                "{\"street\":\"%s\",\"houseNumber\":\"%s\",\"postalCode\":\"%s\",\"city\":\"%s\",\"country\":\"%s\"}",
                current.getString("address.street"), current.getString("address.houseNumber"),
                current.getString("address.postalCode"), current.getString("address.city"),
                current.getString("address.country"));
        String body = String.format(
                "{\"partnerNumber\":\"%s\",\"name\":\"%s\",\"address\":%s,\"email\":%s,\"phone\":%s,"
                        + "\"commissionPercent\":%s,\"active\":%s}",
                newPartnerNumber, current.getString("name"), addressJson,
                quoteOrNull(current.getString("email")), quoteOrNull(current.getString("phone")),
                current.getString("commissionPercent"), current.getBoolean("active"));

        given().auth().oauth2(companyToken).contentType("application/json")
                .body(body)
                .when().put("/api/company/partners/" + partnerId)
                .then().statusCode(200)
                .body("partnerNumber", is(newPartnerNumber));
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
                .body("partnerNumber", is("PARTNER-DEMO"))
                .body("commissionPercent", is(10.0f));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"name\":\"Huemer GmbH\",\"address\":{\"street\":\"Neue Adresse\","
                        + "\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"},"
                        + "\"email\":\"partner-demo@example.at\",\"phone\":\"+43 660 1234567\"}")
                .when().put("/api/partner/profile")
                .then().statusCode(200)
                .body("address.street", is("Neue Adresse"))
                .body("commissionPercent", is(10.0f));
    }
}
