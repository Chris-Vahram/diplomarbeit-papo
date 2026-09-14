package at.papo.partnerportal.business;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
class CompanyProfileResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyReadsAndUpdatesTheSingleProfileWithNormalizedIdentifier() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token).when().get("/api/company/profile")
                .then().statusCode(200).body("businessName", is("Huemer GmbH"));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"businessName\":\"Demo Portal\",\"taxIdentifier\":"
                        + "{\"country\":\"at\",\"type\":\"VAT_ID\",\"value\":\" atu23456789 \"}}")
                .when().put("/api/company/profile")
                .then().statusCode(200)
                .body("taxIdentifier.country", is("AT"))
                .body("taxIdentifier.value", is("ATU23456789"));

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"businessName\":\"Ungültig\",\"taxIdentifier\":"
                        + "{\"country\":\"AT\",\"type\":\"VAT_ID\",\"value\":\"AT123\"}}")
                .when().put("/api/company/profile")
                .then().statusCode(400);

        // Restore the synthetic fixture for the rest of the suite.
        given().auth().oauth2(token).contentType("application/json")
                .body("{\"businessName\":\"Huemer GmbH\",\"taxIdentifier\":"
                        + "{\"country\":\"AT\",\"type\":\"VAT_ID\",\"value\":\"ATU87654321\"}}")
                .when().put("/api/company/profile").then().statusCode(200);
    }

    @Test
    void partnerCannotAccessCompanyProfileAndCannotChangePartnerTaxIdentifier() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partnerToken).when().get("/api/company/profile").then().statusCode(403);
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"businessName\":\"Manipuliert\",\"taxIdentifier\":"
                        + "{\"country\":\"AT\",\"type\":\"VAT_ID\",\"value\":\"ATU00000000\"}}")
                .when().put("/api/company/profile").then().statusCode(403);

        given().auth().oauth2(partnerToken).when().get("/api/partner/profile")
                .then().statusCode(200).body("$", not(hasKey("taxIdentifier")));
    }
}
