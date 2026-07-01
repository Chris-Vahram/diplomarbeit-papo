// Minimal Keycloak direct-grant (Resource Owner Password) auth helper.
// Frontend is a public client ("partnerportal-frontend") with directAccessGrantsEnabled=true,
// so the login form can POST credentials straight to the token endpoint (no redirect flow needed).

const KEYCLOAK_BASE = "http://localhost:8081";
const REALM = "partnerportal";
const CLIENT_ID = "partnerportal-frontend";
const TOKEN_ENDPOINT = `${KEYCLOAK_BASE}/realms/${REALM}/protocol/openid-connect/token`;

const STORAGE_KEY = "papo.auth";

function decodeJwtPayload(token) {
  const payload = token.split(".")[1];
  const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
  return JSON.parse(json);
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
  return getSession();
}

export function logout() {
  sessionStorage.removeItem(STORAGE_KEY);
}

export function getSession() {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  const tokens = JSON.parse(raw);
  const claims = decodeJwtPayload(tokens.access_token);
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

export function hasRole(role) {
  return getSession()?.roles.includes(role) ?? false;
}
