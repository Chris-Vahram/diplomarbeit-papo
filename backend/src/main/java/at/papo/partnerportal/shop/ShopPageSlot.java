package at.papo.partnerportal.shop;

// The pages every shop has. Free pages have no slot. The segment is reserved per shop,
// so a free page can never take "warenkorb" or "artikel".
public enum ShopPageSlot {
    HOME(ShopPageKind.MANDATORY, "startseite", "Startseite", "startseite-klar"),
    ARTICLES(ShopPageKind.MANDATORY, "artikel", "Artikel", "artikel-klar"),
    ARTICLE_DETAIL(ShopPageKind.MANDATORY, "artikeldetail", "Artikeldetail", "artikeldetail-klar"),
    CART(ShopPageKind.SYSTEM, "warenkorb", "Warenkorb", null),
    CHECKOUT(ShopPageKind.SYSTEM, "kasse", "Kasse", null),
    IMPRESSUM(ShopPageKind.SYSTEM, "impressum", "Impressum", null),
    AGB(ShopPageKind.SYSTEM, "agb", "AGB", null),
    DATENSCHUTZ(ShopPageKind.SYSTEM, "datenschutz", "Datenschutz", null),
    WIDERRUF(ShopPageKind.SYSTEM, "widerruf", "Widerruf", null);

    public final ShopPageKind kind;
    public final String segment;
    public final String menuName;
    // Catalogue template a new shop starts with (ShopTemplateCatalog); null for system pages.
    final String defaultTemplate;

    ShopPageSlot(ShopPageKind kind, String segment, String menuName, String defaultTemplate) {
        this.kind = kind;
        this.segment = segment;
        this.menuName = menuName;
        this.defaultTemplate = defaultTemplate;
    }
}
