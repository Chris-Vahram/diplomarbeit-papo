## ADDED Requirements

### Requirement: Partner login linking via partner number
The system SHALL resolve an authenticated partner user to its Partner record by matching the Keycloak username claim against `Partner.partnerNumber`, independent of letter case (Keycloak stores usernames lowercased; partner numbers are conventionally uppercase). Partner numbers SHALL be stored in a single canonical case so that no two partners can differ only by the case of their number, and the login match SHALL therefore resolve to at most one partner. Keycloak users are provisioned manually with username equal to the partner's partner number; the data import SHALL NOT create Keycloak users. A partner record whose partner number has no matching Keycloak user simply cannot log in. The Partner entity SHALL NOT carry a separate Keycloak username column.

#### Scenario: Linked partner logs in
- **WHEN** a user with the `partner` role authenticates and a Partner record exists whose `partnerNumber` matches the username claim
- **THEN** all partner endpoints are scoped to that Partner record (never trusting a client-supplied partner id)

#### Scenario: Lowercased Keycloak username still resolves
- **WHEN** the Keycloak username claim is `partner-demo` (lowercased by Keycloak) and the Partner record's canonical `partnerNumber` is `PARTNER-DEMO`
- **THEN** the login resolves to that partner (case-folding path, not an exact-case match)

#### Scenario: No two partners differ only by case
- **WHEN** a partner with number `P-1001` exists and the company tries to create another numbered `p-1001`
- **THEN** the create is rejected as a duplicate, so a login can never match two partners ambiguously

#### Scenario: Unlinked login is rejected with unchanged German message
- **WHEN** an authenticated `partner` user's username claim matches no Partner record's `partnerNumber`
- **THEN** the system responds 403 with the unchanged message "Kein Partnerkonto mit diesem Login verknüpft."

### Requirement: Partner form and API without Keycloak username field
The company partner create/edit form and the partner API SHALL NOT expose a Keycloak username field. Linking a login is done outside the portal by provisioning the Keycloak user with the partner's partner number as username.

#### Scenario: Create/edit form shows no Keycloak field
- **WHEN** the company opens the "Neuer Partner" or "Partner bearbeiten" form
- **THEN** no "Keycloak-Benutzername" input is shown

#### Scenario: Partner API payloads carry no Keycloak username
- **WHEN** the company reads or writes a partner via the API
- **THEN** the partner representation contains no `keycloakUsername` attribute
