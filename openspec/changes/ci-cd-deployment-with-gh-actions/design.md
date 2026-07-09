## Context

The Partnerportal currently only exists as a local dev environment: `docker compose up` starts Postgres + Keycloak, `mvnw quarkus:dev` runs the backend against them, and the frontend is served by any static file server (`python3 -m http.server`). There is no build artifact, no image registry, no deployment target, and no automation. This design covers everything needed to get from "push to `main`" to "running, HTTPS-reachable instance" on a single Oracle Cloud Free Tier VM (`VM.Standard.A1.Flex`, ARM/Ampere, 2 OCPU / 12 GB RAM, Ubuntu 24.04).

Constraint that shapes most decisions below: **12 GB total RAM shared across every long-running process on the box** (Postgres, Keycloak, Quarkus, Caddy, backup cron) — resource-consciousness is a first-class requirement, not a nice-to-have.

Division of labour (per project owner): this change's code/config is written entirely by the agent. Anything touching live secrets or the live VM (creating the VM, SSH key generation, entering GitHub Actions secrets, creating the GHCR PAT, DNS records) is done by Tobias. The pipeline never gets standing SSH access — only the GitHub Actions run itself uses the deploy key, scoped to the workflow.

## Goals / Non-Goals

**Goals:**
- Push to `main` → tested, built, and deployed automatically, with zero manual steps on the happy path.
- Redeploy touches only the Quarkus container; Postgres and Keycloak keep running (and keep their data) across every deploy.
- Fit comfortably inside 12 GB RAM with headroom, on ARM.
- No secret ever committed to the repo; every credential is either a GitHub Actions secret or a VM-local credential entered by Tobias.
- Postgres data survives container restarts (volumes) and survives VM loss (offsite backup).

**Non-Goals:**
- Native-image builds (see Decision 2).
- Multi-VM, HA, blue-green/canary deploys, autoscaling.
- GitHub Pages or any CDN-hosted frontend.
- Real email sending, external end-customer shop, or any other `phase2_backlog.md` deferred feature — this change is infrastructure-only and touches no application capability.
- Secrets management systems (Vault, etc.) — GitHub Actions secrets + VM-local env files are sufficient at this scale.

## Decisions

### 1. Frontend hosting: same VM via Caddy, not GitHub Pages
Static assets cost negligible RAM/CPU regardless of host, so GitHub Pages buys no real resource saving. Hosting on the same origin as the API means the browser's calls to `/api/*` are same-origin — no CORS configuration, no separate Keycloak `webOrigins`/`redirectUris` entry for a second domain. This also lets the change **retire** the current CORS setup (`docs/SETUP.md` flags wildcard-ish localhost CORS as a prototype shortcut) rather than extend it to a new cross-origin case.
**Alternative considered**: GitHub Pages — rejected, adds CORS + redirect-URI complexity for no measurable benefit at this scale.

### 2. Quarkus build mode: JVM, not native
The VM is **arm64**; GitHub-hosted Actions runners are **x86_64**. Native-image compilation via Mandrel produces target-architecture machine code — cross-compiling for arm64 from an x86_64 runner needs either an arm64 builder container or QEMU-emulated *compilation* (CPU/memory-heavy, slow, and prone to OOM on a standard GH-hosted runner). JVM mode sidesteps this entirely: `mvn package` produces portable bytecode on the x86_64 runner, and `docker buildx build --platform linux/arm64` only has to copy that jar plus a pre-built arm64 JDK base image into a new layer — no emulated execution, cheap even under QEMU. Runtime cost is the trade-off: JVM Quarkus runs ~300-400 MB vs. native's ~50-100 MB. Given the 12 GB budget (Keycloak ~500 MB-1 GB, Postgres ~200-500 MB, Caddy ~20 MB, Quarkus ~400 MB, OS + backup cron ~500 MB — comfortably under 3 GB total), the RAM cost is affordable and the build-complexity savings are worth it.
**Alternative considered**: native-image — rejected for now due to arm64 cross-build friction; revisit if RAM pressure appears in practice (documented as a future optimization, not a blocker).
**Reuse**: `backend/src/main/docker/Dockerfile.jvm` already exists from Quarkus scaffolding — verify/adjust its base image for arm64 (Eclipse Temurin and the default `registry.access.redhat.com/ubi9/openjdk-*` images both ship multi-arch manifests, so no base-image swap should be needed, only verification).

