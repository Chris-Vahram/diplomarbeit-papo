// Unauthenticated reads of one shop's public API (shop-public-catalog spec). Never sends
// cookies or tokens. The article list is loaded once per page view and shared by blocks.
import { API_BASE } from "../lib/config.js";

export function createShopApi(kennung) {
  const base = `${API_BASE}/shop/${encodeURIComponent(kennung)}`;
  let articles;

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
    shop: () => get(""),
    theme: () => get("/theme"),
    navigation: () => get("/navigation"),
    page: (segment) => get(`/pages/${encodeURIComponent(segment)}`),
    article: (id) => get(`/articles/${encodeURIComponent(id)}`),
    articles: () => (articles ??= get("/articles")),
    imageUrl: (imageId) => `${base}/images/${encodeURIComponent(imageId)}`,
    // Backend URLs ("/api/...") must go through API_BASE (separate backend port in local dev).
    resolveUrl: (url) => (url?.startsWith("/api/") ? API_BASE + url.slice(4) : url),
    // Shop-relative link: "" is the Startseite.
    href: (path = "") => `/shop/${encodeURIComponent(kennung)}${path ? `/${path}` : ""}`,
  };
}
