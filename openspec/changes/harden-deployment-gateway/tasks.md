## 1. Local gateway implementation — essential

- [x] 1.1 Implement the forced-command gateway, least-privilege sudoers rule and native-SSH workflow while preserving `frontend-live/releases/<sha>/current`; verify workflow YAML and shell syntax.
- [x] 1.2 Add and run an isolated harness covering command/SHA/archive/lock validation, verify-only behavior, exact release layout, backend-only Compose and failure-safe `current`; 19/19 checks passed locally on 14.09.2026, including fragmented-stream upload regression.

## 2. VM installation and evidence — essential, Tobias

- [x] 2.1 Install the root-owned gateway and sudoers rule, dedicated forced-command-only deploy account/key and read-only repository key; verify `visudo -cf`, ownership/modes, host-key pinning, no Docker group, no `.env.prod` read access and no general sudo. Sanitized VM evidence from the authorized security subagent on 14.09.2026: gateway `root:root 0755`, sudoers `root:root 0440` with `visudo` passing, one forced key with no port/agent/X11 forwarding and no PTY, read-only GitHub deploy key, no Docker group/socket, no `.env.prod` access and only the gateway sudo rule. The account shell is startable for the forced-command SSH path; unrestricted shell access remains unavailable through the forced key.
- [ ] 2.2 Run remote negative tests for empty/arbitrary commands, PTY, forwarding, SCP/SFTP and invalid archives; then verify upload/verify is side-effect-free, activation preserves PostgreSQL/Keycloak/Caddy, HTTPS/OIDC/API/login/order flows pass, and a coupled backend/frontend rollback succeeds. Upload/verify side-effect checks, backend-only activation, HTTPS/OIDC/API `401`, company/partner login and read-only order retrieval, plus coupled rollback `1a48cbf… → 05e0bdd… → 1a48cbf…` passed on 14.09.2026; only the external SSH negative-test matrix remains unexecuted because the private deploy key is intentionally unavailable locally.

### Evidence gate — 14.09.2026

Task 2.1 is complete from sanitized authorized-subagent evidence. Task 2.2 remains open only for the external SSH negative-test matrix; all other listed live-flow, side-effect, preservation and coupled-rollback checks passed. No secret value was exposed and no volume was deleted.
