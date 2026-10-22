# auth-access-control Specification

## Purpose
Keycloak-based login with the roles company and partner, server-side protection of every view and endpoint, linking of logins to partners, and blocking of inactive partners after authentication.

## Requirements

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

### Requirement: Partner login linking
Each partner login SHALL be linked to a Partner record through an explicitly stored binding of the verified OIDC subject (`sub`) from the trusted Keycloak issuer. A subject SHALL be bound to at most one Partner; a Partner MAY have several bindings. The system SHALL NOT infer a binding from username, e-mail, partner number or first login. Keycloak users are provisioned manually; the data import SHALL NOT create Keycloak users or bindings. During the transitional release only, a token whose subject has no binding MAY resolve through the former username-to-partner-number match, and only to a Partner that has never had a binding (active or revoked); this fallback SHALL be removed once the existing demo login is bound.

#### Scenario: Partner user resolves to Partner record
- **WHEN** a user with the `partner` role authenticates with a subject bound to a Partner
- **THEN** the backend scopes every partner endpoint to that Partner record, never trusting a client-supplied partner id

#### Scenario: Partner user without linked Partner record
- **WHEN** an authenticated `partner` user's subject has no binding and no fallback applies
- **THEN** the system responds 403 with "Kein Partnerkonto mit diesem Login verknüpft."

#### Scenario: Username changes
- **WHEN** the Keycloak username of a bound login changes but its subject stays the same
- **THEN** the login still resolves to the same Partner record

#### Scenario: Fallback cannot bypass an existing binding
- **WHEN** an unbound subject's username matches the partner number of a Partner that already has a binding
- **THEN** access is denied with 403 and the existing binding is unchanged

#### Scenario: Revoked login gets no fallback
- **WHEN** a revoked subject authenticates and its username equals the number of its former Partner
- **THEN** access is denied with 403

#### Scenario: Subject wins over username
- **WHEN** a token's subject is bound to Partner A while its username equals the number of Partner B
- **THEN** the request resolves to Partner A

#### Scenario: Fallback removed
- **WHEN** the transitional release has been replaced and an unbound subject authenticates
- **THEN** access is denied with 403 even if its username equals a partner number

### Requirement: Inactive partner blocking
The system SHALL prevent inactive/disabled partners from logging into the internal area and from creating new orders. The active/inactive state is stored solely as the DB flag `Partner.active` and enforced server-side after authentication (Keycloak does not hold this business state). Existing orders of that partner remain visible to the company.

#### Scenario: Inactive partner login attempt
- **WHEN** a partner whose `Partner.active` is false authenticates
- **THEN** the system blocks access to the internal area with a clear message

#### Scenario: Inactive partner order attempt
- **WHEN** an inactive partner attempts to create an order
- **THEN** the system rejects the action server-side

#### Scenario: Company still sees inactive partner's orders
- **WHEN** the company views all orders
- **THEN** orders previously created by a now-inactive partner remain visible
