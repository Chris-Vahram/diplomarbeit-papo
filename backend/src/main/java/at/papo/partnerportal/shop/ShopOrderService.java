package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.AddressRequest;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeOrigin;
import at.papo.partnerportal.customer.KundeRequest;
import at.papo.partnerportal.customer.KundeService;
import at.papo.partnerportal.mail.OrderEmailService;
import at.papo.partnerportal.order.OrderItemRequest;
import at.papo.partnerportal.order.OrderOrigin;
import at.papo.partnerportal.order.OrderService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

// Guest checkout in a partner shop (shop-checkout spec, B-01). The partner comes only from the
// shop route; prices, tax rates, enablement and availability are read on the server and the
// order goes through the same core as portal orders (OrderService.createOrder).
@ApplicationScoped
public class ShopOrderService {

    static final Duration KEY_LIFETIME = Duration.ofHours(24);
    static final int MAX_LINES = 50;
    static final int TEXT_MAX = 120;
    static final int EMAIL_MAX = 254;
    static final int PHONE_MAX = 40;

    // Same answer for honeypot and malformed submissions, so bots learn nothing from it.
    static final String RETRY = "Die Bestellung konnte nicht gesendet werden. Bitte laden Sie die Seite neu und versuchen Sie es erneut.";
    static final String UNAVAILABLE = "Ein Artikel in Ihrem Warenkorb ist nicht mehr verfügbar. Bitte prüfen Sie den Warenkorb.";

