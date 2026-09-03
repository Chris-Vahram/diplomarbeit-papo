## 1. Config externalization (prerequisite, no VM/CI needed)

- [x] 1.1 Replace `%prod` datasource values in `backend/src/main/resources/application.properties` with env-var placeholders (`${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`)
- [x] 1.2 Replace `%prod` OIDC values (`quarkus.oidc.auth-server-url`, `quarkus.oidc.client-id`) with env-var placeholders (`${KC_URL}`, `${KC_CLIENT_ID}`)
- [x] 1.3 Verify `%dev` and `%test` blocks are unchanged; run `cd backend && ./mvnw quarkus:dev` locally to confirm dev mode still works exactly as before — ran it against the existing dev containers, boots clean, `%dev` config untouched, `/api/company/products` correctly 401s unauthenticated

## 2. Local containerized stack (validate before touching CI/VM)

- [x] 2.1 Verify `backend/src/main/docker/Dockerfile.jvm` builds a working image locally (`docker build -f src/main/docker/Dockerfile.jvm -t papo-backend:local .` after `./mvnw package`), targeting `linux/amd64` — built and smoke-tested standalone: JVM boots, reads the externalized `%prod` env vars correctly (fails only on an unreachable dummy DB, as expected without real Postgres)
- [x] 2.2 ~~Confirm arm64 manifest~~ — moot, VM is x86_64. JVM vs native resolved (design.md Decision 2): staying JVM, no cross-arch concern.
- [x] 2.3 Write `docker-compose.prod.yml` (Quarkus, Postgres, Keycloak, Caddy), incl. a generous `mem_limit` per service (design.md Decision 10, standard part of this task, not separate) — separate file from the dev-only `docker-compose.yml`
- [x] 2.4 Add a Postgres init script creating both `partnerportal` and `keycloak` databases in the same instance
- [x] 2.5 Wire the `%prod` env vars from task 1 into `docker-compose.prod.yml`'s `environment:` block, backed by a local (gitignored) `.env.prod.example` → `.env.prod` template
- [x] 2.6 Run the full stack locally via `docker compose -f docker-compose.prod.yml up`, confirm Quarkus connects to Postgres and Keycloak by service name (not `localhost`) — ran end-to-end under an isolated `papo-prod` project (see collision note below), full auth round-trip verified: login via Keycloak → valid Bearer token → `200` on `/api/company/products`, no token → `401`, wrong role → `403`. Found and fixed a real bug along the way: Keycloak's OIDC discovery document returns absolute URLs built from `KC_HOSTNAME` (external), so the backend's internal discovery call got back a `jwks_uri` it couldn't reach from inside the container network — fixed via `%prod.quarkus.oidc.discovery-enabled=false` + `jwks-path` (see application.properties). Also found dev/prod container-name collision (`docker-compose.yml` and `docker-compose.prod.yml` both defaulted to `papo-postgres`/`papo-keycloak` container names and the same project namespace) — running the prod file locally briefly replaced the dev Postgres container (data volume untouched, no data lost, verified after restoring). Fixed by giving `docker-compose.prod.yml` its own `name: papo-prod` and `papo-prod-*` container names.
- [x] 2.7 Confirm existing Keycloak realm JSON still imports correctly in this compose file — realm imports with `redirectUris`/`webOrigins` correctly substituted to the configured `DOMAIN` (verified via `docker exec ... cat .../partnerportal-realm.json`); found and fixed a permission bug in the render step (`/opt/keycloak/data/import` doesn't exist in the base image, so Docker auto-created it as root-owned when bind-mounting a file into it — fixed by mounting the template elsewhere and having the entrypoint script `mkdir -p` the import dir itself)

## 3. CI: test gate + build + push to GHCR

- [x] 3.1 Add `.github/workflows/ci-cd.yml` with a `test` job running `./mvnw test` on push to `main` and on pull requests
- [x] 3.2 Add a `build-and-push` job (`needs: test`, `if: github.ref == 'refs/heads/main'`) using `docker/setup-buildx-action` + `docker/build-push-action` targeting `linux/amd64` (VM is x86_64, matches GH runners — no cross-arch build needed)
- [x] 3.3 Configure the job to push to `ghcr.io/<owner>/<repo>-backend` tagged `latest` and `${{ github.sha }}` — image name is `ghcr.io/chris-vahram/diplomarbeit-papo-backend` (owner/repo lowercased, GHCR requirement)
- [x] 3.4 Grant the workflow `packages: write` permission
- [ ] 3.5 Set the resulting GHCR package to private once first published
- [ ] 3.6 Enable branch protection on `main` (require the `test` job to pass before merge)

## 4. Frontend + reverse proxy config

- [x] 4.1 Write the Caddyfile: `/` → static frontend files, `/api/*` → Quarkus container, `/auth/*` → Keycloak container
- [x] 4.2 Add frontend static files to the Caddy container (bind-mounted volume or copied into a minimal Caddy image layer)
- [x] 4.3 Set `KC_HTTP_RELATIVE_PATH=/auth`, `KC_PROXY_HEADERS=xforwarded`, `KC_HOSTNAME` env vars on the Keycloak service in `docker-compose.prod.yml`
- [x] 4.4 Update the production Keycloak realm export's `redirectUris`/`webOrigins` to the production domain instead of the dev `*` wildcard — done via `deploy/keycloak/partnerportal-realm.prod.json.template` + `render-and-start.sh`, which substitutes `${DOMAIN}` at every container start (not a hardcoded placeholder — reconciles with design.md Decision 4's "don't scaffold against a placeholder" warning, since the value always tracks the live `DOMAIN` env var, never goes stale)
- [x] 4.5 Confirm no CORS properties are needed/enabled in `%prod` — found a bug: `quarkus.http.cors.*` had no profile prefix, so it was live in `%prod` too (locked to `localhost:3000` origins — not a security hole, but wrong per Decision 1's "retire CORS entirely"). Fixed: scoped all four properties to `%dev.` only.

## 5. Deploy job (VM provisioned 2026-08-23 — `papo-server-01`, 178.104.109.45)

- [x] 5.1 Provision VM, install Docker + Docker Compose — done (4 vCPU / 7.6 GB RAM / x86_64 / Ubuntu 26.04 LTS / Docker 29.6.1 + Compose v5.3.1). On 03.09.2026 temporary listeners proved external ingress on 80 and 443; UFW was then enabled with deny-by-default ingress and explicit 22/80/443 TCP allowances. A fresh SSH connection succeeded afterward (design.md Decision 11).
- [ ] 5.2 Generate a **dedicated deploy SSH keypair** (not the personal login key), append its public key to the VM's `authorized_keys` (append, don't overwrite existing keys). Can be done anytime now — not blocked on anything else.
- [ ] 5.3 Add GitHub Actions secrets — `VM_HOST` (178.104.109.45), `VM_SSH_KEY` (deploy key private half from 5.2), `DB_PASSWORD`, `KC_ADMIN_PASSWORD`. Chris owns any interactive repository action; secret values must not appear in logs or documentation.
- [ ] 5.4 Once the first image is in GHCR: Chris creates a classic PAT limited to `read:packages`; use it once with `docker login ghcr.io --password-stdin` on the VM so `docker compose pull` can authenticate against the private package. Never expose the PAT in command history or output.
- [x] 5.5 Add a `deploy` job (`needs: build-and-push`) to the workflow using `appleboy/ssh-action`, running `docker compose pull && docker compose up -d` in the production compose directory on the VM — targets `~/papo-deploy` on the VM (see task 5.6, still open) as `username: papo` (VM account per proposal.md), `VM_HOST`/`VM_SSH_KEY` secrets (task 5.3, still open — Tobias). **Job is temporarily `if: false`'d in `ci-cd.yml`** so the first merge to `main` runs test + build-and-push cleanly without the deploy job failing on the still-missing prerequisites below. Re-enable (swap `if: false` back to the commented-out branch/ref condition) once 5.3, 5.4, and 5.6 are all done.
- [ ] 5.6 Copy `docker-compose.prod.yml` + Caddyfile + Postgres init script to the VM (one-time manual step or a `scp` step in the deploy job)
- [ ] 5.7 Run the pipeline end-to-end against the real VM; verify only the Quarkus container restarts and Postgres/Keycloak data persists across the deploy

