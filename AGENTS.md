# AGENTS.md — PAPO Partnerportal

This file is the Codex companion to `CLAUDE.md`. Keep all Claude files and `.claude/` configuration intact for Tobias. Codex-specific additions belong in `AGENTS.md`, `.agents/`, or `.codex/`.

## Project and source of truth

PAPO is the product/project name for an extensible B2B partner-ordering platform. Its first operational stage replaces partner orders received by e-mail and telephone with a structured internal portal. The real reference client is unnamed. Core chain: Company → Partner → End Customer.

Start every fresh deep-work session with `Specification/CONTINUATION_PROMPT.md`. If the handoff points to an active work package, also read `Specification/NEXT_SESSION_PLAN.md`. Do not preload every project document. Route additional reading by task:

- product/scope decision: `CLAUDE.md`, the newest relevant meeting note, and the affected FSD section;
- active feature/change: only that change's proposal/design/spec/tasks plus affected canonical specs;
- deployment/runtime: `docs/DEPLOYMENT.md`, `docs/SETUP.md`, and `openspec/changes/ci-cd-deployment-with-gh-actions/`;
- implementation convention/data model: only the relevant parts of `docs/CONVENTIONS.md` and `docs/DATA_MODEL.md`;
- future scope: `Specification/phase2_backlog.md` and `Specification/ROADMAP_2026-08-25.md`.

Newer meeting notes override older documents and the FSD when they conflict; the newest is `Besprechungsprotokoll31_08_2026.md`. Verify volatile Git, test, infrastructure, and OpenSpec status with commands instead of trusting a handoff snapshot.

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
- Fully specified planned GitHub/VM apply actions are standing-authorized: execute them autonomously until a task's explicit stop condition, a new decision, or a separate commit/push/merge/deploy-activation/archive authorization is reached.
- Use one branch per feature/change and preserve unrelated working-tree edits.
- End implementation handoffs with the active branch and a ready commit message.
- When a commit message contains a literal `"`, write it to a file and hand Chris `git commit -F <file>`; do not use `-m` or a pasted heredoc.

## Required workflow

- Use OpenSpec for non-trivial features, behavior changes, and user-visible refactors: propose → apply tasks → validate → archive.
- Keep one OpenSpec change per feature or concern; do not create mega-changes.
- `tasks.md` is the progress source of truth for an active change.
- For complex/high-stakes changes, run one independent combined plan/readiness review and one final diff review. Do not add a separate self-sufficiency session after the plan review. Append findings or intermediate phases; do not silently rewrite the original plan history.
- After ordinary implementation, use the project `partnerportal-quality` skill. Add a separate generic correctness/security review only for high-risk changes or unresolved findings.
- KI usage is documented through `Documentation/KI-Nachweise/`. `prompt-register.md` is the documentation prompt backlog; the local LaunchAgent secures sessions automatically. Never invent missing prompt or verification evidence.
- Once per substantive PAPO work block, run `python3 tools/ai_evidence_log.py sync`, inspect the `AUSSTEHEND-*` backlog, then enrich only the verified current-session ID with `--session-id <ID>`. If no current-session record exists, leave it in the backlog; never let default `enrich` overwrite a historical entry. Curate only entries whose evidence is available in the current task; do not mass-process old sessions. Batch related tiny edits into one entry and omit `--check` when no technical verification ran.
- The detailed planning, FSD/OpenSpec audit, scratch, model-handoff, and cold-restart protocol lives in `docs/AI_WORKFLOW_EFFICIENCY.md`; do not duplicate it in session prompts.

## Context and model efficiency

- Keep one session to one bounded outcome, but continue from planning into implementation when the plan is approved and enough context remains. Start a fresh session only for a genuinely new outcome, a deliberate model escalation, or context pressure.
- State each instruction once. Prefer links to source-of-truth files over copied summaries.
- Read narrow sections and request bounded command output; never dump whole tool catalogs or unrelated directories.
- Use only the skills/tools required by the task. For routine implementation from approved OpenSpec tasks, prefer the cost-efficient model; escalate for architecture, security, unclear requirements, stubborn failures, and final high-risk review.
- Do not create subagents for serial work. Use them only for genuinely independent parallel scopes or an explicitly requested independent review.
- In clients with `/context`, prepare a cold-restart handoff around 75% and restart by 85%; do not create a handoff merely because a workflow phase ended. `openspec context` is unrelated to token usage.

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
