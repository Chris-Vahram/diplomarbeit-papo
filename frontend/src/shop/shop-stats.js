// Anonymous shop statistics (shop-statistics spec, add-shop-statistics). Live shop only.
// Without consent nothing is stored in the browser and events carry no identifier. With
// "Statistik" consent a random id ("papo.visitor", owned by that category in shop-consent.js)
// lets the server count unique and returning visitors per day. Consent is checked again at
// every send, so withdrawing it takes effect immediately.
// The referrer is reduced to its host here already; the server keeps only the domain.
import { API_BASE } from "../lib/config.js";
import { CART_EVENT } from "./shop-cart.js";
import { hasConsent } from "./shop-consent.js";

const VISITOR_KEY = "papo.visitor";
const MAX_BATCH = 20; // ShopStatisticsService.MAX_EVENTS
const FLUSH_DELAY_MS = 1500;

function todayIso() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function randomId() {
  if (crypto.randomUUID) return crypto.randomUUID();
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = [...bytes].map((byte) => byte.toString(16).padStart(2, "0")).join("");
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

// { visitor, returning } with consent, otherwise {} (and nothing is read or written).
function visitorFields() {
  if (!hasConsent("statistik")) return {};
  const today = todayIso();
  try {
    const stored = JSON.parse(localStorage.getItem(VISITOR_KEY) ?? "null");
    if (typeof stored?.id === "string" && typeof stored?.since === "string") {
      return { visitor: stored.id, returning: stored.since < today };
    }
    const created = { id: randomId(), since: today };
    localStorage.setItem(VISITOR_KEY, JSON.stringify(created));
    return { visitor: created.id, returning: false };
  } catch {
    return {}; // blocked storage: count anonymously
  }
}

// A visit starts when the page was not opened from this shop's own origin.
function externalReferrer() {
  if (!document.referrer) return { entry: true, referrer: "" };
  try {
    const url = new URL(document.referrer);
    if (url.origin === location.origin) return { entry: false };
    return { entry: true, referrer: `${url.protocol}//${url.host}/` };
  } catch {
    return { entry: true, referrer: "" };
  }
}

// page: segment of the rendered page ("startseite", "artikeldetail", "warenkorb", ...).
// articleId: the article of a detail page. Call once per page view after it rendered.
export function trackShopVisit(api, { page, articleId = null }) {
  if (!api.live) return;
  const endpoint = `${API_BASE}/shop/${encodeURIComponent(api.kennung)}/events`;
  let queue = [];
  let timer = null;

  function flush() {
    clearTimeout(timer);
    timer = null;
    while (queue.length) {
      const events = queue.slice(0, MAX_BATCH);
      queue = queue.slice(MAX_BATCH);
      // text/plain keeps it a simple request (no preflight); keepalive survives leaving the page.
      fetch(endpoint, {
        method: "POST",
        credentials: "omit",
        keepalive: true,
        headers: { "Content-Type": "text/plain" },
        body: JSON.stringify({ ...visitorFields(), events }),
      }).catch(() => {}); // statistics never disturb the shop
    }
  }

  function push(event) {
    queue.push(event);
    if (queue.length >= MAX_BATCH) flush();
    else timer ??= setTimeout(flush, FLUSH_DELAY_MS);
  }

  push({ type: "pageview", page, ...externalReferrer() });
  if (articleId) push({ type: "article", articleId: Number(articleId) });
  window.addEventListener(CART_EVENT, (event) => {
    if (event.detail?.kennung === api.kennung && event.detail.articleId) {
      push({ type: "cart", articleId: event.detail.articleId });
    }
  });
  addEventListener("pagehide", flush);
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") flush();
  });
}
