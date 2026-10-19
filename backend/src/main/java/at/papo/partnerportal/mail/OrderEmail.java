package at.papo.partnerportal.mail;

import at.papo.partnerportal.order.Order;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

// Transactional outbox row (send-order-emails design): written in the same transaction as the
// order change, sent later by OrderEmailSender. The unique key prevents duplicate mails per
// order, type and recipient role.
@Entity
@Table(name = "order_email", uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "type", "recipientRole"}))
public class OrderEmail extends PanacheEntity {

    public enum Type { ORDER_CONFIRMATION, ORDER_NOTICE_PARTNER, ORDER_NOTICE_COMPANY, SHIPPING_CONFIRMATION }

    public enum RecipientRole { CUSTOMER, PARTNER, COMPANY }

    public enum Status { PENDING, SENDING, SENT, FAILED, SKIPPED }

    public enum DeliveryMode { LIVE, REDIRECT }

    @ManyToOne(optional = false)
    public Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RecipientRole recipientRole;

    // Full address only while it may still be needed (pending/failed); masked once sent,
    // null for rows skipped at creation (data minimisation, design "Privacy").
    public String recipientAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DeliveryMode deliveryMode;

    // Test inbox snapshot for REDIRECT rows; a changed inbox counts as a changed configuration.
    public String redirectTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status;

    // DISABLED, INVALID_ADDRESS, CONFIG_CHANGED, RECIPIENT_CHANGED
    public String skipReason;

    @Column(nullable = false)
    public int attempts;

    public Instant nextAttemptAt;

    public Instant claimedAt;

    // Allowlisted codes only, never raw exception text (no addresses in the database log trail).
    public String lastErrorCode;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    public Instant sentAt;
}
