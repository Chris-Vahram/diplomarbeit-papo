# AGENTS.md — PAPO Partnerportal

This file is the Codex companion to `CLAUDE.md`. Keep all Claude files and `.claude/` configuration intact for Tobias. Codex-specific additions belong in `AGENTS.md`, `.agents/`, or `.codex/`.

## Project and source of truth

PAPO is the product/project name for a real internal B2B partner-ordering portal. The real client is unnamed. Core chain: Company → Partner → End Customer.

For deep work, read only as far as the task requires, in this order:

1. `CLAUDE.md` — stable product context, locked scope, stack, roles, and document routing. Its dated “Current status” section may be stale.
2. `Specification/CONTINUATION_PROMPT.md` — newer operational status and handoff rules; verify volatile claims against Git and current files.
3. `Specification/functional_specification_document_partnerportal_final.md` — authoritative MVP requirements.
4. `Specification/phase2_backlog.md` — deferred features and decisions; do not pull them into the MVP.
5. `openspec/changes/<active-change>/` and `openspec/specs/` — current proposal/design/tasks and merged capability behavior.
6. `docs/CONVENTIONS.md`, `docs/SETUP.md`, and `docs/DATA_MODEL.md` — implementation, runtime, and data conventions.
7. Newer meeting notes override older documents when they conflict.

Do not copy old Claude memory facts into code or documentation without checking current primary sources. In particular, never call the client “PAPO” and do not rely on stale model names, branch status, environment diagnoses, or completed UI plans.

## Team and Git boundaries

- Chris owns implementation. Tobias owns visual frontend design and still uses Claude.
- Never delete, move, rename, or replace `CLAUDE.md`, `.claude/`, or Tobias-authored design work as part of Codex setup or normal implementation.
- Chris runs `git commit`, `git push`, and `git merge`. Do not run them unless he explicitly authorizes that exact action.
- Use one branch per feature/change and preserve unrelated working-tree edits.
- End implementation handoffs with the active branch and a ready commit message.
- When a commit message contains a literal `"`, write it to a file and hand Chris `git commit -F <file>`; do not use `-m` or a pasted heredoc.

## Required workflow

- Use OpenSpec for non-trivial features, behavior changes, and user-visible refactors: propose → review → apply tasks → validate → archive.
- Keep one OpenSpec change per feature or concern; do not create mega-changes.
- `tasks.md` is the progress source of truth for an active change.
- For complex/high-stakes changes, independently review the proposal and final diff. Append review findings or intermediate phases; do not silently rewrite the original plan history.
- After implementation, use the project `partnerportal-quality` skill and a generic correctness review.

## Fixed stack

- Frontend: static HTML/CSS/JavaScript with native Web Components and light DOM. No framework, Node, npm, bundler, CDN, or global state store.
- Backend: Java 21, Quarkus REST, Hibernate ORM Panache, OIDC.
- Database: PostgreSQL. Schema through Hibernate generation; no Flyway/manual DDL unless the project convention is explicitly changed.
- Authentication: Keycloak roles `company` and `partner`.
- Money: `BigDecimal`/PostgreSQL `numeric`; frontend aggregation uses integer cents.

Do not apply global Node conventions such as Jest, Supertest, zod/joi, Express middleware, Prisma, or Drizzle to this repository.

## Security and domain hard rules

- Every protected REST method declares the correct `@RolesAllowed` role.
- Partner endpoints resolve identity with `security/CurrentPartner.resolve()`; never trust a client-supplied partner id.
- Kunden, orders, commission data, and enabled products remain partner-scoped. Test cross-partner denial for affected behavior.
- Inactive partners are blocked server-side after authentication and before writes.
- Validate request payloads with Jakarta Bean Validation and return German `ApiException` messages without internal details.
- Use parameterized Panache/ORM queries. Never log or expose tokens, credentials, secrets, or raw stack traces.
- Escape user/import-supplied HTML with `escapeHtml`.
- Keep UI, validation, and error text in plain German with correct ä/ö/ü/ß.

## Testing and verification

Backend tests use JUnit 5, `@QuarkusTest`, and REST Assured as appropriate. New or changed core behavior requires a test in the matching capability test class.

Canonical checks:

```bash
cd backend
./mvnw test
./mvnw package
```

There is no frontend build, lint, npm, or typecheck command. Verify frontend behavior with the existing browser tooling at desktop and mobile sizes when UI behavior changes. Follow `docs/SETUP.md` for runtime startup. If local infrastructure makes a canonical check fail, report the exact failure and diagnose current state instead of copying an old workaround from Claude memory.

Before handoff:

1. Run relevant focused tests during development.
2. Run the complete backend test suite for backend changes.
3. Run `./mvnw package` for implementation changes unless the task is documentation/configuration only.
4. Run `openspec validate <change>` when OpenSpec artifacts changed.
5. Report all commands and exact outcomes.
