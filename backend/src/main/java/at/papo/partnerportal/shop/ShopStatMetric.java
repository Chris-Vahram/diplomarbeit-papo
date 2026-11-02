package at.papo.partnerportal.shop;

// Counters in shop_stat_daily (shop-statistics spec). The dimension column says what is counted.
public enum ShopStatMetric {
    // dimension: page segment ("startseite", "artikeldetail", "warenkorb", ...)
    PAGE_VIEW,
    // first page of a visit (no referrer from the shop itself); dimension ""
    VISIT,
    // dimension: referrer domain of a visit, "" = direct or unknown
    REFERRER,
    // dimension: device class of a visit (Mobil, Tablet, Desktop)
    DEVICE,
    // dimension: article id
    ARTICLE_VIEW,
    // dimension: article id
    ADD_TO_CART,
    // consenting visitors only (shop-statistics "Consent-dependent visitor metrics"); dimension ""
    UNIQUE_VISITOR,
    RETURNING_VISITOR
}
