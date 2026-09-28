## 1. Local gateway implementation — essential

- [x] 1.1 Implement the forced-command gateway, least-privilege sudoers rule and native-SSH workflow while preserving `frontend-live/releases/<sha>/current`; verify workflow YAML and shell syntax.
- [x] 1.2 Add and run an isolated harness covering command/SHA/archive/lock validation, verify-only behavior, exact release layout, backend-only Compose and failure-safe `current`; 19/19 checks passed locally on 14.09.2026, including fragmented-stream upload regression. The 28.09.2026 health/rollback extension passes 30/30 checks, including repeated TERM during an explicit backend rollback.

## 2. VM installation and evidence — essential, Tobias

- [x] 2.1 Install the root-owned gateway and sudoers rule, dedicated forced-command-only deploy account/key and read-only repository key; verify `visudo -cf`, ownership/modes, host-key pinning, no Docker group, no `.env.prod` read access and no general sudo. Sanitized VM evidence from the authorized security subagent on 14.09.2026: gateway `root:root 0755`, sudoers `root:root 0440` with `visudo` passing, one forced key with no port/agent/X11 forwarding and no PTY, read-only GitHub deploy key, no Docker group/socket, no `.env.prod` access and only the gateway sudo rule. The account shell is startable for the forced-command SSH path; unrestricted shell access remains unavailable through the forced key.
- [x] 2.2 Verify side-effect-free upload/verify, backend-only activation, preservation of PostgreSQL/Keycloak/Caddy, HTTPS/OIDC/API/login/order flows, and coupled backend/frontend rollback. Upload/verify side-effect checks, backend-only activation, HTTPS/OIDC/API `401`, company/partner login and read-only order retrieval, plus coupled rollback `1a48cbf… → 05e0bdd… → 1a48cbf…` passed on 14.09.2026. The 28.09.2026 GitHub Actions deployment passed health checks and activated the exact backend image; frontend/API/OIDC returned `200/401/200`, and application/dependency containers had `RestartCount 0`. Chris explicitly removed the separate remote SSH negative-test matrix from this change's acceptance scope on 28.09.2026.

### Completion record — 28.09.2026

Task 2.1 is complete from sanitized authorized-subagent evidence. All required local harness, deployment, live-flow, side-effect, preservation, health-check and coupled-rollback checks passed. Chris explicitly removed the separate remote SSH negative-test matrix from this change's acceptance scope. No further gateway work is required by this change.
