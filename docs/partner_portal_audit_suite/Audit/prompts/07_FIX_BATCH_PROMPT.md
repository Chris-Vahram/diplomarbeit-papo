# Partner Portal - Targeted Fix Batch

## Objective

Implement only the explicitly selected findings from the final action plan.

Do not fix unrelated findings.

Do not expand scope.

## Selected Findings

Replace this section before running the prompt:

- `<FINDING-ID-1>`
- `<FINDING-ID-2>`
- `<FINDING-ID-3>`

## Required Context

Read:

1. `Audit/05_FINAL_ACTION_PLAN.md`
2. `Audit/06_FINDINGS_REGISTER.md`
3. the source reports for the selected findings
4. relevant specification and meeting decisions
5. relevant code and tests

## Rules

- Implement only the selected findings.
- Preserve approved project scope.
- Do not introduce new dependencies.
- Do not use a CDN.
- Preserve Austrian German and umlauts.
- Preserve exact money handling and `HALF_UP`.
- Preserve partner-level data isolation.
- Do not modify Tobias-owned CI/CD work unless the selected finding explicitly concerns an integration issue outside that change.
- Add or update regression tests for every fixed defect where practical.
- Do not silently redesign unrelated code.

## Workflow

For each selected finding:

1. verify the finding still exists
2. identify the smallest safe fix
3. implement the fix
4. add or update tests
5. run relevant tests
6. run the broader affected test suite
7. document any remaining limitation

## Output

After implementation, create:

`Audit/HISTORY/<YYYY-MM-DD>_FIX_BATCH_<SHORT_NAME>.md`

Include:

- selected finding IDs
- files changed
- implementation summary
- tests added or updated
- commands run
- test results
- unresolved issues
- recommended regression-review scope

Update the status in:

`Audit/06_FINDINGS_REGISTER.md`

Use `In Progress` until a separate regression review confirms the fix.

Do not mark findings `Verified` yourself.
