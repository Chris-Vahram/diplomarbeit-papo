import { getSession, logout } from "./auth.js";

// Absolute base: the frontend is served as a separate static site (e.g. :3000),
// so relative "/api" would hit the static server instead of Quarkus. CORS is
// enabled in the backend for localhost origins (see docs/SETUP.md).
const API_BASE = "http://localhost:8080/api";

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

async function handleResponse(response) {
  if (response.status === 401) {
    logout();
    location.hash = "#/login";
    throw new Error("Sitzung abgelaufen. Bitte erneut anmelden.");
  }

  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message || `Anfrage fehlgeschlagen (${response.status})`);
  }

  if (response.status === 204) return null;
  return response.json();
}

export async function apiFetch(path, options = {}) {
  const session = getSession();
  const headers = new Headers(options.headers ?? {});
  if (session) {
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  return handleResponse(response);
}

// For multipart/form-data uploads (data import) - the browser must set its own
// Content-Type with the multipart boundary, so no Content-Type header here.
export async function apiFetchForm(path, formData) {
  const session = getSession();
  const headers = new Headers();
  if (session) {
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { method: "POST", headers, body: formData });
  return handleResponse(response);
}

// Fetches a binary response (e.g. an uploaded product image) with the Bearer token and
// returns the raw Blob - for callers that build their own object URL (see
// lib/product-images.js) instead of triggering a file download like apiDownload below.
export async function apiFetchBlob(path) {
  const session = getSession();
  const headers = new Headers();
  if (session) {
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { headers });
  if (response.status === 401) {
    logout();
    location.hash = "#/login";
    throw new Error("Sitzung abgelaufen. Bitte erneut anmelden.");
  }
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
  const session = getSession();
  const headers = new Headers();
  if (session) {
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { headers });
  if (response.status === 401) {
    logout();
    location.hash = "#/login";
    throw new Error("Sitzung abgelaufen. Bitte erneut anmelden.");
  }
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
