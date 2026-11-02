package at.papo.partnerportal.shop;

// Public shop status: deliberately minimal - no partner number, commission or contact data.
// accountsEnabled tells the shop whether to offer login (add-shop-customer-accounts).
public record PublicShopDto(String kennung, String displayName, boolean reachable, boolean accountsEnabled) {

    static PublicShopDto from(PartnerShop shop, boolean accountsEnabled) {
        return new PublicShopDto(shop.kennung, shop.partner.name, true, accountsEnabled);
    }
}
