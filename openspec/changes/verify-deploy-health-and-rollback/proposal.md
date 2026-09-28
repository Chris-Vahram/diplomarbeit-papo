## Why

The first deployment of `5fff3b0` reported success even though the backend crashed during startup and the public API returned HTTP 502. The gateway currently treats `docker compose up -d` as proof of health and switches the frontend immediately. A failed release must not appear successful or leave a broken backend behind when a known-good SHA image can be restored.

## What Changes

- Check the exact new backend image and the unauthenticated protected API response through local HTTPS after Compose starts it; accept the release only after bounded readiness checks return HTTP 401.
- Keep the previous frontend symlink until the backend passes. On activation failure, attempt to restore the previous immutable backend image and report failure even when rollback succeeds.
- Test healthy startup, startup failure, failed rollback, and unchanged PostgreSQL/Keycloak/Caddy behavior in the isolated gateway harness.
- Document the operator boundary: image/frontend rollback does not reverse database schema or data migrations.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `deployment-pipeline`: deployment success requires backend readiness; failed activation must preserve or restore the prior release when possible.

## Impact

- `deploy/ssh/papo-deploy-gateway`, its isolated shell harness, `docs/DEPLOYMENT.md`, and the root-owned gateway installation on the authorized demo VM.
- No new application endpoint, dependency, secret, database migration, or wider SSH command capability.
