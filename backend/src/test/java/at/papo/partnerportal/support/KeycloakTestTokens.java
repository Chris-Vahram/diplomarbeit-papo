package at.papo.partnerportal.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.restassured.RestAssured.given;

// Shared by test classes that need a real access token from the Dev Services
// Keycloak (which imports keycloak/partnerportal-realm.json).
@ApplicationScoped
public class KeycloakTestTokens {

    @Inject
    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    public String tokenFor(String username, String password) {
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
}
