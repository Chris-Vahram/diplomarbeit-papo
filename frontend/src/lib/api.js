import { getSession, logout } from "./auth.js";

// Absolute base: the frontend is served as a separate static site (e.g. :3000),
// so relative "/api" would hit the static server instead of Quarkus. CORS is
// enabled in the backend for localhost origins (see docs/SETUP.md).
const API_BASE = "http://localhost:8080/api";

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

  if (response.status === 401) {
    logout();
    location.hash = "#/login";
    throw new Error("Sitzung abgelaufen. Bitte erneut anmelden.");
  }

  if (!response.ok) {
    const message = await response.text().catch(() => response.statusText);
    throw new Error(message || `Anfrage fehlgeschlagen (${response.status})`);
  }

  if (response.status === 204) return null;
  return response.json();
}
