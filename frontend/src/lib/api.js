import { ensureValidSession, refreshSession, logout } from "./auth.js";
import { API_BASE } from "./config.js";

// Error responses are JSON ({"message": "...", "details"?: [...]}) from ApiExceptionMapper/
// ValidationExceptionMapper (see backend/.../common/*ExceptionMapper.java) - extract the
// plain German message instead of showing the raw JSON body in a toast. Falls back to the
// raw text for non-JSON error bodies (e.g. a proxy/server error page).
async function extractErrorMessage(response) {
  const text = await response.text().catch(() => "");
  try {
    return JSON.parse(text).message ?? text;
  } catch {
    return text || response.statusText;
  }
}

function clearSessionAndRedirect() {
  logout();
  location.hash = "#/login";
  throw new Error("Sitzung abgelaufen. Bitte erneut anmelden.");
}

// Shared by every apiFetch* helper: attaches a proactively-refreshed access token,
// and on a 401 anyway (clock skew / just-expired) refreshes once more and retries
// the request exactly once before giving up.
async function authorizedFetch(path, options = {}) {
  const session = await ensureValidSession();
  if (!session) clearSessionAndRedirect();

  const headers = new Headers(options.headers ?? {});
  headers.set("Authorization", `Bearer ${session.accessToken}`);

  const doFetch = () => fetch(`${API_BASE}${path}`, { ...options, headers });

  let response = await doFetch();
  if (response.status === 401) {
    const refreshed = await refreshSession();
    if (!refreshed) clearSessionAndRedirect();
    headers.set("Authorization", `Bearer ${refreshed.accessToken}`);
    response = await doFetch();
  }

  return response;
}

async function handleResponse(response) {
  if (response.status === 401) clearSessionAndRedirect();

  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message || `Anfrage fehlgeschlagen (${response.status})`);
  }

  if (response.status === 204) return null;
  return response.json();
}

export async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers ?? {});
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await authorizedFetch(path, { ...options, headers });
  return handleResponse(response);
}

// For multipart/form-data uploads (data import) - the browser must set its own
// Content-Type with the multipart boundary, so no Content-Type header here.
export async function apiFetchForm(path, formData) {
  const response = await authorizedFetch(path, { method: "POST", body: formData });
  return handleResponse(response);
}

// Fetches a binary response (e.g. an uploaded product image) with the Bearer token and
// returns the raw Blob - for callers that build their own object URL (see
// lib/product-images.js) instead of triggering a file download like apiDownload below.
export async function apiFetchBlob(path) {
  const response = await authorizedFetch(path);
  if (response.status === 401) clearSessionAndRedirect();
  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message || `Anfrage fehlgeschlagen (${response.status})`);
  }

  return response.blob();
}

// A plain <a href>/window.location download sends no Authorization header and
// would 401 (data-export spec) - fetch the blob with the Bearer token ourselves
// and trigger the save via a temporary object URL instead.
export async function apiDownload(path, filename) {
  const response = await authorizedFetch(path);
  if (response.status === 401) clearSessionAndRedirect();
  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message || `Anfrage fehlgeschlagen (${response.status})`);
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
