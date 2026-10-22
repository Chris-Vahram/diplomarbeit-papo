# session-persistence Specification

## Purpose
Login sessions that survive access-token expiry through transparent refresh, retry the interrupted request, end cleanly when the refresh token expires and keep umlauts in claims.
## Requirements
### Requirement: Session survives access-token expiry

The system SHALL keep an authenticated partner or company session usable past the access-token lifetime by obtaining a new access token from the Keycloak refresh token, without requiring the user to log in again.

#### Scenario: Navigation after the access token expired

- **WHEN** a logged-in user navigates to another page or switches back to the tab after the access token's lifetime has passed, while the refresh token is still valid
- **THEN** the system obtains a fresh access token using the stored refresh token and the user stays on the requested page without being sent to the login screen

#### Scenario: Proactive refresh before expiry

- **WHEN** the current access token is present but within the near-expiry window
- **THEN** the system refreshes the token bundle before the next protected action so that action does not fail on an expired token

### Requirement: API request transparently retries after refresh

The system SHALL, when a protected API request fails because the access token has just expired, refresh the token once and retry the request a single time before treating it as an authentication failure.

#### Scenario: Request with a just-expired token

- **WHEN** a protected API call is made with an access token that has expired but whose refresh token is still valid
- **THEN** the system refreshes the token, retries the original request once with the new token, and returns the successful response to the caller

#### Scenario: Retry still unauthorized

- **WHEN** a protected API call is refreshed and retried once and the retry still returns 401
- **THEN** the system clears the session and routes the user to the login screen rather than retrying indefinitely

### Requirement: Clean session end on refresh-token expiry

The system SHALL end the session cleanly when the refresh token is expired or invalid, leaving no half-authenticated state.

#### Scenario: Refresh token expired

- **WHEN** a token refresh is attempted and Keycloak rejects the refresh token as expired or invalid
- **THEN** the system removes the stored session and returns the user to the login screen, and no protected page or API call remains reachable with the old tokens

### Requirement: Umlaut claims survive refresh

The system SHALL preserve correct UTF-8 decoding of token claims across a refresh so that names containing ä/ö/ü/ß remain intact.

#### Scenario: Partner name with umlaut after refresh

- **WHEN** a session belonging to a partner whose name contains ä/ö/ü/ß is refreshed
- **THEN** the decoded claims (e.g. the displayed username) show the umlaut characters correctly, not mojibake or ASCII substitutions
