## MODIFIED Requirements

### Requirement: Automated deployment via SSH
The system SHALL deploy the newly built image to the production VM over SSH. The deployment SHALL report success only after the exact new backend image responds through the production API route. It SHALL keep the old frontend active until that check passes and SHALL attempt to restore the prior immutable backend image if activation fails, without requiring the pipeline to hold standing VM access outside the workflow run.

#### Scenario: Deploy job runs after successful build
- **WHEN** the build-and-push job completes successfully for a push to `main`
- **THEN** the deploy job connects to the production VM via SSH using a GitHub Actions secret for the deploy key
- **AND** pulls and starts only the backend for that exact `main` SHA

#### Scenario: Only the Quarkus service is recreated
- **WHEN** a release is activated on the VM
- **THEN** only the Quarkus container is replaced with the new image
- **AND** the Postgres, Keycloak and Caddy containers continue running without interruption or data loss

#### Scenario: Healthy backend activates frontend
- **WHEN** the exact new backend image returns the expected unauthenticated HTTP 401 through the local HTTPS API route on consecutive checks without a container restart within the bounded readiness period
- **THEN** the frontend release pointer switches to the matching SHA
- **AND** the deploy job reports success

#### Scenario: New backend fails after Compose accepts it
- **WHEN** the backend does not pass the bounded readiness check
- **THEN** the frontend pointer remains on its previous release
- **AND** the gateway attempts to restore the previous immutable backend image when one exists
- **AND** the deploy job fails even if that restoration succeeds

#### Scenario: Frontend switch fails after backend becomes healthy
- **WHEN** the new backend passes readiness but the frontend pointer cannot be switched
- **THEN** the previous frontend remains selected
- **AND** the gateway attempts to restore the previous immutable backend image
- **AND** the deploy job fails

#### Scenario: Deploy job fails without breaking existing deployment
- **WHEN** SSH, image pull, Compose activation or backend readiness fails
- **THEN** the deploy job reports failure rather than success
- **AND** the previous frontend remains selected
- **AND** the gateway attempts to restore a previous immutable backend image if backend replacement may have begun

#### Scenario: No safe rollback image or rollback fails
- **WHEN** activation fails and no previous immutable backend image exists, or restoring it does not become healthy
- **THEN** the gateway reports failure and leaves the frontend pointer unchanged
- **AND** it does not report a successful rollback

#### Scenario: Deployment process receives a termination signal during activation
- **WHEN** the gateway receives HUP, INT or TERM after backend replacement begins
- **THEN** it keeps or restores the previous frontend pointer
- **AND** attempts to restore and verify the previous immutable backend image
- **AND** exits unsuccessfully without deleting an active frontend release
