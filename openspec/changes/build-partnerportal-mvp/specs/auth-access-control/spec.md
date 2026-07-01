## ADDED Requirements

### Requirement: Role-based authentication
The system SHALL authenticate users via Keycloak and assign exactly one role: `company` or `partner`. End customers do not authenticate in the MVP.

#### Scenario: Company logs in
- **WHEN** a user with the `company` role authenticates via Keycloak
- **THEN** the system grants access to company views (partner management, product management, import, all orders, commission)

#### Scenario: Partner logs in
- **WHEN** a user with the `partner` role authenticates via Keycloak
- **THEN** the system grants access only to that partner's internal area (dashboard, enabled products, own master data, own orders, order creation)

### Requirement: Protected views require authorization
Protected internal views and endpoints SHALL NOT be reachable via direct URL without a valid session and the required role.

#### Scenario: Unauthenticated deep link
- **WHEN** an unauthenticated request targets a protected view or API endpoint
- **THEN** the system denies access and redirects to login / returns an unauthorized response

#### Scenario: Partner accesses company-only view
- **WHEN** a `partner` requests a company-only endpoint (e.g. partner management)
- **THEN** the system denies access with an unauthorized response

### Requirement: Inactive partner blocking
The system SHALL prevent inactive/disabled partners from logging into the internal area and from creating new orders. Their existing orders remain visible to the company.

#### Scenario: Inactive partner login attempt
- **WHEN** a partner whose status is inactive attempts to log in
- **THEN** the system denies access with a clear message

#### Scenario: Company still sees inactive partner's orders
- **WHEN** the company views all orders
- **THEN** orders previously created by a now-inactive partner remain visible
