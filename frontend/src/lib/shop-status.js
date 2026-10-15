import { escapeHtml } from "./html.js";

// Shared by the partner page "Mein Webshop", the company "Webshop" dialog and the
// "Webshop" row action of the partner table.
const STATUS = {
  NOT_RELEASED: { badge: "Nicht freigegeben", tone: "warning",
    hint: "Der Shop wurde vom Unternehmen noch nicht freigegeben." },
  ONLINE: { badge: "Online", tone: "online",
    hint: "Der Shop ist öffentlich erreichbar." },
  OFFLINE_COMPANY: { badge: "Offline – vom Unternehmen", tone: "offline",
    hint: "Vom Unternehmen offline genommen." },
  OFFLINE_INACTIVE: { badge: "Offline – Partner inaktiv", tone: "offline",
    hint: "Der Partner ist deaktiviert, daher ist der Shop offline." },
  OFFLINE_NO_ARTICLES: { badge: "Offline – keine Artikel", tone: "offline",
    hint: "Keine freigeschalteten Artikel – Shop ist offline." },
};

export function shopStatusLabel(status) {
  return STATUS[status]?.badge ?? "Unbekannt";
}

export function shopStatusHint(status) {
  return STATUS[status]?.hint ?? "";
}

export function shopStatusTone(status) {
  return STATUS[status]?.tone ?? "warning";
}

export function shopStatusBadge(status) {
  return `<span class="shop-badge shop-signal--${shopStatusTone(status)}">${escapeHtml(shopStatusLabel(status))}</span>`;
}

// Partner table row action: a real button in the shop's signal colour.
export function shopActionButton(partnerId, status) {
  const label = escapeHtml(shopStatusLabel(status));
  return `<button type="button" class="shop-action shop-signal--${shopStatusTone(status)}" data-shop-id="${escapeHtml(partnerId)}"
    title="Webshop: ${label}" aria-label="Webshop verwalten (Status: ${label})">Webshop</button>`;
}

// Same URL locally and in production: tools/dev_frontend_server.py mirrors the Caddy rewrite.
export function shopPublicUrl(shop) {
  return `${location.origin}${shop.publicPath}`;
}
