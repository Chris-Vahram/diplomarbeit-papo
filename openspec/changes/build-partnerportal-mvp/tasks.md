# Tasks — build-partnerportal-mvp

Owners (updated 2026-07-02): **C** = Chris (implementation: backend + functional frontend), **T** = Tobias (frontend design + UI/UX docs), **C+T** = joint. `[opt]` = optional enhancement (do only if time allows).
Phases run top-to-bottom; within a phase, tasks are ordered by dependency. Implementation does not wait for the frontend design (Phase FD) — screens are built plain and restyled via `base.css`.

## 1. Phase 0 — Foundation & Setup (essential)

- [x] 1.1 (C+T) Create folder layout inside repo (backend, frontend, docs), README, .gitignore
- [x] 1.2 (T) Bootstrap Quarkus project (Quarkus REST, Hibernate ORM Panache, OIDC, JDBC-Postgres)
- [x] 1.3 (T) Provision PostgreSQL (container) + local dev config
- [x] 1.4 (T) Provision Keycloak (container), create realm, roles `company` + `partner`, commit realm export
- [x] 1.5 (T) Wire Quarkus OIDC to Keycloak; smoke-test a protected "hello" endpoint per role
- [x] 1.6 (C) Frontend scaffold: static HTML/CSS shell, base layout, routing approach for Web Components
- [x] 1.7 (C) Build shared Web Components: data-table, form-field, modal, toast, status-badge, metric-card
- [x] 1.8 (C+T) Agree naming/conventions (packages, component names, REST paths); write in docs

## 2. Phase 1 — Data Model & Core CRUD (essential)

- [x] 2.1 (C) Define entities: Partner (incl. commissionPercent + active), Product, ProductEnablement, Kunde, Order, OrderItem, CommissionRecord (unit-price snapshot on OrderItem)
- [x] 2.2 (C) Generate/migrate schema in PostgreSQL; verify relationships
- [x] 2.3 (C) Product management: REST + service + UI (list/create/edit, availability, validation) — spec: product-management. UI not yet manually browser-verified (no browser tool in this environment, no docker-compose port free during this session) — smoke-test before trusting
- [x] 2.4 (C) Partner management: REST + service + UI (list/create/edit, commissionPercent dropdown 5/10/15+custom, active toggle, validation) — spec: partner-management. UI not yet manually browser-verified, same caveat as 2.3
- [x] 2.5 (C) Partner self-edit of limited master data (contact only) — spec: partner-management. UI not yet manually browser-verified, same caveat as 2.3
- [x] 2.6 (C) Seed dataset (sample partners, products) for development/demo — programmatic (DemoDataSeeder), not import.sql, see design.md

## 3. Phase 2 — Access Control & Enablement (essential)

- [x] 3.1 (C) Enforce role authorization on all endpoints (company vs partner) — spec: auth-access-control. Every resource already `@RolesAllowed`; added `quarkus.security.jaxrs.deny-unannotated-endpoints=true` as a fail-closed backstop for future endpoints (required explicit `@PermitAll` on the pre-existing public Phase-0 `/hello` smoke endpoint)
- [x] 3.2 (C) Link Keycloak user to Partner record; server-side inactive-partner block (Partner.active) on login + order create — spec: auth-access-control. `CurrentPartner.resolve()` (shared by every partner-scoped endpoint) now rejects with 403 if `!partner.active`
- [x] 3.3 (C) Deep-link protection: unauthorized/unauthenticated access denied on protected views — spec: auth-access-control. Already implemented in `router.js` (Phase 1) and browser-verified again this session for the new `/partner/products` route
- [x] 3.4 (C) Product enablement: company enables/disables products per partner (REST + UI) — spec: product-enablement. `EnablementResource`/`EnablementService` (`GET/PUT/DELETE /api/company/partners/{id}/products/{productId}`); UI = "Produkte" modal per partner row in company-partner-list-page.js, checkbox toggles call PUT/DELETE. Browser-verified (enable + disable both persist)
- [x] 3.5 (C) Partner enabled-products view (only enabled) + no-enabled-products message — spec: product-enablement. `PartnerProductResource` (`GET /api/partner/products`, scoped via CurrentPartner); new `partner-product-list-page.js` + nav link, empty-state message via data-table's emptyMessage. Browser-verified end to end (toggle on company side reflected immediately on partner side)

