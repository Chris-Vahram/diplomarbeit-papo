## Purpose

Diese Capability definiert den reproduzierbaren, abgesicherten Weg von einem geprüften Commit zu einer erreichbaren Partnerportal-Instanz einschließlich commit-kohärenter Release-Auslieferung, synthetischer Prüfdaten, Persistenz, Backup und isoliertem Wiederherstellungsnachweis.

## ADDED Requirements

### Requirement: Automated build on push to main
The system SHALL automatically run the backend test suite on every push to `main` and on every pull request targeting `main`.

#### Scenario: Push to main triggers tests
- **WHEN** a commit is pushed to `main`
- **THEN** the GitHub Actions workflow runs the complete backend test suite

#### Scenario: Pull request triggers tests
- **WHEN** a pull request is opened or updated against `main`
- **THEN** the GitHub Actions workflow runs the complete backend test suite

### Requirement: Test gate blocks build and deploy
The system SHALL NOT build or deploy a container image unless the test job for that commit has passed.

#### Scenario: Tests fail
- **WHEN** the backend test suite fails
- **THEN** neither the build-and-push job nor the deploy job runs

#### Scenario: Tests pass on main
- **WHEN** the backend test suite succeeds for a push to `main`
- **THEN** the build-and-push job runs

### Requirement: Immutable and least-privilege workflow dependencies
The workflow SHALL reference every GitHub Action by a verified full-length commit SHA and SHALL grant each job only the permissions it needs.

#### Scenario: Workflow dependency audit
- **WHEN** the workflow is reviewed
- **THEN** every `uses:` entry references a 40-character commit SHA from the expected upstream repository
- **AND** the workflow defaults to `contents: read`
- **AND** only the image-publishing job has `packages: write`

### Requirement: Container image build and publish
The system SHALL build a Quarkus JVM-mode container image for `linux/amd64` and publish it to GHCR as a private package after every successful push to `main`.

#### Scenario: Successful build produces immutable image tag
- **WHEN** the build-and-push job runs for commit `<sha>` on `main`
- **THEN** the image is published with the exact tag `<sha>`
- **AND** the GHCR package visibility is private
- **AND** production deployment does not resolve `latest`

### Requirement: Commit-coherent release delivery
The system SHALL deliver the backend image reference, Compose definition, deployment files, frontend files, and release marker from the same triggering commit while preserving VM-local configuration.

#### Scenario: Release bundle is transferred
- **WHEN** deployment runs for commit `<sha>`
- **THEN** the job checks out `<sha>` and streams one uncompressed Git archive no larger than 25 MiB containing exactly `docker-compose.prod.yml`, `deploy/`, and `frontend/` to `upload <sha>`
- **AND** the server writes `RELEASE_SHA` with `<sha>` only after verifying the archive
- **AND** `~/papo-deploy/.env.prod` is neither transferred nor overwritten

#### Scenario: Release identity is checked before activation
- **WHEN** the remote deployment command starts
- **THEN** the requested SHA, `RELEASE_SHA`, the release-directory name, and `IMAGE_TAG` all equal the triggering commit SHA
- **AND** the requested SHA equals the current protected `origin/main` fetched with a VM-local read-only repository key
- **AND** the transferred archive is byte-equal to a server-generated Git archive for the three allowlisted paths and that commit
- **AND** a mismatch fails the deployment before Compose changes the running stack

### Requirement: Automated deployment via restricted SSH
The system SHALL deploy a successfully published release through native OpenSSH with a dedicated forced-command identity, verify the VM host key before connecting, serialize activation, reject superseded workflow runs, and surface deployment failures without claiming success.

#### Scenario: Runner establishes trusted native SSH transport
- **WHEN** a release is transferred or a remote command is executed
- **THEN** the scanned ED25519 host fingerprint equals the configured repository variable before connection
- **AND** native `scp` and `ssh` use only temporary mode-600 key and known-host files
- **AND** those temporary files are removed when the step exits

#### Scenario: Deploy key cannot obtain general VM access
- **WHEN** the Actions deploy key authenticates
- **THEN** it is mapped to a dedicated operating-system account with no Docker-group membership and no readable access to production configuration
- **AND** OpenSSH `restrict` plus the forced command invoke only the root-owned `/usr/local/libexec/papo-deploy-gateway` through its exact sudoers grant
- **AND** the gateway accepts only `upload <sha>`, `verify <sha>` and `activate <sha>` with one exact lowercase 40-character hexadecimal SHA
- **AND** interactive shells, arbitrary commands, forwarding, reads, Docker commands and every other sudo or filesystem operation are rejected

#### Scenario: Uploaded release is bound to protected main
- **WHEN** the forced-command gateway receives an archive and activation request for `<sha>`
- **THEN** the upload is rejected above 25 MiB and is never extracted
- **AND** a VM-local root-owned read-only GitHub deploy key fetches protected `origin/main`
- **AND** upload, verification and activation proceed only when `<sha>` is the current `origin/main` and the upload equals a server-generated canonical Git archive byte-for-byte
- **AND** only that server-generated archive is extracted and receives the server-written release marker
- **AND** client-supplied scripts or shell text are never executed

#### Scenario: Verification cannot activate a release
- **WHEN** the key requests `verify <sha>` for the current valid uploaded release
- **THEN** the same source and archive checks run
- **AND** no release directory, Compose service, Docker resource or `current` link changes

#### Scenario: Concurrent or delayed deployment is superseded
- **WHEN** a deployment obtains the server-side lock
- **THEN** its SHA is compared with the freshly fetched protected `origin/main` inside the lock
- **AND** a run whose SHA is no longer current exits without changing Compose services or `current`

