## ADDED Requirements

> Review gate 13.09.2026: DNS, HTTPS, private GHCR and protected `main` are evidenced. Production remains unavailable behind Caddy because persistent PostgreSQL credentials differ from VM-local configuration. Recovery and restricted deploy-gateway work remain pending; required evidence is listed in `../../review.md`.

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
The system SHALL deploy through a forced-command SSH gateway that accepts only `upload`, `verify` and `activate` for one lowercase 40-hex SHA matching server-fetched `origin/main`. The uploaded frontend archive SHALL byte-match the server-generated canonical archive and contain only regular Git blobs.

#### Scenario: Deploy job runs after successful build
- **WHEN** the build-and-push job completes successfully for a push to `main`
- **THEN** the deploy job connects to the production VM via SSH using a GitHub Actions secret for the deploy key
- **AND** uploads, verifies and activates the exact commit through the gateway

#### Scenario: Only the Quarkus service is recreated
- **WHEN** the gateway activates a verified release
- **THEN** only the Quarkus container is replaced with the new image
- **AND** `IMAGE_TAG` equals the verified commit SHA
- **AND** the frontend is extracted directly to `frontend-live/releases/<sha>/` and `current` is switched atomically
- **AND** the Postgres and Keycloak containers continue running without interruption or data loss
- **AND** Caddy is not recreated for a normal release

#### Scenario: Deploy job fails without breaking existing deployment
- **WHEN** the SSH connection or the deploy commands fail
- **THEN** the previously running containers SHALL remain unaffected (no partial state that removes a working deployment)
- **AND** `frontend-live/current` SHALL keep its previous target

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
The system SHALL run a scheduled backup job that dumps both production databases and stores copies locally on the VM, with local retention pruning. Offsite replication is deferred under design Decision 7 and is not a requirement of this change. Local copies SHALL NOT be represented as protection against loss of the VM and its storage. This review does not verify or authorize backup operations.

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
