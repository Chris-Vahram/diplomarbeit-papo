## Why

The tax-identifier feature adds required columns while production uses Hibernate `update`. Existing demo rows can therefore make startup fail before application-level backfill runs, and an already populated demo database can still lack its singleton company profile.

## What Changes

- Introduce a verified nullable-first migration for the three partner tax-identifier columns.
- Backfill only the known synthetic demo partners, fail atomically for unknown incomplete rows, and enforce database `NOT NULL` constraints after successful backfill.
- Initialize the known demo company profile independently from the all-or-nothing demo dataset guard.
- Align company-name request validation with the 255-character database column.
- Keep real partner data free from invented tax identifiers and preserve existing demo-seeding behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `business-tax-identifiers`: Required identifiers gain an explicit rollout-safe migration path for existing rows.
- `company-profile`: Existing demo databases gain an idempotent current-profile initialization path and matching business-name length validation.

## Impact

Affected areas are the embedded tax-identifier mapping, startup migration/seeding, company-profile request validation, PostgreSQL integration tests, and rollout documentation. No new dependency, API endpoint, tenant model, or production-data fallback is introduced.
