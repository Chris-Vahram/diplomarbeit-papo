## ADDED Requirements

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
