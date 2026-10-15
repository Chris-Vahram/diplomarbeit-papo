import { escapeHtml } from "./html.js";

// Shared by the partner page "Mein Webshop", the company "Webshop" dialog and the
// "Webshop" column of the partner table.
const STATUS = {
  NOT_RELEASED: { badge: "Nicht freigegeben", tone: "neutral",
    hint: "Der Shop wurde vom Unternehmen noch nicht freigegeben." },
  ONLINE: { badge: "Online", tone: "online",
    hint: "Der Shop ist öffentlich erreichbar." },
  OFFLINE_COMPANY: { badge: "Offline – vom Unternehmen", tone: "offline",
    hint: "Vom Unternehmen offline genommen." },
  OFFLINE_INACTIVE: { badge: "Offline – Partner inaktiv", tone: "warning",
    hint: "Der Partner ist deaktiviert, daher ist der Shop offline." },
  OFFLINE_NO_ARTICLES: { badge: "Offline – keine Artikel", tone: "warning",
    hint: "Keine freigeschalteten Artikel – Shop ist offline." },
};

export function shopStatusLabel(status) {
  return STATUS[status]?.badge ?? "Unbekannt";
}

export function shopStatusHint(status) {
  return STATUS[status]?.hint ?? "";
}

export function shopStatusBadge(status) {
  const tone = STATUS[status]?.tone ?? "neutral";
  return `<span class="shop-badge shop-badge--${tone}">${escapeHtml(shopStatusLabel(status))}</span>`;
}

// Same URL locally and in production: tools/dev_frontend_server.py mirrors the Caddy rewrite.
export function shopPublicUrl(shop) {
  return `${location.origin}${shop.publicPath}`;
}
