## ADDED Requirements

### Requirement: Portal company records trade-register identifier
The current portal-company profile SHALL require one trade-register identifier consisting of ISO country, register type and a 1–64 character value. Austria SHALL use type `GISA`; other countries SHALL use `OTHER_TRADE_REGISTER`.

#### Scenario: Company records Austrian GISA identifier
- **WHEN** a company-role user saves country `AT`, register type `GISA` and a valid nonblank value
- **THEN** the identifier is stored in the current company profile

#### Scenario: Partner accesses company compliance administration
- **WHEN** a partner-role user requests or modifies the portal company's register identifier
- **THEN** access is denied

#### Scenario: Invalid company register identifier
- **WHEN** any component is missing or country `AT` uses a type other than `GISA`
- **THEN** the profile update is rejected without changing stored data
