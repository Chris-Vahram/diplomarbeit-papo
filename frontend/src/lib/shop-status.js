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
  return STATUS[status]?.badge ?? "Status unbekannt";
}

export function shopStatusHint(status) {
  return STATUS[status]?.hint ?? "";
}

// "unknown" when the status could not be loaded - shown neutrally, never as a signal colour.
export function shopStatusTone(status) {
  return STATUS[status]?.tone ?? "unknown";
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

// Fills the shared status block (#shop-badge, #shop-hint, #shop-env-notice,
// #shop-reason[-text], #shop-link, #shop-details) inside `root` - same markup on partner
// page and company dialog. `publicEnabled` false = deployment-wide switch off: the shop
// status stays as stored, but nothing is publicly reachable on this environment.
export function renderShopStatus(root, shop) {
  root.querySelector("#shop-badge").innerHTML = shopStatusBadge(shop.status);
  const hint = root.querySelector("#shop-hint");
  hint.textContent = shop.status === "ONLINE" && shop.publicEnabled === false
    ? "Der Shop ist freigegeben und bereit; er wird erreichbar, sobald öffentliche Shops aktiviert sind."
    : shopStatusHint(shop.status);
  hint.hidden = false;
  root.querySelector("#shop-env-notice").hidden = shop.publicEnabled !== false;

  root.querySelector("#shop-reason").hidden = shop.status !== "OFFLINE_COMPANY" || !shop.offlineReason;
  root.querySelector("#shop-reason-text").textContent = shop.offlineReason ?? "";

  const url = shopPublicUrl(shop);
  const linkCell = root.querySelector("#shop-link");
  linkCell.innerHTML = "";
  if (shop.status === "ONLINE" && shop.publicEnabled !== false) {
    const link = document.createElement("a");
    link.href = url;
    link.target = "_blank";
    link.rel = "noopener";
    link.textContent = url;
    linkCell.append(link);
  } else {
    linkCell.textContent = `${url} (derzeit nicht erreichbar)`;
  }
  root.querySelector("#shop-details").hidden = false;
}
