package at.papo.partnerportal.shop;

import java.util.List;

// Public checkout (shop-checkout spec). Only article ids and quantities are taken from the
// client; any price field sent along is not even mapped. "website" is the honeypot, the
// idempotency key is a UUID the browser generates per checkout attempt.
public record ShopOrderRequest(
        String idempotencyKey,
        Customer customer,
        List<Item> items,
        String website
) {
    public record Customer(
            String lastName,
            String firstName,
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String country,
            String email,
            String phone
    ) {
    }

    public record Item(Long articleId, Integer quantity) {
    }
}
