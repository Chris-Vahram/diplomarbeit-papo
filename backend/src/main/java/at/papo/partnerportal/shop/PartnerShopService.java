package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PartnerShopService {

    private static final Logger LOG = Logger.getLogger(PartnerShopService.class);
    private static final int CREATE_ATTEMPTS = 3;

    // Same text for unknown and unreachable shops, so the public API does not reveal existence.
    public static final String NOT_REACHABLE = "Dieser Shop ist derzeit nicht erreichbar.";

    // The record is created lazily from plain GETs, so company and partner may race on the
    // first visit, and two equally named partners may race on the same identifier. The insert
    // therefore runs in its own transaction; a unique-constraint loser re-reads (and, on an
    // identifier clash, retries with the next suffix) instead of surfacing a 500.
    public PartnerShop findOrCreate(Partner partner) {
        for (int attempt = 0; attempt < CREATE_ATTEMPTS; attempt++) {
            PartnerShop existing = PartnerShop.find("partner", partner).firstResult();
            if (existing != null) {
                return existing;
            }
            try {
                QuarkusTransaction.requiringNew().run(() -> create(partner.id));
            } catch (RuntimeException exception) {
                if (!isUniqueViolation(exception)) {
                    throw exception;
                }
                LOG.debugf("Concurrent shop creation for partner %d, re-reading", partner.id);
            }
        }
        PartnerShop created = PartnerShop.find("partner", partner).firstResult();
        if (created == null) {
            throw new ApiException(409, "Der Webshop konnte gerade nicht angelegt werden. Bitte erneut versuchen.");
        }
        return created;
    }

    private void create(Long partnerId) {
        Partner partner = Partner.findById(partnerId);
        PartnerShop shop = new PartnerShop();
        shop.partner = partner;
        shop.kennung = ShopKennung.suggest(partner.name, this::isTaken);
        shop.createdAt = Instant.now();
        shop.persist();
    }

    private static boolean isUniqueViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException) {
                return true;
            }
        }
        return false;
    }

    public ShopStatus status(PartnerShop shop) {
        return status(shop, hasAvailableArticles(shop.partner));
    }

    private ShopStatus status(PartnerShop shop, boolean hasAvailableArticles) {
        if (shop.releasedAt == null) {
            return ShopStatus.NOT_RELEASED;
        }
        if (shop.offlineByCompany) {
            return ShopStatus.OFFLINE_COMPANY;
        }
        if (!shop.partner.active) {
            return ShopStatus.OFFLINE_INACTIVE;
        }
        if (!hasAvailableArticles) {
            return ShopStatus.OFFLINE_NO_ARTICLES;
        }
        return ShopStatus.ONLINE;
    }

    public PartnerShopDto toDto(PartnerShop shop) {
        return PartnerShopDto.from(shop, status(shop));
    }

    @Transactional
    public PartnerShopDto changeKennung(Partner partner, String input) {
        PartnerShop shop = findOrCreate(partner);
        String kennung = ShopKennung.requireValid(input);
        if (!kennung.equals(shop.kennung)) {
            if (isTaken(kennung)) {
                throw new ApiException(409, "Diese Kennung ist bereits vergeben.");
            }
            shop.kennung = kennung;
        }
        return toDto(shop);
    }

    // First release and re-release after a takedown; re-release clears the reason.
    @Transactional
    public PartnerShopDto release(Partner partner) {
        PartnerShop shop = findOrCreate(partner);
        if (shop.releasedAt == null) {
            shop.releasedAt = Instant.now();
        }
        shop.offlineByCompany = false;
        shop.offlineReason = null;
        return toDto(shop);
    }

    @Transactional
    public PartnerShopDto takeOffline(Partner partner, String reason) {
        PartnerShop shop = findOrCreate(partner);
        if (shop.releasedAt == null) {
            throw new ApiException(409, "Der Shop ist noch nicht freigegeben.");
        }
        shop.offlineByCompany = true;
        shop.offlineReason = reason.strip();
        return toDto(shop);
    }

    // Company partner table: one row per existing shop, status computed with two queries
    // instead of one enablement count per partner. Partners without a record have no shop yet.
    public List<CompanyShopOverviewDto> overview() {
        Set<Long> withArticles = new HashSet<>(ProductEnablement.getEntityManager().createQuery(
                "select distinct e.partner.id from ProductEnablement e where e.product.available = true", Long.class)
                .getResultList());
        return PartnerShop.<PartnerShop>find("select s from PartnerShop s join fetch s.partner").list().stream()
                .map(shop -> new CompanyShopOverviewDto(shop.partner.id, shop.kennung,
                        status(shop, withArticles.contains(shop.partner.id))))
                .toList();
    }

    // Public lookup: present only for released shops that are currently online.
    public Optional<PartnerShop> findOnline(String kennung) {
        if (!ShopKennung.isValid(kennung)) {
            return Optional.empty();
        }
        return PartnerShop.<PartnerShop>find("kennung", kennung).firstResultOptional()
                .filter(shop -> status(shop) == ShopStatus.ONLINE);
    }

    boolean hasAvailableArticles(Partner partner) {
        return ProductEnablement.count("partner = ?1 and product.available = true", partner) > 0;
    }

    private boolean isTaken(String kennung) {
        return PartnerShop.count("kennung", kennung) > 0;
    }
}
