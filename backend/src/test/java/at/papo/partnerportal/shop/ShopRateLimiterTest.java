package at.papo.partnerportal.shop;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopRateLimiterTest {

    @Test
    void blocksAfterLimitAndRefillsOverTime() {
        AtomicLong now = new AtomicLong();
        ShopRateLimiter limiter = new ShopRateLimiter(3, now::get, 100);

        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertFalse(limiter.tryAcquire("203.0.113.1"));

        now.addAndGet(20_000_000_000L); // 20 s = one token at 3/min
        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertFalse(limiter.tryAcquire("203.0.113.1"));
    }

    @Test
    void limitsEachIpSeparately() {
        ShopRateLimiter limiter = new ShopRateLimiter(1, () -> 0L, 100);

        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertFalse(limiter.tryAcquire("203.0.113.1"));
        assertTrue(limiter.tryAcquire("203.0.113.2"));
    }

    @Test
    void rejectsNewIpsWhileFullAndFreesSlotsAfterSweep() {
        AtomicLong now = new AtomicLong();
        ShopRateLimiter limiter = new ShopRateLimiter(1, now::get, 2);

        assertTrue(limiter.tryAcquire("203.0.113.1"));
        assertTrue(limiter.tryAcquire("203.0.113.2"));
        assertFalse(limiter.tryAcquire("203.0.113.3"));

        now.addAndGet(61_000_000_000L); // buckets refilled, sweep due
        assertTrue(limiter.tryAcquire("203.0.113.3"));
    }
}
