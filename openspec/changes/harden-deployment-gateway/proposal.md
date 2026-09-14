## Why

The completed base deployment pipeline used a broadly capable SSH key and third-party SCP/SSH actions. Gateway hardening must remain traceable after that base change is archived, without treating the still-unverified VM installation as complete.

## What Changes

- Replace broad remote shell/SCP deployment with a forced-command gateway accepting only an exact `main` commit.
- Validate the uploaded frontend byte-for-byte against a server-generated Git archive before activation.
- Deploy the backend by immutable SHA and switch the existing frontend release symlink atomically without recreating PostgreSQL, Keycloak or Caddy.
- Pin third-party workflow Actions to full commit SHAs.
- Keep VM installation, ownership, sudoers and negative remote tests explicitly open until Tobias supplies evidence.

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
