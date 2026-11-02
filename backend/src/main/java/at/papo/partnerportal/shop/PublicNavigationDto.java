package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.CompanyProfile;

import java.util.List;

// Generated shop navigation plus the fixed footer data. path is relative to /shop/<kennung>;
// the Startseite is the shop root (""). inquiryPath: first online page with an
// "Anfrageformular" block, so the article detail can link to it (null without one).
public record PublicNavigationDto(List<Item> items, List<Item> legal, String sellerName, String inquiryPath) {

    public record Item(String segment, String menuName, String path) {

        static Item from(ShopPage page) {
            return new Item(page.segment, page.menuName, page.slot == ShopPageSlot.HOME ? "" : page.segment);
        }
    }

    static PublicNavigationDto of(PartnerShop shop, ShopPageService pageService) {
        CompanyProfile seller = shop.partner.company;
        return new PublicNavigationDto(
                pageService.navigation(shop).stream().map(Item::from).toList(),
                pageService.legalPages(shop).stream().map(Item::from).toList(),
                seller == null ? null : seller.businessName,
                pageService.inquiryPage(shop).map(page -> Item.from(page).path()).orElse(null));
    }
}
