# Tasks — build-partnerportal-mvp

Owners: **C** = Chris, **T** = Tobias, **C+T** = joint. `[opt]` = optional enhancement (do only if time allows).
Phases run top-to-bottom; within a phase, tasks are ordered by dependency.

## 1. Phase 0 — Foundation & Setup (essential)

- [ ] 1.1 (C+T) Create folder layout inside repo (backend, frontend, docs), README, .gitignore
- [ ] 1.2 (T) Bootstrap Quarkus project (RESTEasy Reactive, Hibernate ORM Panache, OIDC, JDBC-Postgres)
- [ ] 1.3 (T) Provision PostgreSQL (container) + local dev config
- [ ] 1.4 (T) Provision Keycloak (container), create realm, roles `company` + `partner`, commit realm export
- [ ] 1.5 (T) Wire Quarkus OIDC to Keycloak; smoke-test a protected "hello" endpoint per role
- [ ] 1.6 (C) Frontend scaffold: static HTML/CSS shell, base layout, routing approach for Web Components
- [ ] 1.7 (C) Build shared Web Components: data-table, form-field, modal, toast, status-badge, metric-card
- [ ] 1.8 (C+T) Agree naming/conventions (packages, component names, REST paths); write in docs

## 2. Phase 1 — Data Model & Core CRUD (essential)

- [ ] 2.1 (T) Define entities: Partner (incl. commissionPercent + active), Product, ProductEnablement, Kunde, Order, OrderItem, CommissionRecord (unit-price snapshot on OrderItem)
- [ ] 2.2 (T) Generate/migrate schema in PostgreSQL; verify relationships
- [ ] 2.3 (C) Product management: REST + service + UI (create/view/edit/list, availability, validation) — spec: product-management
- [ ] 2.4 (C) Partner management: REST + service + UI (create/view/edit, commissionPercent dropdown 5/10/15 + custom, active flag, validation) — spec: partner-management
- [ ] 2.5 (C) Partner self-edit of limited master data (contact only) — spec: partner-management
- [ ] 2.6 (T) Seed dataset (sample partners, products) for development/demo

## 3. Phase 2 — Access Control & Enablement (essential)

- [ ] 3.1 (T) Enforce role authorization on all endpoints (company vs partner) — spec: auth-access-control
- [ ] 3.2 (T) Link Keycloak user to Partner record; server-side inactive-partner block (Partner.active) on login + order create — spec: auth-access-control
- [ ] 3.3 (C) Deep-link protection: unauthorized/unauthenticated access denied on protected views — spec: auth-access-control
- [ ] 3.4 (C) Product enablement: company enables/disables products per partner (REST + UI) — spec: product-enablement
- [ ] 3.5 (C) Partner enabled-products view (only enabled) + no-enabled-products message — spec: product-enablement

## 4. Phase 3 — Ordering Core (essential, highest priority)

- [ ] 4.1 (T) Customer (Kunde) management: select existing or create new, mandatory last name + address — spec: customer-management
- [ ] 4.2 (T) Order creation: multi-line (article + quantity), enabled-only, unavailable blocked, order value derived, unit-price snapshot — spec: order-management, product-management
- [ ] 4.3 (T) Auto-assign order to creating partner; persist Order + OrderItems — spec: order-management
- [ ] 4.4 (T) On-screen confirmation with summary + simulated confirmation e-mail component — spec: order-management
- [ ] 4.5 (T) Order visibility: company sees all, partner sees own — spec: order-management
- [ ] 4.6 (T) Order status workflow submitted -> reviewed -> fulfilled (company-only change, partner read-only) — spec: order-management

## 5. Phase 4 — Commission & Dashboard (essential)

- [ ] 5.1 (T) Compute + persist CommissionRecord on order creation (Partner.commissionPercent × order value) — spec: commission
- [ ] 5.2 (T) Missing-commissionPercent handling — spec: commission
- [ ] 5.3 (T) Commission visibility: company all, partner own — spec: commission
- [ ] 5.4 (T) Partner dashboard as landing screen: commission total, revenue, order count, enabled-product count, latest orders — spec: partner-dashboard

## 6. Phase 5 — Data Import (essential)

- [ ] 6.1 (C) Import service: parse Excel (Apache POI) + CSV (opencsv)
- [ ] 6.2 (C) Row validation + duplicate detection; persist accepted rows transactionally — spec: data-import
- [ ] 6.3 (C) Validation report UI (accepted/rejected rows + reasons) — spec: data-import
- [ ] 6.4 (C) [opt] Flexible column mapping UI (vs fixed column order) — spec: data-import

## 7. Phase 6 — Testing (essential)

- [ ] 7.1 (C+T) Backend unit/integration tests for services (partner, product, enablement, order, commission)
- [ ] 7.2 (T) Scenario tests from specs: order assignment, enabled-only ordering, unavailable blocked, status transitions
- [ ] 7.3 (T) Access-control tests: role separation, deep-link protection, inactive-partner block (Partner.active)
- [ ] 7.4 (C) Edge-case tests: empty required fields, duplicates, no-enabled-products, invalid order, import invalid rows
- [ ] 7.5 (C+T) Usability check: navigation clarity, internal-area separation, understandable error messages, ~2s page loads
- [ ] 7.6 (C+T) End-to-end demo run of chain Company -> Partner -> End Customer

## 8. Phase 7 — Documentation (essential)

- [ ] 8.1 (C+T) Setup/run guide (Postgres + Keycloak + Quarkus + frontend, demo users)
- [ ] 8.2 (T) Data model documentation (ER diagram + entity descriptions)
- [ ] 8.3 (C) UI/UX documentation (screens, shared components, flows)
- [ ] 8.4 (C+T) Diploma writeup: link requirements (FSD) -> implementation -> tests; note deferred B-01..B-07
- [ ] 8.5 (C+T) Re-tune research question toward internal partner-driven ordering; confirm wording with supervisor

## 9. Optional Enhancements (only if time allows) — do NOT block MVP

- [ ] 9.1 [opt] Company overview dashboard (totals across partners)
- [ ] 9.2 [opt] Per-partner settlement overview (sales count, revenue, total commission)
- [ ] 9.3 [opt] Product category/image support in UI
- [ ] 9.4 [opt] Search/filter/pagination on large product/partner lists (~800 articles)
- [ ] 9.5 [opt] Export order/commission lists (CSV)

> Deferred to Phase-2 backlog (NOT part of this change): external shop (B-01), shop customization (B-02), auto level-up (B-03), real e-mail (B-04), shop inquiry forward (B-05), BMD (B-06), payout (B-07).
