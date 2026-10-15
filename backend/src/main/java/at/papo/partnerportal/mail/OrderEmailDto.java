package at.papo.partnerportal.mail;

import java.time.Instant;

public record OrderEmailDto(
        Long id,
        OrderEmail.Type type,
        OrderEmail.RecipientRole recipientRole,
        String maskedAddress,
        OrderEmail.Status status,
        String skipReason,
        int attempts,
        String lastErrorCode,
        Instant createdAt,
        Instant sentAt
) {
    public static OrderEmailDto from(OrderEmail e) {
        return new OrderEmailDto(e.id, e.type, e.recipientRole, mask(e.recipientAddress), e.status,
                e.skipReason, e.attempts, e.lastErrorCode, e.createdAt, e.sentAt);
    }

    // "anna@example.at" -> "a***@example.at"; idempotent for already masked values.
    static String mask(String address) {
        if (address == null) {
            return null;
        }
        int at = address.indexOf('@');
        return at <= 0 ? "***" : address.charAt(0) + "***" + address.substring(at);
    }
}
