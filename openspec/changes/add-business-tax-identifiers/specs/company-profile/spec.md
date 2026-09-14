## Purpose

Provides one centrally managed profile for the current portal company while deliberately avoiding multi-company tenancy and subdomain routing.

## ADDED Requirements

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
