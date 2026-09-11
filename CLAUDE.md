# CLAUDE.md – Partnerportal (Diplomarbeit)

## Project owner
This project belongs to **Chris**.

## Project
Web-based, extensible **B2B partner-ordering platform** for partner-driven sales models. The first operational stage is an internal portal that replaces orders received by e-mail and telephone. Chain: **Company -> Partner -> End Customer**. HTL Diplomarbeit (Informatik / IT Medientechnik), team: **Chris + Tobias** (Julian left the project). Split (2026-07-02): **Chris = implementation** (backend + functional frontend), **Tobias = frontend design** (visual design via Claude design, delivered async as CSS for `base.css` — implementation never waits for it). **PAPO = the project/product name itself**, not the client. The real reference client is unnamed and has roughly 100 partners; older article-count estimates must be verified against actual BMD/export data.

## Authoritative documents (read in this order for deep work)
1. `Specification/CONTINUATION_PROMPT.md` — **self-contained current operational handoff**; read it fully in every fresh session, then verify volatile Git/OpenSpec claims.
2. `Documentation/Besprechungsprotokolle/` — newer meeting notes override older documents when they conflict; newest is `Besprechungsprotokoll_10.9.md`.
3. `Specification/functional_specification_document_partnerportal_final.md` — authoritative baseline for the implemented internal ordering stage (FR-01..FR-25) until synchronized with newer decisions.
4. `Specification/phase2_backlog.md` — deferred features **B-01..B-08** + decisions log. Honor it; do not silently promote future directions.
5. `Specification/ROADMAP_2026-08-25.md` — persisted future plan, updated through 31.08.2026; planning context, not implemented behavior.
6. `Specification/IMPLEMENTATION_PLAN.md` — narrative plan (WP1–WP13, phases, risks, team split).
7. `openspec/changes/` and `openspec/specs/` — active task truth and merged capability behavior.
8. `Specification/functional_specification_document_full_vision.md` — older whole-product vision; reference only and defer to newer meetings/roadmap.

**Hard rule:** meetings override the FSD when they conflict (newer = authoritative). The FSD may lag behind the meetings of 25.08/31.08 until an explicit documentation sync; use the continuation prompt and roadmap for that delta. Do not reintroduce v1 assumptions (external shop as current stage, one-product orders, 3-person team).

## Current status (2026-09-11; verify volatile claims)
- The 10.09 meeting decisions and the reviewed implementation-ready OpenSpec changes are summarized in `openspec/PLANNING_REVIEW_2026-09-11.md`. This report supersedes older candidate/READY tables where they conflict.
- MVP implementation and the meeting-driven July feature wave are merged to `main`. Audit Fix Batches A and B are implemented, regression-reviewed and `Verified` (UX-001..004, TECH-001).
- Batch B is archived and synced to the main specs in the current local-history commit `94050cb`. The verified backend suite contains 77 tests (0 failures/errors/skips).
- `build-partnerportal-mvp` is at 50/53 tasks. Only UI/UX documentation (8.3), thesis traceability (8.4) and final visual polish (FD.4) remain.
- Meetings on 25.08 and 31.08 established PAPO as a company-independent, extensible product. Domain `paportal.at` is assigned. The current demo uses one company, but this is an implementation stage rather than the thesis product boundary.
- Multi-company tenancy and `firma-x.paportal.at` are the correct architecture once a second company is onboarded. They are explicitly not the next change; their thesis delivery date/scope remains open.
- The current stage uses one shared organizational login per partner company. Personal partner-employee accounts are not required.
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
- BMD is the leading article-master system. PAPO uses import data and does not own a competing master record. A direct BMD interface remains unapproved/out of scope.
- The unique visible business identifier is the BMD article number. The existing database id remains technical; no extra visible article id is required. `Preislistenbereich` is explicitly omitted.
- Category remains relevant. Fachbereich, product group A/B/C/D, bundles/BOMs, training permissions, and PP/margin-based commission rules are future directions, not current requirements; wait for the simplified company follow-up.

The future target in `Specification/ROADMAP_2026-08-25.md` changes partner identifiers only through separate approved changes: generated partner numbers become company-local business identifiers and imported existing numbers become external references. One shared partner-company access is confirmed for the current stage. Do not implement tenant/auth restructuring now; revisit it when onboarding a second company or when its thesis deadline is explicitly set.

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
B-01 external end-customer shop (CUT) · B-02 shop design customization (CUT) · B-03 auto commission level-up (STUB, manual in current stage) · B-04 real e-mail sending (STUB, simulated currently) · B-05 shop inquiry auto-forward (CUT) · B-06 direct BMD interface (CUT; BMD remains leading source via import) · B-07 commission settlement/payout (CUT) · B-08 full/cross-dashboard previous-period analytics (STUB; partner-dashboard client-side comparison exists). Also later/not current: personal partner-employee accounts, public company self-registration, Fachbereich-based training authorization, product groups/PP-margin rules, bundles/BOMs. Permanently out unless explicitly re-scoped: ERP/CRM completeness, real payments, inventory/shipping detail, accounting.

## Repo layout & conventions
- `backend/` Quarkus; `frontend/` static Web Components app; `docs/` CONVENTIONS.md + SETUP.md; `Specification/`; `Documentation/`; `openspec/`.
- Coding/naming/REST conventions: `docs/CONVENTIONS.md`. How to run dev/demo environment: `docs/SETUP.md` (docker-compose Keycloak+Postgres for dev/demo, Dev Services for tests only; demo users `company-demo` / `partner-demo`).

## Still open (ask, don't assume)
- Simplified company follow-up defining the minimal article fields; do not implement the over-complete earlier mail as one change.
- Import file column sets for partner/article files and BMD export rules — waiting on sample data (blocks final import finalization only).
- Dormancy threshold is currently 60 days; confirm it with the client at the next meeting.
- Production domain is `paportal.at`; final Hetzner DNS, certificate and deployment details remain open. Wildcard subdomains are future tenant work, not an immediate deployment blocker.
- Exact thesis deadline/scope for multi-company tenancy; do not start it merely because it is the target architecture.
- Company-side employee/account model is not resolved; partner-company access is resolved as one shared access for the current stage.
- MVP documentation tasks 8.3/8.4 and visual task FD.4 remain open; see the continuation prompt for ownership and scope.

## KI evidence
- Rules and register: `Documentation/KI-Nachweise/`.
- Automatic local capture: `tools/ai_evidence_log.py`; its LaunchAgent periodically secures Codex and Claude Code sessions in the private archive.
- After relevant PAPO work, run `python3 tools/ai_evidence_log.py sync`, then `python3 tools/ai_evidence_log.py enrich --purpose "<Zweck>" --result "<Ergebnis>" --check "<wirklich ausgeführte Prüfung>"`. Omit `--check` if none ran.
- Preserve exact human prompts and visible answers privately, keep secrets/personal data out of Git, and document only real verification/eigenleistung. Never fabricate missing evidence.

## Writing style
Clear German for thesis documents, suitable for an HTL Diplomarbeit; precise, structured, no exaggerated corporate language. UI texts and validation messages in German, plain language.

## Git commit attribution
Do **not** add any Claude or Claude Code attribution to commits. Specifically:
- No `Co-Authored-By: Claude ...` trailer in commit messages.
- No "Generated with Claude Code" (or similar) lines in commit messages or PR descriptions.
- Commit under the project's own git identity (Tobias), never a bot/Claude identity.
Commit messages should contain only the actual change description, nothing about the tooling used to produce it.
- Keep your commit-messages short (max 10 words), if not really needed