    private static final Pattern KEY = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()/\\-. ]{4,}$");

    @Inject
    OrderService orderService;

    @Inject
    KundeService kundeService;

    @Inject
    OrderEmailService orderEmailService;

    @Inject
    ShopCatalogService catalogService;

    private final ShopRateLimiter rateLimiter;

    // Stricter than the general public limit: ordering is far rarer than reading pages.
    @Inject
    public ShopOrderService(@ConfigProperty(name = "papo.shop.order-rate-limit.per-minute") int perMinute) {
        this.rateLimiter = new ShopRateLimiter(perMinute, System::nanoTime, ShopRateLimiter.DEFAULT_MAX_TRACKED_IPS);
    }

    public ShopOrderConfirmationDto submit(PartnerShop shop, ShopOrderRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new ApiException(429, "Zu viele Bestellungen. Bitte kurz warten.");
        }
        if (request == null || !isBlank(request.website()) || request.idempotencyKey() == null
                || !KEY.matcher(request.idempotencyKey()).matches()) {
            throw new ApiException(400, RETRY);
        }
        String key = request.idempotencyKey().toLowerCase();
        Optional<ShopOrderConfirmationDto> previous = previous(shop.id, key);
        if (previous.isPresent()) {
            return previous.get();
        }
        KundeRequest customer = customer(request.customer());
        List<OrderItemRequest> items = items(request.items());
        try {
            return QuarkusTransaction.requiringNew().call(() -> create(shop.id, key, customer, items));
        } catch (PersistenceException e) {
            // A concurrent submit with the same key won the unique constraint: answer with its order.
            return previous(shop.id, key).orElseThrow(() -> e);
        }
    }

    private ShopOrderConfirmationDto create(long shopId, String key, KundeRequest customer, List<OrderItemRequest> items) {
        PartnerShop shop = PartnerShop.findById(shopId);
        // Stale key of the same shop past its lifetime (cleanup not yet run): free it for reuse.
        ShopOrderKey.delete("shop = ?1 and requestKey = ?2 and createdAt < ?3", shop, key, Instant.now().minus(KEY_LIFETIME));
        // Cart lines that left the catalogue get a dedicated answer, so the cart can mark them.
        for (OrderItemRequest item : items) {
            if (!catalogService.isCatalogueArticle(shop.partner, item.productId())) {
                throw new ApiException(409, UNAVAILABLE);
            }
        }
        Kunde kunde = kundeService.createEntity(shop.partner, customer, KundeOrigin.SHOP);
        OrderService.CreatedOrder created = orderService.createOrder(shop.partner, kunde, items, OrderOrigin.SHOP);
        ShopOrderKey entry = new ShopOrderKey();
        entry.shop = shop;
        entry.requestKey = key;
        entry.order = created.order();
        entry.createdAt = Instant.now();
        entry.persist();
        // Flush inside the transaction: a concurrent duplicate fails here on the unique key and
        // rolls back its whole order instead of creating a second one.
        ShopOrderKey.flush();
        return ShopOrderConfirmationDto.from(created.order(), created.customerEmailNotice());
    }

    private Optional<ShopOrderConfirmationDto> previous(long shopId, String key) {
        return QuarkusTransaction.requiringNew().call(() -> ShopOrderKey
                .<ShopOrderKey>find("shop.id = ?1 and requestKey = ?2 and createdAt >= ?3",
                        shopId, key, Instant.now().minus(KEY_LIFETIME))
                .firstResultOptional()
                .map(entry -> ShopOrderConfirmationDto.from(entry.order,
                        orderEmailService.customerConfirmationNotice(entry.order))));
    }

    // Keys only need to live 24 hours (design D2).
    @Scheduled(every = "1h", delayed = "5m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cleanup() {
        removeExpiredKeys();
    }

    public long removeExpiredKeys() {
        return QuarkusTransaction.requiringNew().call(
                () -> ShopOrderKey.delete("createdAt < ?1", Instant.now().minus(KEY_LIFETIME)));
    }

    private static KundeRequest customer(ShopOrderRequest.Customer customer) {
        if (customer == null) {
            throw new ApiException(400, RETRY);
        }
        String lastName = required(customer.lastName(), "Bitte einen Nachnamen angeben.");
        String street = required(customer.street(), "Bitte eine Straße angeben.");
        String houseNumber = required(customer.houseNumber(), "Bitte eine Hausnummer angeben.");
        String postalCode = required(customer.postalCode(), "Bitte eine Postleitzahl angeben.");
        String city = required(customer.city(), "Bitte einen Ort angeben.");
        String country = clean(customer.country());
        String email = required(customer.email(), "Bitte eine E-Mail-Adresse angeben.");
        String firstName = clean(customer.firstName());
        String phone = clean(customer.phone());
        for (String value : new String[] {lastName, street, houseNumber, postalCode, city, country, firstName}) {
            if (value != null && value.length() > TEXT_MAX) {
                throw new ApiException(400, "Eine Angabe ist zu lang (maximal " + TEXT_MAX + " Zeichen).");
            }
        }
        if (email.length() > EMAIL_MAX || !EMAIL.matcher(email).matches()) {
            throw new ApiException(400, "Die E-Mail-Adresse ist ungültig.");
        }
        if (phone != null && (phone.length() > PHONE_MAX || !PHONE.matcher(phone).matches())) {
            throw new ApiException(400, "Die Telefonnummer ist ungültig.");
        }
        return new KundeRequest(lastName,
                new AddressRequest(street, houseNumber, postalCode, city, country == null ? "Österreich" : country),
                firstName, email, phone);
    }

    // Article ids and quantities only; the existing portal quantity bound applies (1..10000).
    private static List<OrderItemRequest> items(List<ShopOrderRequest.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new ApiException(400, "Ihr Warenkorb ist leer.");
        }
        if (items.size() > MAX_LINES) {
            throw new ApiException(400, "Zu viele Positionen (maximal " + MAX_LINES + ").");
        }
        Set<Long> seen = new HashSet<>();
        return items.stream().map(item -> {
            if (item == null || item.articleId() == null || !seen.add(item.articleId())) {
                throw new ApiException(400, RETRY);
            }
            if (item.quantity() == null || item.quantity() < 1 || item.quantity() > OrderItemRequest.MAX_QUANTITY) {
                throw new ApiException(400, "Die Menge muss zwischen 1 und " + OrderItemRequest.MAX_QUANTITY + " liegen.");
            }
            return new OrderItemRequest(item.articleId(), item.quantity());
        }).toList();
    }

    private static String required(String value, String message) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new ApiException(400, message);
        }
        return cleaned;
    }

    private static String clean(String value) {
        return isBlank(value) ? null : value.strip();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
