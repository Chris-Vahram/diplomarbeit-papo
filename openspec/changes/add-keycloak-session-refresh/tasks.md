## 1. Refresh logic in auth.js

- [ ] 1.1 Add `refreshSession()`: POST `grant_type=refresh_token` (client_id `partnerportal-frontend`, stored `refresh_token`) to `TOKEN_ENDPOINT`; on success overwrite the `papo.auth` bundle and return the decoded session; on non-OK response remove the bundle and return `null`.
- [ ] 1.2 Add a module-level in-flight refresh promise so concurrent callers share one refresh request per expiry.
- [ ] 1.3 Add `async ensureValidSession()`: return the current session if the access token is valid; if expired or within the ~30s near-expiry window and a refresh token exists, await `refreshSession()`; otherwise clear storage and return `null`.
- [ ] 1.4 Adjust `getSession()` so it no longer deletes the bundle solely because the access token is expired while a refresh token is present (keeps the synchronous read contract for display callers).
- [ ] 1.5 Confirm `decodeJwtPayload()` UTF-8 decoding is reused unchanged on the refreshed token (no new decode path).

## 2. Refresh-and-retry in api.js

- [ ] 2.1 In the `apiFetch*` helpers, `await ensureValidSession()` before attaching the bearer token; if it returns `null`, clear session and route to `/login`.
- [ ] 2.2 If a protected request returns 401 despite a token, call `refreshSession()` once and retry the request a single time; a second 401 clears the session and redirects to `/login`.
- [ ] 2.3 Cap the retry at exactly one to prevent loops.

## 3. Router guard

- [ ] 3.1 Update the route guard so protected routes await `ensureValidSession()` (not just the synchronous `getSession()`), redirecting to `/login` only when it resolves `null`.

## 5. Realm config

- [ ] 5.1 Set `accessTokenLifespan` to 900 (15 min) in `backend/src/main/resources/keycloak/partnerportal-realm.json`; leave SSO idle/max defaults.

## 6. Verify

- [ ] 6.1 Log in as `partner-demo`, wait past the old ~5 min access-token window, switch tabs / navigate — session persists, no bounce to login.
- [ ] 6.2 Trigger a protected API call with a just-expired access token (e.g. throttle/wait) — call succeeds via one transparent refresh+retry, no visible logout.
- [ ] 6.3 Let the refresh token expire (or clear it) — next action returns cleanly to `/login` with storage emptied, no half-authenticated page reachable.
- [ ] 6.4 Log in as a partner whose name contains an umlaut (ä/ö/ü/ß) and confirm the displayed name stays correct after a refresh.