#### Scenario: First deployment starts the pinned stack
- **WHEN** no active production release exists and all prerequisites are complete
- **THEN** Compose starts the full release using the exact backend commit tag and digest-pinned stateful images

#### Scenario: Routine deployment leaves stateful services untouched
- **WHEN** an active production release exists and a new backend/frontend release is deployed
- **THEN** only the exact backend image is pulled
- **AND** only backend and Caddy are applied with dependencies disabled
- **AND** Postgres and Keycloak are not recreated

#### Scenario: Deployment succeeds
- **WHEN** the exact release returns HTTP 200 for the frontend and HTTP 401 for the protected backend probe within the bounded retry window
- **THEN** `~/papo-deploy/current` points to that release

#### Scenario: Deployment command fails
- **WHEN** host-key verification, SSH, transfer, image pull, Compose application, or health checking returns a non-zero status
- **THEN** the workflow is marked failed
- **AND** the previous release remains identifiable for the documented SHA-based manual rollback

### Requirement: No live credentials are committed
The system SHALL keep all live credentials outside the repository and SHALL expose each credential only to the component that consumes it.

#### Scenario: Repository contains no live credentials
- **WHEN** workflow, Compose, realm template, application configuration, documentation, and deployment scripts are inspected
- **THEN** they contain only placeholders or environment references
- **AND** the production realm template contains no users or credential values

#### Scenario: Credentials remain in their consumption boundary
- **WHEN** production is configured
- **THEN** GitHub Actions contains only the VM host and dedicated deploy private key
- **AND** the public VM host fingerprint is supplied as a non-secret repository variable
- **AND** database, Keycloak, GHCR-read, and object-storage credentials remain VM-local
- **AND** the Actions key cannot read those VM-local credentials or invoke Docker directly

#### Scenario: Production backend configuration is environment-driven
- **WHEN** the backend starts in `%prod`
- **THEN** datasource and OIDC settings come from Compose-provided environment variables
- **AND** `%dev` and `%test` keep their established local behavior

### Requirement: Production topology uses a single origin
The system SHALL run frontend, backend, Postgres, Keycloak, and Caddy on one self-managed VM and SHALL expose browser-facing production traffic through `https://paportal.at`.

#### Scenario: Production frontend uses same-origin routes
- **WHEN** the frontend is not served on the documented local development port 3000
- **THEN** API requests target `/api`
- **AND** authentication requests target `/auth`
- **AND** no production CORS configuration is required

#### Scenario: Local frontend development remains usable
- **WHEN** the frontend is served on localhost or `127.0.0.1` at port 3000
- **THEN** API and authentication requests target the documented local backend and Keycloak ports

#### Scenario: Automatic HTTPS
- **WHEN** the production domain is requested
- **THEN** HTTP redirects to HTTPS
- **AND** a browser-valid certificate is served

#### Scenario: Fresh Keycloak deployment is reproducible
- **WHEN** Keycloak starts against a new production database
- **THEN** it imports the versioned production realm template without demo users
- **AND** the rendered `redirectUris` and `webOrigins` use the same production domain as `KC_HOSTNAME`

#### Scenario: Shared Postgres instance has isolated databases
- **WHEN** Postgres initializes a fresh production volume
- **THEN** it provisions `partnerportal` and `keycloak` databases
- **AND** the application and Keycloak connect only to their respective database

#### Scenario: Stateful container inputs are immutable
- **WHEN** the production Compose definition is reviewed or applied
- **THEN** Postgres, Keycloak, and Caddy images are referenced by verified immutable digests

### Requirement: Synthetic production verification data
The system SHALL use only synthetic identities and business data and SHALL support verification of both role and partner isolation.

#### Scenario: Verification identities and matching data exist
- **WHEN** production smoke testing begins
- **THEN** Chris has privately created one company identity and at least two partner-organization identities with random passwords
- **AND** each partner username equals its matching canonical partner number
- **AND** matching active partner records, synthetic articles, enablements, and separate customers exist in the application

#### Scenario: Cross-partner isolation can be tested
- **WHEN** either partner attempts to access the other partner's customer or order identifier
- **THEN** the request is denied without exposing the other partner's data

### Requirement: Persistent data survives restarts
The system SHALL persist Postgres and Caddy state in Docker volumes.

#### Scenario: Container and VM restart preserve state
- **WHEN** the production containers or VM restart
- **THEN** application data, Keycloak state, and Caddy certificate state remain available

### Requirement: Database backups are private, local, and encrypted offsite
The system SHALL create scheduled dumps of both production databases with restrictive permissions, retain the newest 14 local dumps per database, and copy every successful dump through a VM-local encrypted `rclone` destination.

#### Scenario: Scheduled backup succeeds
- **WHEN** the backup schedule triggers with both databases and object storage reachable
- **THEN** the script reads `~/papo-deploy/.env.prod` and writes outside immutable release directories to persistent `~/papo-deploy/backups`
- **AND** backup directories are mode 700 and dump files are mode 600
- **AND** each dump becomes visible under its final name only after `pg_dump` succeeds
- **AND** a timestamped dump is created for each database
- **AND** each dump is copied through the configured encrypted offsite destination
- **AND** local dumps beyond the newest 14 per database are pruned

#### Scenario: Offsite upload fails
- **WHEN** a local dump succeeds but its offsite copy fails
- **THEN** the local dump remains available
- **AND** the backup command exits non-zero so the failure is observable

#### Scenario: Restore is proven without touching production
- **WHEN** backup readiness is verified
- **THEN** one offsite backup of each database is downloaded and restored into a temporary PostgreSQL 16 container and disposable volume whose names use the guarded `papo-restore-` prefix
- **AND** the restored schemas are queried successfully
- **AND** only the disposable container, volume, and downloaded files are removed
- **AND** neither production database is a restore target
