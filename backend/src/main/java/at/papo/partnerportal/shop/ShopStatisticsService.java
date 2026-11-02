package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.LegalText;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderOrigin;
import at.papo.partnerportal.product.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Shop statistics (shop-statistics spec, add-shop-statistics). Collection: events are validated
// against the shop, reduced (ShopStatReduction) and only increment daily counters; nothing of the
// request itself (IP, user-agent, URLs) is stored or logged. Reading: own shop only, selected
// period plus the previous period of equal length.
@ApplicationScoped
public class ShopStatisticsService {

    static final String ZONE_ID = "Europe/Vienna";
    static final ZoneId ZONE = ZoneId.of(ZONE_ID);
    static final int MAX_EVENTS = 20;
    static final int MAX_BODY = 16_384;
    static final int MAX_PERIOD_DAYS = 366;
    static final int TOP_ARTICLES = 10;
    static final int TOP_REFERRERS = 10;

    // System segments that are pages of every shop without a ShopPage of their own.
    private static final Set<String> SYSTEM_PAGES = Set.of("warenkorb", "kasse");
    private static final Pattern SEGMENT = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final Pattern VISITOR = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
    private static final String UPSERT = "insert into shop_stat_daily (shop_id, stat_day, metric, dimension, amount) "
            + "values (?1, ?2, ?3, ?4, ?5) on conflict on constraint " + ShopStatDaily.UNIQUE_KEY
            + " do update set amount = shop_stat_daily.amount + excluded.amount";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ShopPageService pageService;

    @Inject
    ShopCatalogService catalogService;

    @Inject
    ShopVisitorRegistry visitors;

    static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    private record Key(ShopStatMetric metric, String dimension) {
    }

    // Invalid batches and events are dropped silently (shop-statistics "Event abuse limits").
    @Transactional
    public void record(PartnerShop shop, String body, String userAgent, String ownHost) {
        if (body == null || body.length() > MAX_BODY || ShopStatReduction.isBot(userAgent)) {
            return;
        }
        ShopEventBatch batch;
        try {
            batch = objectMapper.readValue(body, ShopEventBatch.class);
        } catch (Exception invalid) {
            return;
        }
        if (batch == null || batch.events() == null || batch.events().isEmpty() || batch.events().size() > MAX_EVENTS) {
            return;
        }
        LocalDate today = today();
        Map<Key, Long> counts = new TreeMap<>(Comparator.comparing((Key key) -> key.metric()).thenComparing(Key::dimension));
        Map<String, Boolean> pages = new HashMap<>();
        Map<Long, Boolean> articles = new HashMap<>();
        boolean valid = false;
        for (ShopEventBatch.Event event : batch.events()) {
            if (event == null || event.type() == null) {
                continue;
            }
            switch (event.type()) {
                case "pageview" -> {
                    String page = event.page();
                    if (page == null || !pages.computeIfAbsent(page, segment -> isShopPage(shop, segment))) {
                        continue;
                    }
                    counts.merge(new Key(ShopStatMetric.PAGE_VIEW, page), 1L, Long::sum);
                    if (Boolean.TRUE.equals(event.entry())) {
                        counts.merge(new Key(ShopStatMetric.VISIT, ""), 1L, Long::sum);
                        counts.merge(new Key(ShopStatMetric.REFERRER,
                                ShopStatReduction.referrerDomain(event.referrer(), ownHost)), 1L, Long::sum);
                        counts.merge(new Key(ShopStatMetric.DEVICE, ShopStatReduction.deviceClass(userAgent)), 1L, Long::sum);
                    }
                    valid = true;
                }
                case "article", "cart" -> {
                    Long id = event.articleId();
                    if (id == null || id <= 0 || !articles.computeIfAbsent(id,
                            articleId -> catalogService.isCatalogueArticle(shop.partner, articleId))) {
                        continue;
                    }
                    ShopStatMetric metric = event.type().equals("article") ? ShopStatMetric.ARTICLE_VIEW : ShopStatMetric.ADD_TO_CART;
                    counts.merge(new Key(metric, String.valueOf(id)), 1L, Long::sum);
                    valid = true;
                }
                default -> {
                    // unknown type: ignored
                }
            }
        }
        // The id only decides "counted today?" in memory; it never reaches the database.
        if (valid && batch.visitor() != null && VISITOR.matcher(batch.visitor()).matches()
                && visitors.firstToday(shop.id, batch.visitor(), today)) {
            counts.merge(new Key(ShopStatMetric.UNIQUE_VISITOR, ""), 1L, Long::sum);
            if (Boolean.TRUE.equals(batch.returning())) {
                counts.merge(new Key(ShopStatMetric.RETURNING_VISITOR, ""), 1L, Long::sum);
            }
        }
        // Sorted keys: concurrent batches lock rows in the same order.
        counts.forEach((key, amount) -> ShopStatDaily.getEntityManager().createNativeQuery(UPSERT)
                .setParameter(1, shop.id)
                .setParameter(2, today)
                .setParameter(3, key.metric().name())
                .setParameter(4, key.dimension())
                .setParameter(5, amount)
                .executeUpdate());
    }

