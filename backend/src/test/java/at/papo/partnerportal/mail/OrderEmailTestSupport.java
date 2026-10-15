package at.papo.partnerportal.mail;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import io.quarkus.narayana.jta.QuarkusTransaction;

import java.util.List;

import static io.restassured.RestAssured.given;

final class OrderEmailTestSupport {

    static final String ADDRESS = "{\"street\":\"Hauptstraße\",\"houseNumber\":\"1\",\"postalCode\":\"4020\",\"city\":\"Linz\",\"country\":\"Österreich\"}";

    private OrderEmailTestSupport() {
    }

    static long productId() {
        return QuarkusTransaction.requiringNew().call(
                () -> Product.<Product>find("name", "Akkuschrauber 18V Set").firstResult().id);
    }

    static long partnerDemoId() {
        return QuarkusTransaction.requiringNew().call(
                () -> Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id);
    }

    // Creates an order for a new customer; email may be null. Returns the response's order id.
    static io.restassured.response.Response order(String token, String lastName, String email) {
        String kunde = "{\"lastName\":\"" + lastName + "\",\"firstName\":\"Jürgen\","
                + (email != null ? "\"email\":\"" + email + "\"," : "") + "\"address\":" + ADDRESS + "}";
        return given().auth().oauth2(token).contentType("application/json")
                .body("{\"newKunde\":" + kunde + ",\"items\":[{\"productId\":" + productId() + ",\"quantity\":2}]}")
                .when().post("/api/partner/orders");
    }

    static List<OrderEmail> rows(long orderId) {
        return QuarkusTransaction.requiringNew().call(
                () -> OrderEmail.<OrderEmail>find("order.id = ?1 order by id", orderId).list());
    }

    static void advance(String companyToken, long orderId, String status, int expected) {
        given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"status\":\"" + status + "\"}")
                .when().put("/api/company/orders/" + orderId + "/status")
                .then().statusCode(expected);
    }
}
