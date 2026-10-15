package at.papo.partnerportal.mail;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.mail.OrderEmail.Status;
import at.papo.partnerportal.mail.OrderEmail.Type;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static at.papo.partnerportal.mail.OrderEmailTestSupport.advance;
import static at.papo.partnerportal.mail.OrderEmailTestSupport.order;
import static at.papo.partnerportal.mail.OrderEmailTestSupport.rows;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class OrderEmailTest {

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    MockMailbox mailbox;

    @Inject
    OrderEmailSender sender;

    @Inject
    at.papo.partnerportal.order.OrderService orderService;

    String partnerToken;
    String companyToken;

    @BeforeEach
    void setUp() {
        partnerToken = tokens.tokenFor("partner-demo", "partner-demo");
        companyToken = tokens.tokenFor("company-demo", "company-demo");
        sender.sendDue(); // flush rows queued by other test classes
        mailbox.clear();
    }

    @Test
    void newOrderQueuesAndSendsThreeMails() {
        long id = order(partnerToken, "Größbacher", "anna@example.at").then().statusCode(200)
                .body("customerEmailNotice", is("QUEUED")).extract().jsonPath().getLong("id");
        assertEquals(3, rows(id).size());
        assertTrue(rows(id).stream().allMatch(r -> r.status == Status.PENDING));

        List<String> logs = captureLogs(sender::sendDue);
        assertTrue(logs.stream().noneMatch(l -> l.contains("@") || l.contains("Größbacher")), "no addresses/bodies in logs: " + logs);

        Mail customer = mailbox.getMailsSentTo("anna@example.at").get(0);
        assertTrue(customer.getSubject().contains("Nr. " + id + " ist eingegangen"));
        assertTrue(customer.getText().contains("2 × Akkuschrauber 18V Set"));
        assertTrue(customer.getText().contains("Jürgen Größbacher"));
        assertFalse(customer.getText().contains("€"), "customer mail must not show prices");
        assertTrue(mailbox.getMailsSentTo("partner-demo@example.at").get(0).getText().contains("Provision:"));
        assertTrue(mailbox.getMailsSentTo("firma@example.test").get(0).getText().contains("Bestellwert:"));
        List<OrderEmail> sent = rows(id);
        assertTrue(sent.stream().allMatch(r -> r.status == Status.SENT && r.sentAt != null));
        assertTrue(sent.stream().allMatch(r -> r.recipientAddress.contains("***")), "address masked after sending");
    }

    @Test
    void customerWithoutEmailGetsNoMail() {
        long id = order(partnerToken, "Ohne Mail", null).then().statusCode(200)
                .body("customerEmailNotice", is("NO_ADDRESS")).extract().jsonPath().getLong("id");
        assertEquals(List.of(Type.ORDER_NOTICE_PARTNER, Type.ORDER_NOTICE_COMPANY),
                rows(id).stream().map(r -> r.type).toList());
    }

    @Test
    void companyManualOrderQueuesMails() {
        long id = given().auth().oauth2(companyToken).contentType("application/json")
                .body("{\"partnerId\":" + OrderEmailTestSupport.partnerDemoId() + ",\"newKunde\":{\"lastName\":\"Manuell\","
                        + "\"email\":\"manuell@example.at\",\"address\":" + OrderEmailTestSupport.ADDRESS + "},"
                        + "\"items\":[{\"productId\":" + OrderEmailTestSupport.productId() + ",\"quantity\":1}]}")
                .when().post("/api/company/orders")
                .then().statusCode(200).body("customerEmailNotice", is("QUEUED")).extract().jsonPath().getLong("id");
        assertEquals(3, rows(id).size());
    }

    @Test
    void rejectedOrderQueuesNothing() {
        long before = QuarkusTransaction.requiringNew().call(() -> OrderEmail.count());
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"newKunde\":{\"lastName\":\"X\",\"email\":\"x@example.at\",\"address\":" + OrderEmailTestSupport.ADDRESS
                        + "},\"items\":[{\"productId\":999999,\"quantity\":1}]}")
                .when().post("/api/partner/orders").then().statusCode(404);
        long after = QuarkusTransaction.requiringNew().call(() -> OrderEmail.count());
        assertEquals(before, after);
    }

    @Test
    void rolledBackOrderTransactionLeavesNoMailRows() {
        long before = QuarkusTransaction.requiringNew().call(() -> OrderEmail.count());
        // setRollbackOnly makes the commit fail with a RollbackException - that is the point.
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            orderService.createForPartner(at.papo.partnerportal.partner.Partner.findById(OrderEmailTestSupport.partnerDemoId()),
                    new at.papo.partnerportal.order.OrderRequest(null,
                            new at.papo.partnerportal.customer.KundeRequest("Rollback",
                                    new at.papo.partnerportal.common.AddressRequest("Weg", "1", "4020", "Linz", "Österreich"),
                                    null, "rollback@example.at", null),
                            List.of(new at.papo.partnerportal.order.OrderItemRequest(OrderEmailTestSupport.productId(), 1))));
            assertTrue(OrderEmail.count() > before, "rows are written inside the order transaction");
            QuarkusTransaction.setRollbackOnly();
        }));
        long after = QuarkusTransaction.requiringNew().call(() -> OrderEmail.count());
        assertEquals(before, after);
    }

    @Test
    void malformedNewCustomerEmailIsRejected() {
        order(partnerToken, "Kaputt", "keine-adresse").then().statusCode(400);
    }

    @Test
    void storedInvalidAddressIsSkipped() {
        long id = order(partnerToken, "Alt", "alt@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long kundeId = QuarkusTransaction.requiringNew().call(() -> {
            Kunde k = Order.<Order>findById(id).kunde;
            k.email = "legacy wert";
            return k.id;
        });
        given().auth().oauth2(partnerToken).contentType("application/json")
                .body("{\"kundeId\":" + kundeId + ",\"items\":[{\"productId\":" + OrderEmailTestSupport.productId() + ",\"quantity\":1}]}")
                .when().post("/api/partner/orders")
                .then().statusCode(200).body("customerEmailNotice", is("INVALID_ADDRESS"));
    }

    @Test
    void fulfilmentSendsOneNeutralCompletionMail() {
        long id = order(partnerToken, "Versand", "versand@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        advance(companyToken, id, "REVIEWED", 200);
        given().auth().oauth2(companyToken).contentType("application/json").body("{\"status\":\"FULFILLED\"}")
                .when().put("/api/company/orders/" + id + "/status")
                .then().statusCode(200).body("customerEmailNotice", is("QUEUED"));
        advance(companyToken, id, "FULFILLED", 400);
        assertEquals(1, rows(id).stream().filter(r -> r.type == Type.SHIPPING_CONFIRMATION).count());

        sender.sendDue();
        assertTrue(mailbox.getMailsSentTo("versand@example.at").stream()
                .anyMatch(m -> m.getSubject().contains("wurde abgeschlossen")));
    }

    @Test
    void concurrentFulfilmentAndSeenKeepOneTransitionAndStatus() {
        long id = order(partnerToken, "Parallel", "parallel@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        advance(companyToken, id, "REVIEWED", 200);
        List<CompletableFuture<Integer>> calls = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            calls.add(CompletableFuture.supplyAsync(() -> given().auth().oauth2(companyToken).contentType("application/json")
                    .body("{\"status\":\"FULFILLED\"}").when().put("/api/company/orders/" + id + "/status").statusCode()));
            calls.add(CompletableFuture.supplyAsync(() -> given().auth().oauth2(companyToken)
                    .when().put("/api/company/orders/" + id + "/seen").statusCode()));
        }
        List<Integer> codes = calls.stream().map(CompletableFuture::join).toList();
        // [fulfil, seen, fulfil, seen]: exactly one fulfilment wins; the loser is rejected with 409
        // (lost the race) or 400 (already fulfilled when it read the order). Seen always works.
        assertEquals(1, List.of(codes.get(0), codes.get(2)).stream().filter(c -> c == 200).count(), "codes: " + codes);
        assertTrue(List.of(400, 409).contains(codes.get(0) == 200 ? codes.get(2) : codes.get(0)), "codes: " + codes);
        assertEquals(List.of(200, 200), List.of(codes.get(1), codes.get(3)));
        QuarkusTransaction.requiringNew().run(() -> {
            Order o = Order.findById(id);
            assertEquals("FULFILLED", o.status.name());
            assertTrue(o.seenByCompany);
        });
        assertEquals(1, rows(id).stream().filter(r -> r.type == Type.SHIPPING_CONFIRMATION).count());
    }

    @Test
    void failingSmtpRetriesThenFailsAndCompanyCanRetry() {
        long id = order(partnerToken, "Fehler", "fehler@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        QuarkusMock.installMockForType(new MailTransport() {
            @Override
            public void send(Mail mail) {
                throw new RuntimeException(new ConnectException("Connection refused"));
            }
        }, MailTransport.class);
        List<String> logs = captureLogs(() -> {
            for (int attempt = 1; attempt <= OrderEmailSender.MAX_ATTEMPTS; attempt++) {
                makeDue(id);
                sender.sendDue();
            }
        });
        OrderEmail customer = customerRow(id, Type.ORDER_CONFIRMATION);
        assertEquals(Status.FAILED, customer.status);
        assertEquals(OrderEmailSender.MAX_ATTEMPTS, customer.attempts);
        assertEquals("SMTP_UNAVAILABLE", customer.lastErrorCode);
        assertTrue(logs.stream().noneMatch(l -> l.contains("@")), "no addresses in logs: " + logs);

        // Company sees masked status, retries once even when two requests race.
        given().auth().oauth2(companyToken).when().get("/api/company/orders/" + id + "/emails")
                .then().statusCode(200).body("find { it.type == 'ORDER_CONFIRMATION' }.maskedAddress", is("f***@example.at"));
        List<CompletableFuture<Integer>> retries = List.of(
                CompletableFuture.supplyAsync(() -> retry(id, customer.id)),
                CompletableFuture.supplyAsync(() -> retry(id, customer.id)));
        List<Integer> codes = retries.stream().map(CompletableFuture::join).sorted().toList();
        assertEquals(List.of(200, 409), codes);
        OrderEmail requeued = customerRow(id, Type.ORDER_CONFIRMATION);
        assertEquals(Status.PENDING, requeued.status);
        assertEquals(0, requeued.attempts);
        assertEquals(409, retry(id, requeued.id), "pending mail cannot be retried");
    }

    @Test
    void transientFailureSchedulesNextAttempt() {
        long id = order(partnerToken, "Später", "spaeter@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        QuarkusMock.installMockForType(new MailTransport() {
            @Override
            public void send(Mail mail) {
                throw new RuntimeException("timeout");
            }
        }, MailTransport.class);
        sender.sendDue();
        OrderEmail row = customerRow(id, Type.ORDER_CONFIRMATION);
        assertEquals(Status.PENDING, row.status);
        assertEquals(1, row.attempts);
        assertTrue(row.nextAttemptAt.isAfter(Instant.now()));
    }

    @Test
    void failedAddressIsRemovedAfterRetentionPeriod() {
        long id = order(partnerToken, "Alt Fehler", "altfehler@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long emailId = customerRow(id, Type.ORDER_CONFIRMATION).id;
        QuarkusTransaction.requiringNew().run(() -> {
            OrderEmail e = OrderEmail.findById(emailId);
            e.status = Status.FAILED;
            e.createdAt = Instant.now().minus(OrderEmailSender.ADDRESS_RETENTION).minusSeconds(60);
        });
        sender.sendDue();
        assertNull(customerRow(id, Type.ORDER_CONFIRMATION).recipientAddress);
        assertEquals(409, retry(id, emailId), "no address left to retry");
    }

    @Test
    void stuckSendingRowIsRecovered() {
        long id = order(partnerToken, "Haengt", "haengt@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long emailId = customerRow(id, Type.ORDER_CONFIRMATION).id;
        QuarkusTransaction.requiringNew().run(() -> {
            OrderEmail e = OrderEmail.findById(emailId);
            e.status = Status.SENDING;
            e.claimedAt = Instant.now().minusSeconds(3600);
        });
        sender.sendDue();
        assertEquals(Status.SENT, customerRow(id, Type.ORDER_CONFIRMATION).status);
    }

    @Test
    void recipientChangedWhilePendingIsNotSentAndCanBeRequeued() {
        long id = order(partnerToken, "Umzug", "alt@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        QuarkusTransaction.requiringNew().run(() -> Order.<Order>findById(id).kunde.email = "neu@example.at");
        sender.sendDue();
        OrderEmail row = customerRow(id, Type.ORDER_CONFIRMATION);
        assertEquals(Status.SKIPPED, row.status);
        assertEquals("RECIPIENT_CHANGED", row.skipReason);
        assertTrue(mailbox.getMailsSentTo("alt@example.at").isEmpty());

        assertEquals(200, retry(id, row.id));
        sender.sendDue();
        assertEquals(1, mailbox.getMailsSentTo("neu@example.at").size());
    }

    @Test
    void configChangedWhilePendingIsNotSent() {
        long id = order(partnerToken, "Umgestellt", "config@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long emailId = customerRow(id, Type.ORDER_CONFIRMATION).id;
        QuarkusTransaction.requiringNew().run(() -> {
            OrderEmail e = OrderEmail.findById(emailId);
            e.deliveryMode = OrderEmail.DeliveryMode.REDIRECT;
            e.redirectTo = "altes-testpostfach@example.test";
        });
        sender.sendDue();
        OrderEmail row = customerRow(id, Type.ORDER_CONFIRMATION);
        assertEquals("CONFIG_CHANGED", row.skipReason);
        assertNull(row.recipientAddress);
        assertTrue(mailbox.getMailsSentTo("config@example.at").isEmpty());
    }

    @Test
    void statusEndpointsAreCompanyOnlyAndOrderScoped() {
        long id = order(partnerToken, "Rechte", "rechte@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long other = order(partnerToken, "Andere", "andere@example.at").then().statusCode(200).extract().jsonPath().getLong("id");
        long emailId = customerRow(id, Type.ORDER_CONFIRMATION).id;
        given().when().get("/api/company/orders/" + id + "/emails").then().statusCode(401);
        given().auth().oauth2(partnerToken).when().get("/api/company/orders/" + id + "/emails").then().statusCode(403);
        given().auth().oauth2(partnerToken).when().post("/api/company/orders/" + id + "/emails/" + emailId + "/retry").then().statusCode(403);
        assertEquals(404, retry(other, emailId));
        given().auth().oauth2(companyToken).when().get("/api/company/orders/999999/emails").then().statusCode(404);
    }

    @Test
    void listsDoNotCarryTheNotice() {
        order(partnerToken, "Liste", "liste@example.at").then().statusCode(200);
        given().auth().oauth2(partnerToken).when().get("/api/partner/orders")
                .then().statusCode(200).body("[0].containsKey('customerEmailNotice')", is(false));
    }

    @Test
    void startupRefusesRealRecipientsWithoutPermission() {
        assertNull(MailConfig.startupProblem(false, Optional.empty(), Optional.empty(), false));
        assertNotNull(MailConfig.startupProblem(true, Optional.empty(), Optional.of("t@example.test"), false));
        assertNotNull(MailConfig.startupProblem(true, Optional.of("a@b.at"), Optional.empty(), false));
        assertNull(MailConfig.startupProblem(true, Optional.of("a@b.at"), Optional.of("t@example.test"), false));
        assertNull(MailConfig.startupProblem(true, Optional.of("a@b.at"), Optional.empty(), true));
    }

    private int retry(long orderId, long emailId) {
        return given().auth().oauth2(companyToken).when()
                .post("/api/company/orders/" + orderId + "/emails/" + emailId + "/retry").statusCode();
    }

    private static OrderEmail customerRow(long orderId, Type type) {
        return rows(orderId).stream().filter(r -> r.type == type).findFirst().orElseThrow();
    }

    private static void makeDue(long orderId) {
        QuarkusTransaction.requiringNew().run(() -> OrderEmail.update(
                "nextAttemptAt = ?1 where order.id = ?2 and status = ?3", Instant.now().minusSeconds(1), orderId, Status.PENDING));
    }

    private static List<String> captureLogs(Runnable action) {
        List<String> messages = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(String.valueOf(new java.util.logging.SimpleFormatter().formatMessage(record)));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger root = Logger.getLogger("");
        root.addHandler(handler);
        try {
            action.run();
        } finally {
            root.removeHandler(handler);
        }
        return messages;
    }
}
