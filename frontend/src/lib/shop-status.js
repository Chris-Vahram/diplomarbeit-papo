// Shared by the partner page "Mein Webshop" and the company "Webshop" dialog.
export const SHOP_STATUS_LABELS = {
  NOT_RELEASED: "Nicht freigegeben",
  ONLINE: "Online",
  OFFLINE_COMPANY: "Vom Unternehmen offline genommen",
  OFFLINE_INACTIVE: "Offline – Partner ist deaktiviert",
  OFFLINE_NO_ARTICLES: "Keine freigeschalteten Artikel – Shop ist offline.",
};

export function shopStatusLabel(status) {
  return SHOP_STATUS_LABELS[status] ?? "Unbekannt";
}

// The local static dev server has no /shop/* rewrite (docs/SETUP.md), so dev links
// point at shop.html directly; every other origin uses the public path.
export function shopPublicUrl(shop) {
  const isLocalDev = location.hostname === "localhost" && location.port === "3000";
  return isLocalDev
    ? `${location.origin}/shop.html?kennung=${encodeURIComponent(shop.kennung)}`
    : `${location.origin}${shop.publicPath}`;
}
