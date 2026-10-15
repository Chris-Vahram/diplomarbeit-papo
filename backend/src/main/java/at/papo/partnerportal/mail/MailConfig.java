package at.papo.partnerportal.mail;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

// papo.mail.* (send-order-emails design "Safety configuration"). Sending is off by default.
@ConfigMapping(prefix = "papo.mail")
public interface MailConfig {

    @WithDefault("false")
    boolean enabled();

    Optional<String> from();

    Optional<String> companyRecipient();

    Optional<String> redirectTo();

    @WithDefault("false")
    boolean allowRealRecipients();

    @WithDefault("false")
    boolean shippingEnabled();

    default OrderEmail.DeliveryMode deliveryMode() {
        return redirectTarget() != null ? OrderEmail.DeliveryMode.REDIRECT : OrderEmail.DeliveryMode.LIVE;
    }

    default String redirectTarget() {
        return redirectTo().map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    }

    // Returns a German error when the configuration must not start, otherwise null.
    static String startupProblem(boolean enabled, Optional<String> from, Optional<String> redirectTo,
                                 boolean allowRealRecipients) {
        if (!enabled) {
            return null;
        }
        if (from.filter(s -> !s.isBlank()).isEmpty()) {
            return "papo.mail.from fehlt, obwohl der E-Mail-Versand aktiviert ist.";
        }
        if (redirectTo.filter(s -> !s.isBlank()).isEmpty() && !allowRealRecipients) {
            return "E-Mail-Versand ohne papo.mail.redirect-to braucht papo.mail.allow-real-recipients=true.";
        }
        return null;
    }
}
