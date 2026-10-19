package at.papo.partnerportal.shop;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

// In-memory token bucket per client IP (design.md Decision 5). IPs live only in this map,
// are never persisted and are dropped once their bucket is full again. Per instance only.
// Full buckets are swept at most once per minute (not per request), and the map is capped:
// while it is full, unknown IPs are rejected, so a scan over many addresses cannot grow
// memory or per-request cost without bound.
@ApplicationScoped
public class ShopRateLimiter {

    private static final long MINUTE_NANOS = 60_000_000_000L;
    static final int DEFAULT_MAX_TRACKED_IPS = 50_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final LongSupplier clock;
    private final int maxTrackedIps;
    private final AtomicLong nextCleanupAt;

    @Inject
    public ShopRateLimiter(@ConfigProperty(name = "papo.shop.rate-limit.requests-per-minute") int requestsPerMinute) {
        this(requestsPerMinute, System::nanoTime, DEFAULT_MAX_TRACKED_IPS);
    }

    ShopRateLimiter(int requestsPerMinute, LongSupplier clock, int maxTrackedIps) {
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
        this.maxTrackedIps = maxTrackedIps;
        this.nextCleanupAt = new AtomicLong(clock.getAsLong() + MINUTE_NANOS);
    }

    public boolean tryAcquire(String clientIp) {
        long now = clock.getAsLong();
        long dueAt = nextCleanupAt.get();
        if (now - dueAt >= 0 && nextCleanupAt.compareAndSet(dueAt, now + MINUTE_NANOS)) {
            buckets.values().removeIf(bucket -> bucket.isFull(now));
        }
        Bucket bucket = buckets.get(clientIp);
        if (bucket == null) {
            if (buckets.size() >= maxTrackedIps) {
                return false;
            }
            bucket = buckets.computeIfAbsent(clientIp, ip -> new Bucket(requestsPerMinute, now));
        }
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