## 4. Phase 3 — Ordering Core (essential, highest priority)

- [x] 4.1 (C) Customer (Kunde) management: select existing or create new, mandatory last name + address — spec: customer-management. `KundeResource`/`KundeService` (`/api/partner/customers`), folded into the order-creation form (no standalone Kunde screen, per design's "company sees Kunde only through order detail")
- [x] 4.2 (C) Order creation: multi-line (article + quantity), enabled-only, unavailable blocked, order value derived, unit-price snapshot — spec: order-management, product-management. `OrderService.createForPartner` checks `EnablementService.isEnabledForPartner` + `product.available` per line, snapshots unit price, sums order value
- [x] 4.3 (C) Auto-assign order to creating partner; persist Order + OrderItems — spec: order-management. `OrderResource` (`POST /api/partner/orders`) always uses `CurrentPartner.resolve()`, never a client-supplied partner id
- [x] 4.4 (C) On-screen confirmation with summary + simulated confirmation e-mail component — spec: order-management. `partner-order-new-page.js` swaps to a confirmation view (line items, total, simulated e-mail block) after successful submit — no backend email service, purely rendered from the returned OrderDto (B-04 stays deferred)
- [x] 4.5 (C) Order visibility: company sees all, partner sees own — spec: order-management. `CompanyOrderResource` (`GET /api/company/orders`, all) vs `OrderResource` (`GET /api/partner/orders`, own only via CurrentPartner)
- [x] 4.6 (C) Order status workflow submitted -> reviewed -> fulfilled (company-only change, partner read-only) — spec: order-management. `PUT /api/company/orders/{id}/status` (`@RolesAllowed("company")`); partner order list renders status read-only via `status-badge`

Backend: 23/23 tests pass (`OrderResourceTest`, `KundeResourceTest` added). Browser-verified end-to-end with Playwright: multi-line order creation (new customer), confirmation + simulated e-mail render, partner order list, company order list + status change, deep-link protection on the new partner routes.

## 5. Phase 4 — Commission & Dashboard (essential)

- [x] 5.1 (C) Compute + persist CommissionRecord on order creation (Partner.commissionPercent × order value) — spec: commission. New `CommissionService.createForOrder` (commission package), called from `OrderService.createForPartner`; basis/percentage/value snapshotted on the record
- [x] 5.2 (C) Missing-commissionPercent handling — spec: commission. No runtime check added: `Partner.commissionPercent` is `@NotNull` end to end (entity column non-nullable, `PartnerRequest` validated) - the "missing" state is structurally unreachable, so there's no code path to exercise (see tasks.md decision, avoids dead defensive code)
- [x] 5.3 (C) Commission visibility: company all, partner own — spec: commission. Commission fields folded directly into `OrderDto` (commissionPercentUsed/commissionValue) rather than a separate commission endpoint - visibility is inherited from the already-correct order-list scoping (`OrderResource` own-only vs `CompanyOrderResource` all)
- [x] 5.4 (C) Partner dashboard as landing screen: commission total, revenue, order count, enabled-product count, latest orders — spec: partner-dashboard. New `dashboard` package (`DashboardService`/`DashboardResource`, `GET /api/partner/dashboard`); totals over fulfilled-only orders per the 2026-07-03 decision, latest-orders (last 5) show any status. `partner-dashboard-page.js` rewritten from placeholder to real metric-cards + table

Backend: 26/26 tests pass (`DashboardResourceTest` added, commission assertions added to `OrderResourceTest`, fixed a test-pollution leak in `OrderResourceTest` that would have skewed the enabled-product count). Browser-verified end-to-end: dashboard starts at zero, order creation + fulfillment raises totals by the exact expected amounts, commission column visible on both partner and company order lists.

## 6. Phase 5 — Data Import (essential)

- [x] 6.1 (C) Import service: parse Excel (Apache POI) + CSV (opencsv) — spec: data-import. `ImportedFileParser` (new `importer` package): `.xlsx` via POI, `.csv` via opencsv with delimiter sniffing (`,` vs `;` - German-locale exports commonly use `;` since `,` is the decimal separator). Column layout genuinely unknown (client sample file still not provided, see CONTINUATION_PROMPT "still open") - not a blocker, since column mapping (6.4) makes any layout work
- [x] 6.2 (C) Row validation + duplicate detection; persist accepted rows transactionally — spec: data-import. `ImportService.commit`: validates every row first (in-memory report, no DB writes), then persists only accepted rows in one `@Transactional` call. Duplicate check: partner via `partnerNumber` (existing spec identifier); product has no dedicated identifier field in the locked entity model, so name (case-insensitive) is used as the closest equivalent - both within-file and against the DB
- [x] 6.3 (C) Validation report UI (accepted/rejected rows + reasons) — spec: data-import. `company-import-page.js` renders a per-row report table (row number/accepted-rejected/reasons) after commit
- [x] 6.4 (C) Column mapping UI (map file columns to system fields; default mapping for known headers) — spec: data-import. `ImportService.suggestMapping` normalizes headers and matches against per-field key/label/alias lists (`PartnerImportDefinition`/`ProductImportDefinition`); UI shows a dropdown per system field pre-selected with the suggestion, editable before confirming

Backend: 30/30 tests pass (partner + product import preview/commit/role-check tests added). Browser-verified end-to-end with Playwright: CSV upload → correct auto-mapping suggestion → sample-row preview → commit → report showing 1 accepted / 2 rejected with correct reasons ("Existiert bereits.", "Partnernummer fehlt.") → accepted row confirmed present in the company partner list; deep-link protection holds on the new `/company/import` route.

## 7. Phase 6 — Testing (essential)

- [ ] 7.1 (C+T) Backend unit/integration tests for services (partner, product, enablement, order, commission)
- [ ] 7.2 (C) Scenario tests from specs: order assignment, enabled-only ordering, unavailable blocked, status transitions
- [ ] 7.3 (C) Access-control tests: role separation, deep-link protection, inactive-partner block (Partner.active)
- [ ] 7.4 (C) Edge-case tests: empty required fields, duplicates, no-enabled-products, invalid order, import invalid rows
- [ ] 7.5 (C+T) Usability check: navigation clarity, internal-area separation, understandable error messages, ~2s page loads
- [ ] 7.6 (C+T) End-to-end demo run of chain Company -> Partner -> End Customer

## 8. Phase 7 — Documentation (essential)

- [ ] 8.1 (C+T) Setup/run guide (Postgres + Keycloak + Quarkus + frontend, demo users)
- [ ] 8.2 (C) Data model documentation (ER diagram + entity descriptions)
- [ ] 8.3 (T) UI/UX documentation (screens, shared components, flows)
- [ ] 8.4 (C+T) Diploma writeup: link requirements (FSD) -> implementation -> tests; note deferred B-01..B-07
- [ ] 8.5 (C+T) Re-tune research question toward internal partner-driven ordering; confirm wording with supervisor

## 9. Phase FD — Frontend Design (async, does NOT block any other phase)

Tobias produces the visual design (Claude design) in parallel; screens are built plain until it lands.

- [ ] FD.1 (T) Deliver frontend design (tokens: colors, typography, spacing; layouts for login, company area, partner area, dashboard) and push to GitHub
- [ ] FD.2 (T) Keep the existing shared components (data-table, form-field, modal-dialog, toast-notice, status-badge, metric-card) and company/partner area split (`.area--company` / `.area--partner`) as design targets — structural changes need C+T agreement first
- [ ] FD.3 (C+T) Integrate design into `frontend/src/styles/base.css` (Light-DOM components = CSS-level swap, no component rewrites)
- [ ] FD.4 (T) Visual polish pass over all screens once features exist

## 10. Optional Enhancements (only if time allows) — do NOT block MVP

- [ ] 9.1 [opt] Company overview dashboard (totals across partners)
- [ ] 9.2 [opt] Per-partner settlement overview (sales count, revenue, total commission)
- [ ] 9.3 [opt] Product category/image support in UI
- [ ] 9.4 [opt] Search/filter/pagination on large product/partner lists (~800 articles)
- [ ] 9.5 [opt] Export order/commission lists (CSV)

> Deferred to Phase-2 backlog (NOT part of this change): external shop (B-01), shop customization (B-02), auto level-up (B-03), real e-mail (B-04), shop inquiry forward (B-05), BMD (B-06), payout (B-07).
