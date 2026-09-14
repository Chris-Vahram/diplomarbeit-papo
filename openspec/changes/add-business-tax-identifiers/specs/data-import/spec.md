## ADDED Requirements

### Requirement: Partner import validates tax identity consistently
Partner Excel/CSV import SHALL map tax country, type and value and apply the same normalization and validation as company REST management.

#### Scenario: Valid imported identifier
- **WHEN** a partner row maps all tax fields to a supported valid identifier
- **THEN** the accepted Partner record stores the normalized identifier

#### Scenario: Missing or invalid imported identifier
- **WHEN** a partner row omits part of the identifier or fails type-specific syntax validation
- **THEN** that row is rejected with a German reason in the import report
