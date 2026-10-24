## MODIFIED Requirements

### Requirement: Partner validation
The system SHALL validate required partner fields before saving and prevent duplicate partner identifiers within the same company. Duplicate detection SHALL be case-insensitive and SHALL apply identically to every partner-creation and partner-update path (manual create/update by the company and Excel/CSV import). Partners of other companies SHALL NOT count as duplicates.

#### Scenario: Missing required field
- **WHEN** the company saves a partner with a required field empty
- **THEN** the system rejects the save and shows an understandable validation message

#### Scenario: Duplicate partner identifier
- **WHEN** the company saves a partner with an identifier already used in the same company
- **THEN** the system rejects the save and reports the duplicate

#### Scenario: Duplicate identifier differing only by case
- **WHEN** the company manually creates or updates a partner with an identifier that already exists in the same company under a different letter case (e.g. `"p-1001"` when `"P-1001"` exists)
- **THEN** the system rejects the save as a duplicate, matching the case-insensitive check already applied on import

#### Scenario: Same identifier in another company
- **WHEN** company B creates a partner `P-1001` while company A already has `P-1001`
- **THEN** the partner is stored for company B

### Requirement: Company manages partners
The company SHALL be able to create, view, edit, and manage its own partner records centrally, including partner master data, a commission percentage (`Partner.commissionPercent`), and an active/inactive status (`Partner.active`). Partners of other companies SHALL be invisible and unchangeable.

#### Scenario: Create partner
- **WHEN** the company submits a new partner with valid master data and a commission percentage
- **THEN** the system stores the partner for the caller's company and shows a success confirmation

#### Scenario: Set commission percentage
- **WHEN** the company selects a commission percentage from the dropdown (5/10/15) or enters a custom value
- **THEN** the system saves it as `Partner.commissionPercent` and applies it to future commission calculations

#### Scenario: Deactivate partner
- **WHEN** the company sets `Partner.active` to false
- **THEN** the partner can no longer log in or create orders (see auth-access-control)

#### Scenario: Foreign partner
- **WHEN** a company user lists partners or opens, edits, deactivates, or manages logins, trade proof, enablements, customers or shop of a partner of another company
- **THEN** that partner is absent from the list and every id-based request returns 404 without change

### Requirement: Partner login linking via partner number
The system SHALL resolve an authenticated partner user through the OIDC-subject binding defined in `auth-access-control`, not through the partner number. Partner numbers SHALL be the visible business identifier, unique per company in one canonical case. Changing a partner number or Keycloak username SHALL NOT change a binding. The data import SHALL NOT create Keycloak users or bindings. The Partner entity SHALL NOT carry a Keycloak username column.

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
- **WHEN** a partner with number `P-1001` exists and the same company tries to create another numbered `p-1001`
- **THEN** the create is rejected as a duplicate

#### Scenario: Unlinked login is rejected with unchanged German message
- **WHEN** a partner-role token's subject has no binding
- **THEN** the system responds 403 with "Kein Partnerkonto mit diesem Login verknüpft."

#### Scenario: Import does not provision access
- **WHEN** an import creates Partner records
- **THEN** no Keycloak account and no login binding is created
