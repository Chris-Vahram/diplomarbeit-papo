## ADDED Requirements

### Requirement: Partner company in dashboard heading
The partner dashboard SHALL show “Dashboard der Firma <name>” for the authenticated partner. While loading or after a failed profile request it SHALL keep a neutral heading and provide a read-only retry. Existing dashboard metrics SHALL remain independently available.

#### Scenario: Company identified on dashboard
- **WHEN** the own profile is successfully loaded on the partner dashboard
- **THEN** its name appears in the dashboard heading as plain text

#### Scenario: Changed profile name
- **WHEN** a partner saves a changed name and next enters the dashboard
- **THEN** the dashboard loads and displays the updated own name

#### Scenario: Long name on mobile
- **WHEN** the company name is long on a narrow screen
- **THEN** it wraps without covering dashboard controls
