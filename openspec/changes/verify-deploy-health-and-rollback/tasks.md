## 1. Gateway behavior — essential

- [x] 1.1 Add bounded exact-image, stable-container and consecutive HTTPS API readiness checks before frontend switch; verify `sh -n deploy/ssh/papo-deploy-gateway` and healthy/delayed-readiness harness cases.
- [x] 1.2 Restore the previous immutable backend on failed activation while keeping the old frontend pointer; verify harness cases for startup failure, missing rollback image, failed rollback, frontend-switch failure, TERM during rollback and unchanged other services.

## 2. Verification and documentation — essential

- [x] 2.1 Run the full gateway harness, OpenSpec strict validation, shell syntax and diff checks; obtain independent plan/final-diff review with no blocking finding. On 28.09.2026: 30/30 harness checks, both gateway shell syntax checks, strict validation for both active deployment changes, `git diff --check`, and independent final review passed. Regression cases prove the signal handler retries rollback and ignores a second TERM through recovery verification.
- [x] 2.2 Document health criteria, bounded failure, best-effort rollback and database-migration limitation in `docs/DEPLOYMENT.md`; verify text matches the gateway behavior.

## 3. Authorized demo-VM rollout — essential

- [x] 3.1 Back up and install the reviewed root-owned gateway on the demo VM with unchanged owner/mode/SSH permissions; verify hashes and `sh -n` remotely before any `main` deploy. Original backup: `/usr/local/libexec/papo-deploy-gateway.backup-20260925T144611Z`. The 28.09.2026 installation was backed up as `/usr/local/libexec/papo-deploy-gateway.backup-20260928T122840Z` (previous SHA-256 `577c21d69e3eaf854659e7ea20d6fcdf852e1775fea04043411c8c1acb699c78`); installed SHA-256 `e967b77f2499edd99a74edeafd8534d96e02948b2e67a29d6a0dc6ef8d79481a` matches the local gateway and remote `sh -n` passes. Gateway remains `root:root 755`; sudoers and SSH keys were not changed. Non-secret staging copy remains at `/tmp/papo-deploy-gateway.new-20260928T122840Z`.
- [ ] 3.2 Merge after PR CI, then verify the new `main` deployment reports success only with the exact backend SHA, stable container, unchanged PostgreSQL/Keycloak/Caddy, frontend HTTP 200, protected API HTTP 401 and OIDC HTTP 200; record evidence without secrets. Chris confirmed GitHub-PAT revocation and authorized Codex to commit, push and merge on 28.09.2026. The local key mismatch does not block Actions; the run must verify the GitHub deployment credential.
