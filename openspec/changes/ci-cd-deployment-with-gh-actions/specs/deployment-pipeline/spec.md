## ADDED Requirements

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
The system SHALL build a Quarkus JVM-mode container image targeting `linux/arm64` and publish it to GitHub Container Registry (GHCR) as a private package on every successful push to `main`.

#### Scenario: Successful build produces tagged images
- **WHEN** the build-and-push job runs for a commit with SHA `<sha>` on `main`
- **THEN** an image is pushed to GHCR tagged `latest`
- **AND** an image is pushed to GHCR tagged `<sha>`
- **AND** the GHCR package visibility is private

#### Scenario: Build targets the deployment architecture
- **WHEN** the image is built
- **THEN** it SHALL be built for the `linux/arm64` platform to match the deployment VM's architecture

### Requirement: Automated deployment via SSH
The system SHALL deploy the newly built image to the production VM by connecting over SSH and running `docker compose pull && docker compose up -d`, without requiring the pipeline to hold standing access to the VM outside of the workflow run.

#### Scenario: Deploy job runs after successful build
- **WHEN** the build-and-push job completes successfully for a push to `main`
- **THEN** the deploy job connects to the production VM via SSH using a GitHub Actions secret for the deploy key
- **AND** runs `docker compose pull` followed by `docker compose up -d` in the production compose directory

#### Scenario: Only the Quarkus service is recreated
- **WHEN** `docker compose pull && docker compose up -d` runs on the VM
- **THEN** only the Quarkus container is replaced with the new image
- **AND** the Postgres and Keycloak containers continue running without interruption or data loss

#### Scenario: Deploy job fails without breaking existing deployment
- **WHEN** the SSH connection or the deploy commands fail
- **THEN** the previously running containers SHALL remain unaffected (no partial state that removes a working deployment)

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
The system SHALL run the frontend, backend, Postgres, Keycloak, and reverse proxy as containers via Docker Compose on a single Oracle Cloud VM, with the frontend and API served from the same origin.

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

### Requirement: Database backups, local and offsite
The system SHALL run a scheduled backup job that dumps both production databases and stores copies both locally on the VM and offsite in Oracle Object Storage.

#### Scenario: Scheduled local dump
- **WHEN** the backup schedule triggers
- **THEN** `pg_dump` runs for both the `partnerportal` and `keycloak` databases
- **AND** the resulting dump files are written to a local directory on the VM

#### Scenario: Offsite copy
- **WHEN** a local dump completes successfully
- **THEN** the dump file is uploaded to an Oracle Object Storage bucket via its S3-compatible API
- **AND** upload credentials are sourced from VM-local configuration, not from GitHub Actions secrets

#### Scenario: Local retention
- **WHEN** local dump files exceed the configured retention window
- **THEN** older local dump files are pruned, while offsite copies in Object Storage are retained independently of local pruning
