## Context

See `proposal.md` for motivation. Current verified state on 2026-09-03: tests and backend image publication already run successfully on `main`; the deploy job exists but is disabled. The private repository is not cloned on the VM. `~/papo-deploy` has partial copied assets but no `.env.prod`, no running containers and no backup cron. `paportal.at` resolves to the VM, while HTTP and HTTPS are not yet serving.

Target: `papo-server-01` (`papo@178.104.109.45`), 4 vCPU, 7.6 GB RAM, `x86_64`, Ubuntu 26.04 LTS, Docker 29.6.1 and Compose v5.3.1. The VM replaces the never-provisioned Oracle/ARM target from the first design draft.

Chris owns implementation, deployment and interactive account actions. Tobias owns visual frontend design, not production infrastructure. No private value is recorded in these artifacts.

## Goals / Non-Goals

**Goals:**

- A successful push to `main` deploys one commit-coherent backend/frontend/config release without updating Postgres or Keycloak on routine redeploys.
- First startup and later redeploys are distinguishable, fail visibly and preserve persistent state.
- Browser traffic is same-origin through `https://paportal.at`; local frontend development retains the documented ports.
- Workflow, SSH, demo identities and backups follow least privilege and contain no committed live credential.
- Both databases can be restored from an offsite backup without touching production Postgres.

**Non-Goals:**

- Native image, multiple VMs, HA, blue-green/canary deployment, autoscaling or automatic rollback.
- GitHub Pages/CDN hosting, a general secret-management platform, SMTP or any deferred product feature.
- Automatic production demo seeding in application code.

## Decisions

### 1. Same-origin frontend via Caddy

Caddy serves the static frontend and proxies `/api/*` and `/auth/*` without stripping the paths. Production JavaScript uses relative `/api` and `/auth`; only a frontend served on port 3000 uses the local backend and Keycloak ports. CORS remains enabled only in `%dev`.

### 2. Quarkus remains a JVM image for `linux/amd64`

The VM and GitHub-hosted runner share the architecture. The measured capacity leaves enough headroom for JVM mode, while native compilation would add reflection/build risk to OIDC, Hibernate/Panache and Jackson. Native remains a later optimization only if observed memory demands it.

### 3. One Postgres process, two databases

One container hosts `partnerportal` and `keycloak`. The processes connect only to their database. This reduces memory at the accepted cost of a shared failure domain.

### 4. One production domain from one variable

`DOMAIN=paportal.at` in VM-local `.env.prod` drives Caddy, `KC_HOSTNAME`, the external token issuer and the rendered realm origins. `paportal.at` already resolves to the VM; TLS and issuer behavior remain verification gates.

### 5. `%prod` configuration is environment-driven

Compose supplies database and OIDC values to the implemented `%prod` configuration. `%dev` and `%test` stay independent. The internal Keycloak address is used for JWKS access, while the external HTTPS issuer is validated separately.

### 6. Private GHCR with VM-local read access

The package is private. Chris creates a classic PAT limited to `read:packages` and enters it through `docker login ghcr.io --password-stdin` on the VM. It is not an Actions secret. Builds publish and deploy only the immutable commit-SHA image tag; this change does not publish or resolve `latest`.

### 7. Secure local and offsite backups

The backup script reads the central `~/papo-deploy/.env.prod`, sets `umask 077`, ensures the persistent `~/papo-deploy/backups` directory is mode 700 and creates each plain-SQL dump through a mode-600 temporary file that is atomically renamed only after `pg_dump` succeeds. It keeps the newest 14 completed local dumps per database. `BACKUP_RCLONE_DEST` addresses a VM-local `rclone crypt` remote layered over the private S3-compatible bucket; both storage and crypt parameters live only in mode-600 rclone configuration, with recovery parameters retained by Chris outside the VM.

After each successful dump, `rclone copyto` uploads it. Upload failure retains the local file and exits non-zero. Restore proof downloads one object per database into a mode-700 temporary directory and restores it into a separate short-lived Postgres 16 container using Decision 13's digest and a temporary volume. Cleanup is allowed only for names with an explicit `papo-restore-` prefix; production Postgres is never a restore target.

### 8. CI test gate

Pull requests and pushes to `main` run the complete backend test suite. Build and deploy depend on that test. A failing test never publishes or deploys an image.

