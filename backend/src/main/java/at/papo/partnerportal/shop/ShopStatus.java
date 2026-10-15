package at.papo.partnerportal.shop;

// Effective shop status, checked in this order: release, company takedown,
// partner active flag, enabled available articles.
public enum ShopStatus {
    NOT_RELEASED,
    ONLINE,
    OFFLINE_COMPANY,
    OFFLINE_INACTIVE,
    OFFLINE_NO_ARTICLES
}
