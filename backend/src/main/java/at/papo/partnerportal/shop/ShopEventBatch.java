package at.papo.partnerportal.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// One request of the shop's event sender (shop-statistics spec). visitor/returning are only
// present after "Statistik" consent; without it the batch carries no identifier at all.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopEventBatch(String visitor, Boolean returning, List<Event> events) {

    // type: "pageview" (page, entry, referrer), "article" or "cart" (articleId).
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Event(String type, String page, Long articleId, Boolean entry, String referrer) {
    }
}
