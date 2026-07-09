import { apiFetchBlob } from "./api.js";

// No "/api" prefix: apiFetchBlob (lib/api.js) prepends API_BASE itself, which already
// ends in "/api" - matching every other apiFetch call in this codebase (e.g.
// apiFetch("/company/products"), never apiFetch("/api/company/products")).
const INTERNAL_PREFIX = "/product-images/";

// Object URLs are cached by their source path (module-scope, survives across page
// reloads within the session) so the same uploaded image is fetched once, not on
// every list render.
const blobUrlCache = new Map();

function isInternal(url) {
  return typeof url === "string" && url.startsWith(INTERNAL_PREFIX);
}

// Resolves an internal product-image URL to an authenticated blob object-URL, since
// Keycloak is bearer-token auth and a plain <img src> can't send the Authorization
// header. External URLs (or empty values) pass through unchanged.
export async function resolveImageUrl(url) {
  if (!isInternal(url)) return url;
  if (blobUrlCache.has(url)) return blobUrlCache.get(url);
  const blob = await apiFetchBlob(url);
  const objectUrl = URL.createObjectURL(blob);
  blobUrlCache.set(url, objectUrl);
  return objectUrl;
}

// Resolves the `key` field on every row of `rows` (default "imageUrl") via
// resolveImageUrl, for bulk use before setting <data-table>.rows.
export async function resolveImageUrls(rows, key = "imageUrl") {
  return Promise.all(rows.map(async (row) => ({ ...row, [key]: await resolveImageUrl(row[key]) })));
}
