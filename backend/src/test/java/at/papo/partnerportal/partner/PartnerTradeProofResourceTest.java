package at.papo.partnerportal.partner;

import at.papo.partnerportal.support.TestCompanies;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManagerFactory;
import jakarta.inject.Inject;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PartnerTradeProofResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    PartnerService partnerService;

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Test
    void partnerUploadsCompanyReviewsAndReplacementResetsReview() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        byte[] firstPdf = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        byte[] replacementPdf = "%PDF-1.7\nreplacement\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        long partnerId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;

        boolean activeBefore = given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId)
                .then().statusCode(200).extract().path("active");

        String firstVersion = given().auth().oauth2(partnerToken)
                .multiPart("registerCountry", "AT")
                .multiPart("registerType", "GISA")
                .multiPart("registerValue", "GISA-1001")
                .multiPart("file", "gewerbe.pdf", firstPdf, "application/pdf")
                .when().post("/api/partner/trade-proof")
                .then().statusCode(200)
                .body("present", is(true))
                .body("status", is("PENDING"))
                .body("registerIdentifier.value", is("GISA-1001"))
                .body("data", nullValue())
                .extract().path("version");

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/trade-proof")
                .then().statusCode(200).body("status", is("PENDING"));

        given().auth().oauth2(partnerToken)
                .multiPart("registerCountry", "AT")
                .multiPart("registerType", "GISA")
                .multiPart("registerValue", "GISA-INVALID")
                .multiPart("file", "gewerbe.png", firstPdf, "image/png")
                .when().post("/api/partner/trade-proof")
                .then().statusCode(400);
        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/trade-proof")
                .then().statusCode(200).body("version", is(firstVersion)).body("status", is("PENDING"));

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId + "/trade-proof")
                .then().statusCode(200)
                .body("status", is("PENDING"))
                .body("data", nullValue());

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners")
                .then().statusCode(200)
                .body("find { it.id == " + partnerId + " }.tradeProof.status", is("PENDING"));

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId + "/trade-proof/download")
                .then().statusCode(200)
                .header("Cache-Control", is("private, no-store"))
                .header("X-Content-Type-Options", is("nosniff"))
                .header("Content-Disposition", containsString("attachment"))
                .contentType("application/pdf");

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/trade-proof/download")
                .then().statusCode(200)
                .header("Cache-Control", is("private, no-store"))
                .header("X-Content-Type-Options", is("nosniff"))
                .contentType("application/pdf");

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"version\":\"" + firstVersion + "\",\"status\":\"ACCEPTED\"}")
                .when().put("/api/company/partners/" + partnerId + "/trade-proof/review")
                .then().statusCode(200).body("status", is("ACCEPTED"));

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId)
                .then().statusCode(200).body("active", is(activeBefore));

        byte[] tooLarge = new byte[10 * 1024 * 1024 + 1];
        given().auth().oauth2(partnerToken)
                .multiPart("registerCountry", "AT")
                .multiPart("registerType", "GISA")
                .multiPart("registerValue", "GISA-TOO-LARGE")
                .multiPart("file", "gewerbe.pdf", tooLarge, "application/pdf")
                .when().post("/api/partner/trade-proof")
                .then().statusCode(413);
        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/" + partnerId + "/trade-proof")
                .then().statusCode(200).body("version", is(firstVersion)).body("status", is("ACCEPTED"));

        String secondVersion = given().auth().oauth2(partnerToken)
                .multiPart("registerCountry", "AT")
                .multiPart("registerType", "GISA")
                .multiPart("registerValue", "GISA-1002")
                .multiPart("file", "gewerbe-neu.pdf", replacementPdf, "application/pdf")
                .when().post("/api/partner/trade-proof")
                .then().statusCode(200)
                .body("status", is("PENDING"))
                .extract().path("version");
        assertNotEquals(firstVersion, secondVersion);

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"version\":\"" + firstVersion + "\",\"status\":\"ACCEPTED\"}")
                .when().put("/api/company/partners/" + partnerId + "/trade-proof/review")
                .then().statusCode(409);

        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"version\":\"" + secondVersion + "\",\"status\":\"REJECTED\","
                        + "\"rejectionReason\":\"<script>alert(1)</script>\"}")
                .when().put("/api/company/partners/" + partnerId + "/trade-proof/review")
                .then().statusCode(200).body("status", is("REJECTED"));

        given().auth().oauth2(partnerToken)
                .when().get("/api/partner/trade-proof")
                .then().statusCode(200)
                .body("status", is("REJECTED"))
                .body("rejectionReason", is("<script>alert(1)</script>"));
    }

    @Test
    void rolesAndMissingResourcesAreDenied() {
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");

        given().when().get("/api/partner/trade-proof")
                .then().statusCode(401);

        given().auth().oauth2(partnerToken)
                .when().get("/api/company/partners/999999999/trade-proof")
                .then().statusCode(403);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/999999999/trade-proof")
                .then().statusCode(404);

        given().auth().oauth2(companyToken)
                .when().get("/api/company/partners/999999999/trade-proof/download")
                .then().statusCode(404);
    }

    @Test
    void inactivePartnerCannotUploadProof() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        boolean wasActive = QuarkusTransaction.requiringNew()
                .call(() -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().active);
        QuarkusTransaction.requiringNew()
                .run(() -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().active = false);
        try {
            given().auth().oauth2(partnerToken)
                    .multiPart("registerCountry", "AT")
                    .multiPart("registerType", "GISA")
                    .multiPart("registerValue", "GISA-INACTIVE")
                    .multiPart("file", "gewerbe.pdf", "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII),
                            "application/pdf")
                    .when().post("/api/partner/trade-proof")
                    .then().statusCode(403);
        } finally {
            QuarkusTransaction.requiringNew()
                    .run(() -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().active = wasActive);
        }
    }

    @Test
    void secondPartnerUploadsOnlyToItsOwnRecord() {
        String secondPartnerToken = tokens.tokenFor("partner-second", "partner-second");
        long secondPartnerId = QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.company = TestCompanies.one();
            partner.partnerNumber = "PARTNER-SECOND";
            partner.name = "Testpartner Zwei";
            partner.address = new Address("Teststraße", "2", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID,
                    "ATU00000000");
            partner.commissionPercent = BigDecimal.ZERO;
            partner.persist();
            // Release B: logins resolve only through an explicit subject binding.
            PartnerLogin login = new PartnerLogin();
            login.oidcSubject = io.restassured.path.json.JsonPath.from(new String(java.util.Base64.getUrlDecoder()
                    .decode(secondPartnerToken.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8)).getString("sub");
            login.partner = partner;
            login.createdBy = "test";
            login.persist();
            return partner.id;
        });
        long firstPartnerId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
        long firstProofCount = PartnerTradeProof.count("partner.id", firstPartnerId);
        try {
            given().auth().oauth2(secondPartnerToken)
                    .multiPart("registerCountry", "AT")
                    .multiPart("registerType", "GISA")
                    .multiPart("registerValue", "GISA-SECOND")
                    .multiPart("file", "second.pdf", "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII),
                            "application/pdf")
                    .when().post("/api/partner/trade-proof")
                    .then().statusCode(200).body("present", is(true));

            assertEquals(1, PartnerTradeProof.count("partner.id", secondPartnerId));
            assertEquals(firstProofCount, PartnerTradeProof.count("partner.id", firstPartnerId));
            given().auth().oauth2(secondPartnerToken)
                    .when().get("/api/company/partners/" + firstPartnerId + "/trade-proof")
                    .then().statusCode(403);
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                PartnerTradeProof.delete("partner.id", secondPartnerId);
                PartnerLogin.delete("partner.id", secondPartnerId);
                Partner.deleteById(secondPartnerId);
            });
        }
    }

    @Test
    void companyPartnerListLoadsAllProofMetadataInOneBatch() {
        var company = QuarkusTransaction.requiringNew().call(TestCompanies::one);
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        partnerService.list(company);

        assertEquals(2, statistics.getQueryExecutionCount(),
                "Partnerliste und Nachweis-Metadaten sollen je eine Abfrage benötigen");
    }

    @Test
    void companyPartnerListMarksMissingProofAsAbsent() {
        long partnerId = QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.company = TestCompanies.one();
            partner.partnerNumber = "PARTNER-NO-PROOF";
            partner.name = "Partner ohne Nachweis";
            partner.address = new Address("Teststraße", "3", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID,
                    "ATU00000001");
            partner.commissionPercent = BigDecimal.ZERO;
            partner.persist();
            return partner.id;
        });
        try {
            given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo"))
                    .when().get("/api/company/partners")
                    .then().statusCode(200)
                    .body("find { it.partnerNumber == 'PARTNER-NO-PROOF' }.tradeProof.present", is(false));
        } finally {
            QuarkusTransaction.requiringNew().run(() -> Partner.deleteById(partnerId));
        }
    }

    @Test
    void concurrentReplacementsKeepOneCurrentProof() throws Exception {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<io.restassured.response.Response> first = executor.submit(() -> uploadAfterStart(
                    partnerToken, "GISA-CONCURRENT-1", "one.pdf", start));
            Future<io.restassured.response.Response> second = executor.submit(() -> uploadAfterStart(
                    partnerToken, "GISA-CONCURRENT-2", "two.pdf", start));
            start.countDown();
            assertEquals(200, first.get(15, TimeUnit.SECONDS).statusCode());
            assertEquals(200, second.get(15, TimeUnit.SECONDS).statusCode());
            long partnerId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
            assertEquals(1, PartnerTradeProof.count("partner.id", partnerId));
            assertTrue(PartnerTradeProof.<PartnerTradeProof>find("partner.id", partnerId).firstResult().version != null);
        } finally {
            executor.shutdownNow();
        }
    }

    private io.restassured.response.Response uploadAfterStart(String token, String registerValue,
            String filename, CountDownLatch start) throws Exception {
        start.await();
        return given().auth().oauth2(token)
                .multiPart("registerCountry", "AT")
                .multiPart("registerType", "GISA")
                .multiPart("registerValue", registerValue)
                .multiPart("file", filename, "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII), "application/pdf")
                .when().post("/api/partner/trade-proof").then().extract().response();
    }
}
