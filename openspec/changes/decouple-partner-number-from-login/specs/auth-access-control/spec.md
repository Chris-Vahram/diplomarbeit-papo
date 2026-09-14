## MODIFIED Requirements

### Requirement: Partner login linking
Each partner login SHALL be linked to exactly one Partner record through the OIDC subject from the trusted Keycloak issuer. The system SHALL NOT infer a binding from username, e-mail, partner number or first login. Keycloak users SHALL be provisioned manually, and imported Partner records without an assigned subject SHALL not be able to log in.

#### Scenario: Partner user resolves to Partner record
- **WHEN** a partner-role user authenticates with an explicitly assigned subject
- **THEN** the backend scopes every partner endpoint to that Partner record

#### Scenario: Partner user without linked Partner record
- **WHEN** an authenticated partner-role user has no assigned Partner record
- **THEN** the system responds 403 with "Kein Partnerkonto mit diesem Login verknüpft."

#### Scenario: Username or business number changes
- **WHEN** the username or BMD partner number changes but the trusted subject remains the same
- **THEN** the login continues to resolve to the same Partner record
