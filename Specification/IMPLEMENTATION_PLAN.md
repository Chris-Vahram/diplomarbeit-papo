# Implementation Plan

> Derived strictly from `Specification/functional_specification_document_partnerportal_final.md` (FSD v2) and honoring `Specification/phase2_backlog.md` (deferred features B-01…B-07 are **out of scope**). Team: Chris + Tobias only. Fixed tech stack per FSD §7.

## 1. Project Summary

A web-based **internal B2B partner-ordering portal** following the chain **Company -> Partner -> End Customer**. The company centrally imports and manages partners and articles, enables specific products per partner, and controls order status and per-partner commission levels. The core process is **partner-driven ordering**: a partner logs in, selects or creates an end customer (Kunde), and places an order (one or more articles + quantities) on that customer's behalf. Each order is auto-assigned to the partner, produces a commission record, and moves through submitted -> reviewed -> fulfilled while the company handles fulfillment. Confirmations are shown on-screen and as a simulated e-mail (no real send).

The external end-customer shop is **not** part of this MVP (deferred, B-01).

**Fixed tech stack (no alternatives):** Frontend HTML/CSS + native Web Components/Custom Elements; Backend Quarkus (Java); Database PostgreSQL; Auth Keycloak (roles: company, partner).

## 2. Implementation Strategy

### Overall approach
- Build a small, layered Quarkus backend (REST -> service -> entity/repository) with one package per functional area (partner, product, enablement, customer, order, commission, import) so areas stay separable (maintainability NFR).
- Normalized PostgreSQL schema; order value derived from order items, with a **unit-price snapshot on each order item** so historical orders/commissions are not changed when central prices change later.
- Enforce role authorization on every protected endpoint via Keycloak; check business-level partner `active` state server-side.
- Frontend = a small library of reusable Web Components consumed by company screens and partner screens; each page fetches from REST and renders.
- Vertical slices after the platform is up: get one full stack path working (Product CRUD) before broadening.

### Suggested order of development
1. Platform/foundation (repo, Quarkus, Postgres, Keycloak, shared UI components).
2. Data model + core CRUD (Product, Partner).
3. Access control + product enablement.
4. Ordering core (Kunde, order creation, assignment, confirmation, status) — the demo centerpiece.
5. Commission + partner dashboard.
6. Excel/CSV import.
7. Testing + documentation throughout, hardened at the end.

**Priority rule:** the ordering core (WP5) is the single most important demonstrable process; it must work end-to-end before optional enhancements. Highest technical risk (Keycloak, import) is front-loaded.

## 3. Work Packages

### WP1 — Project Foundation & Environment
- **Goal:** A runnable skeleton on the fixed stack.
- **Tasks:** create repo layout (backend/frontend/docs); generate Quarkus project (Quarkus REST — formerly RESTEasy Reactive, Hibernate ORM Panache, JDBC-Postgres, OIDC); run PostgreSQL (container) + datasource config; run Keycloak (container), create realm + roles `company`/`partner`, commit realm export; verify a role-protected sample endpoint; scaffold frontend shell + build shared Web Components (data-table, form-field, modal, toast, status-badge, metric-card); write conventions doc.
- **Dependencies:** none.
- **Deliverables:** `quarkus:dev` runs; Postgres connected; Keycloak login works with per-role access; reusable component set; conventions doc.

### WP2 — Data Model & Persistence
- **Goal:** Complete schema for all entities.
- **Tasks:** define entities Partner (incl. `commissionPercent` + `active` flag), Product, ProductEnablement, Kunde, Order, OrderItem, CommissionRecord (with unit-price snapshot on OrderItem); relationships per FSD §8; generate/migrate schema; seed demo data.
- **Dependencies:** WP1.
- **Deliverables:** working schema in PostgreSQL; ER diagram; seed dataset. (FSD §8)

