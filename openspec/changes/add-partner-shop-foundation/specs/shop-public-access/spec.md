## ADDED Requirements

### Requirement: Public shop route
The system SHALL serve every shop at `/shop/<kennung>` from a separate frontend entry that loads no portal pages, no portal scripts and no stored portal tokens.

#### Scenario: Shop entry isolated
- **WHEN** a visitor opens `/shop/huemer-elektro-gmbh`
- **THEN** the browser loads the shop entry only, and no request to `/api/company/*`, `/api/partner/*` or Keycloak is made

#### Scenario: Unknown identifier
- **WHEN** a visitor opens a shop identifier that does not exist or is not reachable
- **THEN** the same neutral page "Dieser Shop ist derzeit nicht erreichbar." is shown, without revealing whether the shop exists

### Requirement: Public shop API namespace
Unauthenticated shop access SHALL be limited to the namespace `/api/shop/{kennung}/…`. The partner SHALL be resolved server-side from the identifier only. Public endpoints SHALL expose no internal identifiers beyond what the shop needs (no partner number, commission, contact data unless published by the partner, no customer data).

#### Scenario: Internal API stays protected
- **WHEN** an unauthenticated request calls any `/api/company/*` or `/api/partner/*` endpoint
- **THEN** the system responds 401 as before

#### Scenario: Public status response is minimal
- **WHEN** a visitor's browser calls `/api/shop/{kennung}` for a reachable shop
- **THEN** the response contains only the identifier, the display name and reachability, and no commission or internal partner data

### Requirement: Rate limiting of public endpoints
Public shop endpoints SHALL be rate-limited per client IP. Exceeding the limit SHALL return HTTP 429 with a German message. Client IPs used for limiting SHALL NOT be persisted.

#### Scenario: Limit exceeded
- **WHEN** one client sends more requests than the configured limit within the window
- **THEN** further requests receive 429 "Zu viele Anfragen. Bitte kurz warten."

### Requirement: Search engine indexing control
All shop pages and public shop API responses SHALL carry `noindex` (`X-Robots-Tag: noindex` and `<meta name="robots" content="noindex">`) unless the deployment explicitly enables indexing and the shop is released and reachable. Staging deployments SHALL never enable indexing.

#### Scenario: Staging shop
- **WHEN** a shop is opened on a deployment without the indexing flag
- **THEN** the page and API responses carry noindex
