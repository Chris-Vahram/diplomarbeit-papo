## Why

BMD is the leading article-master system, but PAPO currently identifies imported articles by name and has no visible BMD article number. A stable unique number is required before reliable BMD exports can be imported.

## What Changes

- Add a required visible BMD article number stored as text and unique after canonicalization.
- Make the number the duplicate key for manual article management and Excel/CSV import.
- Preserve leading zeros and report duplicates within the file and against existing data by row.
- Migrate synthetic demo articles without changing technical IDs or existing relationships.
- Keep import create-only until a real BMD sample defines update/deactivation semantics.
- Do not create a PAPO-to-BMD article-master export; BMD remains the master.
- Do not add a direct BMD API connector, synchronization, price lists, product groups or bundles.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `product-management`: Articles have a required visible unique BMD article number.
- `data-import`: Article imports map, validate and deduplicate by BMD article number instead of name.

## Impact

Affected areas are Product persistence and DTOs, company and partner article views, import mapping/preview/commit, demo data, concurrency/error handling, tests and documentation. No new dependency is required.
