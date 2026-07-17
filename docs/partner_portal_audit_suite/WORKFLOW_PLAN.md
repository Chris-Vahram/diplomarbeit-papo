# Partner Portal Audit and Improvement Workflow

## Purpose

This workflow explains how to use the individual audit prompts, where reports are stored, when fixes should happen, and how future features or changes are reviewed.

The process is designed to use a strong repository agent such as Fable for broad project work and a strong reasoning model such as Sol for focused cross-checking.

---

## 1. Repository Structure

Create this structure in the repository:

```text
Audit/
├── 00_AUDIT_INDEX.md
├── 01_PROJECT_OVERVIEW_REPORT.md
├── 02_SCOPE_REVIEW_REPORT.md
├── 03_TECHNICAL_REVIEW_REPORT.md
├── 04_UX_REVIEW_REPORT.md
├── 05_FINAL_ACTION_PLAN.md
├── 06_FINDINGS_REGISTER.md
├── prompts/
│   ├── 01_PROJECT_OVERVIEW_PROMPT.md
│   ├── 02_SCOPE_REVIEW_PROMPT.md
│   ├── 03_TECHNICAL_REVIEW_PROMPT.md
│   ├── 04_UX_REVIEW_PROMPT.md
│   ├── 05_FINAL_SYNTHESIS_PROMPT.md
│   ├── 06_INCREMENTAL_REVIEW_PROMPT.md
│   ├── 07_FIX_BATCH_PROMPT.md
│   └── 08_REGRESSION_REVIEW_PROMPT.md
├── evidence/
│   ├── screenshots/
│   ├── test-results/
│   └── logs/
└── HISTORY/
```

Git provides the main version history.

The `HISTORY` folder is used for dated delta reviews, fix-batch reports, and regression-review reports.

---

## 2. Main Audit Sequence

Run the main prompts sequentially, not in parallel.

Use this order:

```text
1. Project Overview
2. Scope Review
3. Technical Review
4. UX Review
5. Final Synthesis and Action Plan
```

The next phase may read earlier reports, but it must treat them as structured input rather than unquestionable truth.

Material claims must still be checked against:

- primary project documents
- code
- tests
- Git history
- OpenSpec
- reproducible runtime behavior

---

## 3. Phase 1 - Project Overview

Run:

`Audit/prompts/01_PROJECT_OVERVIEW_PROMPT.md`

Expected output:

`Audit/01_PROJECT_OVERVIEW_REPORT.md`

Purpose:

- understand the current project
- summarize architecture
- map FR-01 through FR-25
- identify recent work
- find areas that need deeper review

Do not fix anything after this phase.

Only check whether the project was understood correctly and whether major source documents were missed.

---

## 4. Phase 2 - Scope Review

Run:

`Audit/prompts/02_SCOPE_REVIEW_PROMPT.md`

Expected output:

`Audit/02_SCOPE_REVIEW_REPORT.md`

Purpose:

- determine whether Chris is within MVP scope
- classify recent changes
- identify gold-plating
- identify deferred-feature leakage
- decide what should remain, be simplified, be deferred, or be removed

Do not begin normal fixes yet.

A scope decision should happen before investing time in bugs or polish inside features that may be removed.

---

## 5. Phase 3 - Technical Review

Run:

`Audit/prompts/03_TECHNICAL_REVIEW_PROMPT.md`

Expected output:

`Audit/03_TECHNICAL_REVIEW_REPORT.md`

Purpose:

- find real bugs
- verify partner isolation
- verify money and commission logic
- test order flows
- inspect database behavior
- identify missing high-value tests

The technical reviewer should avoid spending excessive time on features already marked for removal or deferral.

Exceptions:

- shared security risk
- shared data-integrity risk
- shared code used by retained MVP features

---

## 6. Phase 4 - UX Review

Run:

`Audit/prompts/04_UX_REVIEW_PROMPT.md`

Expected output:

`Audit/04_UX_REVIEW_REPORT.md`

Purpose:

- review retained MVP flows
- test desktop and mobile
- check usability for non-technical partners
- check accessibility and print output
- identify high-value polish

Do not deeply review features that are clearly being removed or deferred.

---

## 7. Phase 5 - Final Synthesis

Run:

`Audit/prompts/05_FINAL_SYNTHESIS_PROMPT.md`

Expected output:

`Audit/05_FINAL_ACTION_PLAN.md`

This is the point where all findings are:

- consolidated
- de-duplicated
- prioritized
- assigned
- estimated
- ordered by dependency
- accepted, rejected, deferred, removed, or marked for decision

The final plan should use:

- P0 - immediate blocker
- P1 - before next customer meeting
- P2 - before feature freeze
- P3 - before final submission
- P4 - after MVP
- REMOVE
- DEFER
- NO ACTION
- DECISION REQUIRED

Only after this phase should normal implementation work begin.

---

## 8. When to Fix Immediately

Do not fix after every review by default.

Fix immediately only when a confirmed issue is one of:

- data loss
- authentication bypass
- partner access to foreign data
- incorrect financial or commission result
- a blocker that prevents the remaining audit
- a repository or environment failure that makes further review impossible

For an immediate fix:

1. document the finding
2. create a small isolated fix
3. run tests
4. perform a targeted regression review
5. continue the remaining audit

Do not use immediate fixes for ordinary Medium or Low findings.

---

## 9. Normal Fix Workflow

After the final action plan, fix in small batches.

Recommended order:

