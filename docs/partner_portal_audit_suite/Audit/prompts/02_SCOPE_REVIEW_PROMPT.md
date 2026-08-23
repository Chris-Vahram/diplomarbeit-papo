# Partner Portal - Scope Discipline Review

## Objective

Determine whether recent development work remains within the approved MVP scope.

The primary question is:

> Is Chris building more than the customer, the latest meeting, or the approved specification requires?

This is a read-only review. Do not modify production code.

## Required Context

Read:

1. `Specification/CONTINUATION_PROMPT.md`
2. `CLAUDE.md`
3. `Specification/functional_specification_document_partnerportal_final.md`
4. `Specification/phase2_backlog.md`
5. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`
6. `Audit/01_PROJECT_OVERVIEW_REPORT.md`
7. current OpenSpec changes
8. recent Git history and relevant diffs

Treat the previous overview report as structured input, not as authoritative truth.

Verify material claims against primary project documents and repository evidence.

## Mandatory Project Rules

Respect these rules:

- Austrian German for all user-facing text
- mandatory umlaut preservation
- exact money handling
- `HALF_UP` rounding
- partner-level data isolation
- fixed technology stack
- no new dependencies
- no CDN
- B-01 through B-07 remain deferred
- Tobias owns `ci-cd-deployment-with-gh-actions`

## Review These Changes

At minimum:

- `add-order-list-filters`
- `add-dormant-kunde-visibility`
- `add-order-document-print`
- `partner-order-cart`
- `improve-order-form`
- `remove-keycloak-username-field`
- `add-keycloak-session-refresh`
- `self-host-fonts`
- `order-correctness-hardening`
- `add-product-image-upload`

Also include any newer relevant changes discovered through OpenSpec or Git.

## Classification

Map every change to one or more of:

- direct request from the 10 July meeting
- explicit FSD requirement
- documented earlier decision
- required correctness or security hardening
- reasonable implementation detail
- polish beyond the request
- scope creep
- partial implementation of B-01 through B-07
- unsupported or unclear

Use this table:

| Change | Source | Customer Value | Classification | Complexity | Recommendation |
|---|---|---|---|---|---|

Allowed verdicts:

- on-scope
- required hardening
- polish beyond request
- scope creep
- violates MVP boundary
- unclear

## Also Check

- deferred features partially entering the MVP
- unnecessary configurability
- speculative abstractions
- generic frameworks for one-time functionality
- extra user flows
- excessive error handling for impossible states
- duplicated or overlapping features
- maintenance burden without current customer value
- work that should be simplified instead of expanded

Do not label required security, correctness, or data-integrity work as scope creep merely because it was not explicitly requested.

## Required Verdict

Start with exactly one of:

- Chris is within scope.
- Chris is slightly beyond scope in the following areas: ...
- Chris is materially beyond scope in the following areas: ...

Support the verdict with direct evidence.

## Output

Write the report to:

`Audit/02_SCOPE_REVIEW_REPORT.md`

Use this structure:

1. Scope verdict
2. Change-by-change mapping
3. Possible deferred-feature leakage
4. Gold-plating and overengineering
5. Work that should remain
6. Work that should be simplified, deferred, or removed
7. Confidence and limitations

Do not review unrelated code quality unless it changes the scope conclusion.

Do not fix code.
