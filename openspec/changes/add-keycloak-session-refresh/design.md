## Context

The frontend authenticates with a Keycloak direct grant (Resource Owner Password) from `frontend/src/lib/auth.js`. `login()` POSTs credentials to the token endpoint and stores the full token bundle (`access_token`, `refresh_token`, `expires_in`, …) in `sessionStorage` under `papo.auth`. `getSession()` decodes the access-token JWT and, if `exp` has passed, deletes the stored bundle and returns `null`; the router guard then redirects to `/login`. The `refresh_token` in the stored bundle is never used. The realm export sets no custom `accessTokenLifespan`, so Keycloak's default (~5 min) applies. Result: any navigation or tab switch after ~5 min drops the partner to login.

Protected API calls go through the `apiFetch*` helpers in `frontend/src/lib/api.js`, which attach `Authorization: Bearer <access_token>` from the session.

## Goals / Non-Goals

**Goals:**
- Keep a partner/company session usable for the full Keycloak SSO session (refresh-token lifetime), not just one access-token lifetime.
- Refresh transparently: the user never sees a spurious logout while the refresh token is still valid.
- End the session cleanly (single redirect to `/login`, storage cleared) once the refresh token expires.
- No regression in UTF-8 claim decoding (umlaut names).

**Non-Goals:**
- Production-grade token hardening (rotation policies, silent iframe SSO, token binding). Prototype scope.
- Any backend Java / REST / authorization change. `CurrentPartner.resolve()`, role checks, and inactive-partner blocking are untouched.
- Switching away from the direct-grant flow to the OIDC redirect (Authorization Code) flow — out of scope, larger change.

## Decisions

**1. Refresh via `grant_type=refresh_token` against the existing token endpoint.**
Add `refreshSession()` in `auth.js`: POST `grant_type=refresh_token`, `client_id=partnerportal-frontend`, `refresh_token=<stored>` to the same `TOKEN_ENDPOINT`. On success, overwrite the stored bundle with the new one. On failure (non-OK response), remove the stored bundle and return `null`. No new dependency — reuses `fetch` and `URLSearchParams` exactly like `login()`.

**2. `getSession()` stays synchronous; add an async `ensureValidSession()`.**
`getSession()` is called widely and synchronously (router guard, header render). Changing its signature ripples everywhere. Instead:
- `getSession()` keeps returning the current decoded session or `null` (unchanged contract), but no longer deletes the bundle purely because the access token is expired *when a refresh token is present* — it reports expiry so callers can decide.
- Add `async ensureValidSession()`: if the access token is valid, return the session; if expired/near-expiry and a refresh token exists, `await refreshSession()` and return the result; otherwise clear and return `null`.
- Near-expiry window: refresh if the token expires within ~30 s, to avoid a request racing the boundary.

**3. Refresh-and-retry-once in the API layer.**
In `api.js`, before attaching the bearer token, call `await ensureValidSession()`. If it returns `null`, clear session and route to `/login` (existing unauthorized path). Additionally, if a protected request returns 401 despite a token (clock skew / just-expired), attempt `refreshSession()` once and retry the request a single time; a second 401 clears the session and redirects. The retry is capped at one to avoid loops.

**4. Guard the refresh against concurrent calls.**
Multiple `apiFetch` calls can fire on one page render. Store a module-level in-flight refresh `Promise` and have concurrent callers await the same one, so only a single refresh request hits Keycloak per expiry.

**5. Modest realm `accessTokenLifespan` bump.**
Set `accessTokenLifespan` in the realm export to 900 s (15 min) as defence-in-depth so refreshes are less frequent during a demo. The refresh flow is the real fix; this only reduces churn. SSO session idle/max lifespans keep their defaults (30 min idle / 10 h max), which now actually bound the session.

**6. Preserve UTF-8 decoding.**
`decodeJwtPayload()` already decodes base64url as UTF-8 via `TextDecoder`. Refresh reuses it unchanged; no new decode path is introduced.

## Risks / Trade-offs

- **`getSession()` contract change (no longer auto-deletes on access-token expiry).** Mitigation: callers that must guarantee validity use `ensureValidSession()`; the router guard is updated to await it. Pure display callers tolerate a briefly-stale session that the next API call refreshes. Verify the guard path explicitly.
- **Refresh token stored in `sessionStorage`.** Same exposure surface as the access token already stored there; acceptable for a prototype, and `sessionStorage` is origin-scoped and cleared on tab close. Not introducing `localStorage` persistence.
- **`sessionStorage` is per browser tab.** RESOLVED (Chris, 2026-07-13): the logout he sees happens during **in-app sidebar navigation**, not on opening a new browser tab. The hash router never reloads the document, so `sessionStorage` survives every in-app navigation — the only reason the session dies is `getSession()` wiping the expired access token. The refresh flow is therefore the complete fix; `sessionStorage` stays (no `localStorage` switch needed). Cross-browser-tab persistence remains out of scope.
- **Longer access-token lifespan slightly widens the replay window** if a token leaks. Accepted for prototype; 15 min is modest and the refresh flow, not the lifespan, is what keeps users logged in.
- **Clock skew** between browser and Keycloak could trigger an unnecessary refresh or an unexpected 401. The 401-refresh-retry-once path covers the skew case; the ~30 s near-expiry window absorbs minor drift.
- **Concurrent-refresh race** if not de-duplicated would fire several refreshes and could invalidate each other under refresh-token rotation. Mitigated by the shared in-flight promise (Decision 4); rotation is off by default in the realm.