### 9. Exact image and rollback identity

Each backend build publishes the commit SHA. Deploy passes `${{ github.sha }}` as `IMAGE_TAG`; rollback and forward recovery also name exact SHAs. A non-secret `RELEASE_SHA` file in each release directory records the source commit.

### 10. Resource limits

All long-running services retain generous `mem_limit` values so one process cannot starve the 7.6 GB VM. Limits are adjusted only from observed use.

### 11. Network boundary and SSH identity

UFW allows incoming TCP 22/80/443 only. External ingress and a new SSH connection were verified. The deploy job must validate the VM's independently checked ED25519 fingerprint `SHA256:9zt3nKCzspeSB08IBsqS5hRnnfLKdq9inQgrJ3J9+z4`, stored as a non-secret repository variable, before native `scp` or `ssh` connects.

The first implementation appended an Actions key to the personal `papo` account with OpenSSH `restrict`. Verification proved that `restrict` disables forwarding, PTY and related features but does not prevent arbitrary remote commands. That key therefore still has the effective file and Docker access of `papo`, including indirect access to VM-local secrets. It must be revoked before deployment and is superseded by Decision 16.

### 12. Immutable, least-privilege Actions and native SSH transport

The workflow declares top-level `permissions: contents: read`; only the image job adds `packages: write`. All Actions use verified full SHAs with tag comments:

- `actions/checkout@11d5960a326750d5838078e36cf38b85af677262` (`v4`)
- `actions/setup-java@cf277c60eb25467037889841efdb72551f06f6c3` (`v4`)
- `docker/setup-buildx-action@8d2750c68a42422c14e847fe6c8ac0403b4cbd6f` (`v3`)
- `docker/login-action@c94ce9fb468520275223c153574b00df6fe4bcc9` (`v3`)
- `docker/build-push-action@10e90e3645eae34f1e60eeb005ba3a3d33f178e8` (`v6`)

Release transfer and remote execution use the runner's native OpenSSH client. The workflow writes the dedicated forced-command key to a mode-600 temporary file, builds a temporary `known_hosts` file only after matching the scanned ED25519 fingerprint against `VM_HOST_FINGERPRINT`, and removes both files through a shell trap. No additional third-party transport Action receives the key. Decision 16 defines the server-side command and identity boundary.

Major-version upgrades are separate from this completion change.

### 13. Immutable stateful container inputs

The tested multi-platform image indexes are pinned in Compose:

- `postgres:16@sha256:f1c3376c26f2609ab9f29f71f824103fe2fcd8ee0346485cb6122a4f93df6f94`
- `quay.io/keycloak/keycloak:26.0@sha256:09a381c715ab0b111835b70f2905955274843a219c6f27efb348e4d9f4086858`
- `caddy:2-alpine@sha256:5f5c8640aae01df9654968d946d8f1a56c497f1dd5c5cda4cf95ab7c14d58648`

Digest changes require an explicit tested dependency update; routine deploys never pull or recreate Postgres/Keycloak.

### 14. Commit-coherent release bundle

The deploy job checks out its triggering commit and creates an uncompressed Git archive with exactly `git archive --format=tar <sha> -- docker-compose.prod.yml deploy frontend`. It rejects an archive larger than 25 MiB and streams the archive to the gateway operation `upload <sha>`. `RELEASE_SHA` is not trusted from the runner: the server writes that marker only after the archive has matched the same commit in its read-only repository mirror. `.env.prod` remains at `~/papo-deploy/.env.prod` and is never uploaded or removed.

The root-owned gateway uses `/run/lock/papo-deploy.lock` for upload, verification and activation. Under that lock it fetches only `refs/heads/main` through the VM-local read-only GitHub deploy key into `/var/lib/papo-deploy/repository.git`, requires the requested SHA to equal `refs/remotes/origin/main`, rejects non-blob entries in the three allowlisted Git paths, produces its own canonical archive with the same command and requires byte equality with the uploaded archive. It never extracts the untrusted upload. Only the server-generated canonical archive is extracted into a root-created temporary directory, receives a server-written `RELEASE_SHA`, and is atomically promoted to `~/papo-deploy/releases/<sha>` with ownership `papo:papo`. The single pending archive is root-owned at `/var/lib/papo-deploy/incoming/release.tar`; every accepted upload replaces it atomically, so the key cannot fill the disk with arbitrary archive names.

