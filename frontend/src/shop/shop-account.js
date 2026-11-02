// End-customer login for the public shop (shop-customer-accounts spec). Authorization Code +
// PKCE against the separate Keycloak realm papo-shop: registration, e-mail verification and
// password reset all happen on Keycloak's pages. Never imports portal modules and never reads
// the portal session ("papo.auth"); its own tokens live in sessionStorage under SESSION_KEY.
import { API_BASE, KEYCLOAK_BASE } from "../lib/config.js";

const REALM = "papo-shop";
const CLIENT_ID = "papo-shop-frontend";
const OIDC = `${KEYCLOAK_BASE}/realms/${REALM}/protocol/openid-connect`;
const SESSION_KEY = "papo.shop.session";
const PENDING_KEY = "papo.shop.login";
// Refresh a little early so a request never leaves with a token that expires in flight.
const EARLY_MS = 30_000;

export const ACCOUNT_EVENT = "papo:shop-account";

function storage() {
  try {
    return window.sessionStorage;
  } catch {
    return null; // blocked storage: login simply stays unavailable for this visit
  }
}

function read(key) {
  try {
    return JSON.parse(storage()?.getItem(key) ?? "null");
  } catch {
    return null;
  }
}

function write(key, value) {
  try {
    if (value == null) storage()?.removeItem(key);
    else storage()?.setItem(key, JSON.stringify(value));
  } catch {
    // ignore: quota or blocked storage
  }
}

function base64url(bytes) {
  return btoa(String.fromCharCode(...bytes)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function randomString(length = 32) {
  return base64url(crypto.getRandomValues(new Uint8Array(length)));
}

async function challengeOf(verifier) {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier));
  return base64url(new Uint8Array(digest));
}

function claimsOf(token) {
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const bytes = Uint8Array.from(atob(payload), (c) => c.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch {
    return null;
  }
}

function redirectUri(kennung) {
  return `${location.origin}/shop/${encodeURIComponent(kennung)}/konto`;
}

function store(tokens) {
  const now = Date.now();
  const previous = read(SESSION_KEY);
  const session = {
    accessToken: tokens.access_token,
    refreshToken: tokens.refresh_token,
    idToken: tokens.id_token ?? previous?.idToken,
    expiresAt: now + (tokens.expires_in ?? 60) * 1000,
    refreshExpiresAt: tokens.refresh_expires_in ? now + tokens.refresh_expires_in * 1000 : 0,
  };
  write(SESSION_KEY, session);
  return session;
}

function announce() {
  window.dispatchEvent(new Event(ACCOUNT_EVENT));
}

// The visible identity: e-mail and first name from the ID token (no request needed).
export function currentCustomer() {
  const session = read(SESSION_KEY);
  if (!session) return null;
  // Past both lifetimes nothing can be renewed any more.
  if (session.expiresAt <= Date.now() && (!session.refreshExpiresAt || session.refreshExpiresAt <= Date.now())) {
    write(SESSION_KEY, null);
    return null;
  }
  const claims = claimsOf(session.idToken ?? session.accessToken) ?? {};
  return { email: claims.email ?? "", firstName: claims.given_name ?? "" };
}

// Leaves for Keycloak. returnTo is a shop-relative path ("" Startseite, "kasse", "konto").
// action: "login" | "register" | "password" (Keycloak's own update-password page).
export async function startLogin(kennung, { returnTo = "konto", action = "login" } = {}) {
  const verifier = randomString(48);
  const state = randomString(16);
  const nonce = randomString(16);
  write(PENDING_KEY, { verifier, state, nonce, kennung, returnTo, at: Date.now() });
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: redirectUri(kennung),
    response_type: "code",
    scope: "openid email profile",
    code_challenge: await challengeOf(verifier),
    code_challenge_method: "S256",
    state,
    nonce,
    ui_locales: "de",
  });
  if (action === "password") params.set("kc_action", "UPDATE_PASSWORD");
  // Keycloak's own registration entry; it continues as a normal login afterwards.
  location.assign(`${OIDC}/${action === "register" ? "registrations" : "auth"}?${params}`);
}

