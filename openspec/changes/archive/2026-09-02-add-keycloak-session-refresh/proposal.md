## Why

Partners are logged out after about five minutes of use — switching a tab or navigating to another page bounces them back to the login screen (client meeting 10.07.2026, Chris's Nachtrag: "man wird schnell ausgeloggt"). The client explicitly asked for simple, low-friction usage for non-technical partners; being logged out mid-task directly undermines that. Root cause: the frontend logs in via direct grant and stores the token bundle, but never uses the `refresh_token` Keycloak already returns, so the session dies the moment the short-lived access token expires.

## What Changes

- The frontend keeps a partner logged in past the access-token lifetime by using the stored `refresh_token` to obtain a new access token when the current one is expired or about to expire.
- An API request whose access token just expired transparently refreshes once and retries, instead of failing and dropping the user to the login screen.
- When the refresh token itself is expired or invalid, the session is cleared cleanly and the user is returned to `/login` with no half-authenticated state.
- The Keycloak realm access-token lifetime is raised to a modest prototype value as defence-in-depth (the refresh flow remains the actual fix).

## Capabilities

### New Capabilities
- `session-persistence`: How the authenticated partner/company session is kept alive across page navigation using the Keycloak refresh token, and how it ends cleanly when the refresh token expires.

### Modified Capabilities
<!-- None. auth-access-control (role separation, inactive-partner blocking) is unchanged and lives only inside the not-yet-archived build-partnerportal-mvp change; this change does not alter its requirements. -->

## Impact

- **Frontend:** `frontend/src/lib/auth.js` (refresh logic, session lifecycle), `frontend/src/lib/api.js` (refresh-and-retry on a just-expired token). Native Web Components only, no new dependency, no CDN.
- **Config:** `backend/src/main/resources/keycloak/partnerportal-realm.json` — one-value `accessTokenLifespan` bump.
- **No backend Java change.** No entity, REST, or role-authorization change. Prototype scope, not production token hardening.
- User-facing texts stay German.
