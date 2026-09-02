# AGENTS.md — PAPO Partnerportal

This file is the Codex companion to `CLAUDE.md`. Keep all Claude files and `.claude/` configuration intact for Tobias. Codex-specific additions belong in `AGENTS.md`, `.agents/`, or `.codex/`.

## Project and source of truth

PAPO is the product/project name for an extensible B2B partner-ordering platform. Its first operational stage replaces partner orders received by e-mail and telephone with a structured internal portal. The real reference client is unnamed. Core chain: Company → Partner → End Customer.

In every fresh deep-work session read the full continuation prompt first. Before a non-trivial change, read and analyze the remaining sources relevant to that change in this order:

1. `Specification/CONTINUATION_PROMPT.md` — self-contained current handoff, required reading in every fresh session; verify its volatile Git/OpenSpec snapshot.
2. `CLAUDE.md` — stable product context, locked scope, stack, roles, and document routing.
3. `Documentation/Besprechungsprotokolle/` — newer meeting notes override older documents and the FSD when they conflict; newest is `Besprechungsprotokoll31_08_2026.md`.
4. `Specification/functional_specification_document_partnerportal_final.md` — authoritative baseline requirements until synchronized by an approved documentation/OpenSpec change.
5. `Specification/phase2_backlog.md` — deferred features and decisions; do not pull them into the current stage.
6. `Specification/ROADMAP_2026-08-25.md` — persisted future plan, updated through the meeting of 31.08.2026; planning context, not implemented behavior.
7. `openspec/changes/<active-change>/` and `openspec/specs/` — current proposal/design/tasks and merged capability behavior.
8. `docs/CONVENTIONS.md`, `docs/SETUP.md`, and `docs/DATA_MODEL.md` — implementation, runtime, and data conventions.

Do not copy old Claude memory facts into code or documentation without checking current primary sources. In particular, never call the client “PAPO” and do not rely on stale model names, branch status, environment diagnoses, or completed UI plans.

## Current product direction

- The thesis result is a functioning, extensible product, not a permanently single-company system. The current demo has one reference company only.
- Multi-company tenancy with company subdomains is the correct architecture when a second company is onboarded, but it is explicitly not the next implementation step and has no fixed thesis deadline yet.
- The current core remains partner-driven ordering. The public end-customer partner shop stays later/Phase 2.
- For the current stage each partner company has one shared organizational login; personal employee accounts are not required.
- User-facing and thesis language uses **Artikel**. Existing internal Java/entity/API names such as `Product` do not need a speculative rename.
- BMD is the leading article-master system. PAPO currently receives data through Excel/CSV import; no direct BMD interface is approved.
- The visible business identifier is the unique BMD article number. PAPO's database id remains technical; no second visible article id is needed. The price-list area is omitted.
- Category is relevant. Fachbereich, product group A/B/C/D, bundles/BOMs, training permissions, and PP/margin-based commission logic are future directions only until the simplified company follow-up confirms a smaller scope.

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
- KI usage is documented through `Documentation/KI-Nachweise/`. The local LaunchAgent secures sessions automatically; never invent missing verification evidence.
- After relevant PAPO work, run `python3 tools/ai_evidence_log.py sync`, then enrich the current session with `python3 tools/ai_evidence_log.py enrich --purpose "<Zweck>" --result "<Ergebnis>" --check "<wirklich ausgeführte Prüfung>"`. Omit `--check` when no technical verification ran.

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
