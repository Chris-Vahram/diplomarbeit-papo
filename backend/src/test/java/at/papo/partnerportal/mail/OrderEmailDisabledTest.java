package at.papo.partnerportal.mail;

import at.papo.partnerportal.mail.OrderEmail.Status;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static at.papo.partnerportal.mail.OrderEmailTestSupport.order;
import static at.papo.partnerportal.mail.OrderEmailTestSupport.rows;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(OrderEmailDisabledTest.Disabled.class)
class OrderEmailDisabledTest {

    public static class Disabled implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("papo.mail.enabled", "false");
        }
    }

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    MockMailbox mailbox;

    @Inject
    OrderEmailSender sender;

    @Test
    void disabledSendingRecordsWithoutAddressAndNeverSends() {
        mailbox.clear();
        long id = order(tokens.tokenFor("partner-demo", "partner-demo"), "Aus", "aus@example.at")
                .then().statusCode(200).body("customerEmailNotice", is("DISABLED")).extract().jsonPath().getLong("id");
        List<OrderEmail> rows = rows(id);
        assertEquals(3, rows.size());
        assertTrue(rows.stream().allMatch(r -> r.status == Status.SKIPPED && "DISABLED".equals(r.skipReason) && r.recipientAddress == null));

        sender.sendDue();
        assertEquals(0, mailbox.getTotalMessagesSent());

        given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo"))
                .when().post("/api/company/orders/" + id + "/emails/" + rows.get(0).id + "/retry")
                .then().statusCode(409);
    }
}
