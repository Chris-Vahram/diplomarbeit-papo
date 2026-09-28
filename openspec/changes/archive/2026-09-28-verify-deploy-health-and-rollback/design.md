## Context

The gateway currently validates the exact `main` SHA and frontend archive, then starts the backend and immediately switches `frontend-live/current`. `docker compose up -d` can return before Quarkus finishes startup. The VM already has `curl`, a valid domain certificate, and a protected `/api/company/profile` route that returns HTTP 401 without credentials when Quarkus is reachable. The GitHub workflow does not automatically install changed gateway files on the VM.

## Goals / Non-Goals

**Goals:** Make an unhealthy startup fail closed, keep the old frontend selected, and restore a known-good immutable backend image when possible. Preserve the forced-command SSH boundary and existing service isolation.

**Non-Goals:** Database schema/data rollback, a public health endpoint, new VM credentials, or a generic deployment framework.

## Decisions

- Capture the currently running backend image before replacement. Accept it as a rollback target only when it has the expected repository and a full lowercase 40-character SHA tag. Never use mutable `latest` as a rollback target. Do not edit `.env.prod`; pass `IMAGE_TAG` only to backend-only Compose commands.
- Read only the `DOMAIN=` value from `.env.prod` and validate it as a hostname; do not source that file as root. Probe via `curl --noproxy '*' --resolve <domain>:443:127.0.0.1 https://<domain>/api/company/profile`, preserving certificate validation and expecting exactly HTTP 401. Do not use `curl --fail`, because HTTP 401 is the expected success signal. Require consecutive successful probes with the same container identity, requested immutable image and unchanged restart count; retry for a bounded period because startup is asynchronous.
- Keep `current` unchanged until the new image is ready. On Compose/readiness failure, restart the previously inspected SHA image and verify its readiness. Always return failure for the attempted release; emit a distinct operator message if rollback itself fails or no safe previous image exists.
- On HUP, INT or TERM after backend replacement begins, attempt frontend restoration (if already switched), then backend restoration and verification. Ignore repeated HUP, INT or TERM while recovery runs so a second signal cannot interrupt it. Clear temporary-release cleanup ownership before switching `current`; a failed switch may leave an unused release directory, which is safer than deleting the active release.
- Extend the isolated shell harness with deterministic Docker/curl stubs for delayed readiness, permanent failure, rollback failure, wrong image, identity churn, termination signal, frontend-switch failure and unchanged dependent services. Test mode uses a short readiness window without production-length waits.

## Risks / Trade-offs

- [Risk] A release changes database schema incompatibly, so the old backend cannot start against the new schema. Mitigation: rollback is best-effort and its health is checked; deployments with migrations still require staging, compatible migration design and backup. Never claim DB rollback.
- [Risk] TLS/DNS or proxy failure causes a false negative. Mitigation: probe the production hostname via local loopback with normal certificate validation, and report failure rather than activating an unverified release.
- [Risk] Docker Compose can hang while signal recovery is running; repeated HUP/INT/TERM are ignored to let rollback finish. Mitigation: use the VM admin path for a hung daemon; add bounded Compose calls if this occurs operationally.
- [Risk] The repository script changes but the root-owned VM copy remains old. Mitigation: install the reviewed script through authorized admin access before merging, verify owner/mode/hash, then monitor the main deploy and public endpoints. Keep a root-owned backup for recovery.

## Migration Plan

1. Validate the OpenSpec change, shell syntax and local harness; get independent plan/diff review.
2. With authorized VM access, back up the currently installed root-owned gateway and install the reviewed file without changing sudoers or keys.
3. Merge the PR after CI. Its normal `main` deploy exercises the new gateway on a healthy release; verify image SHA, zero restarts and HTTP 200/401/200 directly.
4. If the new gateway blocks a healthy deploy, restore the backed-up gateway via admin access and inspect logs. Do not delete volumes or roll back database state automatically.
