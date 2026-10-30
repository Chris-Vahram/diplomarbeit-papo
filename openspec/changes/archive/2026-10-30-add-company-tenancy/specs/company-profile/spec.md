## MODIFIED Requirements

### Requirement: Company users manage current company profile
Authenticated company-role users SHALL read and update the profile of their own company, including its business name, tax identifier and optional order-notice e-mail address. Each company SHALL have exactly one profile. Partner-role users SHALL not read or modify this administrative profile.

#### Scenario: Company updates profile
- **WHEN** a company-role user submits valid profile data
- **THEN** the profile of the caller's company is updated and returned

#### Scenario: Other company's profile untouched
- **WHEN** a company user of company B reads or updates the profile
- **THEN** only company B's profile is returned or changed

#### Scenario: Partner requests company administration
- **WHEN** a partner-role user calls a company-profile administration endpoint
- **THEN** access is denied

#### Scenario: Duplicate profile creation
- **WHEN** initialization or concurrent requests attempt to create a second profile for the same company
- **THEN** the system retains exactly one profile per company
