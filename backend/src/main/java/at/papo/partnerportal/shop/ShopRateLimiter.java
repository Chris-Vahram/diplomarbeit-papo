package at.papo.partnerportal.shop;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

// In-memory token bucket per client IP (design.md Decision 5). IPs live only in this map,
// are never persisted and are dropped once their bucket is full again. Per instance only.
@ApplicationScoped
public class ShopRateLimiter {

    private static final long MINUTE_NANOS = 60_000_000_000L;
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final LongSupplier clock;

    @Inject
    public ShopRateLimiter(@ConfigProperty(name = "papo.shop.rate-limit.requests-per-minute") int requestsPerMinute) {
        this(requestsPerMinute, System::nanoTime);
    }

    ShopRateLimiter(int requestsPerMinute, LongSupplier clock) {
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
    }

    public boolean tryAcquire(String clientIp) {
        long now = clock.getAsLong();
        if (buckets.size() > CLEANUP_THRESHOLD) {
            buckets.values().removeIf(bucket -> bucket.isFull(now));
        }
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> new Bucket(requestsPerMinute, now));
        return bucket.tryTake(now);
    }

    private final class Bucket {
        private double tokens;
        private long updatedAt;

        Bucket(double tokens, long updatedAt) {
            this.tokens = tokens;
            this.updatedAt = updatedAt;
        }

        synchronized boolean tryTake(long now) {
            refill(now);
            if (tokens < 1) {
                return false;
            }
            tokens -= 1;
            return true;
        }

        synchronized boolean isFull(long now) {
            refill(now);
            return tokens >= requestsPerMinute;
        }

        private void refill(long now) {
            double refill = (now - updatedAt) * (double) requestsPerMinute / MINUTE_NANOS;
            tokens = Math.min(requestsPerMinute, tokens + refill);
            updatedAt = now;
        }
    }
}
