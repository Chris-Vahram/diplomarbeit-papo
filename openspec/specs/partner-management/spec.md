# partner-management Specification

## Purpose
TBD - created by archiving change add-list-search-pagination. Update Purpose after archive.

## Requirements

### Requirement: Manual partner creation starts with five percent commission
The company-facing new-partner form SHALL preselect 5 percent commission every time a new creation is started. The company SHALL retain the ability to select 5, 10, 15 or a valid custom percentage. Editing SHALL display the partner's persisted percentage. The change SHALL NOT introduce an API/import default or change existing partner values or historical commission records.

#### Scenario: New partner without changing the preset
- **WHEN** the company opens a new partner form and submits otherwise valid data without changing the commission choice
- **THEN** the submitted and stored commission percentage is 5

#### Scenario: Deliberate commission choice
- **WHEN** the company selects another preset or enters a valid custom percentage
- **THEN** that explicit value is submitted and stored

#### Scenario: Editing and returning to creation
- **WHEN** the company edits a partner with 10 percent or a custom percentage and subsequently starts a new creation
- **THEN** the edit form displays the persisted value, while the new form starts at 5 percent

#### Scenario: Cancelled form does not change defaults
- **WHEN** the company changes the creation preset, cancels and opens another new-partner form
- **THEN** no partner was created and the new form again starts at 5 percent

### Requirement: Partner list search, filter, and pagination
The company partner list SHALL support searching by name, and pagination, to remain usable at the client's partner scale (~200 partners). Searching SHALL treat German special characters (ä/ö/ü/ß) correctly (case-insensitive match includes umlauts).

#### Scenario: Company searches partners by name
- **WHEN** the company enters a search term in the partner list
- **THEN** the system shows only partners whose name matches the search term

#### Scenario: Search with umlauts
- **WHEN** the company searches for "müller" and a partner "Müller GmbH" exists
- **THEN** that partner is found (case-insensitive umlaut match)

#### Scenario: Company pages through partners
- **WHEN** the partner list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the company navigate to further pages

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

### Requirement: Partner validation
The system SHALL validate required partner fields before saving and prevent duplicate partner identifiers. Duplicate detection SHALL be case-insensitive and SHALL apply identically to every partner-creation and partner-update path (manual create/update by the company and Excel/CSV import).

#### Scenario: Missing required field
- **WHEN** the company saves a partner with a required field empty
- **THEN** the system rejects the save and shows an understandable validation message

#### Scenario: Duplicate partner identifier
- **WHEN** the company saves a partner with an already-used identifier
- **THEN** the system rejects the save and reports the duplicate

#### Scenario: Duplicate identifier differing only by case
- **WHEN** the company manually creates or updates a partner with an identifier that already exists under a different letter case (e.g. `"p-1001"` when `"P-1001"` exists)
- **THEN** the system rejects the save as a duplicate, matching the case-insensitive check already applied on import

### Requirement: Company manages partners
The company SHALL be able to create, view, edit, and manage partner records centrally, including partner master data, a commission percentage (`Partner.commissionPercent`), and an active/inactive status (`Partner.active`).

#### Scenario: Create partner
- **WHEN** the company submits a new partner with valid master data and a commission percentage
- **THEN** the system stores the partner and shows a success confirmation

#### Scenario: Set commission percentage
- **WHEN** the company selects a commission percentage from the dropdown (5/10/15) or enters a custom value
- **THEN** the system saves it as `Partner.commissionPercent` and applies it to future commission calculations

#### Scenario: Deactivate partner
- **WHEN** the company sets `Partner.active` to false
- **THEN** the partner can no longer log in or create orders (see auth-access-control)

### Requirement: Partner limited self-edit
A partner SHALL be able to edit limited own master data (e.g. contact data) but SHALL NOT change commission percentage, role, active status, or other central system data.

#### Scenario: Partner edits contact data
- **WHEN** a partner updates its own contact fields
- **THEN** the system saves the change

#### Scenario: Partner cannot change commission percentage
- **WHEN** a partner attempts to change its commission percentage
- **THEN** the system rejects the action (field not editable / unauthorized)

### Requirement: Company-only internal partner note
Each partner SHALL have one optional plain-text internal note of at most 4,000 characters. Only company-role APIs SHALL return or modify it; partner profile, import, export, order and commission responses SHALL omit it entirely.

#### Scenario: Company saves long operational note
- **WHEN** a company user saves a note containing at most 4,000 characters, line breaks and German characters
- **THEN** the exact plain text is stored and shown in company partner management

#### Scenario: Note exceeds limit
- **WHEN** a company user submits 4,001 characters
- **THEN** the request is rejected with a German validation message and the previous note remains

#### Scenario: Partner reads or modifies profile
- **WHEN** a partner retrieves or updates its own profile, including a manipulated note property
- **THEN** no note property is returned and the stored internal note remains unchanged

#### Scenario: Company clears note
- **WHEN** a company user saves an empty note
- **THEN** the internal note is removed

### Requirement: Company manages partner tax identity
Company users SHALL manage the partner company's required tax identifier. Partner self-service SHALL neither return an editable central tax field nor accept changes to it.

#### Scenario: Company updates partner tax identifier
- **WHEN** a company user submits a complete valid identifier
- **THEN** it is stored for that partner

#### Scenario: Partner attempts self-edit
- **WHEN** a partner adds tax fields to its profile update payload
- **THEN** the stored tax identifier remains unchanged and no internal field is exposed

### Requirement: Company partner list shows proof state
Company partner management SHALL show whether the current proof is missing, pending, accepted or rejected without embedding document bytes in ordinary partner responses.

#### Scenario: Company opens partner management
- **WHEN** partners have different proof states
- **THEN** each partner row or detail shows its current state and company can open the protected review flow
