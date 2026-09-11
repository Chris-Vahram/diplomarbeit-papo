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
