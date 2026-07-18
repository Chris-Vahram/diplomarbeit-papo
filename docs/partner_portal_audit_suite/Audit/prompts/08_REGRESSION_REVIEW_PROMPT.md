# Partner Portal - Targeted Regression Review

## Objective

Verify only the findings addressed by the most recent fix batch.

Do not perform an unrelated broad audit.

Remain read-only.

## Findings to Verify

Replace this section before running:

- `<FINDING-ID-1>`
- `<FINDING-ID-2>`
- `<FINDING-ID-3>`

## Required Inputs

Read:

1. the relevant original audit findings
2. `Audit/05_FINAL_ACTION_PLAN.md`
3. `Audit/06_FINDINGS_REGISTER.md`
4. the latest fix-batch report
5. the implementation diff
6. relevant tests

## Tasks

For every selected finding:

1. reproduce the original issue where practical
2. inspect the implementation change
3. run the relevant tests
4. verify the expected behavior
5. check for regressions in directly affected flows
6. determine whether the finding is:
   - verified fixed
   - partially fixed
   - not fixed
   - replaced by a different issue
   - no longer applicable

Do not accept the fix solely because tests pass.

Do not reject the fix solely because implementation differs from the original recommendation.

Judge the observed behavior and project requirements.

## Output

Write:

`Audit/HISTORY/<YYYY-MM-DD>_REGRESSION_REVIEW_<SHORT_NAME>.md`

For every finding include:

- finding ID
- verification result
- evidence
- tests run
- observed behavior
- remaining risk
- follow-up action

Update:

`Audit/06_FINDINGS_REGISTER.md`

Only this regression-review phase may mark a finding as `Verified`.
