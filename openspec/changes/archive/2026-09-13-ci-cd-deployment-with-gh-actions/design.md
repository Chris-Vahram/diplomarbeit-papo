## Context

> Current gate, 13.09.2026: P0 root cause confirmed as PostgreSQL password drift after the persistent volume was initialized. Recovery preserves the volume, creates local dumps first, rotates only the existing role password to the current VM-local value, then verifies OIDC/API/login/order behavior. DNS/HTTPS/GHCR/branch protection are evidenced. The forced-command gateway from `48ad4d8` is a source, not a cherry-pick: it must retain the current `frontend-live/releases/<sha>/current` deployment model.

The Partnerportal currently only exists as a local dev environment: `docker compose up` starts Postgres + Keycloak, `mvnw quarkus:dev` runs the backend against them, and the frontend is served by any static file server (`python3 -m http.server`). There is no build artifact, no image registry, no deployment target, and no automation. This design covers everything needed to get from "push to `main`" to "running, HTTPS-reachable instance" on a single VPS: `papo-server-01` (`papo@178.104.109.45`), 4 vCPU, 7.6 GB RAM, `x86_64`, Ubuntu 26.04 LTS, Docker 29.6.1 + Docker Compose v5.3.1 already installed.

**Provider change (2026-08-23):** earlier drafts of this design targeted an Oracle Cloud Free Tier VM (`VM.Standard.A1.Flex`, ARM/Ampere, 2 OCPU / 12 GB RAM, Ubuntu 24.04). That VM was never provisioned; the project moved to the VPS described above instead. The architecture shift (arm64 → x86_64) reopens Decision 2 below. Decision 7 (backup destination) also needs a new answer since it was Oracle-Object-Storage-specific.

Constraint that shapes most decisions below: **7.6 GB total RAM shared across every long-running process on the box** (Postgres, Keycloak, Quarkus, Caddy, backup cron) — resource-consciousness is a first-class requirement, not a nice-to-have. Tighter than the 12 GB originally planned for, so explicit `mem_limit` per service (Risk 1 below) matters more than it did on paper.

Division of labour: historical implementation steps below belong to the deployment change; live VM and credential operations belong to Tobias. Review 10.09.2026 authorizes only OpenSpec corrections. A key in authorized_keys can remain usable outside a workflow run; secret storage alone supplies neither an expiry nor a forced-command boundary. Actual restrictions are an unresolved evidence gate in review.md.

## Goals / Non-Goals

**Goals:**
- Push to `main` → tested, built, and deployed automatically, with zero manual steps on the happy path.
- Redeploy touches only the Quarkus container; Postgres and Keycloak keep running (and keep their data) across every deploy.
- Fit comfortably inside 7.6 GB RAM with headroom, on x86_64.
- No secret ever committed to the repo; every credential is either a GitHub Actions secret or a VM-local credential entered by Tobias.
- Persistent volumes are intended to preserve data across container restarts. Local dumps only help while their storage remains available; they do not protect against loss of the VM and its disk. Offsite remains deferred under Decision 7. No verified data-loss guarantee is claimed.

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

### 2. Quarkus build mode: JVM (reopened 2026-08-23, re-confirmed same day)
**Original reasoning (no longer applies):** the previously-planned VM was arm64; GitHub-hosted Actions runners are x86_64. Cross-compiling native-image for arm64 from an x86_64 runner needs either an arm64 builder or QEMU-emulated compilation (CPU/memory-heavy, slow, prone to OOM on a standard GH-hosted runner). JVM mode sidestepped this: `mvn package` produces portable bytecode, and `docker buildx build --platform linux/arm64` only had to copy that jar plus a pre-built arm64 JDK base image — no emulated execution.

