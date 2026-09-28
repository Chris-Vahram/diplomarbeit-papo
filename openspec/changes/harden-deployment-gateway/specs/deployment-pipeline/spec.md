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

#### Scenario: Healthy backend activates frontend
- **WHEN** the exact backend SHA returns HTTP 401 through the local HTTPS API route on consecutive checks without a container restart within the bounded readiness period
- **THEN** the gateway switches `frontend-live/current` to the matching release
- **AND** the deploy job reports success

#### Scenario: New backend fails after Compose accepts it
- **WHEN** the backend does not pass the bounded readiness check
- **THEN** the previous frontend remains selected
- **AND** the gateway attempts to restore the previous immutable backend image when one exists
- **AND** the deploy job fails even if that restoration succeeds

#### Scenario: Frontend switch fails after backend becomes healthy
- **WHEN** the new backend passes readiness but the frontend pointer cannot be switched
- **THEN** the previous frontend remains selected
- **AND** the gateway attempts to restore the previous immutable backend image
- **AND** the deploy job fails

#### Scenario: No safe rollback image or rollback fails
- **WHEN** activation fails and no previous immutable backend image exists, or restoring it does not become healthy
- **THEN** the gateway reports failure and leaves the frontend pointer unchanged
- **AND** it does not report a successful rollback

#### Scenario: Deployment process receives a termination signal during activation
- **WHEN** the gateway receives HUP, INT or TERM after backend replacement begins
- **THEN** it keeps or restores the previous frontend pointer
- **AND** attempts to restore and verify the previous immutable backend image
- **AND** exits unsuccessfully without deleting an active frontend release

#### Scenario: Verification has no deployment side effects
- **WHEN** a valid `verify` request is processed
- **THEN** no container, release directory or active frontend link is changed
