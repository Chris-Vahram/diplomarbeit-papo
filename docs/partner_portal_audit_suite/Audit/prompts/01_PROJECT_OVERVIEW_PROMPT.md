# Partner Portal - Project Overview and Ground-Truth Report

## Objective

Create a reliable overview of the current Partner Portal project state.

This is an orientation and status task. Do not perform a full bug, security, or UX audit yet.

Remain read-only. Do not modify production code.

## Operating Rules

You may:

- inspect files
- inspect Git history
- inspect OpenSpec changes
- run safe read-only commands
- summarize architecture and project status

Do not:

- modify application code
- commit or push changes
- make product decisions
- perform broad implementation work
- treat existing code as proof that a requirement is approved

If a tool or command is unavailable, continue with the best available alternative and document the limitation.

## Read First

Read these sources completely and in this exact order:

1. `Specification/CONTINUATION_PROMPT.md`
2. `CLAUDE.md`
3. `Specification/functional_specification_document_partnerportal_final.md`
4. `Specification/phase2_backlog.md`
5. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`
6. current contents of `openspec/changes/`
7. `git log --oneline -30`

The latest explicit customer decision overrides older requirements.

Do not infer requirements merely because matching code already exists.

## Mandatory Project Rules

Respect these project rules throughout the review:

- All user-facing language and error messages must use clear Austrian German.
- Umlauts such as `ä`, `ö`, `ü`, and `ß` must be preserved.
- Money must use exact decimal or integer-cent handling.
- Rounding must use `HALF_UP`.
- `money.js` and `commissionCentsHalfUp` are canonical where applicable.
- A customer is scoped to the partner who created that customer.
- A partner must never access another partner's customers, orders, or commissions.
- The fixed stack is:
  - native Web Components
  - Quarkus
  - PostgreSQL
  - Keycloak
- No new dependencies.
- No CDN usage.
- Deferred backlog features B-01 through B-07 must not silently enter the MVP.
- `ci-cd-deployment-with-gh-actions` belongs to Tobias and must not be audited unless it causes a demonstrated integration problem outside that change.

## Tasks

### 1. Summarize the project

Explain briefly:

- purpose of the Partner Portal
- primary user roles
- core business flows
- frontend structure
- backend structure
- database and persistence
- authentication and authorization
- current development phase
- ownership split between Chris and Tobias

### 2. Build an MVP status matrix

For FR-01 through FR-25, classify each requirement as:

- complete
- partially complete
- not implemented
- unclear
- blocked
- possibly outside approved scope

Use this table:

| Requirement | Description | Status | Implementation Evidence | Test Evidence | Remaining Work | Confidence |
|---|---|---|---|---|---|---|

Do not perform a deep code audit here.

Flag suspicious areas for later review instead of trying to fully solve them.

### 3. Summarize recent work

Use OpenSpec and Git history to list:

- recent completed changes
- active changes
- unfinished changes
- changes whose purpose is unclear
- changes that may overlap with deferred backlog items
- changes that appear to belong to Tobias

### 4. Identify review targets

Produce a shortlist of areas that deserve deeper analysis in later phases, especially:

- scope risk
- financial correctness
- authorization and partner isolation
- order creation
- customer ownership
- dormant customer logic
- printing
- file uploads
- session handling
- desktop and mobile UX

### 5. Record unresolved decisions

List genuine requirement or ownership ambiguities.

Do not stop the report for non-blocking ambiguity.

## Output

Write the report to:

`Audit/01_PROJECT_OVERVIEW_REPORT.md`

Use this structure:

1. Executive overview
2. Architecture and major flows
3. MVP status matrix
4. Recent change overview
5. Current project risks
6. Recommended targets for later reviews
7. Open decisions
8. Evidence and limitations

Do not fix code.
