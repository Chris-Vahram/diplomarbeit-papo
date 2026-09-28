## Why

The base deployment pipeline used a broadly capable remote deployment path. This change records the constrained gateway, installed VM boundary and verified release behavior alongside that archived pipeline.

## What Changes

- Replace broad remote shell/SCP deployment with a forced-command gateway accepting only an exact `main` commit.
- Validate the uploaded frontend byte-for-byte against a server-generated Git archive before activation.
- Deploy the backend by immutable SHA and switch the existing frontend release symlink atomically without recreating PostgreSQL, Keycloak or Caddy.
- Pin third-party workflow Actions to full commit SHAs.
- Verify the installed VM boundary and deployed runtime using configuration evidence, the local harness, and successful release/rollback checks.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `deployment-pipeline`: restrict deployment authorization, archive validation and activation failure behavior.

## Impact

- `.github/workflows/ci-cd.yml`
- `deploy/ssh/`
- `docs/DEPLOYMENT.md`
- Production VM SSH account, repository key and sudoers installation after review
