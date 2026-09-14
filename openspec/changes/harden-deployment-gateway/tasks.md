## 1. Local gateway implementation — essential

- [x] 1.1 Implement the forced-command gateway, least-privilege sudoers rule and native-SSH workflow while preserving `frontend-live/releases/<sha>/current`; verify workflow YAML and shell syntax.
- [x] 1.2 Add and run an isolated harness covering command/SHA/archive/lock validation, verify-only behavior, exact release layout, backend-only Compose and failure-safe `current`; 18/18 checks passed locally on 14.09.2026.

## 2. VM installation and evidence — essential, Tobias

- [ ] 2.1 Install the root-owned gateway and sudoers rule, dedicated no-shell deploy account/key and read-only repository key; verify `visudo -cf`, ownership/modes, host-key pinning, no Docker group, no `.env.prod` read access and no general sudo.
- [ ] 2.2 Run remote negative tests for empty/arbitrary commands, PTY, forwarding, SCP/SFTP and invalid archives; then verify upload/verify is side-effect-free, activation preserves PostgreSQL/Keycloak/Caddy, HTTPS/OIDC/API/login/order flows pass, and a coupled backend/frontend rollback succeeds.

### Evidence gate — 14.09.2026

Tasks 2.1 and 2.2 remain blocked. Repository and Git history contain the local gateway implementation and `18 Harness-Prüfungen bestanden`, but no sanitized Tobias evidence for VM installation, root ownership/modes, `visudo -cf`, deploy/read-only keys, host-key pinning, no Docker/general sudo/`.env.prod` access, remote negative tests, live HTTPS/OIDC/API/login/order checks, or coupled rollback. No VM, SSH, secret, or volume action was performed.
