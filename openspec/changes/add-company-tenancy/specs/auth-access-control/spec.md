## MODIFIED Requirements

### Requirement: Role-based authentication
The system SHALL authenticate users via Keycloak and assign exactly one role: `company` or `partner`. End customers do not authenticate in the MVP. A company-role user SHALL additionally need an active company login binding and SHALL act only within that company (see `company-tenancy`).

#### Scenario: Company logs in
- **WHEN** a user with the `company` role and an active company binding authenticates via Keycloak
- **THEN** the system grants access to company views (partner management, product management, import, all orders, commission) limited to the user's own company

#### Scenario: Company role without company binding
- **WHEN** a user with the `company` role but without an active company binding calls a company endpoint
- **THEN** the system responds 403 with "Kein Firmenkonto mit diesem Login verknüpft."

#### Scenario: Partner logs in
- **WHEN** a user with the `partner` role authenticates via Keycloak
- **THEN** the system grants access only to that partner's internal area (dashboard, enabled products, own master data, own orders, order creation)
