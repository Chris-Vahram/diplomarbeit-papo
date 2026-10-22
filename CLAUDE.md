# CLAUDE.md – Partnerportal (Diplomarbeit)

### Vorrangiger Next-Session-Orchesterstand — 22.10.2026

- Einstieg: `Specification/CONTINUATION_PROMPT.md`, Abschnitt „Stand 22.10.2026 — hier einsteigen“. `refine-portal-frontend` umgesetzt auf Branch `feat/refine-portal-frontend` (lokal committet); offen nur Tobias' visuelle Abnahme (Task 5.2), dann Push/Merge/Deploy mit Chris' OK und Archiv.

### Vorheriger Orchesterstand — 16.10.2026

- Einstieg: `Specification/CONTINUATION_PROMPT.md`, Abschnitt „Stand 16.10.2026 — hier einsteigen“. Live: B-08, B-04 (Mail auf VM aus bis SMTP-Daten), OIDC-Login-Bindung A+B, Kategorie als Katalog-Referenz, Ponytail-Audit. Nächster Block: `refine-portal-frontend` (impeccable). Shop-Changes gehören Tobias.

### Stand 14.10.2026

- Neuestes Protokoll: `Besprechungsprotokoll_12.10.md`. Präsentationen abgenommen. UI-Feedback umgesetzt und gemergt (PR #37 Zeitraum-Presets/Partner-Dashboard/„Meine Endkunden“/„Meine Artikel“, PR #38 Endkunden bearbeiten); beide Changes archiviert.
- Shop = partnerseitiger Homepage-Baukasten „Mein Webshop“ (Pfad oder Subdomain offen); Tobias erstellt Empfehlungen/Spezifikation, Fragen gehen über Prof. Hammer an die Firma. Datenimport bleibt Dummy. Firmentermin offen.
- 15.10.: B-08, B-04 (Mailversand vorbereitet, auf der VM aus bis SMTP-Daten da sind) und OIDC-Login-Bindung (Release A+B, `partner-demo` auf der VM verknüpft) live. `decouple-partner-number-from-login` wartet nur noch auf BMD-Beispieldaten. Geplant: `refine-portal-frontend`, `derive-product-category-from-catalog`, Ponytail-Audit.

### Älterer Orchesterstand — 25.09.2026

- Branch: `main`, synchron mit `origin/main` nach PR #27 (`5fff3b0`) und dem Migrationsfix aus PR #28 (`b5ab7fb`). Die 122 Suffix-` 2`-Duplikate wurden mit Chris' Freigabe geprüft und entfernt; kanonische Dateien bleiben erhalten.
- Trade Proof/Registerkennung, Review-Fixes und Thesis-/Plan-Sync sind gepusht. Der erste `main`-Deploy zeigte trotz grünem Job einen Backend-Startfehler am alten VM-Demo-Namen; auf das vorige Image zurückgestellt, Fix per PR #28, danach live geprüft.
- Verifiziert: 130/130 Backend-Tests mit erfolgreichem Package nach dem Fix; `main`-Run `36115368910` grün. VM: Backend läuft ohne Restarts, Frontend 200, geschützte API ohne Login 401, OIDC 200. Der Deploy-Gateway braucht vor der nächsten größeren Änderung Health-Check und Rollback.
- Chris möchte nun Phase 2 plus Mandantenfähigkeit/persönliche Konten für eine sicher deploybare Staging-Version verfolgen. BMD/B-07-Firmen-/Schulentscheidungen und echte Staging-Umgebung sind offen. Die aktuelle VM enthält laut Chris nur Demo-Daten; bekannte Demo-Logins sind aktiv und vor echten Daten zu entfernen.

Maßgeblich für den nächsten Arbeitsblock ist `Specification/CONTINUATION_PROMPT.md`.
Die fachliche Ausbaureihenfolge steht in `Specification/PRODUCT_COMPLETION_PLAN.md`;
der Handoff trennt davon menschliche Thesis-/UI-Abnahme und die SSH-Negativmatrix.

## Aktueller Orchesterstatus — 25.09.2026

Aktive OpenSpec-Changes (Stand 17.10.2026): `refine-portal-frontend` 11/12 (nur Tobias-Abnahme offen), `send-order-emails` 5/6,
`complete-thesis-documentation` 4/6, `decouple-partner-number-from-login` 0/4, `add-unique-bmd-article-number` 0/6, dazu Tobias' 11 Shop-Changes.
`harden-tax-identifier-rollout` ist vollständig und archiviert. Die vollständige offene Liste, Protokollauswertung und der Startprompt stehen in
`Specification/CONTINUATION_PROMPT.md`. Chris hat Push auf `main` samt Deploy für
den geprüften Stand erlaubt. VM-Demo-Status ist geklärt; kein echter
Kundenbetrieb freigegeben. Secrets und Volumes bleiben geschützt.

## Project owner
This project belongs to **Chris**.

## Project
Web-based, extensible **B2B partner-ordering platform** for partner-driven sales models. The first operational stage is an internal portal that replaces orders received by e-mail and telephone. Chain: **Company -> Partner -> End Customer**. HTL Diplomarbeit (Informatik / IT Medientechnik), team: **Chris + Tobias** (Julian left the project). Split (2026-07-02): **Chris = implementation** (backend + functional frontend), **Tobias = frontend design** (visual design via Claude design, delivered async as CSS for `base.css` — implementation never waits for it). **PAPO = the project/product name itself**, not the client. The real reference client is unnamed and has roughly 100 partners; older article-count estimates must be verified against actual BMD/export data.

## Authoritative documents (read in this order for deep work)
1. `Specification/CONTINUATION_PROMPT.md` — **self-contained current operational handoff**; read it fully in every fresh session, then verify volatile Git/OpenSpec claims.
2. `Documentation/Besprechungsprotokolle/` — newer meeting notes override older documents when they conflict; newest is `Besprechungsprotokoll_12.10.md`.
3. `Specification/functional_specification_document_partnerportal_final.md` — authoritative baseline for the implemented internal ordering stage (FR-01..FR-25) until synchronized with newer decisions.
4. `Specification/phase2_backlog.md` — deferred features **B-01..B-08** + decisions log. Honor it; do not silently promote future directions.
5. `Specification/ROADMAP_2026-08-25.md` — persisted future plan, updated through 31.08.2026; planning context, not implemented behavior.
6. `Specification/IMPLEMENTATION_PLAN.md` — narrative plan (WP1–WP13, phases, risks, team split).
7. `openspec/changes/` and `openspec/specs/` — active task truth and merged capability behavior.
8. `Specification/functional_specification_document_full_vision.md` — older whole-product vision; reference only and defer to newer meetings/roadmap.

**Hard rule:** meetings override the FSD when they conflict (newer = authoritative). The FSD may lag behind the meetings of 25.08/31.08 until an explicit documentation sync; use the continuation prompt and roadmap for that delta. Do not reintroduce v1 assumptions (external shop as current stage, one-product orders, 3-person team).

## Historischer Stand (2026-09-16; durch den Block vom 25.09. überholt)
- Active OpenSpec: `harden-deployment-gateway` 3/4, `complete-thesis-documentation` 3/6, `decouple-partner-number-from-login` 0/6 and `add-unique-bmd-article-number` 0/6. `add-partner-trade-proof` and `harden-tax-identifier-rollout` are archived after implementation.
- Branch: `codex/archive-business-tax-identifiers`; verify `git status --short --branch` and `openspec list --json` at session start.
- Verified locally: 125/125 backend tests, package and clean-package, strict OpenSpec validation, JavaScript syntax, `git diff --check`, Playwright Desktop/Mobile and quality/security/database reviews.
- Gateway remote evidence remains open. “Tobias evidence” means sanitized VM/key/sudoers/host-key/negative-SSH/live/rollback proof; Chris can produce it himself if he has the same authorized VM and key access. GitHub commits alone do not prove VM installation or runtime state.
- Tax-identifier foundation and company profile are implemented and hardened. Trade-proof details are fixed in `openspec/changes/archive/2026-09-16-add-partner-trade-proof/`: current document only, no history/OCR, private access, versioned review and bounded uploads. Legal retention must not be invented; document only the specified current-document/backup boundary. Direct BMD integration is not approved.
- The public VM still renders old “Produkte” labels; local source corrections are pending deployment and are not yet claimed as live.
- Der vorrangige Block vom 25.09. und `Specification/CONTINUATION_PROMPT.md` ersetzen die volatilen Angaben dieses historischen Abschnitts.
- Meetings on 25.08 and 31.08 established PAPO as a company-independent, extensible product. Domain `paportal.at` is assigned. The current demo uses one company, but this is an implementation stage rather than the thesis product boundary.
- Am 31.08. war Multi-Company noch nicht der nächste Change; Chris hat am 25.09. zwei Testunternehmen als neue Zielrichtung bestätigt. Subdomains bleiben unentschieden.
- Der implementierte Stand nutzt weiterhin einen gemeinsamen Partner-Login; Chris hat persönliche Firmen- und Partnerkonten als Ausbauscope bestätigt.
- The future plan and ordered change candidates are persisted in `Specification/ROADMAP_2026-08-25.md`.
- Full current implementation state, exact open work and next-step order: `Specification/CONTINUATION_PROMPT.md`.

## Locked current-stage scope
Core process: one organizational partner-company login (Keycloak) -> select or create an end customer (Kunde) -> place an order (multiple articles + quantities) on the customer's behalf -> order auto-assigned to the partner -> on-screen confirmation + **simulated** e-mail (no real send) -> company advances status **submitted -> reviewed -> fulfilled** and handles fulfillment. This replaces manual e-mail/telephone ordering and is the first working stage of an extensible product.

Also in the current stage: central partner + article management by the company, Excel/CSV import of partners and articles (column mapping + duplicate detection + validation report — **mapping is essential, not optional**), per-partner article enablement (partner sees/orders only enabled articles), partner dashboard (landing screen), per-order commission.

### Key decisions (do not re-litigate; details in FSD §13 + backlog log)
- Commission = `Partner.commissionPercent` field; company sets via dropdown **5/10/15 + optional custom value**; % of total order value; stored per order as own `CommissionRecord`. **No CommissionLevel entity.** Auto level-up deferred (B-03).
- Active status = DB-only flag `Partner.active`, enforced server-side after authentication. Keycloak stays simple (no enabled-sync).
- Kunde = reusable entity (select existing or create new); mandatory **Nachname + address**; optional first name, e-mail, phone.
- Multiple articles per order; `OrderItem` stores a **unit-price snapshot**.
- Keycloak users are provisioned **manually** (realm export); import creates Partner records only. The login username claim is matched to canonical uppercase `Partner.partnerNumber`; there is no separate `keycloakUsername` field.
- Schema: Hibernate auto-generation, no Flyway. dev/test = drop-and-create + programmatic seed (`DemoDataSeeder`, dev/test only); demo/prod = update.
- Affiliate identifier: deferred, not in MVP.
- Kunde records are scoped to the creating partner — a partner never sees another partner's end customers.
- Partner dashboard totals (commission/revenue/order count) count only `fulfilled` own orders; latest-orders list shows any status.
- The current research question was confirmed by the supervisor. It frames the internal ordering MVP as the first step of an extensible portal; the external partner shop remains Phase 2.
- In user-facing UI and thesis language use **Artikel**, not alternating Product/Artikel terminology. Internal code/entity/API names such as `Product` may remain until a real technical reason requires renaming.
- BMD is the leading article-master system. PAPO currently uses import data and does not own a competing master record. Chris reopened a direct BMD interface for decision; company/school approval and an integration contract are still absent.
- Target decision: the unique visible business identifier should be the BMD article number; the existing database id remains technical and no second visible ID is required. In the current code snapshot, `Product` has no BMD-number field yet and import dedupe still uses the article name, so implementation waits for the simplified company mail/sample export. `Preislistenbereich` remains omitted.
- Category remains relevant. Fachbereich, product group A/B/C/D, bundles/BOMs, training permissions, and PP/margin-based commission rules are future directions, not current requirements; wait for the simplified company follow-up.

The future target in `Specification/ROADMAP_2026-08-25.md` changes partner identifiers only through separate approved changes: generated partner numbers become company-local business identifiers and imported existing numbers become external references. One shared partner-company access is the implemented stage. Chris now requests two manually onboarded test companies and personal accounts on both sides; specify tenant isolation and identity migration in separate OpenSpec changes before implementation. Subdomains remain undecided.

## Fixed tech stack (no alternatives — do not suggest others)
- **Frontend:** HTML, CSS, native Web Components / Custom Elements (no framework).
- **Backend:** Quarkus (Java) — Quarkus REST (formerly RESTEasy Reactive), Hibernate ORM Panache, OIDC.
- **Database:** PostgreSQL.
- **Auth:** Keycloak (realm `partnerportal`, roles `company` / `partner`).
- **Import libs:** Apache POI (xlsx) + opencsv (csv).

## Entities
Partner (commissionPercent, active), Product (technical name; fachlich „Artikel“), ProductEnablement (partner × article), Kunde, Order (table `orders` — `Order` is a reserved SQL word), OrderItem (unit-price snapshot), CommissionRecord. Money = `BigDecimal`.

## Roles
- **Company:** manages partners (incl. commission %) and articles, imports data, enables articles per partner, sees all orders, changes order status, sees all commission records.
- **Partner:** one shared organizational access per partner company in the current stage; limited self-edit of own contact data; sees only enabled articles; creates orders for Kunden; sees only own orders/commission; dashboard is landing screen.
- **End customer:** does **not** use the portal in the MVP; receives goods and the (simulated) confirmations.

## Out of scope / deferred (`phase2_backlog.md` — say "implement B-XX" to build)
B-01 external end-customer shop (CUT in MVP) · B-02 shop design customization (CUT in MVP) · B-03 auto commission level-up (STUB, manual in current stage) · B-04 real e-mail sending (STUB, simulated currently) · B-05 shop inquiry auto-forward (CUT in MVP) · B-06 direct BMD interface (reopened for decision) · B-07 commission settlement/payout (reopened for decision) · B-08 full/cross-dashboard previous-period analytics (STUB; partner-dashboard client-side comparison exists). Chris requests Phase 2 plus tenant isolation and personal accounts for a staged product; see `Specification/PRODUCT_COMPLETION_PLAN.md`. Fachbereich-based training authorization, product groups/PP-margin rules and bundles/BOMs still lack company rules. Actual bank transfers are not yet approved.

## Repo layout & conventions
- `backend/` Quarkus; `frontend/` static Web Components app; `docs/` CONVENTIONS.md + SETUP.md; `Specification/`; `Documentation/`; `openspec/`.
- Coding/naming/REST conventions: `docs/CONVENTIONS.md`. How to run dev/demo environment: `docs/SETUP.md` (docker-compose Keycloak+Postgres for dev/demo, Dev Services for tests only; demo users `company-demo` / `partner-demo`).

## Still open (ask, don't assume)
- Simplified company follow-up defining the minimal article fields; do not implement the over-complete earlier mail as one change.
- Import file column sets for partner/article files and BMD export rules — waiting on sample data (blocks final import finalization only).
- Partner-number/BMD-number rules still require anonymized company/BMD data. Tax-identifier and trade-proof rules are specified in their completed planning artifacts; do not invent legal retention periods or expand the current-document-only scope.
- Dormancy threshold is currently 60 days; confirm it with the client at the next meeting.
- Production domain is `paportal.at`; the demo deployment is live. A separate Staging environment and any wildcard-subdomain decision remain open.
- Chris requests two isolated test companies and personal accounts for company and partner staff, with admin/employee roles and actor/time audit. Provisioning/recovery details and the binding Herbstferien acceptance list remain open.
- MVP documentation tasks 8.3/8.4 and visual task FD.4 remain open; see the continuation prompt for ownership and scope.

## KI evidence
- Rules and register: `Documentation/KI-Nachweise/`.
- Automatic local capture: `tools/ai_evidence_log.py`; its LaunchAgent periodically secures Codex and Claude Code sessions in the private archive.
- After relevant PAPO work, run `python3 tools/ai_evidence_log.py sync`, then `python3 tools/ai_evidence_log.py enrich --purpose "<Zweck>" --result "<Ergebnis>" --check "<wirklich ausgeführte Prüfung>"`. Omit `--check` if none ran.
- Preserve exact human prompts and visible answers privately, keep secrets/personal data out of Git, and document only real verification/eigenleistung. Never fabricate missing evidence.

## Writing style
Clear German for thesis documents, suitable for an HTL Diplomarbeit; precise, structured, no exaggerated corporate language. UI texts and validation messages in German, plain language.

## Git workflow – commit often
- Commit after every completed, self-contained step: a small feature, a bug fix,
  a refactoring, added tests, a spec/doc update, or a finished OpenSpec task.
  Do not batch several unrelated changes into one commit.
- Rule of thumb: if the change can be described in one short commit message,
  commit it now. Never end a work block with substantial uncommitted changes.
- Before each commit, run the relevant checks (backend tests for backend changes,
  `git diff --check`). Commit only a working state; if checks fail, fix first or ask.
- Stage only the files belonging to that step (`git add <paths>`, no blind
  `git add -A`). Never commit secrets, `.env` files, volumes or private KI evidence.
- When an OpenSpec task is done, commit the code together with the updated `tasks.md`.
- Local commits on the current working branch need no confirmation.
  Pushing, merging into `main` and deploying still require Chris's explicit approval.
- Work on a feature branch, not directly on `main`.

## Git commit attribution
Do **not** add any Claude or Claude Code attribution to commits. Specifically:
- No `Co-Authored-By: Claude ...` trailer in commit messages.
- No "Generated with Claude Code" (or similar) lines in commit messages or PR descriptions.
- Commit under the project's own git identity (Tobias), never a bot/Claude identity.
Commit messages should contain only the actual change description, nothing about the tooling used to produce it.
- Keep your commit-messages short (max 10 words), if not really needed
