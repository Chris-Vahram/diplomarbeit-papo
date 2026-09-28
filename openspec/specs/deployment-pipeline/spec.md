# deployment-pipeline Specification

## Purpose
Builds and deploys the PAPO services on the demo VM through tested, constrained workflows while preserving persistent data and healthy releases.

## Requirements

### Requirement: Automated build on push to main
The system SHALL automatically run the backend test suite on every push to `main` and on every pull request, using a GitHub Actions workflow.

#### Scenario: Push to main triggers tests
- **WHEN** a commit is pushed to `main`
- **THEN** the GitHub Actions workflow runs `./mvnw test` for the backend

#### Scenario: Pull request triggers tests
- **WHEN** a pull request is opened or updated against `main`
- **THEN** the GitHub Actions workflow runs `./mvnw test` for the backend

### Requirement: Test gate blocks build and deploy
The system SHALL NOT build or deploy a container image unless the test job for that commit has passed.

#### Scenario: Tests fail
- **WHEN** `./mvnw test` fails for a push to `main`
- **THEN** the build-and-push job SHALL NOT run
- **AND** the deploy job SHALL NOT run

#### Scenario: Tests pass
- **WHEN** `./mvnw test` succeeds for a push to `main`
- **THEN** the build-and-push job runs

### Requirement: Container image build and publish
The system SHALL build a Quarkus JVM-mode container image targeting `linux/amd64` and publish it to GitHub Container Registry (GHCR) as a private package on every successful push to `main`.

#### Scenario: Successful build produces tagged images
- **WHEN** the build-and-push job runs for a commit with SHA `<sha>` on `main`
- **THEN** an image is pushed to GHCR tagged `latest`
- **AND** an image is pushed to GHCR tagged `<sha>`
- **AND** the GHCR package visibility is private

#### Scenario: Build targets the deployment architecture
- **WHEN** the image is built
- **THEN** it SHALL be built for the `linux/amd64` platform to match the deployment VM's architecture (VM is `x86_64`, same as GitHub-hosted runners — no cross-arch build needed)

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

### Requirement: No secrets committed to the repository
The system SHALL keep all deployment credentials (database password, Keycloak admin credentials, deploy SSH key, VM public IP) out of the repository, sourced exclusively from GitHub Actions secrets or VM-local configuration not checked into version control.

#### Scenario: Repository contains no live credentials
- **WHEN** the repository is inspected (workflow files, compose files, `application.properties`)
- **THEN** no real secret values are present — only placeholders or references to `${{ secrets.* }}` / environment variables

#### Scenario: Production datasource and OIDC config are environment-driven
- **WHEN** the Quarkus application runs in the `%prod` profile inside `docker-compose.prod.yml`
- **THEN** the datasource URL, credentials, and OIDC auth-server URL are read from environment variables injected by Compose
- **AND** the `%dev` and `%test` profiles remain unchanged, continuing to work with `mvnw quarkus:dev` exactly as before this change

### Requirement: Production topology — single VM, containerized services
The system SHALL run the frontend, backend, Postgres, Keycloak, and reverse proxy as containers via Docker Compose on a single self-managed VPS, with the frontend and API served from the same origin.

#### Scenario: Single-origin routing
- **WHEN** a client requests the production domain
- **THEN** Caddy serves the static frontend at `/`
- **AND** routes `/api/*` to the Quarkus container
- **AND** routes `/auth/*` to the Keycloak container
- **AND** no cross-origin (CORS) configuration is required for frontend-to-API calls

#### Scenario: Automatic HTTPS
- **WHEN** the production domain is requested over HTTP or HTTPS
- **THEN** Caddy automatically provisions and renews a Let's Encrypt certificate and serves the site over HTTPS

#### Scenario: Keycloak realm reproducibility
- **WHEN** the Keycloak container starts
- **THEN** it imports the versioned realm JSON export (`backend/src/main/resources/keycloak/partnerportal-realm.json`)
- **AND** the imported realm's `redirectUris`/`webOrigins` are scoped to the production domain, not a wildcard

#### Scenario: Shared Postgres instance, isolated databases
- **WHEN** the Postgres container starts for the first time
- **THEN** it provisions two databases, `partnerportal` and `keycloak`, within the same Postgres instance
- **AND** the application and Keycloak each connect only to their own database

### Requirement: Persistent data survives restarts
The system SHALL persist Postgres data via a Docker volume so that data survives container and VM restarts.

#### Scenario: Container restart preserves data
- **WHEN** the Postgres container is stopped and restarted (e.g. as part of a deploy or a VM reboot)
- **THEN** all previously stored data (partners, products, orders, Keycloak realm state) is still present

### Requirement: Database backups, local now, offsite deferred
The system SHALL run a scheduled backup job that dumps both production databases and stores copies locally on the VM, with local retention pruning. Offsite replication to an S3-compatible object storage bucket via `rclone` is designed but deferred to a follow-up, provider TBD (design.md Decision 7) — not a requirement of this change; local backup bounds the VM-loss risk window for the evaluation period.

#### Scenario: Scheduled local dump
- **WHEN** the backup schedule triggers
- **THEN** `pg_dump` runs for both the `partnerportal` and `keycloak` databases
- **AND** the resulting dump files are written to a local directory on the VM

#### Scenario: Local retention
- **WHEN** local dump files exceed the configured retention window (last 14 dumps per database)
- **THEN** older local dump files are pruned

#### Scenario: Offsite copy (deferred, out of scope for this change)
- **WHEN** the offsite half is implemented in a follow-up change
- **THEN** each local dump SHALL be uploaded to an S3-compatible bucket via `rclone`
- **AND** upload credentials SHALL be sourced from VM-local configuration, never from GitHub Actions secrets
