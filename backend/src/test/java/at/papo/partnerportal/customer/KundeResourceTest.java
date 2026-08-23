package at.papo.partnerportal.customer;

import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class KundeResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Test
    void partnerCreatesAndListsOwnCustomer() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"lastName\":\"Gruber\",\"address\":{\"street\":\"Musterweg\",\"houseNumber\":\"3\","
                        + "\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}}")
                .when().post("/api/partner/customers")
                .then().statusCode(200)
                .body("lastName", is("Gruber"));

        given().auth().oauth2(token)
                .when().get("/api/partner/customers")
                .then().statusCode(200)
                .body("find { it.lastName == 'Gruber' }.address.street", is("Musterweg"));
    }

    @Test
    void kundeWithoutOrdersHasNullLastOrderDate() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"lastName\":\"Ohne Bestellung\",\"address\":{\"street\":\"Musterweg\",\"houseNumber\":\"1\","
                        + "\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}}")
                .when().post("/api/partner/customers")
                .then().statusCode(200)
                .body("lastOrderDate", nullValue());

        given().auth().oauth2(token)
                .when().get("/api/partner/customers")
                .then().statusCode(200)
                .body("find { it.lastName == 'Ohne Bestellung' }.lastOrderDate", nullValue());
    }

    @Test
    void kundeWithOrderHasTodaysLastOrderDate() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        long productId = Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id;

        Number kundeId = given().auth().oauth2(token).contentType("application/json")
                .body("{\"lastName\":\"Mit Bestellung\",\"address\":{\"street\":\"Musterweg\",\"houseNumber\":\"1\","
                        + "\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}}")
                .when().post("/api/partner/customers")
                .then().statusCode(200)
                .extract().path("id");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"kundeId\":" + kundeId + ",\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/api/partner/customers")
                .then().statusCode(200)
                .body("find { it.lastName == 'Mit Bestellung' }.lastOrderDate", is(LocalDate.now().toString()));
    }

    @Test
    void createWithoutLastNameIsRejected() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");

        given().auth().oauth2(token).contentType("application/json")
                .body("{\"address\":{\"street\":\"Musterweg\",\"houseNumber\":\"3\",\"postalCode\":\"4020\","
                        + "\"city\":\"Linz\",\"country\":\"Österreich\"}}")
                .when().post("/api/partner/customers")
                .then().statusCode(400);
    }
}
