# AGENTS.md — PAPO Partnerportal

Codex companion to `CLAUDE.md`. Keep all Claude files and `.claude/` configuration intact for Tobias; Codex additions belong in `AGENTS.md`, `.agents/`, or `.codex/`.

## Project and sources

PAPO is an extensible B2B partner-ordering platform, not the unnamed reference client. First stage replaces partner e-mail/telephone orders with an internal portal: Company → Partner → End Customer.

For every fresh deep-work session read `Specification/CONTINUATION_PROMPT.md` fully first and verify its volatile Git/OpenSpec snapshot. Before a non-trivial change, use these sources in order:

1. `Specification/CONTINUATION_PROMPT.md` — current handoff.
2. `CLAUDE.md` — stable context, scope, stack, roles, routing.
3. `Documentation/Besprechungsprotokolle/` — newer notes override older notes and FSD; newest: `Besprechungsprotokoll_10.9.md`.
4. `Specification/functional_specification_document_partnerportal_final.md` — baseline requirements until an approved documentation/OpenSpec sync.
5. `Specification/phase2_backlog.md` — deferred features/decisions; do not pull into this stage.
6. `Specification/ROADMAP_2026-08-25.md` — future planning, not implemented behavior.
7. `openspec/changes/<active-change>/` and `openspec/specs/` — active tasks and merged behavior.
8. `docs/CONVENTIONS.md`, `docs/SETUP.md`, `docs/DATA_MODEL.md` — implementation/runtime/data rules.

Never copy stale Claude-memory facts into code/docs: client is never “PAPO”; verify model names, branch/environment state, and completed UI work against primary sources.

## Locked direction

- Thesis delivers an extensible product; current demo has one reference company. Multi-company subdomains are correct only when a second company is onboarded, not the next step and without fixed thesis deadline.
- Current core is partner-driven orders with one shared organizational login per partner company; public end-customer shop and personal employee accounts are Phase 2/later.
- User-facing/thesis term is **Artikel**; internal `Product` names may remain. BMD leads article data; PAPO imports Excel/CSV only, with no approved direct BMD interface. Visible ID is unique BMD article number; DB ID remains technical and price lists are omitted.
- Category matters. Fachbereich, product groups A–D, bundles/BOMs, training permissions, and PP/margin commission await the simplified company follow-up.

## Team, Git, workflow

- Chris implements; Tobias owns visual frontend design and uses Claude. Never delete, move, rename, or replace `CLAUDE.md`, `.claude/`, or Tobias-authored design work.
- Chris alone runs commit/push/merge; preserve unrelated changes and use one branch per feature/change. Handoff includes active branch and ready commit message. For a literal `"` in a commit message, write a file and give Chris `git commit -F <file>`, never `-m` or a pasted heredoc.
- Use OpenSpec for non-trivial features, behavior changes, and user-visible refactors: propose, review, apply top-down, validate, archive. One concern/change; `tasks.md` is progress truth. Complex/high-stakes work needs independent plan and final-diff review; append findings/phases rather than rewriting history.
- After implementation use `partnerportal-quality` and a generic correctness review. KI usage is documented through `Documentation/KI-Nachweise/`; the local LaunchAgent secures sessions automatically. For relevant PAPO work run `python3 tools/ai_evidence_log.py sync`, then `python3 tools/ai_evidence_log.py enrich --purpose "<Zweck>" --result "<Ergebnis>" --check "<wirklich ausgeführte Prüfung>"`; omit `--check` without a technical check. Never invent evidence.

## Fixed stack

- Frontend: static HTML/CSS/JavaScript, native Web Components, light DOM; no framework, Node, npm, bundler, CDN, or global store.
- Backend: Java 21, Quarkus REST, Hibernate ORM Panache, OIDC. Database: PostgreSQL via Hibernate generation; no Flyway/manual DDL unless convention changes.
- Auth: Keycloak roles `company` and `partner`. Money: `BigDecimal`/PostgreSQL `numeric`; frontend sums integer cents. Do not introduce global Node conventions such as Jest, Supertest, zod/joi, Express middleware, Prisma, or Drizzle.

## Security and domain rules

- Every protected REST method declares correct `@RolesAllowed`; partner endpoints use `security/CurrentPartner.resolve()`, never a client partner ID. Customers, orders, commission data, and enabled articles are partner-scoped; test cross-partner denial.
- Block inactive partners server-side after authentication and before writes. Validate payloads with Jakarta Bean Validation; return German `ApiException` messages without internals. Use parameterized Panache/ORM queries; never log/expose tokens, credentials, secrets, or stack traces.
- Escape user/import HTML with `escapeHtml`. UI, validation, and errors are plain German with correct ä/ö/ü/ß.

## Verification

Use JUnit 5, `@QuarkusTest`, and REST Assured as appropriate; changed core behavior needs a matching capability test. Canonical backend checks:

```bash
cd backend
./mvnw test
./mvnw package
```

No frontend build/lint/npm/typecheck exists. For UI changes, use existing browser tooling at desktop and mobile; use `docs/SETUP.md` for runtime. If infrastructure blocks a check, report exact failure/current diagnosis, never a stale workaround.

Before handoff: focused tests; full backend suite and package for backend/implementation changes; `openspec validate <change>` for OpenSpec changes; exact commands and outcomes.
