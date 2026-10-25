// Minimal Keycloak direct-grant (Resource Owner Password) auth helper.
// Frontend is a public client ("partnerportal-frontend") with directAccessGrantsEnabled=true,
// so the login form can POST credentials straight to the token endpoint (no redirect flow needed).

import { KEYCLOAK_BASE } from "./config.js";

const REALM = "partnerportal";
const CLIENT_ID = "partnerportal-frontend";
const TOKEN_ENDPOINT = `${KEYCLOAK_BASE}/realms/${REALM}/protocol/openid-connect/token`;

const STORAGE_KEY = "papo.auth";

function decodeJwtPayload(token) {
  const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
  // atob alone corrupts non-ASCII claim values (e.g. umlauts in names) — decode as UTF-8.
  const bytes = Uint8Array.from(atob(payload), (c) => c.charCodeAt(0));
  return JSON.parse(new TextDecoder().decode(bytes));
}

export async function login(username, password) {
  const body = new URLSearchParams({
    grant_type: "password",
    client_id: CLIENT_ID,
    username,
    password,
  });

  const response = await fetch(TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });

  if (!response.ok) {
    throw new Error("Login fehlgeschlagen. Bitte Zugangsdaten prüfen.");
  }

  const tokens = await response.json();
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(tokens));
  window.dispatchEvent(new Event(SESSION_CHANGED));
  return getSession();
}

// Fired on login and logout so per-identity caches (e.g. product image blobs) are dropped:
// another company's user in the same tab must never see a cached image (add-company-tenancy).
export const SESSION_CHANGED = "papo:session-changed";

export function logout() {
  sessionStorage.removeItem(STORAGE_KEY);
  window.dispatchEvent(new Event(SESSION_CHANGED));
}

export function getSession() {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  const tokens = JSON.parse(raw);
  const claims = decodeJwtPayload(tokens.access_token);
  const expired = claims.exp && claims.exp * 1000 <= Date.now();

  // An expired access token with no refresh token can never become valid again -
  // drop it. If a refresh token is present, keep reporting the (stale) session so
  // synchronous display callers don't flicker; ensureValidSession()/apiFetch* are
  // what actually refresh before a protected action is allowed through.
  if (expired && !tokens.refresh_token) {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }

  const roles = claims.realm_access?.roles ?? [];

  return {
    accessToken: tokens.access_token,
    username: claims.preferred_username,
    roles,
    isCompany: roles.includes("company"),
    isPartner: roles.includes("partner"),
  };
}

export function isAuthenticated() {
  return getSession() !== null;
}

const NEAR_EXPIRY_MS = 30_000;

// Concurrent apiFetch* calls on one page render must not each fire their own
// refresh request - share a single in-flight promise per expiry.
let refreshPromise = null;

export async function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = doRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

async function doRefresh() {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  const tokens = raw ? JSON.parse(raw) : null;
  if (!tokens?.refresh_token) {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }

  const body = new URLSearchParams({
    grant_type: "refresh_token",
    client_id: CLIENT_ID,
    refresh_token: tokens.refresh_token,
  });

  const response = await fetch(TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });

  if (!response.ok) {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }

  const newTokens = await response.json();

  // The user may have logged out while this request was in flight - don't
  // resurrect a session they explicitly ended.
  if (sessionStorage.getItem(STORAGE_KEY) === null) return null;

  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(newTokens));
  return getSession();
}

// Awaited before any protected action (route guard, apiFetch*) that needs a
// token guaranteed valid right now - refreshes proactively on expiry/near-expiry,
// or clears the session cleanly if the refresh token itself is gone/rejected.
export async function ensureValidSession() {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  const tokens = JSON.parse(raw);
  const claims = decodeJwtPayload(tokens.access_token);
  const expiresInMs = (claims.exp ?? 0) * 1000 - Date.now();

  if (expiresInMs > NEAR_EXPIRY_MS) {
    return getSession();
  }

  if (!tokens.refresh_token) {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }

  return refreshSession();
}
