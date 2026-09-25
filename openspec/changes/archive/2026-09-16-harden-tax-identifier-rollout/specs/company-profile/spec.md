## ADDED Requirements

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