**Reopened:** the actual VM (`papo-server-01`) is **x86_64** — same architecture as GitHub-hosted runners — so the cross-arch friction that drove the original JVM choice is gone, and native-image runtime footprint (~50-100 MB vs. JVM's ~300-400 MB) matters more on the tighter 7.6 GB budget than it would have on the originally-planned 12 GB.

**Re-confirmed: stay JVM.** Reasoning (Tobias, 2026-08-23): the RAM native would save (~300 MB) is slack the box already has — rough footprint estimate is ~2.5-3 GB total on a 7.6 GB box, comfortable headroom either way. Native-image's reflection risk lands squarely on this stack (OIDC, Hibernate/Panache, Jackson all use runtime reflection) and isn't guaranteed to build clean without dedicated testing — a time cost with no payoff when the RAM it would save isn't needed. Native remains a valid future optimization if real-load footprint turns out larger than estimated, not a requirement now.
**Build target**: `linux/amd64` (not arm64 — architecture assumption from the original Oracle plan no longer applies).
**Reuse**: `backend/src/main/docker/Dockerfile.jvm` already exists from Quarkus scaffolding, used as-is.

### 3. Keycloak database: shared Postgres instance, separate database
One Postgres **container**, two **databases** (`partnerportal`, `keycloak`) created via a Postgres init script (`POSTGRES_MULTIPLE_DATABASES`-style entrypoint script, or a plain `CREATE DATABASE keycloak;` in an init `.sql` mounted to `/docker-entrypoint-initdb.d/`). Saves a second container's baseline memory and halves the backup surface (one Postgres process to pg_dump, looped over two DB names). Isolation loss (shared instance = shared blast radius for e.g. a Postgres crash) is acceptable for a single-VM Diplomarbeit deployment.
**Alternative considered**: separate Postgres container for Keycloak — rejected, doubles container overhead for isolation benefits that don't matter at this scale.

### 4. Domain routing: single domain, path-based, no CORS
Caddy routes by path on one domain: `/` → static frontend files, `/api/*` → Quarkus, `/auth/*` → Keycloak (`KC_HTTP_RELATIVE_PATH=/auth`, `KC_PROXY_HEADERS=xforwarded`, `KC_HOSTNAME=<domain>`). One TLS cert, one DNS A record, and — combined with Decision 1 — zero CORS configuration anywhere in the stack. Keycloak `redirectUris`/`webOrigins` in the realm export narrow to the single production domain (currently `*` in the dev-only realm — production realm config must not reuse the dev wildcard).
**Alternative considered**: subdomains (`app.`/`api.`/`auth.`) — Caddy would handle the extra certs trivially, but it reintroduces cross-origin CORS and multi-origin OIDC config for a benefit (cleaner per-service Caddy blocks) that isn't worth it here.

**Status correction (10.09.2026):** `paportal.at` is confirmed by the meeting of 25.08.2026. Domain procurement is no longer open. Actual DNS/HTTPS/OIDC configuration and end-to-end behavior remain unverified in this session; Tobias must provide dated evidence for the deployed revision before readiness. No configuration or infrastructure change is authorized here.

### 5. `%prod` config externalization
`application.properties` currently hardcodes `%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/...` and `%prod.quarkus.oidc.auth-server-url=http://localhost:8081/...`. Inside `docker-compose.prod.yml`, Postgres and Keycloak are reachable by Compose service name (e.g. `postgres:5432`, `keycloak:8080`), not `localhost` — the hardcoded values would simply fail to connect. Fix: replace the `%prod` block's fixed values with env-var placeholders (`${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`, `${KC_URL}`, `${KC_CLIENT_ID}`), injected via `docker-compose.prod.yml`'s `environment:` block, values sourced from a VM-local `.env` file (not committed) that Tobias populates. `%dev` and `%test` blocks are untouched — `mvnw quarkus:dev` keeps working exactly as documented in `docs/SETUP.md`.

### 6. GHCR package visibility: private, with VM-side PAT
The image is set to a **private** GHCR package. The VM authenticates with `docker login ghcr.io` using a read-only (`packages:read`) Personal Access Token before `docker compose pull`. This PAT is created and placed on the VM by Tobias directly (e.g. `docker login` once, credential persisted in the VM's Docker config) — it is explicitly **not** a GitHub Actions secret, since it's consumed by the VM's Docker daemon, not by a workflow run. Private visibility is an access control, not obfuscation — no bytecode protection is implied or needed, since only the VM (holding the PAT) can pull.

### 7. Backup: local now, offsite deferred (resolved 2026-08-23)
Split into two independent halves:
- **Local `pg_dump` cron — in scope now, own small task (tasks.md §7.1, 7.4, 7.5 minus offsite).** `pg_dump` looped over both databases (`partnerportal`, `keycloak`) to a local directory on the VM, with retention pruning (e.g. keep last 14 daily dumps). No provider decision blocks this — near-zero effort, and it shrinks the risk window during the client/supervisor evaluation period where real-ish partner/order data may accumulate before the offsite half exists. This is standard scope for this change, not a stretch goal.
- **Offsite sync — deferred, post-MVP (historical decision, Tobias, 23.08.2026).** Local dumps do not survive loss of the VM and its storage. This corrects the earlier contrary risk claim, without expanding scope or validating any backup. Provider and operational evidence remain external; no backup access or work is part of this review.

### 8. CI test gating
`./mvnw test` (Quarkus Dev Services — needs Docker on the runner, which GitHub-hosted runners provide) runs on every push and PR. The `build-and-push` and `deploy` jobs both declare `needs: test`, so a failing test suite never reaches production. Test and build-and-push are otherwise independent of the VM's existence — this half of the pipeline is fully exercisable before the VM is provisioned.

### 9. Image tagging
Every build pushes two tags: `latest` (what `docker-compose.prod.yml` pulls) and `<git-sha>` (immutable, for audit trail / manual rollback via `docker compose up -d` after editing the compose file's tag). No separate rollback automation in this change — manual SHA pin is the rollback path, documented in tasks.md.

### 10. Per-service `mem_limit` in `docker-compose.prod.yml` (resolved 2026-08-23)
Set generous per-service memory limits for every container (Quarkus, Postgres, Keycloak, Caddy) as standard part of writing `docker-compose.prod.yml` (tasks.md §2.3), not a separate later hardening task. Reasoning: even though the RAM budget has comfortable headroom on paper (Decision 2), limits are cheap insurance against one runaway container (e.g. a Keycloak memory leak under load) starving the others with no guardrail — a safety net, not a tight leash. Sized generously enough not to throttle normal operation.

### 11. Firewall / ports (resolved 2026-08-23)
`ufw` is inactive on `papo-server-01`, so ports 80/443 are open locally — sufficient for MVP, no action needed now. Two deferred, explicitly-noted caveats (not required before this change ships): (a) a provider-level/upstream firewall may still exist independent of `ufw` — the real test is reachability from outside once Caddy is actually listening on 80/443, verify then, not before; (b) before any production use beyond the thesis evaluation, enable `ufw` and allow only `22/80/443` — currently deferred alongside the offsite backup (Decision 7), tracked as the same class of "before production use" to-do.

### 12. Forced-command release gateway (implemented locally 14.09.2026)
The workflow uses pinned third-party Actions plus native `ssh`. A root-owned forced-command gateway accepts only `upload`, `verify` and `activate` with one lowercase 40-hex SHA. It fetches `origin/main` with a read-only repository key, byte-compares the upload with a server-generated canonical frontend archive, rejects non-blob tree entries, extracts directly to `frontend-live/releases/<sha>/`, requires a regular `index.html`, deploys only the backend with `IMAGE_TAG=<sha>`, and atomically changes `frontend-live/current` after Compose succeeds. PostgreSQL, Keycloak and Caddy are not recreated. The latest five frontend releases are retained; rollback stays manual and coupled across backend SHA and frontend symlink.

## Risks / Trade-offs

- **[Risk] JVM Quarkus RAM footprint under real load is higher than estimated, crowding Keycloak/Postgres on the 7.6 GB box (tighter than the 12 GB originally planned for).** → Mitigation: `mem_limit` per service (Decision 10) so one process can't starve the others; native-image (Decision 2) remains a documented fallback if real-load footprint exceeds the estimate.
- **[Risk] Single Postgres instance means a Postgres failure takes down both the app and auth simultaneously.** → Mitigation: accepted trade-off per Decision 3, scale-appropriate; local `pg_dump` backups (Decision 7) bound the damage now, offsite copy bounds VM-loss specifically once implemented post-MVP.
- **[Risk] Path-based Keycloak routing (`/auth`) under a reverse proxy is a known source of subtle misconfiguration (redirect loops, wrong issuer URL in tokens).** → Mitigation: `KC_HOSTNAME` and `KC_HTTP_RELATIVE_PATH` set explicitly and tested against a real HTTPS request before declaring the deploy job done; documented as its own task with a manual verification step.
- **[Risk] The historical plan excluded health checks/automatic rollback, but the 09.09 handoff reports pipeline success with HTTP 404/502.** This contradiction blocks readiness. Tobias must establish the current cause, verifiable success criteria and recovery evidence before a revised implementation plan; do not infer runtime success from an Action result.
- **[Risk] GHCR private package + VM-side PAT is a credential Tobias must remember to rotate/manage outside the "everything is a GH secret" model.** → Mitigation: explicitly documented in proposal.md's Impact section and called out as a VM-local (not repo, not GH Actions) credential.

## Migration Plan

1. Externalize `%prod` config (Decision 5) — safe, additive, no behavior change to `%dev`/`%test`.
2. Stand up the full stack locally via Docker Compose (Quarkus container included) to validate the Dockerfile and env-var wiring before anything touches GitHub Actions or the VM.
3. Add the `test` → `build-and-push` workflow (Decisions 8, 9, 6) — independently testable via a throwaway public/private GHCR package, no VM required.
4. Author `docker-compose.prod.yml` + Caddy config (Decisions 1, 3, 4) — testable locally by running it against `localhost` before pointing it at a real domain.
5. Add the deploy job through the forced-command gateway; install its account, keys, root-owned script and sudoers boundary only after Tobias provides task-5.9 evidence.
6. Add the local `pg_dump` backup cron (Decision 7, local half) — independent of the VM's deploy job, can be scheduled directly on the VM at any point once the VM is reachable. Offsite sync is deferred post-MVP, not part of this migration.

Historical migration outline only: the claim that no deployment exists is superseded by the local workflow and 09.09 handoff. Current running revision and data state were not checked. Recovery and service-recreation guarantees need Tobias's evidence; the commands above are not authorized actions for this session.

## Open Questions

Readiness is blocked by the evidence/decisions in review.md; the historical two-item list was incomplete.

- **Production domain behavior** — domain name `paportal.at` is settled. Missing: dated DNS/HTTPS/OIDC/API/login/order checks for the actual deployed revision and the current P0 diagnosis from Tobias.
- **Offsite backup provider** — deferred post-MVP by design (Decision 7), not blocking. To be picked later from real dump sizes and provider options once it's revisited; not needed for this change to ship.

Resolved and no longer open: JVM vs native (→ JVM, Decision 2), `mem_limit` (→ yes, standard, Decision 10), ports 80/443 (→ open locally via inactive `ufw`, Decision 11), local backup (→ in scope now, Decision 7).
