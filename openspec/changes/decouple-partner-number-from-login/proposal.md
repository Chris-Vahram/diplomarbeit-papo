## Why

BMD shall provide the visible business partner number. Today partner numbers are free text set at creation, and import dedupe does not follow BMD rules. Login identity no longer depends on the partner number once `bind-partner-login-to-oidc-subject` is done, so the number can be changed safely.

## What Changes

- Treat the BMD partner number as the visible, unique business identifier (text, leading zeros kept, canonical comparison from the BMD sample).
- Allow the company to replace demo/legacy numbers with BMD numbers; manual and imported numbers follow the same rules.
- No login, provisioning, tenancy or direct BMD connector changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-management`: BMD partner number integrity.

## Impact

Partner validation, partner import mapping/dedupe, company partner form and docs. Depends on `bind-partner-login-to-oidc-subject` (Release B deployed) and on an anonymized BMD sample or rule sheet. Technical IDs and relations stay unchanged.
