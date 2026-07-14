# security-hardening Specification

## Purpose
TBD - created by archiving change security-hardening-review-fixes. Update Purpose after archive.
## Requirements
### Requirement: Escape user-controlled data before HTML rendering
User-controlled text (partner name, Kunde name, username, and any other partner- or company-entered strings) SHALL be HTML-escaped before it is inserted into the DOM via `innerHTML`.

#### Scenario: Partner name rendered on company dashboard
- **WHEN** a partner sets their name to a string containing HTML markup (e.g. `<img src=x onerror=alert(1)>`) via `PUT /api/partner/profile` and that partner appears in a company dashboard chart
- **THEN** the company dashboard renders the name as literal text and does not execute the markup

#### Scenario: Kunde name rendered on partner dashboard
- **WHEN** a Kunde name containing HTML markup is rendered in a partner dashboard chart
- **THEN** the markup is displayed as text and not executed

#### Scenario: Username rendered in navigation
- **WHEN** the current user's username is rendered in the profile menu
- **THEN** it is HTML-escaped before insertion

### Requirement: Client-facing error messages hide internal detail
Error messages returned to the client SHALL NOT contain internal or third-party library detail (exception messages, class names, stack traces). Import and processing failures SHALL return a fixed German plain-language message; the underlying exception is logged server-side only.

#### Scenario: Import file cannot be parsed
- **WHEN** parsing an uploaded import file throws a POI/opencsv exception
- **THEN** the response contains a fixed German error message with no library exception text, and the original exception is written to the server log

### Requirement: Secrets sourced from environment configuration
Production configuration SHALL source database credentials and the OIDC issuer from environment variables (with development-safe defaults) rather than hardcoding them, so a packaged or demo run does not silently ship default credentials.

#### Scenario: Production profile reads credentials from environment
- **WHEN** the application runs under the `%prod` profile
- **THEN** database credentials and the OIDC issuer are taken from environment variables if set, falling back to documented defaults otherwise