### 3. Keycloak database: shared Postgres instance, separate database
One Postgres **container**, two **databases** (`partnerportal`, `keycloak`) created via a Postgres init script (`POSTGRES_MULTIPLE_DATABASES`-style entrypoint script, or a plain `CREATE DATABASE keycloak;` in an init `.sql` mounted to `/docker-entrypoint-initdb.d/`). Saves a second container's baseline memory and halves the backup surface (one Postgres process to pg_dump, looped over two DB names). Isolation loss (shared instance = shared blast radius for e.g. a Postgres crash) is acceptable for a single-VM Diplomarbeit deployment.
**Alternative considered**: separate Postgres container for Keycloak — rejected, doubles container overhead for isolation benefits that don't matter at this scale.

### 4. Domain routing: single domain, path-based, no CORS
Caddy routes by path on one domain: `/` → static frontend files, `/api/*` → Quarkus, `/auth/*` → Keycloak (`KC_HTTP_RELATIVE_PATH=/auth`, `KC_PROXY_HEADERS=xforwarded`, `KC_HOSTNAME=<domain>`). One TLS cert, one DNS A record, and — combined with Decision 1 — zero CORS configuration anywhere in the stack. Keycloak `redirectUris`/`webOrigins` in the realm export narrow to the single production domain (currently `*` in the dev-only realm — production realm config must not reuse the dev wildcard).
**Alternative considered**: subdomains (`app.`/`api.`/`auth.`) — Caddy would handle the extra certs trivially, but it reintroduces cross-origin CORS and multi-origin OIDC config for a benefit (cleaner per-service Caddy blocks) that isn't worth it here.

### 5. `%prod` config externalization
`application.properties` currently hardcodes `%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/...` and `%prod.quarkus.oidc.auth-server-url=http://localhost:8081/...`. Inside `docker-compose.prod.yml`, Postgres and Keycloak are reachable by Compose service name (e.g. `postgres:5432`, `keycloak:8080`), not `localhost` — the hardcoded values would simply fail to connect. Fix: replace the `%prod` block's fixed values with env-var placeholders (`${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`, `${KC_URL}`, `${KC_CLIENT_ID}`), injected via `docker-compose.prod.yml`'s `environment:` block, values sourced from a VM-local `.env` file (not committed) that Tobias populates. `%dev` and `%test` blocks are untouched — `mvnw quarkus:dev` keeps working exactly as documented in `docs/SETUP.md`.

### 6. GHCR package visibility: private, with VM-side PAT
The image is set to a **private** GHCR package. The VM authenticates with `docker login ghcr.io` using a read-only (`packages:read`) Personal Access Token before `docker compose pull`. This PAT is created and placed on the VM by Tobias directly (e.g. `docker login` once, credential persisted in the VM's Docker config) — it is explicitly **not** a GitHub Actions secret, since it's consumed by the VM's Docker daemon, not by a workflow run. Private visibility is an access control, not obfuscation — no bytecode protection is implied or needed, since only the VM (holding the PAT) can pull.

### 7. Backup: local + offsite (Oracle Object Storage)
A cron job on the VM runs `pg_dump` (looped over both databases) to a local file, then syncs that file to an Oracle Object Storage bucket via `rclone` configured against Oracle's **S3-compatible API endpoint** with a Customer Secret Key (access key/secret key pair) — this avoids setting up the heavier native `oci-cli` (which needs an API signing keypair and `~/.oci/config`). Local copies are pruned after N days; offsite copies protect against VM/disk loss, not just container restarts. Bucket credentials are VM-local (Tobias's domain), not GitHub Actions secrets.

