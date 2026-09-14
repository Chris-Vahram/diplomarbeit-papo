package at.papo.partnerportal.customer;

import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
class KundeResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    KundeService kundeService;

    private static final String VALID_ADDRESS = "{\"street\":\"Musterweg\",\"houseNumber\":\"3\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}";

    private Partner partner(String number) {
        return Partner.<Partner>find("partnerNumber", number).firstResult();
    }

    private long customerCount() {
        return Kunde.count();
    }

    private String invalidAddress(String blankField) {
        return "{\"street\":\"" + (blankField.equals("street") ? " " : "Musterweg")
                + "\",\"houseNumber\":\"" + (blankField.equals("houseNumber") ? " " : "3")
                + "\",\"postalCode\":\"" + (blankField.equals("postalCode") ? " " : "4020")
                + "\",\"city\":\"" + (blankField.equals("city") ? " " : "Linz")
                + "\",\"country\":\"" + (blankField.equals("country") ? " " : "Österreich") + "\"}";
    }

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

    @Test
    void httpCreationAndListRemainPartnerScoped() {
        String token = tokens.tokenFor("partner-demo", "partner-demo");
        long foreignId = QuarkusTransaction.requiringNew().call(() -> {
            Kunde foreign = new Kunde();
            foreign.ownerPartner = partner("P-1002");
            foreign.lastName = "P1002-Nur-Test";
            foreign.address = new at.papo.partnerportal.common.Address("Testweg", "1", "4020", "Linz", "Österreich");
            foreign.persist();
            return foreign.id;
        });
        try {
            given().auth().oauth2(token).when().get("/api/partner/customers")
                    .then().statusCode(200).body("id", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem((int) foreignId)));

            Number createdId = given().auth().oauth2(token).contentType("application/json")
                    .body("{\"lastName\":\"P1-HTTP-Test\",\"address\":" + VALID_ADDRESS + "}")
                    .when().post("/api/partner/customers").then().statusCode(200).extract().path("id");
            try {
                QuarkusTransaction.requiringNew().run(() -> {
                    Kunde created = Kunde.findById(createdId.longValue());
                    assertEquals("PARTNER-DEMO", created.ownerPartner.partnerNumber);
                    assertFalse(kundeService.listForPartner(partner("P-1002")).stream()
                            .anyMatch(k -> k.id().equals(createdId.longValue())));
                });
            } finally {
                QuarkusTransaction.requiringNew().run(() -> Kunde.deleteById(createdId.longValue()));
            }
        } finally {
            QuarkusTransaction.requiringNew().run(() -> Kunde.deleteById(foreignId));
        }
    }

    @Test
    void customerEndpointRejectsWrongRolesInactivePartnerAndInvalidAddresses() {
        String partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        for (String method : new String[] { "get", "post" }) {
            if (method.equals("get")) {
                given().when().get("/api/partner/customers").then().statusCode(401);
                given().auth().oauth2(companyToken).when().get("/api/partner/customers").then().statusCode(403);
            } else {
                given().contentType("application/json").body("{\"lastName\":\"Test\",\"address\":" + VALID_ADDRESS + "}").when().post("/api/partner/customers").then().statusCode(401);
                given().auth().oauth2(companyToken).contentType("application/json").body("{\"lastName\":\"Test\",\"address\":" + VALID_ADDRESS + "}").when().post("/api/partner/customers").then().statusCode(403);
            }
        }
        for (String field : new String[] { "street", "houseNumber", "postalCode", "city", "country" }) {
            long before = customerCount();
            given().auth().oauth2(partnerToken).contentType("application/json")
                    .body("{\"lastName\":\"Test\",\"address\":" + invalidAddress(field) + "}")
                    .when().post("/api/partner/customers").then().statusCode(400);
            assertEquals(before, customerCount());
        }
        boolean active = QuarkusTransaction.requiringNew().call(() -> partner("PARTNER-DEMO").active);
        QuarkusTransaction.requiringNew().run(() -> partner("PARTNER-DEMO").active = false);
        try {
            long before = customerCount();
            given().auth().oauth2(partnerToken).contentType("application/json")
                    .body("{\"lastName\":\"Inaktiv\",\"address\":" + VALID_ADDRESS + "}")
                    .when().post("/api/partner/customers").then().statusCode(403);
            assertEquals(before, customerCount());
        } finally {
            QuarkusTransaction.requiringNew().run(() -> partner("PARTNER-DEMO").active = active);
        }
    }
}
