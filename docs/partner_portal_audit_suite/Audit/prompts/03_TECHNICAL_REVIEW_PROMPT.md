# Partner Portal - Technical Quality, Security, and Correctness Review

## Objective

Perform a focused technical audit of the Partner Portal.

Concentrate on:

- confirmed defects
- security risks
- financial correctness
- broken core flows
- database behavior
- missing high-value tests

Remain read-only except for running tests and safe local verification.

## Required Context

Read:

1. `Specification/CONTINUATION_PROMPT.md`
2. `CLAUDE.md`
3. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`
4. relevant FSD sections
5. `Audit/01_PROJECT_OVERVIEW_REPORT.md`
6. `Audit/02_SCOPE_REVIEW_REPORT.md`
7. recent Git diffs
8. relevant OpenSpec changes

Previous reports are structured input, not authoritative truth.

Verify all material findings against code, tests, runtime behavior, or primary project documents.

## Safety Rules

Only test against the local development environment.

Do not:

- target external systems
- use destructive payloads
- expose secrets
- permanently alter shared data
- modify production code
- commit or push changes

## Priority Areas

### 1. Authorization and partner isolation

Actively verify where possible:

- Partner A cannot access Partner B's customers.
- Partner A cannot access Partner B's orders.
- Partner A cannot access Partner B's commissions.
- UI restrictions are also enforced server-side.
- inactive users are rejected correctly after authentication.
- IDs cannot be used to bypass object ownership.
- direct API calls cannot bypass role restrictions.

### 2. Money and commission logic

Check:

- integer-cent or exact decimal handling
- `HALF_UP`
- custom commission percentages
- awkward totals and rounding boundaries
- frontend-backend consistency
- order-item price snapshots
- commission snapshots
- overflow and unsafe conversion
- zero and boundary values

### 3. Order flow

Check:

- quantity boundaries
- cart validation
- duplicate submission
- stale prices
- empty cart
- transaction behavior
- validation feedback
- order ownership
- partial persistence failures
- double-click and rapid-submit behavior

### 4. Data and query correctness

Check:

- `PartnerNumber.canonicalize()`
- `Locale.ROOT` uppercase handling
- import paths
- dormant customer 60-day boundary
- customers without orders
- duplicate query results
- N+1 queries
- inefficient loading
- timezone and date semantics

### 5. Printing and uploads

Check:

- multi-page print behavior
- cloned DOM behavior
- long values and umlauts
- duplicate IDs after cloning
- hidden interactive elements in print
- product image type validation
- file-size validation
- path traversal
- unsafe filenames
- stored content exposure
- MIME and extension mismatch

### 6. Tests

Run available automated tests.

For the backend:

```bash
cd backend
./mvnw test
```

If the documented Keycloak dev-services timeout occurs on macOS, retry once and record both attempts.

Identify missing high-value tests for:

- authorization
- money boundaries
- duplicate submission
- dormant customer boundary
- customer ownership
- order ownership
- inactive accounts
- uploads
- umlauts
- unhappy paths

## Finding Standard

Report only findings with evidence.

For every finding include:

- ID
- severity
- confidence
- file and line
- affected flow
- actual behavior
- expected behavior
- reproduction or reasoning
- impact
- recommended fix direction
- suggested owner
- whether a regression test is required
- whether the finding affects MVP scope

Severity:

- Critical: authentication bypass, broad data exposure, destructive data loss
- High: broken partner isolation, incorrect financial result, broken core flow
- Medium: meaningful edge-case failure or important test gap
- Low: limited defensive or maintainability improvement

## Cross-Check

Use Sol or Codex only for:

- Critical findings
- High findings
- findings below 90 percent confidence
- disagreements between reviewers

Give the secondary model only the relevant files, diff, and focused questions.

Record whether each checked finding was:

- confirmed
- partially confirmed
- disputed
- not checked
- tool unavailable

## Output

Write the report to:

`Audit/03_TECHNICAL_REVIEW_REPORT.md`

Use this structure:

1. Technical verdict
2. Critical and High findings
3. Correctness findings
4. Security findings
5. Database and performance findings
6. Test failures
7. Missing tests
8. Medium and Low findings
9. Cross-check disagreements
10. Audit limitations

Do not fix code.
