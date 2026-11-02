package at.papo.partnerportal.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// shop-statistics "Anonymous event collection": only domain and device class survive.
class ShopStatReductionTest {

    private static final String IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 "
            + "(KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1";
    private static final String ANDROID_PHONE = "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36";
    private static final String ANDROID_TABLET = "Mozilla/5.0 (Linux; Android 15; SM-X910) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/130.0 Safari/537.36";
    private static final String IPAD = "Mozilla/5.0 (iPad; CPU OS 18_0 like Mac OS X) AppleWebKit/605.1.15";
    private static final String MAC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 "
            + "(KHTML, like Gecko) Version/18.0 Safari/605.1.15";

    @Test
    void referrerIsReducedToItsDomain() {
        assertEquals("example.com", ShopStatReduction.referrerDomain("https://www.example.com/search?q=lampe", "paportal.at"));
        assertEquals("google.at", ShopStatReduction.referrerDomain("https://google.at/", "paportal.at"));
        assertEquals("m.facebook.com", ShopStatReduction.referrerDomain("http://M.Facebook.com/path#frag", null));
    }

    @Test
    void ownHostDirectAndInvalidReferrersAreDirect() {
        assertEquals("", ShopStatReduction.referrerDomain("https://paportal.at/shop/huemer", "paportal.at"));
        assertEquals("", ShopStatReduction.referrerDomain("http://localhost:3000/shop/x", "localhost:8080"));
        assertEquals("", ShopStatReduction.referrerDomain(null, "paportal.at"));
        assertEquals("", ShopStatReduction.referrerDomain("", "paportal.at"));
        assertEquals("", ShopStatReduction.referrerDomain("javascript:alert(1)", "paportal.at"));
        assertEquals("", ShopStatReduction.referrerDomain("not a url", "paportal.at"));
        // IP literals never become a counted value.
        assertEquals("", ShopStatReduction.referrerDomain("http://203.0.113.77/x", "paportal.at"));
        assertEquals("", ShopStatReduction.referrerDomain("http://[2001:db8::1]/x", "paportal.at"));
    }

    @Test
    void deviceClassFromUserAgent() {
        assertEquals("Mobil", ShopStatReduction.deviceClass(IPHONE));
        assertEquals("Mobil", ShopStatReduction.deviceClass(ANDROID_PHONE));
        assertEquals("Tablet", ShopStatReduction.deviceClass(ANDROID_TABLET));
        assertEquals("Tablet", ShopStatReduction.deviceClass(IPAD));
        assertEquals("Desktop", ShopStatReduction.deviceClass(MAC));
    }

    @Test
    void botsAndMissingAgentsAreFiltered() {
        assertTrue(ShopStatReduction.isBot("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));
        assertTrue(ShopStatReduction.isBot("curl/8.7.1"));
        assertTrue(ShopStatReduction.isBot("Mozilla/5.0 HeadlessChrome/130.0"));
        assertTrue(ShopStatReduction.isBot(null));
        assertTrue(ShopStatReduction.isBot(" "));
        assertFalse(ShopStatReduction.isBot(IPHONE));
        assertFalse(ShopStatReduction.isBot(MAC));
    }
}
