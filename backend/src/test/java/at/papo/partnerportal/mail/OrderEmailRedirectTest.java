package at.papo.partnerportal.mail;

import at.papo.partnerportal.mail.OrderEmail.Type;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static at.papo.partnerportal.mail.OrderEmailTestSupport.advance;
import static at.papo.partnerportal.mail.OrderEmailTestSupport.order;
import static at.papo.partnerportal.mail.OrderEmailTestSupport.rows;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Demo-VM setup: redirect to a test inbox, real recipients not allowed, shipping mails off.
@QuarkusTest
@TestProfile(OrderEmailRedirectTest.Redirect.class)
class OrderEmailRedirectTest {

    public static class Redirect implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("papo.mail.redirect-to", "testpostfach@example.test",
                    "papo.mail.allow-real-recipients", "false",
                    "papo.mail.shipping-enabled", "false");
        }
    }

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    MockMailbox mailbox;

    @Inject
    OrderEmailSender sender;

    @Test
    void everyMailGoesToTheTestInboxAndShippingStaysOff() {
        sender.sendDue();
        mailbox.clear();
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        long id = order(tokens.tokenFor("partner-demo", "partner-demo"), "Umgeleitet", "echt@example.at")
                .then().statusCode(200).body("customerEmailNotice", is("REDIRECTED")).extract().jsonPath().getLong("id");

        sender.sendDue();
        List<Mail> mails = mailbox.getMailsSentTo("testpostfach@example.test");
        assertEquals(3, mails.size());
        assertTrue(mails.stream().allMatch(m -> m.getText().startsWith("[Testumleitung")));
        assertTrue(mailbox.getMailsSentTo("echt@example.at").isEmpty());

        advance(companyToken, id, "REVIEWED", 200);
        given().auth().oauth2(companyToken).contentType("application/json").body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + id + "/status")
                .then().statusCode(200).body("customerEmailNotice", is("DISABLED"));
        assertTrue(rows(id).stream().noneMatch(r -> r.type == Type.SHIPPING_CONFIRMATION));
    }
}
