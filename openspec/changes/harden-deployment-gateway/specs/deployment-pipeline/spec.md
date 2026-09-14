## MODIFIED Requirements

### Requirement: Automated deployment via SSH
The system SHALL deploy through a forced-command SSH gateway that accepts only `upload`, `verify` and `activate` for one lowercase 40-hex SHA matching server-fetched `origin/main`. The uploaded frontend archive SHALL byte-match the server-generated canonical archive and contain only regular Git blobs.

#### Scenario: Deploy job runs after successful build
- **WHEN** build-and-push succeeds for a commit on `main`
- **THEN** the workflow uploads, verifies and activates that exact commit
- **AND** the backend image tag equals the verified SHA
- **AND** the frontend is extracted directly to `frontend-live/releases/<sha>/`
- **AND** `frontend-live/current` is switched atomically only after backend deployment succeeds

#### Scenario: Only the Quarkus service is recreated
- **WHEN** a normal release is activated
- **THEN** PostgreSQL, Keycloak and Caddy SHALL NOT be recreated

#### Scenario: Deploy job fails without breaking existing deployment
- **WHEN** the command, SHA, archive, repository revision, file type or runtime prerequisite is invalid
- **THEN** the gateway rejects the request
- **AND** the previous `frontend-live/current` target remains unchanged

#### Scenario: Verification has no deployment side effects
- **WHEN** a valid `verify` request is processed
- **THEN** no container, release directory or active frontend link is changed