The activation program then supplies `IMAGE_TAG=<sha>` explicitly. On the first deployment it starts the complete pinned stack. On routine deployments it pulls only `backend` and applies only `backend` plus `caddy` with `--no-deps`, so Caddy binds the frontend from the new release while Postgres/Keycloak remain untouched. It retries until the frontend returns HTTP 200 and the protected backend probe returns HTTP 401, then atomically updates `~/papo-deploy/current`. A delayed workflow for an older SHA fails because that SHA is no longer `origin/main`. Any failed command fails the job; it does not claim automatic rollback.

### 15. Production demo identities and data are provisioned, not seeded

The production realm template contains roles/client configuration but no users or credentials. After first startup Chris creates one company and at least two partner organizational accounts through the Keycloak admin UI with privately stored random passwords. Partner usernames equal canonical partner numbers.

Using existing company UI/import functions, the demo receives matching active partner records, synthetic articles, per-partner enablements and separate synthetic customers. No real company/person data or new seeding code is introduced. Two partner identities make cross-partner denial observable.

### 16. Forced-command deploy boundary

The Actions key belongs to a dedicated `papo-deploy` operating-system account, not to `papo`. The account has a locked password, `/bin/sh` only so OpenSSH can execute the forced command, no Docker-group membership, no readable access to `/home/papo`, and no general sudo rule. Its root-owned mode-600 `authorized_keys` entry combines `restrict` with exactly `command="/usr/bin/sudo -n /usr/local/libexec/papo-deploy-gateway"`. The only sudoers entries are `Defaults:papo-deploy env_keep += "SSH_ORIGINAL_COMMAND"` and `papo-deploy ALL=(root) NOPASSWD: /usr/local/libexec/papo-deploy-gateway ""` in root-owned mode-440 `/etc/sudoers.d/papo-deploy`; the explicit empty argument string forbids gateway arguments. The root program reads `SSH_ORIGINAL_COMMAND` as untrusted data and validates it before any other action. Direct shell commands, forwarding, arbitrary download/read operations and every other sudo or filesystem operation are rejected.

The mode-755 root-owned gateway accepts exactly three full command forms and no extra whitespace or arguments: `upload <sha>`, `verify <sha>` and `activate <sha>`, where `<sha>` is exactly 40 lowercase hexadecimal characters. `upload` reads at most 25 MiB plus one byte from standard input, rejects an oversized or non-canonical archive, and atomically replaces the one pending root-owned archive. `verify` repeats current-`main` and archive equality checks without creating a release or invoking Docker. `activate` repeats the same checks, promotes the canonical release, then performs only the Decision 14 Compose and health sequence through `runuser --user papo --`; it never evaluates client text or executes an uploaded script. Status output contains only the operation, SHA and non-sensitive failure reason.

The repository mirror is the bare root-owned mode-700 directory `/var/lib/papo-deploy/repository.git` with only `git@github.com:Chris-Vahram/diplomarbeit-papo.git` as `origin`. Its root-owned mode-600 read-only GitHub deploy key and pinned `github.com` host keys live under `/var/lib/papo-deploy/ssh/` and are selected by a fixed `GIT_SSH_COMMAND`; `papo-deploy` cannot read them. `/var/lib/papo-deploy/incoming/`, the mirror, gateway, sudoers rule and repository key are root-owned and not writable or readable by `papo-deploy`. The workflow connects only as `papo-deploy` and calls `upload`, then `activate`; `verify` exists solely for the no-deploy boundary proof.

This boundary limits a stolen Actions SSH key to requesting deployment of the current protected `main` commit. It cannot read production secrets, select an arbitrary historical commit, upload different executable content, invoke Docker directly or obtain an interactive shell. Chris's personal key remains separate and is used for explicitly authorized manual rollback and administration.

## Risks / Trade-offs

