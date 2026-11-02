package at.papo.partnerportal.mail;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.mail.OrderEmail.RecipientRole;
import at.papo.partnerportal.mail.OrderEmail.Status;
import at.papo.partnerportal.mail.OrderEmail.Type;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.security.CurrentCompany;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

// Queues order mails inside the caller's order transaction (send-order-emails design
// "Transactional outbox") and serves the company status/retry operations.
@ApplicationScoped
public class OrderEmailService {

    // Simple syntax check only; the mail server decides about deliverability.
    private static final Pattern ADDRESS = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Inject
    MailConfig config;

    @Inject
    CurrentCompany currentCompany;

    // Returns the customer notice for the order response (QUEUED, REDIRECTED, NO_ADDRESS,
    // INVALID_ADDRESS, DISABLED).
    public String queueForNewOrder(Order order) {
        OrderEmail customer = queue(order, Type.ORDER_CONFIRMATION, RecipientRole.CUSTOMER);
        queue(order, Type.ORDER_NOTICE_PARTNER, RecipientRole.PARTNER);
        queue(order, Type.ORDER_NOTICE_COMPANY, RecipientRole.COMPANY);
        return notice(customer);
    }

    // Notice of an already queued order confirmation, e.g. for a repeated shop checkout.
    public String customerConfirmationNotice(Order order) {
        return notice(OrderEmail.find("order = ?1 and type = ?2 and recipientRole = ?3",
                order, Type.ORDER_CONFIRMATION, RecipientRole.CUSTOMER).firstResult());
    }

    public String queueShipping(Order order) {
        if (!config.shippingEnabled()) {
            return "DISABLED";
        }
        return notice(queue(order, Type.SHIPPING_CONFIRMATION, RecipientRole.CUSTOMER));
    }

    public List<OrderEmailDto> listForOrder(Long orderId) {
        findOwnOrder(orderId);
        return OrderEmail.<OrderEmail>find("order.id = ?1 order by id", orderId).stream()
                .map(OrderEmailDto::from)
                .toList();
    }

    @Transactional
    public OrderEmailDto retry(Long orderId, Long emailId) {
        findOwnOrder(orderId);
        OrderEmail email = OrderEmail.findById(emailId);
        if (email == null || !email.order.id.equals(orderId)) {
            throw new ApiException(404, "E-Mail nicht gefunden.");
        }
        if (!config.enabled()) {
            throw new ApiException(409, "Der E-Mail-Versand ist nicht aktiviert.");
        }
        boolean recipientChanged = email.status == Status.SKIPPED && "RECIPIENT_CHANGED".equals(email.skipReason);
        if (email.status != Status.FAILED && !recipientChanged) {
            throw new ApiException(409, "Nur fehlgeschlagene E-Mails können erneut gesendet werden.");
        }
        String address = recipientChanged ? normalize(currentAddress(email.order, email.recipientRole)) : email.recipientAddress;
        if (!isValid(address)) {
            throw new ApiException(409, "Für diesen Empfänger ist keine gültige E-Mail-Adresse hinterlegt.");
        }
        // Conditional update: two concurrent retries queue the mail only once.
        int updated = OrderEmail.update("status = ?1, attempts = 0, nextAttemptAt = ?2, lastErrorCode = null, "
                        + "skipReason = null, claimedAt = null, recipientAddress = ?3, deliveryMode = ?4, redirectTo = ?5 "
                        + "where id = ?6 and status = ?7",
                Status.PENDING, Instant.now(), address, config.deliveryMode(), config.redirectTarget(), emailId, email.status);
        if (updated == 0) {
            throw new ApiException(409, "Die E-Mail wurde bereits erneut eingereiht.");
        }
        OrderEmail.getEntityManager().refresh(email);
        return OrderEmailDto.from(email);
    }

    String currentAddress(Order order, RecipientRole role) {
        return switch (role) {
            case CUSTOMER -> order.kunde.email;
            case PARTNER -> order.partner.email;
            case COMPANY -> order.partner.company.orderNoticeEmail;
        };
    }

    static String normalize(String address) {
        return address == null || address.isBlank() ? null : address.trim();
    }

    static boolean isValid(String address) {
        return address != null && address.length() <= 254 && ADDRESS.matcher(address).matches();
    }

    private OrderEmail queue(Order order, Type type, RecipientRole role) {
        OrderEmail existing = OrderEmail.find("order = ?1 and type = ?2 and recipientRole = ?3", order, type, role).firstResult();
        if (existing != null) {
            return existing;
        }
        String address = normalize(currentAddress(order, role));
        // A customer without address simply gets no mail; a company without recipient keeps a
        // visible SKIPPED row so the omission is explained in the order details (design D6).
        if (address == null && role != RecipientRole.COMPANY) {
            return null;
        }
        OrderEmail email = new OrderEmail();
        email.order = order;
        email.type = type;
        email.recipientRole = role;
        email.deliveryMode = config.deliveryMode();
        email.redirectTo = config.redirectTarget();
        if (address == null) {
            email.status = Status.SKIPPED;
            email.skipReason = "NO_RECIPIENT";
        } else if (!config.enabled()) {
            email.status = Status.SKIPPED;
            email.skipReason = "DISABLED";
        } else if (!isValid(address)) {
            email.status = Status.SKIPPED;
            email.skipReason = "INVALID_ADDRESS";
        } else {
            email.status = Status.PENDING;
            email.recipientAddress = address;
            email.nextAttemptAt = Instant.now();
        }
        email.persist();
        return email;
    }

    private static String notice(OrderEmail customer) {
        if (customer == null) {
            return "NO_ADDRESS";
        }
        if (customer.status == Status.SKIPPED) {
            return "DISABLED".equals(customer.skipReason) ? "DISABLED" : "INVALID_ADDRESS";
        }
        return customer.deliveryMode == OrderEmail.DeliveryMode.REDIRECT ? "REDIRECTED" : "QUEUED";
    }

    // Company-side: mails of another company's order behave like a missing order.
    private void findOwnOrder(Long orderId) {
        if (Order.count("id = ?1 and partner.company = ?2", orderId, currentCompany.resolve()) == 0) {
            throw new ApiException(404, "Bestellung nicht gefunden.");
        }
    }
}
