package at.papo.partnerportal.shop;

// Public shop status: deliberately minimal - no partner number, commission or contact data.
public record PublicShopDto(String kennung, String displayName, boolean reachable) {

    static PublicShopDto from(PartnerShop shop) {
        return new PublicShopDto(shop.kennung, shop.partner.name, true);
    }
}
