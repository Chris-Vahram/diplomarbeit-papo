# Partner Portal - Incremental Review for New Changes

## Objective

Review only the changes introduced since the previous completed audit.

Do not repeat the entire audit unless new evidence shows that a broader re-review is necessary.

Remain read-only.

## Required Inputs

Read:

1. `Audit/00_AUDIT_INDEX.md`
2. `Audit/05_FINAL_ACTION_PLAN.md`
3. `Audit/06_FINDINGS_REGISTER.md`
4. the relevant current audit reports
5. current OpenSpec changes
6. latest meeting notes or specification updates
7. Git history and diff since the recorded baseline commit

Use:

```bash
git diff <BASE_COMMIT>..HEAD
```

Replace `<BASE_COMMIT>` with the baseline commit recorded in `Audit/00_AUDIT_INDEX.md`.

## Tasks

### 1. Identify new scope

List:

- new features
- changed requirements
- new OpenSpec changes
- modified core flows
- newly affected files
- changes in ownership

### 2. Review only affected areas

Check whether the new changes affect:

- approved MVP scope
- deferred backlog boundaries
- partner isolation
- money or commission logic
- order flow
- customer ownership
- printing
- uploads
- session handling
- desktop or mobile UX
- existing tests

### 3. Update findings

For existing findings:

- keep the same stable ID
- update status only when evidence supports it
- do not erase historical conclusions
- mark regressions clearly
- link new evidence

For new findings, use the next available stable ID:

- `SCOPE-###`
- `BUG-###`
- `SEC-###`
- `UX-###`
- `TEST-###`
- `DOC-###`

### 4. Update the action plan

Re-prioritize only the items affected by the new changes.

Do not rewrite unrelated priorities.

## Output

Write a dated delta report to:

`Audit/HISTORY/<YYYY-MM-DD>_INCREMENTAL_REVIEW.md`

Then update:

- `Audit/00_AUDIT_INDEX.md`
- `Audit/05_FINAL_ACTION_PLAN.md`
- `Audit/06_FINDINGS_REGISTER.md`

Do not modify application code.
