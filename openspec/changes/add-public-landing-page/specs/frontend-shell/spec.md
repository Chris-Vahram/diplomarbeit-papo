## ADDED Requirements

### Requirement: Portal is served under /portal/
The internal portal SHALL be served under `/portal/` with its existing hash routes (e.g. `/portal/#/login`, `/portal/#/partner/dashboard`). `/portal` without trailing slash SHALL redirect to `/portal/`. The portal's document title SHALL use the prefix „PAPO“ instead of „Partnerportal“. Former `/#/…` links are not forwarded.

#### Scenario: Direct portal entry
- **WHEN** a user opens `/portal/` without a session
- **THEN** the portal login is shown

#### Scenario: Missing trailing slash
- **WHEN** a user opens `/portal`
- **THEN** the browser is redirected to `/portal/`

#### Scenario: Page title
- **WHEN** a logged-in partner opens the dashboard
- **THEN** the document title starts with „PAPO“

### Requirement: Explicit logout returns to the landing page
An explicit logout from the profile menu SHALL clear the session and open the landing page at `/`. An ended session caused by an expired or rejected refresh token SHALL keep routing to the portal login.

#### Scenario: User logs out
- **WHEN** a user selects „Abmelden“ in the profile menu
- **THEN** the session is cleared and the landing page at `/` is shown

#### Scenario: Session expires during work
- **WHEN** a protected request fails because the session can no longer be refreshed
- **THEN** the user is routed to `/portal/#/login`, not to the landing page

### Requirement: Login page without demo credentials
The login page SHALL NOT display demo login names or passwords. It SHALL show a link „Was ist PAPO?“ to the landing page and a hint recommending to bookmark `paportal.at/portal`. The bookmark hint SHALL be dismissible; the dismissal SHALL be remembered in the browser, and the login page SHALL work normally when browser storage is unavailable.

#### Scenario: Login page content
- **WHEN** a visitor opens `/portal/#/login`
- **THEN** no demo credentials are shown
- **AND** a link „Was ist PAPO?“ leads to `/`

#### Scenario: Dismiss the bookmark hint
- **WHEN** a visitor dismisses the bookmark hint and later opens the login page again in the same browser
- **THEN** the hint stays hidden

#### Scenario: Storage blocked
- **WHEN** browser storage throws or is empty
- **THEN** the login form works and the hint is simply shown again
