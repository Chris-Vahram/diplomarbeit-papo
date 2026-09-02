## ADDED Requirements

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
