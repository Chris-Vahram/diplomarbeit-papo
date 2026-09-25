# business-tax-identifiers Specification

## Purpose
Defines one understandable, country-aware tax identifier for each business without assuming every country or Austrian business uses a VAT identifier.

## Requirements

### Requirement: One required business tax identifier
Each portal company and partner company SHALL have exactly one current tax identifier consisting of ISO 3166-1 alpha-2 country, explicit type and textual value. The three values SHALL be supplied and changed together.

#### Scenario: Complete identifier
- **WHEN** a company user submits a supported country, type and valid value
- **THEN** the normalized identifier is stored and returned to authorized company users

#### Scenario: Partial identifier
- **WHEN** any part is absent or type does not apply to the selected country
- **THEN** the request is rejected with a German validation message

### Requirement: Austria distinguishes UID and national tax number
For country `AT`, the system SHALL accept either an Austrian UID or Austrian national tax number and SHALL apply syntax validation for the selected type. Syntax validation SHALL NOT be represented as official register confirmation.

#### Scenario: Austrian business without UID
- **WHEN** an Austrian business selects national tax number and supplies a syntactically valid value
- **THEN** it satisfies the required tax-identifier field without a UID

#### Scenario: Invalid Austrian syntax
- **WHEN** an Austrian value does not match the selected type's documented syntax
- **THEN** it is rejected without changing stored data

### Requirement: Existing demo identifiers migrate before enforcement
The system SHALL add partner tax-identifier fields in a nullable-first rollout, populate only an explicitly enabled and exactly verified synthetic demo dataset, validate existing identifiers, and enforce complete database-level identifiers only after successful backfill. It SHALL abort atomically instead of inventing values when the dataset cannot be verified.

#### Scenario: Known demo database is upgraded
- **WHEN** existing synthetic demo partners have no tax identifier during startup
- **THEN** their verified identifiers are populated before all three database columns become mandatory

#### Scenario: Migration is repeated
- **WHEN** startup migration runs after all identifiers are already complete
- **THEN** existing values remain unchanged and database constraints remain mandatory

#### Scenario: Unknown incomplete partner exists
- **WHEN** an existing partner without a complete identifier is not part of the verified synthetic demo mapping
- **THEN** migration fails without partially writing identifiers or weakening the final constraints

#### Scenario: Demo-like real partner exists
- **WHEN** a partner number matches the demo mapping but the complete fixture or explicit demo setting does not match
- **THEN** migration fails without assigning a synthetic identifier

#### Scenario: Concurrent startup
- **WHEN** two application instances run the migration concurrently
- **THEN** they serialize and both complete without a lock-upgrade deadlock
