package at.papo.partnerportal;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;

// Smoke-tests role separation (FR-20) against the Dev Services Keycloak instance,
// which imports keycloak/partnerportal-realm.json (roles company/partner, demo users).
@QuarkusTest
class HelloResourceRoleTest {

    @Inject
    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    private String tokenFor(String username, String password) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "partnerportal-frontend")
                .formParam("username", username)
                .formParam("password", password)
                .when().post(authServerUrl + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().path("access_token");
    }

    @Test
    void companyUserCanAccessCompanyEndpointNotPartnerEndpoint() {
        String token = tokenFor("company-demo", "company-demo");

        given().auth().oauth2(token)
                .when().get("/hello/company")
                .then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/hello/partner")
                .then().statusCode(403);
    }

    @Test
    void partnerUserCanAccessPartnerEndpointNotCompanyEndpoint() {
        String token = tokenFor("partner-demo", "partner-demo");

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
