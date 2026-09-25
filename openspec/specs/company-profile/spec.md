# company-profile Specification

## Purpose
Provides one centrally managed profile for the current portal company while deliberately avoiding multi-company tenancy and subdomain routing.

## Requirements

### Requirement: Company users manage current company profile
Authenticated company-role users SHALL read and update the single current portal-company profile, including its business name and tax identifier. Partner-role users SHALL not read or modify this administrative profile.

#### Scenario: Company updates profile
- **WHEN** a company-role user submits valid profile data
- **THEN** the single current profile is updated and returned

#### Scenario: Partner requests company administration
- **WHEN** a partner-role user calls a company-profile administration endpoint
- **THEN** access is denied

#### Scenario: Duplicate profile creation
- **WHEN** initialization or concurrent requests attempt to create a second current profile
- **THEN** the system retains exactly one profile

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

### Requirement: Existing demo database receives its current company profile
The system SHALL initialize the single known synthetic demo company profile even when demo partners already exist and the remaining demo dataset is therefore not reseeded.

#### Scenario: Existing demo partners but missing profile
- **WHEN** the application starts with the known synthetic demo partners and no current company profile
- **THEN** exactly one verified synthetic demo company profile is created

#### Scenario: Existing current profile
- **WHEN** the application starts with a current company profile
- **THEN** the profile remains unchanged

### Requirement: Company business name respects persistence limit
Company profile updates SHALL reject business names longer than 255 characters before persistence and SHALL leave the stored profile unchanged.

#### Scenario: Business name exceeds column length
- **WHEN** a company-role user submits a 256-character business name
- **THEN** the request is rejected with no stored profile change
