---
name: partnerportal-quality
description: Review PAPO Partnerportal changes against project-specific rules for role and tenant scoping, German text, money handling, REST conventions, MVP scope, and required tests. Use after implementing PAPO changes and before commit, merge, or handoff.
---

# Partnerportal quality review

Review the selected diff only. Report findings; do not modify files unless the user explicitly asks for fixes.

## 1. Select scope

- If the user names files, a branch, or a commit range, review exactly that scope.
- Otherwise, if uncommitted changes exist, review `git diff`, `git diff --cached`, and relevant untracked files under `backend/` or `frontend/`.
- Otherwise review `git diff main...HEAD`.

Review changed lines and their immediate context. Mention serious pre-existing issues separately as out of scope.

## 2. Read current rules

Always read:

- `AGENTS.md`
- `CLAUDE.md`
- `docs/CONVENTIONS.md`

Read when relevant:

- `docs/DATA_MODEL.md` for entity/schema changes.
- `openspec/specs/<capability>/spec.md` for behavior changes.
- `Specification/phase2_backlog.md` when scope leakage is possible.
- The active change proposal/design/tasks when reviewing an OpenSpec implementation.

Current primary documents override this checklist. Flag any checklist drift.

## 3. Check hard rules

### Access control and scoping

- Protected REST methods use the correct `@RolesAllowed("company")` or `@RolesAllowed("partner")`.
- Partner endpoints resolve callers with `security/CurrentPartner.resolve()` and never trust a client-supplied partner id.
- Kunden, orders, commission data, and enabled products cannot cross partner boundaries.
- Inactive partners are blocked server-side after authentication and before writes.

### Data and money

- Backend money and percentages use `BigDecimal` mapped to `numeric`, never `double` or `float`.
- Frontend monetary aggregation uses integer cents, not floating-point reduction.
- `Order` maps to table `orders`.
- Timestamps use `java.time`.
- Schema follows the repository's Hibernate generation convention; do not introduce Flyway or manual DDL casually.

### Text and errors

- UI, validation, and user-facing errors are plain German with correct ä/ö/ü/ß.
- Users never see raw stack traces, internal exception messages, or HTTP status codes.
- Backend errors use the established `ApiException` and Jakarta Validation patterns.

### Frontend

- User- and import-supplied HTML passes through `escapeHtml`.
- Use native Web Components, light DOM, and existing modules; do not introduce frameworks, CDNs, or unnecessary dependencies.
- Component state remains local and API access uses the existing `apiFetch` pattern.
- POST forms guard against double submission.

### REST and scope

- Endpoints use `/api/company/...` or `/api/partner/...` role prefixes.
- HTTP verbs match existing conventions; do not add `DELETE` without an explicit capability requirement.
- Do not implement deferred backlog features or re-open settled FSD decisions.

### Tests

- New or changed backend behavior has JUnit/Quarkus/REST Assured coverage in the matching capability test class.
- Authorization and partner-scoping changes include negative tests.
- UI behavior is browser-verified at relevant desktop and mobile sizes when practical.

## 4. Report

Rank findings by severity:

```text
[blocker]    file:line — violated rule — concrete fix
[should-fix] file:line — violated rule — concrete fix
[nit]        file:line — minor divergence — concrete fix
```

- `blocker`: security/scoping hole, wrong money handling, data corruption, or MVP scope violation.
- `should-fix`: missing test or meaningful project convention break.
- `nit`: minor divergence with a clear benefit to correcting.

If clean, say so directly. Do not invent findings. Close by stating whether Codex's generic `/review` should also run for correctness beyond project-specific rules.
