package at.papo.partnerportal.shop;

import java.time.Instant;

// Partner inbox entry (shop-inquiries spec "Assignment and inbox").
public record ShopInquiryDto(
        Long id,
        String name,
        String email,
        String phone,
        String message,
        Long articleId,
        String articleName,
        Instant createdAt,
        Instant readAt,
        Instant doneAt
) {
    static ShopInquiryDto from(ShopInquiry inquiry) {
        return new ShopInquiryDto(inquiry.id, inquiry.name, inquiry.email, inquiry.phone, inquiry.message,
                inquiry.article == null ? null : inquiry.article.id,
                inquiry.article == null ? null : inquiry.article.name,
                inquiry.createdAt, inquiry.readAt, inquiry.doneAt);
    }
}