```text
Fix Batch 1 - Critical security and partner isolation
Fix Batch 2 - Money and commission correctness
Fix Batch 3 - Core order-flow bugs
Fix Batch 4 - Required MVP gaps
Fix Batch 5 - Desktop and mobile UX
Fix Batch 6 - Test coverage and cleanup
```

For every batch:

```text
Select findings
→ run targeted fix prompt
→ implement smallest safe changes
→ run tests
→ create fix-batch report
→ run targeted regression review
→ update finding statuses
→ commit
```

Use:

`Audit/prompts/07_FIX_BATCH_PROMPT.md`

Then:

`Audit/prompts/08_REGRESSION_REVIEW_PROMPT.md`

Do not send one prompt that says:

> Fix everything from all reports.

That usually causes scope expansion, missed regressions, and poor traceability.

---

## 10. Findings Register

Maintain:

`Audit/06_FINDINGS_REGISTER.md`

Use stable IDs:

- `SCOPE-001`
- `BUG-001`
- `SEC-001`
- `UX-001`
- `TEST-001`
- `DOC-001`

Recommended columns:

| ID | Title | Category | Severity | Priority | Status | Owner | Source Report | Introduced Commit | Fix Commit | Verification Report |
|---|---|---|---|---|---|---|---|---|---|---|

Allowed statuses:

- Open
- Confirmed
- In Progress
- Fixed
- Verified
- Deferred
- Rejected
- Duplicate
- Decision Required

Rules:

- Fix implementation changes status to `Fixed`.
- Only a separate regression review changes status to `Verified`.
- Do not delete rejected or duplicate findings.
- Preserve history.

---

## 11. Audit Index

Maintain:

`Audit/00_AUDIT_INDEX.md`

Track:

- audit date
- baseline commit
- current HEAD
- completed review phases
- report paths
- tools used
- unresolved blockers
- number of open findings
- number of verified findings
- next planned review
- latest incremental review

Suggested structure:

```markdown
# Audit Index

## Current Baseline

- Initial audit date:
- Baseline commit:
- Current reviewed commit:
- Latest meeting document:

## Completed Reports

| Phase | Status | Report | Reviewed Commit |
|---|---|---|---|

## Finding Summary

| Status | Count |
|---|---|

## Next Actions

1.
2.
3.
```

---

## 12. Using Fable and Sol

### Fable

Use Fable as the primary repository agent for:

- reading the project
- running the main audit prompts
- using Git and OpenSpec
- starting the local environment
- running tests
- using Playwright
- producing the main reports
- implementing selected fix batches

### Sol

Use Sol as an independent challenger for:

- the final scope verdict
- Critical findings
- High findings
- findings below 90 percent confidence
- disagreements between reviewers
- reviewing a small relevant diff
- checking whether a proposed fix really solves the root cause

Do not give Sol the entire repository audit again unless a full independent review is intentionally desired.

Give Sol:

- exact files
- exact diff
- short context
- three to four focused questions
- request for evidence and file-line references

Sol should challenge specific conclusions, not duplicate the full process.

---

## 13. New Features and Future Changes

Do not rerun the complete audit after every new feature.

Use the incremental review prompt:

`Audit/prompts/06_INCREMENTAL_REVIEW_PROMPT.md`

The incremental review should:

- compare the current branch against the last reviewed baseline commit
- inspect new OpenSpec changes
- inspect new meeting or specification decisions
- update affected findings
- create new stable finding IDs
- update the final action plan only where necessary
- preserve older reports and conclusions

Expected dated output:

`Audit/HISTORY/<YYYY-MM-DD>_INCREMENTAL_REVIEW.md`

Run a full new audit only when:

- the architecture changes significantly
- authentication or data ownership changes
- the FSD changes materially
- several core flows are replaced
- the previous audit is too old to remain reliable
- the project approaches feature freeze or final submission

---

## 14. Commit Strategy

Recommended commits:

```text
audit: add initial project overview
audit: add scope review
audit: add technical review
audit: add UX review
audit: add final action plan
fix(auth): resolve SEC-001 and SEC-002
test(auth): add cross-partner regression coverage
audit: verify authorization fix batch
```

Keep audit-only commits separate from implementation commits where practical.

This makes later comparison and review easier.

---

## 15. Decision Rules

Do not let the model silently decide unresolved product questions.

Use `DECISION REQUIRED` when the answer requires:

- Chris
- Tobias
- the customer
- the supervising teacher

Continue all unaffected review work.

Stop only when:

- a destructive action would be required
- the local environment cannot safely run
- essential source files are missing
- a technical blocker makes the remaining review impossible

---

## 16. Recommended Complete Cycle

```text
Initial audit
→ final action plan
→ scope decisions
→ fix batch
→ regression review
→ update findings register
→ commit
→ new feature development
→ incremental review
→ next fix batch
```

Before the next customer meeting:

```text
Run or update scope review
→ run relevant incremental technical review
→ update UX evidence
→ regenerate final action plan
→ select the three highest-leverage actions
```

Before feature freeze:

```text
Run a broader incremental review
→ verify all P0, P1, and P2 findings
→ confirm MVP completeness
→ reject or defer remaining non-essential work
```

Before final submission:

```text
Run final technical regression review
→ run final UX and print review
→ confirm test suite
→ verify OpenSpec and documentation hygiene
→ archive completed findings
```

---

## 17. Core Principle

The correct sequence is:

```text
Review
→ verify
→ prioritize
→ decide
→ implement
→ regression-test
→ update records
```

Do not use:

```text
Review
→ immediately fix everything
```

The first process protects scope, reduces wasted work, and makes every model decision traceable.
