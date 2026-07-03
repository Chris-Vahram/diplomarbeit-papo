package at.papo.partnerportal.customer;

import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class KundeResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void partnerCreatesAndListsOwnCustomer() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"lastName\":\"Gruber\",\"address\":\"Musterweg 3\"}")
                .when().post("/api/partner/customers")
                .then().statusCode(200)
                .body("lastName", is("Gruber"));

        given().auth().oauth2(token)
                .when().get("/api/partner/customers")
                .then().statusCode(200)
                .body("find { it.lastName == 'Gruber' }.address", is("Musterweg 3"));
    }

    @Test
    void createWithoutLastNameIsRejected() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"address\":\"Musterweg 3\"}")
                .when().post("/api/partner/customers")
                .then().statusCode(400);
    }
}
