## Context

The base CI/CD change is archived on `origin/main`; before this change it used broad SCP and shell access. Historical commit `48ad4d8` contains a forced-command design, while `1513f9a` establishes the stable `frontend-live/releases/<sha>/` layout.

## Goals / Non-Goals

**Goals:**
- Restrict the deploy key to one validated release protocol.
- Preserve the current frontend release layout and backend-only recreation.
- Leave an executable local harness and verify the deployed workflow and runtime behavior.

**Non-Goals:**
- Automatic rollback, VM provisioning or secret rotation.
- Changes to application behavior, demo data or presentation content.

## Decisions

The workflow uses native OpenSSH and pinned Actions. The root-owned gateway fetches `origin/main` through a separate read-only repository key, generates the canonical frontend archive itself and compares it byte-for-byte with the uploaded stream. Activation extracts into a temporary directory, requires a regular top-level `index.html`, moves the release into place, deploys only `backend` with `IMAGE_TAG=<sha>`, then switches `current`. PostgreSQL, Keycloak and Caddy are excluded from normal release commands.

The dedicated `papo-deploy` account has no Docker-group membership or general sudo. `authorized_keys` forces one empty-argument sudo invocation; `SSH_ORIGINAL_COMMAND` carries the validated protocol request. Task 2.1's installation and sanitized VM evidence are complete. The separate remote SSH negative-test matrix is not an acceptance requirement for this change; configuration evidence, the isolated harness and successful deployed health/rollback checks cover the agreed scope.

## Risks / Trade-offs

- **[Risk]** A compromised GitHub runner can submit requests with the deploy key. → The gateway accepts only the current fetched `main` SHA and canonical bytes.
- **[Risk]** Backend activation succeeds but the final symlink operation fails. → `current` remains unchanged and rollback is manual and coupled.
- **[Risk]** VM permissions differ from the planned boundary. → Do not enable the new workflow until task 2.1 evidence passes.

## Migration Plan

1. Merge the archived base CI/CD change from `origin/main`.
2. Ship the locally verified gateway, sudoers rule, harness and workflow.
3. Install the restricted account and root-owned files, then verify their permissions and forced-command configuration.
4. Run one verify-only request, then one activation and coupled rollback; mark the change complete after health checks pass.