    private boolean isShopPage(PartnerShop shop, String segment) {
        if (segment.length() > 60 || !SEGMENT.matcher(segment).matches()) {
            return false;
        }
        return SYSTEM_PAGES.contains(segment) || LegalText.bySegment(segment).isPresent()
                || pageService.onlinePage(shop, segment).isPresent();
    }

    // Defaults to the current month up to today (Protokoll 12.10.: "Dieses Monat" as default).
    public ShopStatisticsDto statistics(PartnerShop shop, LocalDate from, LocalDate to) {
        LocalDate today = today();
        LocalDate start = from == null ? today.withDayOfMonth(1) : from;
        LocalDate end = to == null ? today : to;
        if (start.isAfter(end) || ChronoUnit.DAYS.between(start, end) >= MAX_PERIOD_DAYS) {
            throw new ApiException(400, "Bitte einen gültigen Zeitraum von höchstens einem Jahr wählen.");
        }
        long length = ChronoUnit.DAYS.between(start, end);
        LocalDate previousTo = start.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(length);

        Map<LocalDate, long[]> eventsByDay = eventsByDay(shop, start, end);
        Map<LocalDate, Object[]> ordersByDay = ordersByDay(shop, start, end);
        List<ShopStatisticsDto.Day> days = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            long[] events = eventsByDay.getOrDefault(day, new long[2]);
            Object[] orders = ordersByDay.get(day);
            days.add(new ShopStatisticsDto.Day(day, events[0], events[1],
                    orders == null ? 0 : (long) orders[0], orders == null ? BigDecimal.ZERO : (BigDecimal) orders[1]));
        }

