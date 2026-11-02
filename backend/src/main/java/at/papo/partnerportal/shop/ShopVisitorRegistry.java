package at.papo.partnerportal.shop;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Random visitor ids of consenting visitors, kept in memory for the current day only (design
// decision 2). They answer one question, "already counted today?", and are dropped when the
// day ends; only the resulting counts reach the database. A restart forgets the ids, so a
// visitor can be counted twice that day (accepted, design "Risks"). Capped against floods.
@ApplicationScoped
public class ShopVisitorRegistry {

    static final int MAX_IDS = 200_000;

    private final Set<String> seen = ConcurrentHashMap.newKeySet();
    private volatile LocalDate day;

    // true when this visitor is new for this shop today and should be counted.
    public boolean firstToday(long shopId, String visitorId, LocalDate today) {
        rollOver(today);
        if (seen.size() >= MAX_IDS) {
            return false;
        }
        return seen.add(shopId + ":" + visitorId);
    }

    public int size() {
        return seen.size();
    }

    // Day change without traffic: discard yesterday's ids shortly after midnight anyway.
    @Scheduled(cron = "0 1 0 * * ?", timeZone = ShopStatisticsService.ZONE_ID)
    void discardAfterDay() {
        rollOver(ShopStatisticsService.today());
    }

    synchronized void rollOver(LocalDate today) {
        if (!today.equals(day)) {
            seen.clear();
            day = today;
        }
    }
}
