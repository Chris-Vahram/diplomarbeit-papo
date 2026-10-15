## MODIFIED Requirements

### Requirement: Partner login linking via partner number
The system SHALL resolve an authenticated partner user through the OIDC-subject binding defined in `auth-access-control`, not through the partner number. Partner numbers SHALL remain the unique visible business identifier and SHALL be stored in a single canonical case so that no two partners differ only by case. Changing a partner number or Keycloak username SHALL NOT change a binding. Keycloak users remain manually provisioned; the data import SHALL NOT create Keycloak users or bindings, and a Partner without a binding cannot log in once the transitional fallback is removed. The Partner entity SHALL NOT carry a Keycloak username column.

#### Scenario: Linked partner logs in
- **WHEN** a partner-role token carries a subject bound to an active Partner
- **THEN** all partner endpoints are scoped to that Partner record

#### Scenario: Lowercased Keycloak username still resolves
- **WHEN** Keycloak lowercases or renames the username of a bound login
- **THEN** the login still resolves to its Partner because only the subject is compared

#### Scenario: Partner number changes
- **WHEN** the company changes a bound partner's number
- **THEN** the binding and the partner's orders, customers, enablements and commission records remain linked to the same Partner record

#### Scenario: No two partners differ only by case
- **WHEN** a partner with number `P-1001` exists and the company tries to create another numbered `p-1001`
- **THEN** the create is rejected as a duplicate

#### Scenario: Unlinked login is rejected with unchanged German message
- **WHEN** a partner-role token's subject has no binding and no transitional fallback applies
- **THEN** the system responds 403 with "Kein Partnerkonto mit diesem Login verknüpft."

#### Scenario: Import does not provision access
- **WHEN** an import creates Partner records
- **THEN** no Keycloak account and no login binding is created

### Requirement: Partner form and API without Keycloak username field
The company partner create/edit form, the partner self-edit form and the ordinary partner API SHALL NOT expose a Keycloak username or OIDC-subject field. Login bindings SHALL be managed only through a separate company-only operation: list a partner's bindings, add a binding for a given subject, and revoke a binding. Adding SHALL reject a blank subject, a subject longer than 255 characters or containing whitespace (400), and a subject already used by another partner or already active for this partner (409 "Dieses Login ist bereits mit einem Partner verknüpft.") without changing existing bindings; re-adding a revoked subject of the same partner reactivates it. Revocation SHALL keep the binding as history. A revoke request for a login that does not belong to the given partner SHALL return 404 without change. Each binding SHALL record creation and revocation time and the acting company user. Subjects SHALL be displayed as escaped text.

#### Scenario: Create/edit form shows no Keycloak field
- **WHEN** the company opens the "Neuer Partner" or "Partner bearbeiten" form
- **THEN** no Keycloak username or OIDC-subject input is shown

#### Scenario: Partner API payloads carry no Keycloak username
- **WHEN** the company reads or writes ordinary partner master data, or a partner edits its own profile
- **THEN** the representation contains no `keycloakUsername` or `oidcSubject` attribute

#### Scenario: Company binds a login
- **WHEN** the company adds a valid, unused subject in the partner's "Logins" dialog
- **THEN** the binding is stored for that partner with time and acting user, and the dialog lists it

#### Scenario: Duplicate subject
- **WHEN** the company adds a subject already bound to the same or another partner
- **THEN** the request is rejected with 409 and no binding changes

#### Scenario: Company removes a login
- **WHEN** the company revokes a binding
- **THEN** that subject can no longer resolve to any partner, also not through the transitional fallback, and other bindings stay unchanged

#### Scenario: Login of another partner
- **WHEN** the company revokes a login id under a partner it does not belong to
- **THEN** the system responds 404 and no binding changes

#### Scenario: Only company may manage bindings
- **WHEN** an unauthenticated user or a partner calls the binding operations
- **THEN** access is denied with 401 or 403 respectively
