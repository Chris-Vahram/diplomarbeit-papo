// Unauthenticated reads of one shop's public API (shop-public-catalog spec). Never sends
// cookies or tokens. The article list is loaded once per page view and shared by blocks.
import { API_BASE } from "../lib/config.js";

export function createShopApi(kennung) {
  const base = `${API_BASE}/shop/${encodeURIComponent(kennung)}`;
  let articles;

  // Errors carry the server's German message (inquiry form shows it as is).
  async function send(path, body) {
    const response = await fetch(base + path, {
      method: "POST",
      credentials: "omit",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      const error = new Error(`HTTP ${response.status}`);
      error.status = response.status;
      error.serverMessage = await response.json().then((data) => data?.message).catch(() => null);
      throw error;
    }
  }

  async function get(path) {
    const response = await fetch(base + path, { credentials: "omit" });
    if (!response.ok) {
      const error = new Error(`HTTP ${response.status}`);
      error.status = response.status;
      throw error;
    }
    return response.json();
  }

  return {
    kennung,
    // Only the public shop sends inquiries; preview and template demo never do.
    live: true,
    shop: () => get(""),
    theme: () => get("/theme"),
    navigation: () => get("/navigation"),
    page: (segment) => get(`/pages/${encodeURIComponent(segment)}`),
    article: (id) => get(`/articles/${encodeURIComponent(id)}`),
    articles: () => (articles ??= get("/articles")),
    // Signed render timestamp, then the submission (shop-inquiries spec).
    inquiryToken: () => get("/inquiries/token").then((data) => data.token),
    sendInquiry: (body) => send("/inquiries", body),
    // Block images are the shop's own uploads (shop-assets spec).
    assetUrl: (assetId) => `${base}/assets/${encodeURIComponent(assetId)}`,
    // Backend URLs ("/api/...") must go through API_BASE (separate backend port in local dev).
    resolveUrl: (url) => (url?.startsWith("/api/") ? API_BASE + url.slice(4) : url),
    // Shop-relative link: "" is the Startseite.
    href: (path = "") => `/shop/${encodeURIComponent(kennung)}${path ? `/${path}` : ""}`,
  };
}

// Editor preview (shop-builder spec): same interface, but all data comes from the editor
// via postMessage, so drafts of a not yet released shop render without any public request.
// assetUrls maps asset id -> object URL the editor fetched with the partner's login.
export function createPreviewApi(data) {
  return {
    kennung: data.shop.kennung,
    // Blocks render empty texts as fillable placeholders (blocks/text-field.js).
    editing: true,
    shop: async () => data.shop,
    theme: async () => data.theme,
    navigation: async () => data.navigation,
    page: async () => data.page,
    article: async () => data.article,
    articles: async () => data.articles,
    // Nothing is sent from the preview; the form shows it is a preview instead.
    inquiryToken: async () => null,
    sendInquiry: async () => {},
    assetUrl: (assetId) => data.assetUrls?.[assetId] ?? "",
    resolveUrl: (url) => data.imageUrls?.[url] ?? (url?.startsWith("/api/") ? "" : url),
    href: (path = "") => `#${path}`,
  };
}
