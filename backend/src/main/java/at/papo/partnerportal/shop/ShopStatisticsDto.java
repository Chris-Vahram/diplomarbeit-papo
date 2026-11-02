package at.papo.partnerportal.shop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Partner page "Webshop-Statistik" (shop-statistics spec): the selected period and the
// previous period of equal length, so the page can show the change of every KPI.
public record ShopStatisticsDto(
        LocalDate from,
        LocalDate to,
        LocalDate previousFrom,
        LocalDate previousTo,
        Totals current,
        Totals previous,
        List<Day> days,
        List<ArticleRow> topArticles,
        List<CountRow> referrers,
        List<CountRow> devices) {

    // conversionRate: orders per visit in percent, null without visits. uniqueVisitors and
    // returningVisitors are null when no consenting visitor was counted in the period.
    public record Totals(long visits, long pageViews, long articleViews, long addToCart, long orders,
                         BigDecimal revenue, BigDecimal conversionRate, Long uniqueVisitors, Long returningVisitors) {
    }

    public record Day(LocalDate day, long visits, long pageViews, long orders, BigDecimal revenue) {
    }

    // Articles that no longer exist are left out.
    public record ArticleRow(long articleId, String name, long views, long addToCart) {
    }

    // label: referrer domain ("" = direct or unknown) or device class.
    public record CountRow(String label, long count) {
    }
}
