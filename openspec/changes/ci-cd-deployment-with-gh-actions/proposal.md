## Why

Right now the Partnerportal only runs locally (`docker compose up` for Postgres/Keycloak + `mvnw quarkus:dev` + a static file server). There is no path from a merged commit to a running, reachable demo instance. The client (PAPO) and the thesis supervisor need a real, continuously-updated deployment to evaluate — not a screenshare of localhost. This change adds continuous deployment: every push to `main` builds the backend image, pushes it to GHCR, and redeploys it on a single Oracle Cloud Free Tier VM via SSH, without touching the long-running Postgres/Keycloak containers.

## What Changes

- Add a resource-lean Quarkus **JVM**-mode Dockerfile build path (reuse existing `backend/src/main/docker/Dockerfile.jvm` scaffold), targeting `linux/arm64` to match the Oracle VM's Ampere A1 architecture.
- Add a GitHub Actions workflow: on push to `main`, run backend tests (Dev Services) as a gate, then build + push the Quarkus image to **GHCR** (private package) tagged with both `latest` and the commit SHA.
- Add a deploy job (`appleboy/ssh-action`) that SSHes into the VM and runs `docker compose pull && docker compose up -d`, gated on the build job succeeding. The agent/pipeline gets no persistent SSH access outside of the workflow run itself; the deploy SSH key is a GitHub Actions secret.
- Add a production Docker Compose file (`docker-compose.prod.yml`, separate from the dev-only `docker-compose.yml`) covering: Quarkus (pulled from GHCR), Postgres (single instance, two databases — `partnerportal` + `keycloak`), Keycloak (realm imported from the existing versioned JSON export), Caddy (reverse proxy, automatic HTTPS, single-domain path-based routing: `/` frontend, `/api/*` backend, `/auth/*` Keycloak), and a scheduled pg_dump backup job.
- **BREAKING** (prod config only, not dev): externalize `%prod` datasource/OIDC URLs in `application.properties` to environment variables (`DB_URL`, `KC_URL`, etc.) instead of hardcoded `localhost` values, since prod containers reach each other by Compose service name. `%dev`/`%test` profiles and `mvnw quarkus:dev` are unaffected.
- Serve the static frontend from Caddy on the same VM/domain (not GitHub Pages) — same-origin, no CORS or Keycloak redirect-URI changes needed for the frontend↔backend pairing.
- Add a backup job (pg_dump, cron-scheduled) that writes locally on the VM **and** ships a copy to Oracle Object Storage (S3-compatible endpoint, `rclone`), so backups survive VM loss, not just container restarts.
- Document all required GitHub Actions secrets (DB password, Keycloak admin credentials, deploy SSH key, VM public IP) as placeholders/env templates — no real values or the GHCR pull PAT (VM-side, not a GH secret) are ever committed.

## Capabilities

### New Capabilities
- `deployment-pipeline`: CI/CD requirements — test gating, image build/push to GHCR, SSH-based deploy to the VM, production Compose topology (Caddy routing, shared Postgres w/ separate DBs, Keycloak realm import), and the dual-destination backup job.

### Modified Capabilities
- (none — no existing app-facing capability's requirements change; the `%prod` config externalization is an implementation detail of `deployment-pipeline`, not a behavior change to any existing capability spec)

## Impact

- **Affected code**: `backend/src/main/resources/application.properties` (`%prod` block only), `backend/src/main/docker/Dockerfile.jvm` (verify/adjust for arm64 JVM build), new `docker-compose.prod.yml`, new `.github/workflows/*.yml`, new Caddy config, new backup script.
- **Affected systems**: GitHub Actions (new workflow, `packages: write` permission, branch protection on `main`), GHCR (new private package), Oracle Cloud VM (new deploy target, not yet provisioned), Oracle Object Storage (new backup destination).
- **Dependencies**: none added to the Java build; adds `rclone` (or `aws-cli`) on the VM for Object Storage sync, and Caddy as a new container in prod topology only (not dev).
- **Out of scope**: native-image build (JVM mode chosen for arm64 cross-build simplicity — see design.md), GitHub Pages frontend hosting, multi-VM/HA topology, blue-green or canary deploys.