- **[Risk] A routine deploy replaces backend and Caddy without automatic rollback.** → The job fails on the first error, checks HTTPS, and retains the previous SHA release for manual rollback.
- **[Risk] The runner handles the deploy private key.** → Only native OpenSSH receives the dedicated forced-command key through a mode-600 temporary file; fingerprint validation happens before connection, a trap removes temporary material, and Decision 16 prevents general VM access.
- **[Risk] Shared Postgres takes application and identity down together.** → Accepted for one VM; local and encrypted offsite backups plus isolated restore proof bound data loss.
- **[Risk] Loss of rclone crypt recovery parameters makes offsite copies unusable.** → Chris retains them separately from the VM and proves restore before readiness.
- **[Risk] Public synthetic accounts can be abused.** → Passwords are random/private, data is synthetic, roles remain server-enforced and no credential is documented.

## Migration Plan

1. Finish same-origin frontend logic, remove production realm users, pin Actions/container digests and implement the SHA release-bundle workflow while deploy remains disabled.
2. Revoke the interim `papo` Actions key and implement Decision 16's still-disabled workflow, gateway and deterministic local harness.
3. Push that disabled implementation to the feature branch only after Chris authorizes commit/push, then prove the required PR check without merging.
4. Install the dedicated forced-command identity, root-owned gateway and VM-local read-only repository key and prove the boundary without deploying. Enable deploy only in a later reviewed commit after every transport and VM preflight passes; merge that commit with Chris's authorization to start the first exact-SHA release.
5. Verify HTTPS/OIDC, provision synthetic accounts/data and prove role/partner isolation.
6. Configure secure local/offsite backups and prove restore in the isolated container.
7. Prove routine deploy, persistence, rollback and forward recovery; synchronize docs, run reviews/checks, then archive after approval.

There are no remaining architecture decisions after Decision 16. Missing root access, GitHub deploy-key administration, external credentials or a check that contradicts these decisions is a stop condition, not permission to weaken the design.

## Independent Plan Review — 2026-09-03

The independent review returned `NOT READY` with ten findings. They are retained here instead of silently rewriting history:

1. Release artifacts were not commit-coherently delivered → resolved by Decision 14.
2. Mutable `latest`, broad Compose deploy and false-green shell behavior → resolved by Decisions 9, 13 and 14.
3. Committed production demo passwords → resolved by Decision 15.
4. No production data or second partner identity → resolved by Decision 15 and dedicated tasks.
5. Missing SSH fingerprint and inherited workflow permissions → resolved by Decisions 11 and 12.
6. Unsafe dump permissions and restore on production Postgres → resolved by Decision 7.
7. Cyclic task DoDs → task dependencies/checks reordered.
8. Non-self-sufficient checks → open tasks now name inputs, expected results, evidence and guarded cleanup.
9. Mutable stateful image tags → resolved by Decision 13.
10. Stale context/document statements → current context corrected; final documentation sync follows proven deployment evidence.

## Final Pre-Apply Corrections — 2026-09-03

Four execution findings discovered after the combined review are incorporated without adding another readiness gate:

1. Third-party SCP/SSH Actions are replaced by native runner `scp`/`ssh` with explicit fingerprint verification and temporary-key cleanup (Decisions 11, 12 and 14).
2. A server-side deployment lock plus monotone workflow run marker prevents concurrent or delayed older runs from replacing a newer release (Decision 14).
3. Activation requires both a frontend HTTP 200 and a protected backend HTTP 401, so proxy and backend reachability are checked together (Decision 14).
4. Backups live outside immutable releases, use the central VM-local environment, and expose only atomically completed mode-600 dumps (Decision 7).

## Apply-Self-Sufficiency Review — 2026-09-05

The review returned `NOT READY` because OpenSSH `restrict` did not provide the claimed command boundary and several remaining tasks mixed implementation, human authorization and evidence. The plan now incorporates these corrections:

1. Decision 16 replaces the personal-account deploy key with a dedicated forced-command identity and fixes its exact commands, paths, size bound, sudo boundary and canonical protected-`main` archive verification before activation.
2. Branch-protection configuration is proven by API state; its practical PR gate is checked by the intermediate feature-branch PR, avoiding a prohibited direct push to `main`.
3. First deployment, synthetic data, S3/rclone setup, restore, second deployment, smoke tests and rollback are split into independently verifiable tasks with explicit human gates.
4. Final deployment documentation follows rollback/forward-recovery evidence, and MVP traceability uses a fixed source inventory and matrix schema.
