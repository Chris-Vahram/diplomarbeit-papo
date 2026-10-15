package at.papo.partnerportal.partner;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class PartnerLoginTest {

    private static final String UNLINKED = "Kein Partnerkonto mit diesem Login verknüpft.";

    @Inject
    KeycloakTestTokens tokens;

    @AfterEach
    void removeTestBindings() {
        // Keep the seeded partner-demo binding; drop everything the tests created.
        QuarkusTransaction.requiringNew().run(() -> PartnerLogin.delete("createdBy <> ?1", "demo-seed"));
    }

    @Test
    void seededSubjectResolvesPartnerDemo() {
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/partner/profile")
                .then().statusCode(200).body("partnerNumber", is("PARTNER-DEMO"))
                .body("$", not(hasKey("oidcSubject")));
    }

    @Test
    void fallbackWorksOnlyForPartnersThatNeverHadABinding() {
        String p1002 = tokens.tokenFor("p-1002", "p-1002");
        profile(p1002).then().statusCode(200).body("partnerNumber", is("P-1002"));

        // Another subject gets bound to P-1002: the username match no longer counts.
        bind(partnerId("P-1002"), "fremdes-subject-1002").then().statusCode(200);
        profile(p1002).then().statusCode(403).body("message", is(UNLINKED));
    }

    @Test
    void partnerWithOnlyRevokedBindingGetsNoFallback() {
        long partnerId = partnerId("P-1002");
        long loginId = bind(partnerId, "frueheres-subject-1002").then().statusCode(200).extract().jsonPath().getLong("id");
        given().auth().oauth2(company()).when().delete("/api/company/partners/" + partnerId + "/logins/" + loginId)
                .then().statusCode(204);
        profile(tokens.tokenFor("p-1002", "p-1002")).then().statusCode(403).body("message", is(UNLINKED));
    }

    @Test
    void concurrentReactivationsYieldOneSuccess() {
        long a = partnerId("P-1002");
        long loginId = bind(a, "reaktiviert-subject").then().statusCode(200).extract().jsonPath().getLong("id");
        given().auth().oauth2(company()).when().delete("/api/company/partners/" + a + "/logins/" + loginId).then().statusCode(204);
        List<CompletableFuture<Integer>> calls = List.of(
                CompletableFuture.supplyAsync(() -> bind(a, "reaktiviert-subject").statusCode()),
                CompletableFuture.supplyAsync(() -> bind(a, "reaktiviert-subject").statusCode()));
        assertEquals(List.of(200, 409), calls.stream().map(CompletableFuture::join).sorted().toList());
    }

    @Test
    void nullBodyIsRejected() {
        given().auth().oauth2(company()).contentType("application/json").body("null")
                .when().post("/api/company/partners/" + partnerId("PARTNER-DEMO") + "/logins").then().statusCode(400);
    }

    @Test
    void subjectWinsOverUsername() {
        String p1002 = tokens.tokenFor("p-1002", "p-1002");
        bind(partnerId("PARTNER-DEMO"), subjectOf(p1002)).then().statusCode(200);
        profile(p1002).then().statusCode(200).body("partnerNumber", is("PARTNER-DEMO"));
    }

    @Test
    void revokedLastBindingGetsNoFallback() {
        String p1002 = tokens.tokenFor("p-1002", "p-1002");
        long partnerId = partnerId("P-1002");
        long loginId = bind(partnerId, subjectOf(p1002)).then().statusCode(200).extract().jsonPath().getLong("id");
        profile(p1002).then().statusCode(200).body("partnerNumber", is("P-1002"));

        given().auth().oauth2(company()).when().delete("/api/company/partners/" + partnerId + "/logins/" + loginId)
                .then().statusCode(204);
        profile(p1002).then().statusCode(403).body("message", is(UNLINKED));

        // Re-adding the revoked subject to the same partner reactivates it.
        bind(partnerId, subjectOf(p1002)).then().statusCode(200).body("id", is((int) loginId)).body("revokedAt", is((Object) null));
        profile(p1002).then().statusCode(200);
    }

    @Test
    void renumberingKeepsTheLogin() {
        long id = partnerId("PARTNER-DEMO");
        QuarkusTransaction.requiringNew().run(() -> Partner.<Partner>findById(id).partnerNumber = "BMD-000123");
        try {
            profile(tokens.tokenFor("partner-demo", "partner-demo"))
                    .then().statusCode(200).body("partnerNumber", is("BMD-000123"));
        } finally {
            QuarkusTransaction.requiringNew().run(() -> Partner.<Partner>findById(id).partnerNumber = "PARTNER-DEMO");
        }
    }

    @Test
    void bindingValidationAndConflicts() {
        long demo = partnerId("PARTNER-DEMO");
        long other = partnerId("P-1003");
        bind(demo, "").then().statusCode(400);
        bind(demo, "mit leerzeichen").then().statusCode(400);
        bind(demo, "x".repeat(256)).then().statusCode(400);
        bind(demo, at.papo.partnerportal.seed.DemoDataSeeder.PARTNER_DEMO_SUBJECT).then().statusCode(409).body("message", is(PartnerLoginService.DUPLICATE));
        bind(other, at.papo.partnerportal.seed.DemoDataSeeder.PARTNER_DEMO_SUBJECT).then().statusCode(409);
        bind(999_999L, "neu-subject").then().statusCode(404);

        long loginId = bind(other, "  getrimmt-subject  ").then().statusCode(200)
                .body("oidcSubject", is("getrimmt-subject")).extract().jsonPath().getLong("id");
        given().auth().oauth2(company()).when().delete("/api/company/partners/" + demo + "/logins/" + loginId)
                .then().statusCode(404);
        given().auth().oauth2(company()).when().get("/api/company/partners/" + other + "/logins")
                .then().statusCode(200).body("find { it.id == " + loginId + " }.revokedAt", is((Object) null));
    }

    @Test
    void concurrentDuplicateAddsYieldOneSuccess() {
        long a = partnerId("P-1002");
        long b = partnerId("P-1003");
        List<CompletableFuture<Integer>> calls = List.of(
                CompletableFuture.supplyAsync(() -> bind(a, "parallel-subject").statusCode()),
                CompletableFuture.supplyAsync(() -> bind(b, "parallel-subject").statusCode()));
        assertEquals(List.of(200, 409), calls.stream().map(CompletableFuture::join).sorted().toList());
    }

    @Test
    void onlyCompanyManagesBindings() {
        long demo = partnerId("PARTNER-DEMO");
        given().when().get("/api/company/partners/" + demo + "/logins").then().statusCode(401);
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo"))
                .when().get("/api/company/partners/" + demo + "/logins").then().statusCode(403);
        given().auth().oauth2(tokens.tokenFor("partner-demo", "partner-demo")).contentType("application/json")
                .body("{\"oidcSubject\":\"x\"}").when().post("/api/company/partners/" + demo + "/logins").then().statusCode(403);
        given().auth().oauth2(company()).when().get("/api/company/partners/" + demo)
                .then().statusCode(200).body("$", not(hasKey("oidcSubject")));
    }

    private io.restassured.response.Response bind(long partnerId, String subject) {
        return given().auth().oauth2(company()).contentType("application/json")
                .body("{\"oidcSubject\":\"" + subject + "\"}")
                .when().post("/api/company/partners/" + partnerId + "/logins");
    }

    private io.restassured.response.Response profile(String token) {
        return given().auth().oauth2(token).when().get("/api/partner/profile");
    }

    private String company() {
        return tokens.tokenFor("company-demo", "company-demo");
    }

    private static long partnerId(String number) {
        return QuarkusTransaction.requiringNew().call(() -> Partner.<Partner>find("partnerNumber", number).firstResult().id);
    }

    private static String subjectOf(String token) {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        return io.restassured.path.json.JsonPath.from(payload).getString("sub");
    }
}
