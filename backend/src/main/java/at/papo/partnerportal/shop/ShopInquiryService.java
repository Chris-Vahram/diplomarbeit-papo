package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

// Shop inquiries (shop-inquiries spec, B-05): public submission with spam protection and the
// partner's inbox. The shop - and with it the partner - comes from the route, never the form.
@ApplicationScoped
public class ShopInquiryService {

    static final int NAME_MAX = 120;
    static final int EMAIL_MAX = 254;
    static final int PHONE_MAX = 40;
    static final int MESSAGE_MAX = 2000;

    // Same answer for honeypot, too-fast and expired forms, so bots learn nothing from it.
    static final String RETRY = "Die Anfrage konnte nicht gesendet werden. Bitte warten Sie kurz und versuchen Sie es erneut.";
    static final String NOT_FOUND = "Anfrage nicht gefunden.";

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()/\\-. ]{4,}$");

    @Inject
    ShopInquiryToken token;

    @Inject
    ShopCatalogService catalogService;

    private final ShopRateLimiter rateLimiter;

    // Stricter than the general public limit: sending is far rarer than reading pages.
    @Inject
    public ShopInquiryService(@ConfigProperty(name = "papo.shop.inquiry-rate-limit.per-minute") int perMinute) {
        this.rateLimiter = new ShopRateLimiter(perMinute, System::nanoTime, ShopRateLimiter.DEFAULT_MAX_TRACKED_IPS);
    }

    public String issueToken(PartnerShop shop) {
        return token.issue(shop.kennung);
    }

    @Transactional
    public void submit(PartnerShop shop, ShopInquiryRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new ApiException(429, "Zu viele Anfragen. Bitte kurz warten.");
        }
        if (!isBlank(request.website()) || !token.isValid(shop.kennung, request.token())) {
            throw new ApiException(400, RETRY);
        }
        String name = clean(request.name());
        String email = clean(request.email());
        String phone = clean(request.phone());
        String message = request.message() == null ? null : request.message().strip();
        if (name == null) {
            throw new ApiException(400, "Bitte geben Sie Ihren Namen an.");
        }
        if (name.length() > NAME_MAX) {
            throw new ApiException(400, "Der Name ist zu lang (maximal " + NAME_MAX + " Zeichen).");
        }
        if (email == null && phone == null) {
            throw new ApiException(400, "Bitte E-Mail oder Telefonnummer angeben.");
        }
        if (email != null && (email.length() > EMAIL_MAX || !EMAIL.matcher(email).matches())) {
            throw new ApiException(400, "Die E-Mail-Adresse ist ungültig.");
        }
        if (phone != null && (phone.length() > PHONE_MAX || !PHONE.matcher(phone).matches())) {
            throw new ApiException(400, "Die Telefonnummer ist ungültig.");
        }
        if (message == null || message.isEmpty()) {
            throw new ApiException(400, "Bitte schreiben Sie eine Nachricht.");
        }
        if (message.length() > MESSAGE_MAX) {
            throw new ApiException(400, "Die Nachricht ist zu lang (maximal " + MESSAGE_MAX + " Zeichen).");
        }
        if (!request.consent()) {
            throw new ApiException(400, "Bitte stimmen Sie der Datenschutzerklärung zu.");
        }
        Product article = null;
        if (request.articleId() != null) {
            article = catalogService.article(shop.partner, request.articleId())
                    .orElseThrow(() -> new ApiException(400, "Dieser Artikel ist nicht verfügbar."));
        }
        ShopInquiry inquiry = new ShopInquiry();
        inquiry.shop = shop;
        inquiry.name = name;
        inquiry.email = email;
        inquiry.phone = phone;
        inquiry.message = message;
        inquiry.article = article;
        inquiry.createdAt = Instant.now();
        inquiry.persist();
    }

    // Open inquiries first, newest first within each group.
    public List<ShopInquiryDto> list(Partner partner) {
        return ShopInquiry.<ShopInquiry>find("shop.partner = ?1 order by case when doneAt is null then 0 else 1 end, createdAt desc, id desc",
                        partner)
                .stream().map(ShopInquiryDto::from).toList();
    }

    public long unreadCount(Partner partner) {
        return ShopInquiry.count("shop.partner = ?1 and readAt is null", partner);
    }

    @Transactional
    public ShopInquiryDto markRead(Partner partner, long id) {
        ShopInquiry inquiry = own(partner, id);
        if (inquiry.readAt == null) {
            inquiry.readAt = Instant.now();
        }
        return ShopInquiryDto.from(inquiry);
    }

    // Done implies read; reopening keeps it read.
    @Transactional
    public ShopInquiryDto setDone(Partner partner, long id, boolean done) {
        ShopInquiry inquiry = own(partner, id);
        Instant now = Instant.now();
        if (inquiry.readAt == null) {
            inquiry.readAt = now;
        }
        inquiry.doneAt = done ? (inquiry.doneAt == null ? now : inquiry.doneAt) : null;
        return ShopInquiryDto.from(inquiry);
    }

    // Permanent: the row is gone, nothing is archived (shop-inquiries "Inquiry data handling").
    @Transactional
    public void delete(Partner partner, long id) {
        own(partner, id).delete();
    }

    // Another partner's inquiry is answered like a missing one.
    private ShopInquiry own(Partner partner, long id) {
        return ShopInquiry.<ShopInquiry>find("id = ?1 and shop.partner = ?2", id, partner).firstResultOptional()
                .orElseThrow(() -> new ApiException(404, NOT_FOUND));
    }

    private static String clean(String value) {
        return isBlank(value) ? null : value.strip();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
