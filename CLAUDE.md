# CLAUDE.md – Partnerportal (Diplomarbeit)

## Project owner
This project belongs to **Chris**.

## Project
Web-based **internal B2B partner-ordering portal** (Partnerportal) for partner-driven sales models. Chain: **Company -> Partner -> End Customer**. HTL Diplomarbeit (Informatik / IT Medientechnik), team: **Chris + Tobias** (Julian left the project). Split (2026-07-02): **Chris = implementation** (backend + functional frontend), **Tobias = frontend design** (visual design via Claude design, delivered async as CSS for `base.css` — implementation never waits for it). **PAPO = the project/product name itself** (PartnerPortal — this thesis's own name), not a client. The real reference client is unnamed (~200 partners, ~800 articles; orders today arrive manually by e-mail/phone).

## Authoritative documents (read in this order for deep work)
1. `Specification/CONTINUATION_PROMPT.md` — **current operational handoff**; read this first in every fresh session, then verify its volatile Git/OpenSpec claims.
2. `Specification/functional_specification_document_partnerportal_final.md` — **FSD v2, authoritative** for the MVP build (FR-01..FR-25, NFRs, entities, business rules, acceptance criteria, resolved decisions in §13).
3. `Specification/phase2_backlog.md` — deferred features **B-01..B-08** + append-only decisions log. Honor it; do not rebuild deferred features into the MVP.
4. `Specification/IMPLEMENTATION_PLAN.md` — narrative plan (WP1–WP13, phases, risks, team split).
5. `openspec/changes/build-partnerportal-mvp/` — proposal, design, capability specs, **tasks.md = task/progress source of truth**.
6. `Specification/functional_specification_document_full_vision.md` — whole-project vision incl. deferred features; reference only, not the build target.
7. `Documentation/Besprechungsprotokolle/` — client meeting notes; newest is `Besprechungsprotokoll10_07_2026.txt`.

**Hard rule:** meetings override the FSD when they conflict (newer = authoritative). FSD v2 + backlog already encode the meeting decisions. Do not reintroduce v1 assumptions (external shop as MVP, one-product orders, 3-person team).

## Current status (2026-08-24)
- MVP implementation and the meeting-driven July feature wave are merged to `main`. Audit Fix Batches A and B are implemented, regression-reviewed and `Verified` (UX-001..004, TECH-001).
- Batch B is archived and synced to the main specs in commit `b38dfe5`. The verified backend suite contains 77 tests (0 failures/errors/skips).
- `build-partnerportal-mvp` is at 50/53 tasks. Only UI/UX documentation (8.3), thesis traceability (8.4) and final visual polish (FD.4) remain.
- Full current state, completed work since the 10.07 meeting, exact open work and next-step order: `Specification/CONTINUATION_PROMPT.md`.

## Locked MVP scope
Core process: partner logs in (Keycloak) -> selects or creates an end customer (Kunde) -> places an order (multiple articles + quantities) on the customer's behalf -> order auto-assigned to the partner -> on-screen confirmation + **simulated** e-mail (no real send) -> company advances status **submitted -> reviewed -> fulfilled** and handles fulfillment.

Also in MVP: central partner + product management by the company, Excel/CSV import of partners and articles (column mapping + duplicate detection + validation report — **mapping is essential, not optional**), per-partner product enablement (partner sees/orders only enabled products), partner dashboard (landing screen), per-order commission.

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

## Fixed tech stack (no alternatives — do not suggest others)
- **Frontend:** HTML, CSS, native Web Components / Custom Elements (no framework).
- **Backend:** Quarkus (Java) — Quarkus REST (formerly RESTEasy Reactive), Hibernate ORM Panache, OIDC.
- **Database:** PostgreSQL.
- **Auth:** Keycloak (realm `partnerportal`, roles `company` / `partner`).
- **Import libs:** Apache POI (xlsx) + opencsv (csv).

## Entities
Partner (commissionPercent, active), Product, ProductEnablement (partner × product), Kunde, Order (table `orders` — `Order` is a reserved SQL word), OrderItem (unit-price snapshot), CommissionRecord. Money = `BigDecimal`.

## Roles
- **Company:** manages partners (incl. commission %) and products, imports data, enables products per partner, sees all orders, changes order status, sees all commission records.
- **Partner:** limited self-edit of own contact data; sees only enabled products; creates orders for Kunden; sees only own orders/commission; dashboard is landing screen.
- **End customer:** does **not** use the portal in the MVP; receives goods and the (simulated) confirmations.

## Out of scope / deferred (`phase2_backlog.md` — say "implement B-XX" to build)
B-01 external end-customer shop (CUT) · B-02 shop design customization (CUT) · B-03 auto commission level-up (STUB, manual in MVP) · B-04 real e-mail sending (STUB, simulated in MVP) · B-05 shop inquiry auto-forward (CUT) · B-06 BMD interface (CUT, permanent) · B-07 commission settlement/payout (CUT) · B-08 full/cross-dashboard previous-period analytics (STUB; partner-dashboard client-side comparison exists). Also permanently out: ERP/CRM completeness, real payments, inventory/shipping detail, accounting.

## Repo layout & conventions
- `backend/` Quarkus; `frontend/` static Web Components app; `docs/` CONVENTIONS.md + SETUP.md; `Specification/`; `Documentation/`; `openspec/`.
- Coding/naming/REST conventions: `docs/CONVENTIONS.md`. How to run dev/demo environment: `docs/SETUP.md` (docker-compose Keycloak+Postgres for dev/demo, Dev Services for tests only; demo users `company-demo` / `partner-demo`).

## Still open (ask, don't assume)
- Import file column sets for partner/article files — waiting on sample data from the client (blocks final import finalization only).
- Dormancy threshold is currently 60 days; confirm it with the client at the next meeting.
- Production domain and final Hetzner deployment details are coordinated by Tobias/company; do not take over Tobias's CI/CD change without an explicit request.
- MVP documentation tasks 8.3/8.4 and visual task FD.4 remain open; see the continuation prompt for ownership and scope.

## Writing style
Clear German for thesis documents, suitable for an HTL Diplomarbeit; precise, structured, no exaggerated corporate language. UI texts and validation messages in German, plain language.

## Git commit attribution
Do **not** add any Claude or Claude Code attribution to commits. Specifically:
- No `Co-Authored-By: Claude ...` trailer in commit messages.
- No "Generated with Claude Code" (or similar) lines in commit messages or PR descriptions.
- Commit under the project's own git identity (Tobias), never a bot/Claude identity.
Commit messages should contain only the actual change description, nothing about the tooling used to produce it.
- Keep your commit-messages short (max 10 words), if not really needed