### 8. CI test gating
`./mvnw test` (Quarkus Dev Services — needs Docker on the runner, which GitHub-hosted runners provide) runs on every push and PR. The `build-and-push` and `deploy` jobs both declare `needs: test`, so a failing test suite never reaches production. Test and build-and-push are otherwise independent of the VM's existence — this half of the pipeline is fully exercisable before the VM is provisioned.

### 9. Image tagging
Every build pushes two tags: `latest` (what `docker-compose.prod.yml` pulls) and `<git-sha>` (immutable, for audit trail / manual rollback via `docker compose up -d` after editing the compose file's tag). No separate rollback automation in this change — manual SHA pin is the rollback path, documented in tasks.md.

## Risks / Trade-offs

- **[Risk] JVM Quarkus RAM footprint under real load is higher than estimated, crowding Keycloak/Postgres on the 12 GB box.** → Mitigation: set explicit container memory limits in `docker-compose.prod.yml` per service (`mem_limit`) so one process can't starve the others; revisit native-image if headroom becomes an issue.
- **[Risk] Single Postgres instance means a Postgres failure takes down both the app and auth simultaneously.** → Mitigation: accepted trade-off per Decision 3, scale-appropriate; offsite backups (Decision 7) bound the damage.
- **[Risk] Path-based Keycloak routing (`/auth`) under a reverse proxy is a known source of subtle misconfiguration (redirect loops, wrong issuer URL in tokens).** → Mitigation: `KC_HOSTNAME` and `KC_HTTP_RELATIVE_PATH` set explicitly and tested against a real HTTPS request before declaring the deploy job done; documented as its own task with a manual verification step.
- **[Risk] Deploy job has no automated rollback or health check — a bad image can go live and stay live until someone notices.** → Mitigation: out of scope for this change (see Non-Goals); document the manual SHA-rollback procedure (Decision 9) so it's at least possible.
- **[Risk] GHCR private package + VM-side PAT is a credential Tobias must remember to rotate/manage outside the "everything is a GH secret" model.** → Mitigation: explicitly documented in proposal.md's Impact section and called out as a VM-local (not repo, not GH Actions) credential.

## Migration Plan

1. Externalize `%prod` config (Decision 5) — safe, additive, no behavior change to `%dev`/`%test`.
2. Stand up the full stack locally via Docker Compose (Quarkus container included) to validate the Dockerfile and env-var wiring before anything touches GitHub Actions or the VM.
3. Add the `test` → `build-and-push` workflow (Decisions 8, 9, 6) — independently testable via a throwaway public/private GHCR package, no VM required.
4. Author `docker-compose.prod.yml` + Caddy config (Decisions 1, 3, 4) — testable locally by running it against `localhost` before pointing it at a real domain.
5. Add the `deploy` job (SSH via `appleboy/ssh-action`) — cannot be exercised end-to-end until the VM exists; gate its rollout on Tobias provisioning the VM and entering the deploy secrets.
6. Add the backup cron (Decision 7) — last, since it depends on the VM and Object Storage credentials existing.

No live traffic exists yet to migrate — this is a first deployment, not a cutover. Rollback (if a deploy breaks the VM) is: SSH in manually (Tobias), edit `docker-compose.prod.yml`'s image tag to a known-good SHA, `docker compose up -d`.

## Open Questions

- Production domain name — not yet chosen/owned. Blocks final Caddy/Keycloak-hostname config and the deploy job's first real run; everything up to that point is domain-agnostic.
- Whether Oracle Free Tier Object Storage's 10 GB limit is sufficient long-term for retained backups, or whether a pruning/retention policy is needed sooner rather than later — start with a simple retention window (e.g. last 14 daily dumps) and revisit if the client's real order volume changes the calculus.
