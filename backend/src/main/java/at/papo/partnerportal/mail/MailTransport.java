package at.papo.partnerportal.mail;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;

// Thin seam around the mailer so tests can install a failing transport (QuarkusMock).
// Waits for the SMTP result with a bound, so a row is only marked SENT after acceptance.
@ApplicationScoped
public class MailTransport {

    @Inject
    ReactiveMailer mailer;

    public void send(Mail mail) {
        mailer.send(mail).await().atMost(Duration.ofSeconds(30));
    }
}
