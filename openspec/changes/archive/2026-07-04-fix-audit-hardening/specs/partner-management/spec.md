## ADDED Requirements

### Requirement: Company links Keycloak login to partner
The company SHALL be able to set an optional, unique `keycloakUsername` on a partner when creating or editing it, linking a manually provisioned Keycloak login to that partner record. Import SHALL NOT set this field.

#### Scenario: Company links a login
- **WHEN** the company saves a partner with a `keycloakUsername` matching a manually provisioned Keycloak user
- **THEN** that user's next login resolves to this partner record

#### Scenario: Duplicate login link rejected
- **WHEN** the company saves a partner with a `keycloakUsername` already linked to another partner
- **THEN** the system rejects the save and reports the conflict

### Requirement: Commission percentage bounds
The system SHALL reject a commission percentage below 0 or above 100, and SHALL NOT accept an empty custom commission value as 0%.

#### Scenario: Empty custom value rejected
- **WHEN** the company selects the custom commission option and leaves the value empty
- **THEN** the form does not submit and shows an understandable validation message

#### Scenario: Out-of-range percentage rejected
- **WHEN** a commission percentage above 100 is submitted
- **THEN** the system rejects the save with an understandable validation message
