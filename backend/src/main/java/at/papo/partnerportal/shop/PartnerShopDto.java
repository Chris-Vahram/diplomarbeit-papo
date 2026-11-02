package at.papo.partnerportal.shop;

import java.time.Instant;

// Portal view of a shop (company and owning partner). Not used by the public API.
// publicEnabled = deployment-wide switch; false means even an ONLINE shop is not reachable.
// missingLegalTexts = company legal texts still shown as placeholder; legalGate = this deployment
// is production, so missing texts block release and take the shop offline.
public record PartnerShopDto(
        String kennung,
        String publicPath,
        ShopStatus status,
        Instant releasedAt,
        String offlineReason,
        boolean publicEnabled,
        int missingLegalTexts,
        boolean legalGate
) {
    static PartnerShopDto from(PartnerShop shop, ShopStatus status, boolean publicEnabled,
                               int missingLegalTexts, boolean legalGate) {
        return new PartnerShopDto(shop.kennung, "/shop/" + shop.kennung, status, shop.releasedAt,
                shop.offlineByCompany ? shop.offlineReason : null, publicEnabled, missingLegalTexts, legalGate);
    }
}