// Runs on /konto when Keycloak sends the visitor back. Returns { returnTo } after a successful
// login, { error } when it failed or was cancelled, or null when this is no login return.
export async function completeLogin(kennung) {
  const query = new URLSearchParams(location.search);
  if (!query.has("state") || (!query.has("code") && !query.has("error"))) return null;
  const pending = read(PENDING_KEY);
  write(PENDING_KEY, null);
  history.replaceState(null, "", location.pathname);
  if (query.get("error") || !pending || pending.state !== query.get("state") || pending.kennung !== kennung) {
    const cancelled = query.get("error") === "access_denied" || query.get("kc_action_status") === "cancelled";
    return { error: cancelled ? "cancelled" : "failed", returnTo: pending?.returnTo ?? "konto" };
  }
  try {
    const response = await fetch(`${OIDC}/token`, {
      method: "POST",
      credentials: "omit",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        grant_type: "authorization_code",
        client_id: CLIENT_ID,
        code: query.get("code"),
        redirect_uri: redirectUri(kennung),
        code_verifier: pending.verifier,
      }),
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const tokens = await response.json();
    if (claimsOf(tokens.id_token)?.nonce !== pending.nonce) throw new Error("nonce");
    store(tokens);
    announce();
    return { returnTo: pending.returnTo };
  } catch {
    return { error: "failed", returnTo: pending.returnTo };
  }
}

let refreshing = null;

// A valid access token, refreshed when needed; null once the session is over.
export async function accessToken() {
  const session = read(SESSION_KEY);
  if (!session) return null;
  if (session.expiresAt - EARLY_MS > Date.now()) return session.accessToken;
  if (!session.refreshToken) {
    endSession();
    return null;
  }
  refreshing ??= fetch(`${OIDC}/token`, {
    method: "POST",
    credentials: "omit",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ grant_type: "refresh_token", client_id: CLIENT_ID, refresh_token: session.refreshToken }),
  })
    .then(async (response) => (response.ok ? store(await response.json()).accessToken : (endSession(), null)))
    .catch(() => null)
    .finally(() => { refreshing = null; });
  return refreshing;
}

// Forgets the tokens in this tab only (expired session, deleted account).
export function endSession() {
  const had = read(SESSION_KEY) != null;
  write(SESSION_KEY, null);
  if (had) announce();
}

// Ends the Keycloak session too, then returns to the shop's Startseite.
export function logout(kennung) {
  const session = read(SESSION_KEY);
  write(SESSION_KEY, null);
  const target = `${location.origin}/shop/${encodeURIComponent(kennung)}`;
  if (!session?.idToken) {
    location.assign(target);
    return;
  }
  const params = new URLSearchParams({ client_id: CLIENT_ID, id_token_hint: session.idToken, post_logout_redirect_uri: target });
  location.assign(`${OIDC}/logout?${params}`);
}

// Customer API (/api/shop-customer/*), always with the shop realm's token. Errors carry the
// server's German message; 401 ends the local session.
export async function accountRequest(path, { method = "GET", body, raw = false } = {}) {
  const token = await accessToken();
  if (!token) {
    throw Object.assign(new Error("signed out"), { status: 401 });
  }
  const response = await fetch(`${API_BASE}/shop-customer${path}`, {
    method,
    credentials: "omit",
    headers: {
      Authorization: `Bearer ${token}`,
      ...(body === undefined ? {} : { "Content-Type": "application/json" }),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    if (response.status === 401) endSession();
    const error = new Error(`HTTP ${response.status}`);
    error.status = response.status;
    error.serverMessage = await response.json().then((data) => data?.message).catch(() => null);
    throw error;
  }
  if (raw) return response;
  return response.status === 204 ? null : response.json();
}
