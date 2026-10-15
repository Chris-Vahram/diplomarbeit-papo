package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PartnerShopService {

    // Same text for unknown and unreachable shops, so the public API does not reveal existence.
    public static final String NOT_REACHABLE = "Dieser Shop ist derzeit nicht erreichbar.";

    @Transactional
    public PartnerShop findOrCreate(Partner partner) {
        PartnerShop existing = PartnerShop.find("partner", partner).firstResult();
        if (existing != null) {
            return existing;
        }
        PartnerShop shop = new PartnerShop();
        shop.partner = partner;
        shop.kennung = ShopKennung.suggest(partner.name, this::isTaken);
        shop.createdAt = Instant.now();
        shop.persist();
        return shop;
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
