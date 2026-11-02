package at.papo.partnerportal.shop;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopInquiryTokenTest {

    @Test
    void acceptsOnlyBetweenThreeSecondsAndOneDay() {
        AtomicLong now = new AtomicLong(1_000_000);
        ShopInquiryToken tokens = new ShopInquiryToken(now::get);
        String token = tokens.issue("huemer");

        now.addAndGet(1_000);
        assertFalse(tokens.isValid("huemer", token), "1 s after rendering is bot speed");
        now.addAndGet(2_000);
        assertTrue(tokens.isValid("huemer", token));
        now.addAndGet(ShopInquiryToken.MAX_AGE_MILLIS);
        assertFalse(tokens.isValid("huemer", token), "older than 24 h");
    }

    @Test
    void rejectsForeignShopTamperingAndGarbage() {
        AtomicLong now = new AtomicLong(1_000_000);
        ShopInquiryToken tokens = new ShopInquiryToken(now::get);
        String token = tokens.issue("huemer");
        now.addAndGet(5_000);

        assertFalse(tokens.isValid("anderer-shop", token));
        // Moving the timestamp back to fake the fill time breaks the signature.
        String backdated = (1_000_000 - 10_000) + token.substring(token.indexOf('.'));
        assertFalse(tokens.isValid("huemer", backdated));
        assertFalse(tokens.isValid("huemer", new ShopInquiryToken(now::get).issue("huemer")));
        assertFalse(tokens.isValid("huemer", null));
        assertFalse(tokens.isValid("huemer", "kein-token"));
        assertFalse(tokens.isValid("huemer", "abc.def"));
    }
}
