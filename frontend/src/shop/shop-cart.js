// Shop cart (shop-checkout spec "Shop cart"): article ids and quantities per shop in
// localStorage, nothing personal, so it stays technically necessary storage. Prices are never
// stored: totals are always computed from the current public catalogue, and the server re-reads
// everything on order. Storage may be blocked (private mode); the cart then lives in memory.
import { netCentsHalfUp, toCents } from "../lib/money.js";

export const MAX_QUANTITY = 10000; // same bound as the server (OrderItemRequest.MAX_QUANTITY)
export const MAX_LINES = 50; // ShopOrderService.MAX_LINES
export const CART_EVENT = "shop-cart-change";

const memory = new Map();

function storageKey(kennung) {
  return `papo-shop-cart:${kennung}`;
}

function clampQuantity(value) {
  const number = Math.trunc(Number(value));
  if (!Number.isFinite(number) || number < 1) return 1;
  return Math.min(number, MAX_QUANTITY);
}

function sanitize(raw) {
  if (!Array.isArray(raw)) return [];
  const seen = new Set();
  return raw
    .filter((entry) => Number.isSafeInteger(entry?.id) && entry.id > 0 && !seen.has(entry.id) && seen.add(entry.id))
    .slice(0, MAX_LINES)
    .map((entry) => ({ id: entry.id, quantity: clampQuantity(entry.quantity) }));
}

export function readCart(kennung) {
  try {
    const stored = localStorage.getItem(storageKey(kennung));
    if (stored !== null) return sanitize(JSON.parse(stored));
  } catch {
    // blocked or corrupt storage: fall back to memory
  }
  return sanitize(memory.get(kennung) ?? []);
}

// addedId: article just added (header bump, statistics event "In den Warenkorb").
function writeCart(kennung, lines, addedId = null) {
  const clean = sanitize(lines);
  memory.set(kennung, clean);
  try {
    if (clean.length) localStorage.setItem(storageKey(kennung), JSON.stringify(clean));
    else localStorage.removeItem(storageKey(kennung));
  } catch {
    // memory copy is enough for this page view
  }
  window.dispatchEvent(new CustomEvent(CART_EVENT, { detail: { kennung, added: addedId !== null, articleId: addedId } }));
  return clean;
}

// Adds to an existing line; returns false when the cart is full (line limit).
export function addToCart(kennung, id, quantity = 1) {
  const lines = readCart(kennung);
  const line = lines.find((entry) => entry.id === id);
  if (line) line.quantity = clampQuantity(line.quantity + clampQuantity(quantity));
  else if (lines.length >= MAX_LINES) return false;
  else lines.push({ id, quantity: clampQuantity(quantity) });
  writeCart(kennung, lines, id);
  return true;
}

export function setQuantity(kennung, id, quantity) {
  writeCart(kennung, readCart(kennung).map((entry) => (entry.id === id ? { id, quantity: clampQuantity(quantity) } : entry)));
}

export function removeFromCart(kennung, id) {
  writeCart(kennung, readCart(kennung).filter((entry) => entry.id !== id));
}

export function clearCart(kennung) {
  writeCart(kennung, []);
}

export function cartCount(kennung) {
  return readCart(kennung).reduce((sum, entry) => sum + entry.quantity, 0);
}

// Joins the stored lines with the public catalogue. A line whose article left the catalogue
// (disabled, unavailable) stays visible as unavailable and is excluded from the totals.
// Totals mirror the backend (ArticleTax.totals): gross exact in cents, contained USt per rate
// as gross minus the half-up rounded net of that rate.
export function summarize(kennung, articles) {
  const byId = new Map((articles ?? []).map((article) => [article.id, article]));
  const lines = readCart(kennung).map((entry) => {
    const article = byId.get(entry.id) ?? null;
    const lineCents = article ? toCents(article.grossPrice) * entry.quantity : 0;
    return { ...entry, article, available: Boolean(article), lineCents };
  });
  const available = lines.filter((line) => line.available);
  const byRate = new Map();
  for (const line of available) {
    const rate = Number(line.article.taxRatePercent);
    const group = byRate.get(rate) ?? [];
    group.push({ grossCents: line.lineCents, ratePercent: rate });
    byRate.set(rate, group);
  }
  const taxes = [...byRate.entries()]
    .sort(([a], [b]) => b - a)
    .map(([rate, group]) => {
      const gross = group.reduce((sum, entry) => sum + entry.grossCents, 0);
      return { rate, taxCents: gross - netCentsHalfUp(group) };
    });
  return {
    lines,
    available,
    hasUnavailable: available.length !== lines.length,
    grossCents: available.reduce((sum, line) => sum + line.lineCents, 0),
    taxes,
  };
}
