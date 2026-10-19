package at.papo.partnerportal.shop;

import java.util.List;

// Generated shop navigation plus the fixed footer data. path is relative to /shop/<kennung>;
// the Startseite is the shop root ("").
public record PublicNavigationDto(List<Item> items, List<Item> legal, String sellerName) {

    public record Item(String segment, String menuName, String path) {

        static Item from(ShopPage page) {
            return new Item(page.segment, page.menuName, page.slot == ShopPageSlot.HOME ? "" : page.segment);
        }
    }
}