### WP3 — Product Management (FR-02, FR-03, FR-18, FR-24)
- **Goal:** Company CRUD of articles with central price/description control.
- **Tasks:** REST + service + UI for create/view/edit/list; availability status; central-content rule (partners can't edit); unavailable-not-orderable rule; validation.
- **Dependencies:** WP1, WP2.
- **Deliverables:** product management screens; validated CRUD; availability handling.

### WP4 — Partner Management (FR-01, FR-08)
- **Goal:** Company CRUD of partners incl. commission level + status; partner limited self-edit.
- **Tasks:** REST + service + UI for partner CRUD; commission percentage via dropdown 5/10/15 + optional custom value (`Partner.commissionPercent`); active/inactive status (`Partner.active`); partner self-edit of contact data only; validation + duplicate-identifier prevention.
- **Dependencies:** WP1, WP2.
- **Deliverables:** partner management screens; commission percentage field; status toggle; partner self-edit view.

### WP5 — Access Control (FR-20, FR-23)
- **Goal:** Enforced role separation and inactive-partner blocking.
- **Tasks:** role authorization on all endpoints; link Keycloak user to Partner record; server-side inactive-partner block on login + order creation; deep-link protection on protected views.
- **Dependencies:** WP1, WP4.
- **Deliverables:** company vs partner access enforced; inactive partners blocked; no unauthorized direct-URL access.

### WP6 — Product Enablement (FR-05, FR-06, FR-25)
- **Goal:** Per-partner product visibility.
- **Tasks:** company enables/disables products per partner; partner enabled-products view (enabled only); no-enabled-products message + order block.
- **Dependencies:** WP3, WP4, WP5.
- **Deliverables:** enablement management UI; partner-scoped product list; empty-state handling.

### WP7 — Customer (Kunde) Management (FR-10)
- **Goal:** Reusable end-customer records.
- **Tasks:** select existing or create new Kunde; mandatory last name + address, optional contact fields; validation.
- **Dependencies:** WP2, WP5.
- **Deliverables:** Kunde select/create UI usable inside order flow; reusable records.

### WP8 — Ordering Core (FR-11..FR-17) — highest priority
- **Goal:** Partner-driven ordering end-to-end.
- **Tasks:** order creation with multiple article lines + quantities (enabled only, unavailable blocked); auto-assignment to creating partner; persist Order + OrderItems + derived value; on-screen confirmation + simulated e-mail component; order visibility (company all / partner own); status workflow submitted -> reviewed -> fulfilled (company-only change, partner read-only).
- **Dependencies:** WP6, WP7.
- **Deliverables:** working order flow; confirmations; order lists per role; status transitions.

### WP9 — Commission (FR-19)
- **Goal:** Per-order commission tied to partner percentage.
- **Tasks:** `Partner.commissionPercent` set by company via dropdown 5/10/15 + optional custom value; compute + persist CommissionRecord on order creation (commissionPercent × order value); missing-percent handling; commission visibility (company all / partner own).
- **Dependencies:** WP8.
- **Deliverables:** commission records per order; role-scoped commission views.

### WP10 — Partner Dashboard (FR-09)
- **Goal:** Compact partner landing screen.
- **Tasks:** dashboard as landing after login; metrics — total commission, revenue, order count, enabled-product count, latest orders; partner-scoped figures only.
- **Dependencies:** WP8, WP9.
- **Deliverables:** partner dashboard screen.

### WP11 — Data Import (FR-04)
- **Goal:** Excel/CSV import of partners and articles.
- **Tasks:** parse Excel (Apache POI) + CSV (opencsv); column mapping; row validation + duplicate detection; transactional persist of accepted rows; validation report (accepted/rejected + reasons).
- **Dependencies:** WP3, WP4.
- **Deliverables:** import UI; validation report; no silent partial corruption.

### WP12 — Testing & Hardening
- **Goal:** Verify requirements + edge cases.
- **Tasks:** unit/integration tests for services; scenario tests from FSD acceptance criteria; access-control tests; edge-case tests; usability check; full end-to-end demo run.
- **Dependencies:** WP3–WP11.
- **Deliverables:** test suite; passing acceptance-criteria checklist.

### WP13 — Documentation
- **Goal:** Diploma-grade documentation.
- **Tasks:** setup/run guide (Postgres+Keycloak+Quarkus+frontend, demo users); data-model doc (ER + descriptions); UI/UX doc; requirements->implementation->tests mapping; note deferred B-01..B-07; re-tune research question toward internal ordering (confirm with supervisor).
- **Dependencies:** ongoing; finalized after WP12.
- **Deliverables:** complete project documentation set.

## 4. Suggested Development Phases

### Phase 1 — Foundation
WP1, WP2. Runnable stack, schema, seed data. (Front-load Keycloak — highest setup risk.)

### Phase 2 — Central Management
WP3 (Product), WP4 (Partner). First full vertical slices; company CRUD works.

### Phase 3 — Access & Enablement
WP5 (Access Control), WP6 (Enablement). Role separation enforced; partner sees only enabled products.

### Phase 4 — Ordering Core (centerpiece)
WP7 (Kunde), WP8 (Ordering). The demonstrable core process end-to-end.

### Phase 5 — Commission & Dashboard
WP9 (Commission), WP10 (Dashboard). Motivational partner landing + traceable commission.

### Phase 6 — Import
WP11. Bulk data path for ~200 partners / ~800 articles.

### Phase 7 — Testing & Documentation
WP12, WP13. Harden, verify acceptance criteria, finalize docs.

### Team split (Chris + Tobias — updated 2026-07-02)
- **Chris:** implementation — backend (data model, CRUD, access control, ordering core, commission, import) and functional frontend wiring (pages built plain with the existing shared components).
- **Tobias:** frontend design (visual design via Claude design) — delivered as design tokens/CSS for `base.css` + screen polish; UI/UX documentation.
- **Joint:** conventions, testing (WP12), diploma writeup (WP13). Data model signed off by both before Phase 4.
- Design arrives asynchronously; implementation does not wait for it (Light-DOM components + central `base.css` make restyling a CSS-level swap, see `docs/CONVENTIONS.md`).

## 5. Technical Considerations

### Architecture assumptions
- Layered Quarkus backend (REST -> service -> repository/entity), one package per functional area. REST layer = Quarkus REST (formerly RESTEasy Reactive).
- Native Web Components frontend; no framework (fixed stack). Small shared-component library fixed early to prevent sprawl.
- Keycloak realm export committed; Quarkus OIDC validates tokens; demo users documented.
- Containers for PostgreSQL + Keycloak for reproducible demo environment.

### Data handling
- Normalized schema; Order value derived from OrderItems; **unit-price snapshot on OrderItem** protects historical orders/commissions from later price edits.
- Commission computed once at order creation and stored as CommissionRecord (traceability, no recompute drift).
- Import persists accepted rows in a single transaction; rejected rows reported, never silently applied.

### Validation
- Required-field validation before save (partner, product, Kunde, order).
- Duplicate detection for partner/product identifiers, especially during import.
- Order rules: enabled-only, unavailable blocked, valid quantities, at least one line.

### UI considerations
- Clear separation of company vs partner areas; simple, structured navigation.
- Partner dashboard is the landing screen and stays compact (motivational metrics).
- Understandable, non-technical error/validation messages.
- Target ~2s page loads at reference scale (~200 partners / ~800 articles) — implies list pagination/filtering (see Optional).

### Error handling
- Handle all FSD §10 edge cases: invalid/incomplete input, duplicates, import invalid rows, no enabled products, non-enabled/unavailable order attempts, product-becomes-unavailable, inactive-partner access, wrong-role actions, deep-link access, missing commission level.
- Server-side authorization failures return clear unauthorized responses; UI shows friendly messages.

## 6. Testing Plan

### Functional tests
- Company CRUD of partners (incl. commission level) and products.
- Product enablement and partner-scoped visibility.
- Kunde select/create with mandatory last name + address.
- Partner order creation (multi-line), auto-assignment, order value, confirmation.
- Order visibility (company all / partner own); status transitions (company-only).
- Commission record creation and role-scoped visibility.
- Partner dashboard metrics.
- Excel/CSV import with validation report.

### Edge case tests
- Empty required fields; duplicate partner/product identifiers.
- Partner with no enabled products (order blocked + message).
- Order attempt on non-enabled or unavailable product; product becomes unavailable after enablement.
- Inactive partner login + order-creation attempts.
- Wrong-role actions; unauthorized deep-link access to protected views.
- Import file with invalid/duplicate rows -> accurate report, no partial corruption.
- Missing commission level for a partner at order time.

### Usability checks
- Clear internal-area navigation; distinct company vs partner experiences.
- Understandable error/validation messages.
- ~2s page loads for product list, partner views, dashboard, order list.
- End-to-end demo run of Company -> Partner -> End Customer.

> Test coverage maps 1:1 to FSD §12 Acceptance Criteria (1–18).

## 7. Risks and Open Issues

### Risks
- **Keycloak setup overhead** for a 2-person team → front-load in Phase 1; use committed realm export / Dev Services; timebox.
- **Two Keycloak instances in dev** (docker-compose on :8081 vs Quarkus Dev Services on a random port) issue tokens with different issuers → frontend tokens rejected by the backend (401 everywhere) → convention: dev + demo use the compose Keycloak; Dev Services only for tests (see `docs/SETUP.md`).
- **Keycloak user provisioning for imported partners:** the Excel import creates Partner records, **not** logins. Keycloak users are created manually (demo users in the committed realm export) and linked to the Partner record via the username claim. Must stay documented, or ordering tests stall in Phase 3.
- **Excel import complexity** (mapping, dedupe, validation report) can consume time → build incrementally: fixed default mapping + validation report first, then the flexible mapping UI. Column mapping itself is in MVP scope (decision log 2026-07-01: importer = real, full); keep seed dataset as demo fallback.
- **`Order` is a reserved word in SQL/Postgres** → map the entity to table `orders` (`@Table(name = "orders")`) from the start to avoid schema-generation errors in Phase 1 (WP2).
- **No-framework Web Components sprawl** → fix a small shared-component library + naming conventions before feature work.
- **Data-model ownership** shifted after Julian left → Tobias owns schema; both sign off before Phase 4.
- **Central price change vs historical orders** → unit-price snapshot on OrderItem.
- **Two-person scope** → optional enhancements strictly deferred; ordering core protected as must-have.
- **Thesis narrative gap:** the original research question centers on the external shop, now deferred (B-01) → re-tune research question toward internal partner-driven ordering; confirm with supervisor.

### Open questions (from FSD §13)
- Exact research-question wording (direction decided: internal Partner ↔ Company ordering; confirm with supervisor).
- Import file column sets for the partner and article files — waiting on sample data from the client (needed to define default column mapping and validation rules before WP11).

### Resolved (2026-07-03, see FSD §13)
- Dashboard totals (WP10): commission/revenue/order-count count only `fulfilled` own orders; latest-orders list shows any status.

### Resolved (2026-07-01, see FSD §13)
- Commission percentages: `Partner.commissionPercent`, dropdown 5/10/15 + optional custom value; no separate level entity.
- Optional Kunde fields on the order form: first name, e-mail, phone (optional) beyond mandatory last name + address.
- Affiliate identifier: deferred, not part of MVP entity/UI.
- Schema management: Hibernate auto-generation (drop-and-create for dev/test with seed script; `update` for the demo/prod profile so demo data survives restarts), no Flyway.

### Missing information from the FSD
- No fixed acceptance thresholds for the ~2s performance target beyond "demo environment."

## 8. Optional Enhancements

Clearly **not** part of the core MVP; do only if time allows, and never at the expense of the ordering core:

- Company overview dashboard (aggregate totals across all partners).
- Per-partner settlement overview (sales count, revenue, total commission) — display only, not payout (payout = B-07, deferred).
- Product category/image support in the UI.
- Search / filter / pagination on large product and partner lists (helps the ~800-article scale + ~2s target).
- CSV export of order/commission lists.

> **Explicitly deferred to `phase2_backlog.md` (NOT part of this plan):** external partner shop + direct end-customer ordering (B-01), shop design customization (B-02), automatic commission level-up (B-03), real e-mail sending (B-04), shop inquiry auto-forward (B-05), BMD interface (B-06, permanent), commission settlement/payout (B-07).
