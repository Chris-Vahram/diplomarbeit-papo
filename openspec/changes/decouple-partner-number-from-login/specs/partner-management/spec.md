## MODIFIED Requirements

### Requirement: Partner login linking via partner number
The system SHALL store each partner's BMD partner number as its unique visible business identifier and SHALL resolve an authenticated partner through an explicitly assigned OIDC subject from the trusted issuer. Changing a partner number or Keycloak username SHALL NOT change that binding. Keycloak users SHALL remain manually provisioned, imports SHALL NOT create users, and an unbound partner SHALL not be able to log in.

#### Scenario: Linked partner logs in
- **WHEN** a partner-role token has the subject assigned to one active Partner record
- **THEN** every partner endpoint is scoped to that record without a client-supplied partner id

#### Scenario: Lowercased Keycloak username still resolves
- **WHEN** Keycloak lowercases or changes a username while the assigned OIDC subject remains unchanged
- **THEN** login still resolves to the subject-bound Partner record

#### Scenario: Partner number changes
- **WHEN** the company replaces a demo partner number with its unique BMD number
- **THEN** the existing OIDC-subject binding, orders, customers, enablements and commissions remain linked to the same Partner record

#### Scenario: No two partners differ only by case
- **WHEN** a partner number conflicts under the documented canonical comparison
- **THEN** create or update is rejected as a duplicate

#### Scenario: Unlinked login is rejected with unchanged German message
- **WHEN** a token subject has no Partner binding
- **THEN** access is rejected with "Kein Partnerkonto mit diesem Login verknüpft."

#### Scenario: Duplicate subject binding
- **WHEN** a requested subject binding is already used by another partner
- **THEN** assignment is rejected without changing existing bindings

#### Scenario: Import does not provision access
- **WHEN** a BMD partner row creates a Partner record
- **THEN** no Keycloak account or automatic subject binding is created

### Requirement: Partner form and API without Keycloak username field
The ordinary company partner create/edit form and partner business API SHALL NOT expose a Keycloak username or OIDC-subject field. Keycloak users remain manually provisioned; a separate company-only controlled binding operation assigns the verified OIDC subject independently of partner number.

#### Scenario: Create/edit form shows no Keycloak field
- **WHEN** the company opens the "Neuer Partner" or "Partner bearbeiten" form
- **THEN** no Keycloak username or OIDC-subject input is shown

#### Scenario: Partner API payloads carry no Keycloak username
- **WHEN** the company reads or writes ordinary partner master data
- **THEN** the representation contains no `keycloakUsername` or `oidcSubject` attribute

## ADDED Requirements

### Requirement: BMD partner number integrity
Partner numbers SHALL be imported and stored as text, preserve significant leading zeros, and be unique under the documented canonical comparison.

#### Scenario: Duplicate BMD partner number
- **WHEN** manual entry or import supplies a number already assigned to another partner
- **THEN** the operation is rejected with a German duplicate message