        return new ShopStatisticsDto(start, end, previousFrom, previousTo,
                totals(shop, start, end), totals(shop, previousFrom, previousTo), days,
                topArticles(shop, start, end),
                countRows(shop, ShopStatMetric.REFERRER, start, end, TOP_REFERRERS),
                countRows(shop, ShopStatMetric.DEVICE, start, end, 3));
    }

    private ShopStatisticsDto.Totals totals(PartnerShop shop, LocalDate from, LocalDate to) {
        Map<ShopStatMetric, Long> sums = ShopStatDaily.getEntityManager().createQuery(
                        "select s.metric, sum(s.amount) from ShopStatDaily s where s.shop = ?1 and s.day between ?2 and ?3 "
                                + "group by s.metric", Object[].class)
                .setParameter(1, shop).setParameter(2, from).setParameter(3, to)
                .getResultList().stream()
                .collect(Collectors.toMap(row -> (ShopStatMetric) row[0], row -> ((Number) row[1]).longValue()));
        Object[] orders = Order.getEntityManager().createQuery(
                        "select count(o), coalesce(sum(o.orderValue), 0) from Order o where o.partner = ?1 and o.origin = ?2 "
                                + "and o.orderDate between ?3 and ?4", Object[].class)
                .setParameter(1, shop.partner).setParameter(2, OrderOrigin.SHOP)
                .setParameter(3, from).setParameter(4, to)
                .getSingleResult();
        long visits = sums.getOrDefault(ShopStatMetric.VISIT, 0L);
        long orderCount = ((Number) orders[0]).longValue();
        BigDecimal conversion = visits == 0 ? null : BigDecimal.valueOf(orderCount * 100L)
                .divide(BigDecimal.valueOf(visits), 1, RoundingMode.HALF_UP);
        return new ShopStatisticsDto.Totals(visits,
                sums.getOrDefault(ShopStatMetric.PAGE_VIEW, 0L),
                sums.getOrDefault(ShopStatMetric.ARTICLE_VIEW, 0L),
                sums.getOrDefault(ShopStatMetric.ADD_TO_CART, 0L),
                orderCount, toMoney(orders[1]), conversion,
                sums.get(ShopStatMetric.UNIQUE_VISITOR),
                sums.containsKey(ShopStatMetric.UNIQUE_VISITOR) ? sums.getOrDefault(ShopStatMetric.RETURNING_VISITOR, 0L) : null);
    }

    // [visits, pageViews] per day.
    private Map<LocalDate, long[]> eventsByDay(PartnerShop shop, LocalDate from, LocalDate to) {
        Map<LocalDate, long[]> result = new HashMap<>();
        for (Object[] row : ShopStatDaily.getEntityManager().createQuery(
                        "select s.day, s.metric, sum(s.amount) from ShopStatDaily s where s.shop = ?1 and s.day between ?2 and ?3 "
                                + "and s.metric in ?4 group by s.day, s.metric", Object[].class)
                .setParameter(1, shop).setParameter(2, from).setParameter(3, to)
                .setParameter(4, List.of(ShopStatMetric.VISIT, ShopStatMetric.PAGE_VIEW))
                .getResultList()) {
            long[] values = result.computeIfAbsent((LocalDate) row[0], day -> new long[2]);
            values[row[1] == ShopStatMetric.VISIT ? 0 : 1] = ((Number) row[2]).longValue();
        }
        return result;
    }

    // [count, revenue] of shop orders per day.
    private Map<LocalDate, Object[]> ordersByDay(PartnerShop shop, LocalDate from, LocalDate to) {
        return Order.getEntityManager().createQuery(
                        "select o.orderDate, count(o), sum(o.orderValue) from Order o where o.partner = ?1 and o.origin = ?2 "
                                + "and o.orderDate between ?3 and ?4 group by o.orderDate", Object[].class)
                .setParameter(1, shop.partner).setParameter(2, OrderOrigin.SHOP)
                .setParameter(3, from).setParameter(4, to)
                .getResultList().stream()
                .collect(Collectors.toMap(row -> (LocalDate) row[0],
                        row -> new Object[] {((Number) row[1]).longValue(), toMoney(row[2])}));
    }

    private Map<String, Long> sumByDimension(PartnerShop shop, ShopStatMetric metric, LocalDate from, LocalDate to) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : ShopStatDaily.getEntityManager().createQuery(
                        "select s.dimension, sum(s.amount) from ShopStatDaily s where s.shop = ?1 and s.metric = ?2 "
                                + "and s.day between ?3 and ?4 group by s.dimension order by sum(s.amount) desc, s.dimension",
                        Object[].class)
                .setParameter(1, shop).setParameter(2, metric).setParameter(3, from).setParameter(4, to)
                .getResultList()) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    private List<ShopStatisticsDto.CountRow> countRows(PartnerShop shop, ShopStatMetric metric,
                                                       LocalDate from, LocalDate to, int limit) {
        return sumByDimension(shop, metric, from, to).entrySet().stream()
                .limit(limit)
                .map(entry -> new ShopStatisticsDto.CountRow(entry.getKey(), entry.getValue()))
                .toList();
    }

    // Most viewed articles; an article only put into the cart (e.g. from a list) counts too.
    private List<ShopStatisticsDto.ArticleRow> topArticles(PartnerShop shop, LocalDate from, LocalDate to) {
        Map<String, Long> views = sumByDimension(shop, ShopStatMetric.ARTICLE_VIEW, from, to);
        Map<String, Long> carts = sumByDimension(shop, ShopStatMetric.ADD_TO_CART, from, to);
        Set<Long> ids = new HashSet<>();
        for (String dimension : views.keySet()) {
            parseId(dimension, ids);
        }
        for (String dimension : carts.keySet()) {
            parseId(dimension, ids);
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, String> names = Product.<Product>list("id in ?1 and company = ?2", ids, shop.partner.company).stream()
                .collect(Collectors.toMap(product -> product.id, product -> product.name));
        Function<Long, ShopStatisticsDto.ArticleRow> row = id -> new ShopStatisticsDto.ArticleRow(id, names.get(id),
                views.getOrDefault(String.valueOf(id), 0L), carts.getOrDefault(String.valueOf(id), 0L));
        return ids.stream()
                .map(row)
                .filter(entry -> Objects.nonNull(entry.name()))
                .sorted(Comparator.comparingLong(ShopStatisticsDto.ArticleRow::views).reversed()
                        .thenComparing(Comparator.comparingLong(ShopStatisticsDto.ArticleRow::addToCart).reversed())
                        .thenComparing(ShopStatisticsDto.ArticleRow::name))
                .limit(TOP_ARTICLES)
                .toList();
    }

    private static void parseId(String dimension, Set<Long> ids) {
        try {
            ids.add(Long.parseLong(dimension));
        } catch (NumberFormatException ignored) {
            // not an article dimension
        }
    }

    private static BigDecimal toMoney(Object value) {
        BigDecimal amount = value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