## 6. HTTPS + domain

- [x] 6.1 Register `paportal.at` and point its A record at the VM's public IP — verified on 03.09.2026: `paportal.at` resolves to `178.104.109.45`
- [ ] 6.2 Set `KC_HOSTNAME`, `redirectUris`, and `webOrigins` together as one work package (design.md Decision 4) — do not scaffold any of the three against a placeholder domain beforehand
- [ ] 6.3 Verify Caddy successfully provisions a Let's Encrypt certificate and the site is reachable over HTTPS. External ingress on 80/443 is already proven by task 5.1; this task verifies the real TLS/application path.
- [ ] 6.4 Verify Keycloak login flow works end-to-end through the `/auth` path behind the proxy (no redirect loops, correct issuer URL in tokens)

## 7. Backups — local and offsite before production-ready status (design.md Decision 7, updated 2026-09-03)

**In scope now** (no provider decision needed, near-zero effort, shrinks risk window during eval period):
- [x] 7.1 Write a backup script: `pg_dump` for both `partnerportal` and `keycloak` databases to a local directory — `deploy/backup/backup.sh`
- [x] 7.4 Add local retention pruning (e.g. keep last 14 daily dumps locally) — same script, keeps last 14 per database
- [ ] 7.5 Schedule the backup script via cron on the VM — script + crontab line ready (see script header / docs/DEPLOYMENT.md); VM access is available
- [ ] 7.6a Manually verify one local cycle: dump created, old dumps pruned on schedule

**Offsite hardening — now in scope before production-ready status:**
- [ ] 7.2 S3-compatible access data was received through a private Vaultwarden Send; securely verify provider endpoint, bucket name, and write permission without copying secrets into Git, logs, or documentation
- [ ] 7.3 Add VM-local `rclone` configuration using the supplied credentials and extend the backup script to upload each dump to the bucket
- [ ] 7.6b Manually verify offsite cycle once 7.2/7.3 land: dump uploaded to bucket, old local dumps still pruned correctly alongside it

## 8. Documentation

- [x] 8.1 Update `docs/SETUP.md` (or add `docs/DEPLOYMENT.md`) describing the production topology, required secrets/env vars, and manual rollback procedure (edit image tag in `docker-compose.prod.yml` to a known-good SHA, `docker compose up -d`) — added `docs/DEPLOYMENT.md`; rollback edits `IMAGE_TAG` in `.env.prod` (not the compose file directly) so the compose file stays generic
- [x] 8.2 Document that CORS is intentionally disabled/unused in `%prod` (same-origin), contrasting with the documented dev-mode CORS shortcut
