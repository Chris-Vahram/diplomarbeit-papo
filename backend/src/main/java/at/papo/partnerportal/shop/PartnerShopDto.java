package at.papo.partnerportal.shop;

import java.time.Instant;

// Portal view of a shop (company and owning partner). Not used by the public API.
// publicEnabled = deployment-wide switch; false means even an ONLINE shop is not reachable.
public record PartnerShopDto(
        String kennung,
        String publicPath,
        ShopStatus status,
        Instant releasedAt,
        String offlineReason,
        boolean publicEnabled
) {
    static PartnerShopDto from(PartnerShop shop, ShopStatus status, boolean publicEnabled) {
        return new PartnerShopDto(shop.kennung, "/shop/" + shop.kennung, status, shop.releasedAt,
                shop.offlineByCompany ? shop.offlineReason : null, publicEnabled);
    }
}
