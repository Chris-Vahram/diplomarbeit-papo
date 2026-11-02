package at.papo.partnerportal.shop;

// Public form submission. "website" is the honeypot (hidden from people, left empty by them);
// "token" is the signed render timestamp from GET /api/shop/<kennung>/inquiries/token.
public record ShopInquiryRequest(
        String name,
        String email,
        String phone,
        String message,
        Long articleId,
        boolean consent,
        String website,
        String token
) {
}
