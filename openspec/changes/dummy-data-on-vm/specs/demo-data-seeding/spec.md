## ADDED Requirements

### Requirement: Opt-in demo data seeding independent of launch mode
The system SHALL support seeding the existing demo dataset (partners, products, product enablements, and order history) into a database running under `LaunchMode.NORMAL` (packaged/deployed runs, e.g. the VM), gated by an explicit configuration flag that defaults to off.

#### Scenario: Flag disabled (default)
- **WHEN** the application starts under `LaunchMode.NORMAL` and the seed flag is not set (or `false`)
- **THEN** no demo data is seeded, matching current production behavior

#### Scenario: Flag enabled on an empty database
- **WHEN** the application starts under `LaunchMode.NORMAL`, the seed flag is `true`, and no `Partner` rows exist
- **THEN** the same demo partners, products, product enablements, and order history that dev/test seed are persisted

#### Scenario: Dev and test behavior unchanged
- **WHEN** the application starts under `LaunchMode.DEVELOPMENT` or `LaunchMode.TEST`
- **THEN** seeding behaves exactly as before this change, regardless of the new flag's value

### Requirement: Seeding never overwrites or duplicates existing data
The system SHALL only seed demo data when no `Partner` rows already exist, regardless of how seeding was triggered.

#### Scenario: Flag enabled but data already present
- **WHEN** the seed flag is `true` and at least one `Partner` row already exists (seeded previously, or from a real import)
- **THEN** the seeding step is skipped entirely, leaving existing data untouched

#### Scenario: Redeploy or restart with flag left on
- **WHEN** the VM container restarts or is redeployed with `SEED_DEMO_DATA=true` still set and demo data already present
- **THEN** no duplicate rows are created
