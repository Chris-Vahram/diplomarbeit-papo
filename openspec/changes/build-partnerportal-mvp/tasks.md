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
- [ ] 2.3 (C) Product management: REST + service done, tested; UI (list/create/edit) still open — spec: product-management
- [ ] 2.4 (C) Partner management: REST + service done, tested; UI (list/create/edit, commissionPercent dropdown, active toggle) still open — spec: partner-management
- [ ] 2.5 (C) Partner self-edit of limited master data (contact only) — backend done, tested; UI still open — spec: partner-management
- [x] 2.6 (C) Seed dataset (sample partners, products) for development/demo — programmatic (DemoDataSeeder), not import.sql, see design.md

## 3. Phase 2 — Access Control & Enablement (essential)

- [ ] 3.1 (C) Enforce role authorization on all endpoints (company vs partner) — spec: auth-access-control
- [ ] 3.2 (C) Link Keycloak user to Partner record; server-side inactive-partner block (Partner.active) on login + order create — spec: auth-access-control
- [ ] 3.3 (C) Deep-link protection: unauthorized/unauthenticated access denied on protected views — spec: auth-access-control
- [ ] 3.4 (C) Product enablement: company enables/disables products per partner (REST + UI) — spec: product-enablement
- [ ] 3.5 (C) Partner enabled-products view (only enabled) + no-enabled-products message — spec: product-enablement

## 4. Phase 3 — Ordering Core (essential, highest priority)

- [ ] 4.1 (C) Customer (Kunde) management: select existing or create new, mandatory last name + address — spec: customer-management
- [ ] 4.2 (C) Order creation: multi-line (article + quantity), enabled-only, unavailable blocked, order value derived, unit-price snapshot — spec: order-management, product-management
- [ ] 4.3 (C) Auto-assign order to creating partner; persist Order + OrderItems — spec: order-management
- [ ] 4.4 (C) On-screen confirmation with summary + simulated confirmation e-mail component — spec: order-management
- [ ] 4.5 (C) Order visibility: company sees all, partner sees own — spec: order-management
- [ ] 4.6 (C) Order status workflow submitted -> reviewed -> fulfilled (company-only change, partner read-only) — spec: order-management

## 5. Phase 4 — Commission & Dashboard (essential)

- [ ] 5.1 (C) Compute + persist CommissionRecord on order creation (Partner.commissionPercent × order value) — spec: commission
- [ ] 5.2 (C) Missing-commissionPercent handling — spec: commission
- [ ] 5.3 (C) Commission visibility: company all, partner own — spec: commission
- [ ] 5.4 (C) Partner dashboard as landing screen: commission total, revenue, order count, enabled-product count, latest orders — spec: partner-dashboard

## 6. Phase 5 — Data Import (essential)

- [ ] 6.1 (C) Import service: parse Excel (Apache POI) + CSV (opencsv)
- [ ] 6.2 (C) Row validation + duplicate detection; persist accepted rows transactionally — spec: data-import
- [ ] 6.3 (C) Validation report UI (accepted/rejected rows + reasons) — spec: data-import
- [ ] 6.4 (C) Column mapping UI (map file columns to system fields; default mapping for known headers) — spec: data-import. Essential per decision log ("importer = real, full"); build after 6.1–6.3 work with the default mapping

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
