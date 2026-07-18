# Partner Portal - Final Audit Synthesis and Action Plan

## Objective

Combine the existing audit reports into one practical, prioritized action plan.

Do not perform a complete new audit from scratch.

Verify only the most important disputed, unclear, Critical, or High-severity claims against primary evidence.

Do not modify production code.

## Required Inputs

Read:

1. `Audit/01_PROJECT_OVERVIEW_REPORT.md`
2. `Audit/02_SCOPE_REVIEW_REPORT.md`
3. `Audit/03_TECHNICAL_REVIEW_REPORT.md`
4. `Audit/04_UX_REVIEW_REPORT.md`
5. `Specification/CONTINUATION_PROMPT.md`
6. `CLAUDE.md`
7. the latest meeting protocol
8. current OpenSpec status
9. recent Git history

Treat all previous audit reports as structured input, not authoritative truth.

Resolve contradictions using primary project documents, code evidence, test evidence, and reproducible runtime behavior.

## Tasks

### 1. Consolidate findings

Merge duplicates and related findings.

Keep stable IDs where possible.

Do not create multiple entries for the same root cause.

### 2. Validate priorities

Re-rank findings according to:

1. security and partner isolation
2. financial correctness
3. broken core flows
4. approved MVP completeness
5. next customer meeting readiness
6. high-risk test coverage
7. usability for non-technical partners
8. maintainability
9. optional polish

Do not prioritize bugs in features that should be removed or deferred unless they affect shared code or security.

### 3. Assign action status

Use these statuses:

- P0 - immediate blocker
- P1 - before next customer meeting
- P2 - before feature freeze
- P3 - before final submission
- P4 - after MVP
- REMOVE - remove from MVP
- DEFER - move to Phase 2
- NO ACTION - not convincing or not worth changing
- DECISION REQUIRED - needs Chris, Tobias, or customer input

### 4. Assign ownership

Use:

- Chris
- Tobias
- both
- decision required

Do not assign Tobias work that belongs to Chris without evidence.

Do not assign Chris the CI/CD change owned by Tobias.

### 5. Estimate effort

Use:

- XS: less than 1 hour
- S: 1 to 4 hours
- M: roughly half to one working day
- L: 1 to 2 working days
- XL: more than 2 working days or requires decomposition

### 6. Build the implementation sequence

Group work into:

1. immediate blockers
2. scope decisions and removals
3. authorization and financial correctness
4. core order-flow fixes
5. required MVP gaps
6. UX improvements
7. test coverage
8. cleanup and documentation

Account for dependencies.

Do not recommend fixing low-value issues before scope decisions are settled.

## Output

Write the final plan to:

`Audit/05_FINAL_ACTION_PLAN.md`

Use this structure:

1. Executive verdict
2. Current MVP readiness
3. Final scope conclusion
4. Confirmed Critical and High findings
5. Findings to remove, defer, reject, or decide
6. Prioritized action plan
7. Recommended fix batches
8. Decisions required
9. If you only do three things before the meeting
10. Audit limitations

Use this table for the action plan:

| Priority | Finding ID | Action | Reason | Owner | Effort | Dependencies | Risk if Deferred | Definition of Done |
|---|---|---|---|---|---|---|---|---|

List exactly three actions in:

`If you only do three things before the meeting`

Do not fix code.
