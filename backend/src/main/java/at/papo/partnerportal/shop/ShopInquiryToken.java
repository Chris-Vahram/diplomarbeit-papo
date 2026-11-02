package at.papo.partnerportal.shop;

import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.LongSupplier;

// Signed render timestamp for the minimum fill time (add-shop-inquiries design.md Decision 2):
// "<epochMillis>.<HMAC(kennung|epochMillis)>". The server clock decides, never the client's.
// The key is random per process: after a restart open forms get the generic retry message,
// which is acceptable for a spam guard and needs no secret management.
@ApplicationScoped
public class ShopInquiryToken {

    static final long MIN_FILL_MILLIS = 3_000;
    static final long MAX_AGE_MILLIS = 24L * 60 * 60 * 1000;

    private final SecretKeySpec key;
    private final LongSupplier clock;

    public ShopInquiryToken() {
        this(System::currentTimeMillis);
    }

    ShopInquiryToken(LongSupplier clock) {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        this.key = new SecretKeySpec(secret, "HmacSHA256");
        this.clock = clock;
    }

    public String issue(String kennung) {
        long now = clock.getAsLong();
        return now + "." + sign(kennung, now);
    }

    // True only for an untampered token of this shop that is between 3 s and 24 h old.
    public boolean isValid(String kennung, String token) {
        if (token == null) {
            return false;
        }
        int dot = token.indexOf('.');
        if (dot <= 0) {
            return false;
        }
        long issuedAt;
        try {
            issuedAt = Long.parseLong(token.substring(0, dot));
        } catch (NumberFormatException e) {
            return false;
        }
        byte[] expected = sign(kennung, issuedAt).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, token.substring(dot + 1).getBytes(StandardCharsets.US_ASCII))) {
            return false;
        }
        long age = clock.getAsLong() - issuedAt;
        return age >= MIN_FILL_MILLIS && age <= MAX_AGE_MILLIS;
    }

    private String sign(String kennung, long issuedAt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] digest = mac.doFinal((kennung + "|" + issuedAt).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 nicht verfügbar", e);
        }
    }
}
