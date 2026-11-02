package at.papo.partnerportal.shop;

// Effective shop status, checked in this order: release, company takedown, legal texts
// (production deployments only), partner active flag, enabled available articles.
public enum ShopStatus {
    NOT_RELEASED,
    ONLINE,
    OFFLINE_COMPANY,
    OFFLINE_LEGAL,
    OFFLINE_INACTIVE,
    OFFLINE_NO_ARTICLES
}
