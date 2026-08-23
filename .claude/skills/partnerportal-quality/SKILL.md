---
name: partnerportal-quality
description: Project-specific quality review for the Partnerportal (Diplomarbeit). Use after implementing a change, before commit/merge, to check the diff against project rules the generic reviews don't know - role scoping, German texts, money handling, MVP scope discipline.
metadata:
  author: chris
  version: "1.0"
---

Review a diff against the Partnerportal's project-specific rules. This skill covers what the built-in `code-review` / `security-review` / `simplify` skills cannot know: this project's role/scoping model, German-text requirements, money conventions, and MVP scope discipline. It does NOT replace generic bug-hunting — for that, run the built-in `code-review` separately.

**Input**: Optionally files, a branch, or a commit range. If omitted, use the default scope below.

## Steps

### 1. Select the review scope

- If the user named files, a branch, or a range: review exactly that.
- Else if there are uncommitted changes: review `git diff`, `git diff --cached`, and untracked files under `backend/` or `frontend/`.
- Else: review the current branch against `main` (`git diff main...HEAD`).

Review only the changed lines and their immediate context. Do not report findings in untouched code (surgical-changes rule) — if you notice a serious pre-existing problem, mention it in one line at the end as out-of-scope, without a severity tag.

### 2. Read the authoritative rules

Always read fresh (do not rely on memory of them):

- `docs/CONVENTIONS.md` — package layout, REST paths, data conventions, frontend structure
- `CLAUDE.md` — locked scope, key decisions, entities, roles

Read only when the diff touches the relevant area:

- `docs/DATA_MODEL.md` — when entities or schema change
- `openspec/specs/<capability>/spec.md` — when a capability's behavior changes
- `Specification/phase2_backlog.md` — when the diff might add deferred functionality

Where these documents and the checklist below disagree, the documents win — flag the divergence so the skill can be updated.

### 3. Check the hard rules

**Access control & scoping**
- Every REST resource method has `@RolesAllowed("company")` or `@RolesAllowed("partner")` — no unannotated protected endpoints.
- Partner-facing endpoints resolve the caller via `security/CurrentPartner.resolve()` (Keycloak username claim). Never trust a client-supplied partner id.
- Kunde records are scoped to the creating partner — no query may return another partner's Kunden or orders.
- Inactive partners (`Partner.active == false`) are blocked server-side, after authentication, before any write.

**Data**
- Money (prices, order values, commission, percent) = `java.math.BigDecimal` mapped to `numeric`. Never `double`/`float`.
- Frontend money aggregation sums integer cents (see `sumCents` in `frontend/src/lib/dashboard-data.js`), never a naive float `reduce`.
- `Order` entity maps to table `orders` (`ORDER` is reserved in PostgreSQL).
- Timestamps: `java.time` types, never `java.util.Date`.
- No Flyway, no manual DDL — schema via Hibernate generation; seed data only via `DemoDataSeeder` (dev/test gated).

**Texts**
- All UI, validation, and error messages in German, plain language, with correct umlauts ä/ö/ü/ß (never ae/oe/ue/ss substitutions).
- No raw stack traces, exception messages, or HTTP status codes shown to users.
- Backend errors via `ApiException(status, "German message.")`; Bean Validation annotations carry German `message = "..."`.

**Frontend**
- User- or import-supplied text interpolated into HTML must pass through `escapeHtml` (`frontend/src/lib/html.js`).
- Native Web Components only, light DOM, no framework, no CDN/external dependencies.
- State local to the component; data fetched via `apiFetch` in `connectedCallback`. No global store.
- Naming: pages `<area>-<thing>-page.js`, kebab-case custom-element tags.
- Forms that POST have a double-submit guard (disable the submit button during the request).

**REST**
- Paths: `/api/company/<resource>` or `/api/partner/<resource>` — explicit role prefixes.
- Standard verbs; no `DELETE` unless a capability spec calls for it.

**Scope discipline**
- Nothing from `Specification/phase2_backlog.md` (B-01…B-07) built into the MVP: no external shop, no auto commission level-up, no real e-mail sending, no BMD, no settlement.
- No re-litigating FSD §13 decisions (e.g. no `CommissionLevel` entity, simulated e-mail only, manual Keycloak provisioning).
- Meeting notes override the FSD when they conflict (newer = authoritative).

**Tests**
- Backend tests per capability, class `<Capability>ResourceTest`, method names as behavior sentences (e.g. `partnerCannotOrderNonEnabledProduct`).
- New or changed behavior in the diff has a test — or the report states why not.

**Package layout**
- One capability package under `at.papo.partnerportal`, layered `Resource` → `Service` → entity. New code goes into the matching capability package, not a grab-bag.

### 4. Report

Findings ranked by severity, most severe first:

```
[blocker]    file:line — rule violated — concrete fix
[should-fix] file:line — rule violated — concrete fix
[nit]        file:line — rule violated — concrete fix
```

- `blocker` = security/scoping hole, wrong money type, scope violation. Must be fixed before merge.
- `should-fix` = convention break that will cost later (naming, missing test, non-German text).
- `nit` = minor style divergence.

If the diff is clean: say so in one line. Do not invent findings to have something to report.

Close with a one-line reminder to run the built-in `code-review` for generic correctness if it has not run on this diff yet.

## Non-goals

- No auto-fixing — report only, unless the user explicitly asks for fixes afterwards.
- No reformatting or style suggestions beyond the rules above.
- No proposals to expand scope or "improve" adjacent code.
