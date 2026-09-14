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
