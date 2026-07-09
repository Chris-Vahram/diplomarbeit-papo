## 1. Config externalization (prerequisite, no VM/CI needed)

- [ ] 1.1 Replace `%prod` datasource values in `backend/src/main/resources/application.properties` with env-var placeholders (`${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`)
- [ ] 1.2 Replace `%prod` OIDC values (`quarkus.oidc.auth-server-url`, `quarkus.oidc.client-id`) with env-var placeholders (`${KC_URL}`, `${KC_CLIENT_ID}`)
- [ ] 1.3 Verify `%dev` and `%test` blocks are unchanged; run `cd backend && ./mvnw quarkus:dev` locally to confirm dev mode still works exactly as before

## 2. Local containerized stack (validate before touching CI/VM)

- [ ] 2.1 Verify `backend/src/main/docker/Dockerfile.jvm` builds a working image locally (`docker build -f src/main/docker/Dockerfile.jvm -t papo-backend:local .` after `./mvnw package`)
- [ ] 2.2 Confirm the base image in `Dockerfile.jvm` has an arm64 manifest (multi-arch); adjust only if it doesn't
- [ ] 2.3 Write `docker-compose.prod.yml` (Quarkus, Postgres, Keycloak, Caddy) — separate file from the dev-only `docker-compose.yml`
- [ ] 2.4 Add a Postgres init script creating both `partnerportal` and `keycloak` databases in the same instance
- [ ] 2.5 Wire the `%prod` env vars from task 1 into `docker-compose.prod.yml`'s `environment:` block, backed by a local (gitignored) `.env.prod.example` → `.env.prod` template
- [ ] 2.6 Run the full stack locally via `docker compose -f docker-compose.prod.yml up`, confirm Quarkus connects to Postgres and Keycloak by service name (not `localhost`)
- [ ] 2.7 Confirm existing Keycloak realm JSON still imports correctly in this compose file

## 3. CI: test gate + build + push to GHCR

- [ ] 3.1 Add `.github/workflows/ci-cd.yml` with a `test` job running `./mvnw test` on push to `main` and on pull requests
- [ ] 3.2 Add a `build-and-push` job (`needs: test`, `if: github.ref == 'refs/heads/main'`) using `docker/setup-buildx-action` + `docker/build-push-action` targeting `linux/arm64`
- [ ] 3.3 Configure the job to push to `ghcr.io/<owner>/<repo>-backend` tagged `latest` and `${{ github.sha }}`
- [ ] 3.4 Grant the workflow `packages: write` permission
- [ ] 3.5 Set the resulting GHCR package to private once first published
- [ ] 3.6 Enable branch protection on `main` (require the `test` job to pass before merge)

## 4. Frontend + reverse proxy config

- [ ] 4.1 Write the Caddyfile: `/` → static frontend files, `/api/*` → Quarkus container, `/auth/*` → Keycloak container
- [ ] 4.2 Add frontend static files to the Caddy container (bind-mounted volume or copied into a minimal Caddy image layer)
- [ ] 4.3 Set `KC_HTTP_RELATIVE_PATH=/auth`, `KC_PROXY_HEADERS=xforwarded`, `KC_HOSTNAME` env vars on the Keycloak service in `docker-compose.prod.yml`
- [ ] 4.4 Update the production Keycloak realm export's `redirectUris`/`webOrigins` to the production domain (placeholder until domain is chosen) instead of the dev `*` wildcard
- [ ] 4.5 Confirm no CORS properties are needed/enabled in `%prod` (same-origin routing should make `quarkus.http.cors.*` unnecessary in prod)

## 5. Deploy job (blocked on VM provisioning — Tobias)

- [ ] 5.1 (Tobias) Provision the Oracle Cloud VM, install Docker + Docker Compose, open ports 80/443
- [ ] 5.2 (Tobias) Generate a deploy SSH keypair, add the public key to the VM's `authorized_keys`
- [ ] 5.3 (Tobias) Add GitHub Actions secrets: `VM_HOST`, `VM_SSH_KEY`, `DB_PASSWORD`, `KC_ADMIN_PASSWORD`, and any other `%prod` env values needed at deploy time
- [ ] 5.4 (Tobias) `docker login ghcr.io` on the VM using a `packages:read` PAT, so `docker compose pull` can authenticate against the private package
- [ ] 5.5 Add a `deploy` job (`needs: build-and-push`) to the workflow using `appleboy/ssh-action`, running `docker compose pull && docker compose up -d` in the production compose directory on the VM
- [ ] 5.6 Copy `docker-compose.prod.yml` + Caddyfile + Postgres init script to the VM (one-time manual step or a `scp` step in the deploy job)
- [ ] 5.7 Run the pipeline end-to-end against the real VM; verify only the Quarkus container restarts and Postgres/Keycloak data persists across the deploy

## 6. HTTPS + domain (blocked on domain choice — Tobias)

- [ ] 6.1 (Tobias) Choose/acquire a domain, point an A record at the VM's public IP
- [ ] 6.2 Update Caddyfile and Keycloak `KC_HOSTNAME` from placeholder to the real domain
- [ ] 6.3 Verify Caddy successfully provisions a Let's Encrypt certificate and the site is reachable over HTTPS
- [ ] 6.4 Verify Keycloak login flow works end-to-end through the `/auth` path behind the proxy (no redirect loops, correct issuer URL in tokens)

## 7. Backups

- [ ] 7.1 Write a backup script: `pg_dump` for both `partnerportal` and `keycloak` databases to a local directory
- [ ] 7.2 (Tobias) Create an Oracle Object Storage bucket and a Customer Secret Key (S3-compatible credentials)
- [ ] 7.3 Add `rclone` config (VM-local, using Tobias's credentials) and extend the backup script to upload each dump to the bucket
- [ ] 7.4 Add local retention pruning (e.g. keep last 14 daily dumps locally; offsite copies retained independently)
- [ ] 7.5 Schedule the backup script via cron on the VM
- [ ] 7.6 Manually verify one full cycle: dump created locally, uploaded to Object Storage, old local dumps pruned

## 8. Documentation

- [ ] 8.1 Update `docs/SETUP.md` (or add `docs/DEPLOYMENT.md`) describing the production topology, required secrets/env vars, and manual rollback procedure (edit image tag in `docker-compose.prod.yml` to a known-good SHA, `docker compose up -d`)
- [ ] 8.2 Document that CORS is intentionally disabled/unused in `%prod` (same-origin), contrasting with the documented dev-mode CORS shortcut
