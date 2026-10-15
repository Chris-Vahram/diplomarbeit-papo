package at.papo.partnerportal.shop;

import java.time.Instant;

// Portal view of a shop (company and owning partner). Not used by the public API.
public record PartnerShopDto(
        String kennung,
        String publicPath,
        ShopStatus status,
        Instant releasedAt,
        String offlineReason
) {
    static PartnerShopDto from(PartnerShop shop, ShopStatus status) {
        return new PartnerShopDto(shop.kennung, "/shop/" + shop.kennung, status, shop.releasedAt,
                shop.offlineByCompany ? shop.offlineReason : null);
    }
}
