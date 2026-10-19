package at.papo.partnerportal.shop;

// shop-pages spec "Page kinds": system pages are fixed and outside the limits,
// mandatory and free pages are designable and count toward them.
public enum ShopPageKind {
    SYSTEM,
    MANDATORY,
    FREE;

    public boolean designable() {
        return this != SYSTEM;
    }
}
