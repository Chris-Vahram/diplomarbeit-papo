package at.papo.partnerportal;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;

// Smoke-tests role separation (FR-20) against the Dev Services Keycloak instance,
// which imports keycloak/partnerportal-realm.json (roles company/partner, demo users).
@QuarkusTest
class HelloResourceRoleTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void companyUserCanAccessCompanyEndpointNotPartnerEndpoint() {
        String token = tokens.tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .when().get("/hello/company")
                .then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/hello/partner")
                .then().statusCode(403);
    }

    @Test
    void partnerUserCanAccessPartnerEndpointNotCompanyEndpoint() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token)
                .when().get("/hello/partner")
                .then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/hello/company")
                .then().statusCode(403);
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        given()
                .when().get("/hello/company")
                .then().statusCode(401);
    }
}
