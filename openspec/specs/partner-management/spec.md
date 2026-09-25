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
