package at.papo.partnerportal.mail;

import at.papo.partnerportal.mail.OrderEmail.Status;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

// Outbox sender (send-order-emails design "Send loop"): one row at a time - claim in a short
// transaction, send outside any transaction, record the outcome in its own transaction, so an
// error never rolls back rows already accepted by the mail server. At-least-once: a crash after
// SMTP acceptance can repeat one mail.
// ponytail: single VM, so claiming via conditional update suffices; use FOR UPDATE SKIP LOCKED
// once several backend instances run.
@ApplicationScoped
public class OrderEmailSender {

    static final int MAX_ATTEMPTS = 5;
    private static final Duration[] BACKOFF = {
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(15), Duration.ofMinutes(60)};
    private static final Duration STUCK_AFTER = Duration.ofMinutes(10);
    // Chris' decision 2026-10-15: failed mails keep the full address for 90 days (retry window),
    // then it is removed. A chosen data-minimisation value, not a statutory period.
    static final Duration ADDRESS_RETENTION = Duration.ofDays(90);

    @Inject
    MailConfig config;

    @Inject
    MailTransport transport;

    @Inject
    OrderEmailService orderEmailService;

    void checkConfig(@Observes StartupEvent event) {
        String problem = MailConfig.startupProblem(config.enabled(), config.from(), config.redirectTo(),
                config.allowRealRecipients());
        if (problem != null) {
            throw new IllegalStateException(problem);
        }
    }

    @Scheduled(every = "30s", delayed = "20s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void run() {
        sendDue();
    }

    public void sendDue() {
        Instant now = Instant.now();
        QuarkusTransaction.requiringNew().run(() -> OrderEmail.update(
                "recipientAddress = null where status = ?1 and recipientAddress is not null and createdAt < ?2",
                Status.FAILED, now.minus(ADDRESS_RETENTION)));
        QuarkusTransaction.requiringNew().run(() -> OrderEmail.update(
                "status = ?1, claimedAt = null where status = ?2 and claimedAt < ?3",
                Status.PENDING, Status.SENDING, now.minus(STUCK_AFTER)));
        List<Long> due = QuarkusTransaction.requiringNew().call(() -> OrderEmail
                .<OrderEmail>find("status = ?1 and nextAttemptAt <= ?2 order by id", Status.PENDING, now)
                .page(0, 20).stream().map(e -> e.id).toList());
        due.forEach(this::sendOne);
    }

    private void sendOne(Long id) {
        boolean claimed = QuarkusTransaction.requiringNew().call(() -> OrderEmail.update(
                "status = ?1, claimedAt = ?2 where id = ?3 and status = ?4",
                Status.SENDING, Instant.now(), id, Status.PENDING) == 1);
        if (!claimed) {
            return;
        }
        Mail mail = QuarkusTransaction.requiringNew().call(() -> prepare(id));
        if (mail == null) {
            return;
        }
        try {
            transport.send(mail);
        } catch (RuntimeException e) {
            String code = errorCode(e);
            QuarkusTransaction.requiringNew().run(() -> markFailedAttempt(id, code));
            return;
        }
        // Accepted by the mail server. If recording SENT fails, the row stays SENDING and the
        // stuck-row recovery requeues it (at-least-once) instead of counting a failed attempt.
        try {
            QuarkusTransaction.requiringNew().run(() -> markSent(id));
        } catch (RuntimeException e) {
            Log.warnf("Bestell-E-Mail %d: Versand angenommen, Status konnte nicht gespeichert werden", id);
        }
    }

    // Returns null when the row was skipped (configuration or recipient changed since queueing).
    private Mail prepare(Long id) {
        OrderEmail email = OrderEmail.findById(id);
        if (!config.enabled() || !java.util.Objects.equals(email.redirectTo, config.redirectTarget())) {
            skip(email, "CONFIG_CHANGED");
            return null;
        }
        String current = OrderEmailService.normalize(orderEmailService.currentAddress(email.order, email.recipientRole));
        if (current == null || !current.equals(email.recipientAddress)) {
            skip(email, "RECIPIENT_CHANGED");
            return null;
        }
        boolean redirected = email.deliveryMode == OrderEmail.DeliveryMode.REDIRECT;
        String to = redirected ? email.redirectTo : email.recipientAddress;
        Mail mail = OrderEmailContent.build(email, to, redirected);
        config.from().ifPresent(mail::setFrom);
        return mail;
    }

    private void skip(OrderEmail email, String reason) {
        email.status = Status.SKIPPED;
        email.skipReason = reason;
        email.recipientAddress = null;
        email.claimedAt = null;
        Log.infof("Bestell-E-Mail %d %s: SKIPPED %s", email.id, email.type, reason);
    }

    private void markSent(Long id) {
        OrderEmail email = OrderEmail.findById(id);
        email.status = Status.SENT;
        email.sentAt = Instant.now();
        email.claimedAt = null;
        email.recipientAddress = OrderEmailDto.mask(email.recipientAddress);
        Log.infof("Bestell-E-Mail %d %s: SENT", id, email.type);
    }

    private void markFailedAttempt(Long id, String code) {
        OrderEmail email = OrderEmail.findById(id);
        email.attempts++;
        email.lastErrorCode = code;
        email.claimedAt = null;
        if (email.attempts >= MAX_ATTEMPTS) {
            email.status = Status.FAILED;
        } else {
            email.status = Status.PENDING;
            email.nextAttemptAt = Instant.now().plus(BACKOFF[email.attempts - 1]);
        }
        Log.warnf("Bestell-E-Mail %d %s: %s %s (Versuch %d)", id, email.type, email.status, code, email.attempts);
    }

    static String errorCode(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConnectException) {
                return "SMTP_UNAVAILABLE";
            }
            if (t instanceof TimeoutException || t.getClass().getSimpleName().contains("Timeout")) {
                return "TIMEOUT";
            }
            String message = String.valueOf(t.getMessage()).toLowerCase();
            if (message.contains("auth")) {
                return "AUTH_FAILED";
            }
            if (message.contains("550") || message.contains("553") || message.contains("554") || message.contains("reject")) {
                return "REJECTED";
            }
        }
        return "OTHER";
    }
}
