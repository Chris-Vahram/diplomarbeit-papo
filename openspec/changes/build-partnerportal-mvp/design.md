## Context

> Quellenreview 11.09.2026: Historische Phasen/Entscheidungen unten bleiben erhalten. Die Forschungsfrage ist gemäß FSD §13 bestätigt und keine offene neue Entscheidung. Alte 200/800-Skalierungsannahmen sind kein aktueller Datenbeleg. Chris verantwortet Implementierung, Tobias visuelles Design; alte Schema-/Teamzuordnungen sind nicht neu zu übernehmen. Aktuelle getrennte Planentscheidungen stehen in `../../PLANNING_REVIEW_2026-09-11.md`. Kein Greenfield-Neustart oder automatischer Rückbau vorhandener Daten.

HTL Diplomarbeit, 2-person team (Chris, Tobias). Authoritative requirements: `Specification/functional_specification_document_partnerportal_final.md` (v2), `Specification/IMPLEMENTATION_PLAN.md`, and the capability specs in this change. Reference scale ~200 partners / ~800 articles, data loaded via Excel/CSV import. Fixed stack (no alternatives): HTML + CSS + native Web Components/Custom Elements (frontend), Quarkus/Java (backend), PostgreSQL (DB), Keycloak (auth). Deferred features B-01..B-07 are out of scope (`Specification/phase2_backlog.md`).

## Goals / Non-Goals

**Goals:**
- Demonstrate the full internal chain Company -> Partner -> End Customer via partner-driven ordering.
- Clean, layered Quarkus backend with a normalized PostgreSQL schema, separable per capability.
- Keycloak-enforced role separation (company, partner); business active-state in DB.
- A small reusable set of Web Components for the UI, no framework.
- Realistic scope for two students within the diploma timeline.

**Non-Goals:**
- External end-customer shop / direct ordering (B-01), shop customization (B-02).
- Auto commission level-up (B-03), real e-mail (B-04), shop inquiry forwarding (B-05), BMD (B-06), payout (B-07).
- Real payment, full inventory/shipping, accounting.

## Decisions

- **Backend = Quarkus + Hibernate ORM (Panache) + Quarkus REST (formerly RESTEasy Reactive).** Rationale: fixed stack; Panache keeps entity/repository code small for a 2-person team. Alternative (plain JDBC) rejected — more boilerplate at ~8-entity scale.
- **DB = PostgreSQL, normalized.** Entities: `Partner` (incl. `commissionPercent`, `active`), `Product`, `ProductEnablement` (partner×product), `Kunde`, `Order`, `OrderItem`, `CommissionRecord`. Order value derived from OrderItems; **store a unit-price snapshot on OrderItem** so historical orders/commissions keep their value if the central product price later changes. Alternative (recompute from live price) rejected — corrupts historical values. **Naming/type conventions:** the `Order` entity maps to table `orders` (`ORDER` is a reserved SQL word); all money fields (price, snapshot, order value, commission) use `BigDecimal` / `numeric`, never floating point.
- **Commission = field on Partner, not an entity.** `Partner.commissionPercent` set by the company via dropdown 5/10/15 + optional custom value. A separate CommissionLevel table was considered and rejected — the 3 presets act as the "level" label and a custom value must be allowed, so a plain constrained numeric field is simpler. Commission computed once at order creation and persisted as `CommissionRecord`.
- **Auth = Keycloak, backend-enforced. Active-state = DB.** Roles `company`/`partner` in a Keycloak realm; Quarkus OIDC validates tokens; every endpoint annotated with the required role. Partner identity linked to a `Partner` record via a Keycloak subject/username claim. Business inactivity lives in `Partner.active` and is checked server-side after authentication (login + order creation). Alternative (sync Keycloak `enabled`) rejected — avoids two sources of truth and keeps Keycloak simple.
- **Keycloak user provisioning = manual.** The Excel import creates `Partner` records only, never Keycloak users. Demo/partner logins are created manually (committed realm export) and linked to their `Partner` record via the username claim; an imported partner without a Keycloak user cannot log in yet. Alternative (auto-provision users on import via Keycloak Admin API) rejected — out of proportion for the MVP demo.
- **One Keycloak/Postgres topology per mode.** Dev + demo use the docker-compose containers (Keycloak on :8081 with the imported realm, Postgres on :5432) so the frontend's fixed Keycloak URL and the backend validate against the **same issuer**; Quarkus Dev Services provisions throwaway containers for tests only. Alternative (Dev Services in dev too) rejected — its random-port Keycloak has a different issuer than the compose instance the frontend talks to, so every API call would fail with 401. Schema strategy: dev/test `drop-and-create`; prod/demo `update` so demo data survives restarts.
- **Seed data = programmatic (`DemoDataSeeder`), not `import.sql`.** A `@Observes StartupEvent` bean persists demo Partner/Product rows via Panache, gated to dev/test via `LaunchMode`. Alternative (raw `import.sql`) rejected after inspecting the generated DDL: Hibernate's default sequence optimizer allocates ids in blocks of 50 starting at 1, so hand-picked ids in a raw `INSERT` collide with the very first id the app allocates at runtime for that entity; working around it means guessing Postgres-folded sequence/column identifiers, which is fragile. The programmatic seeder sidesteps both problems and is type-safe.
- **Dashboard totals = fulfilled orders only.** Commission/revenue/order-count totals count only the partner's own orders with status `fulfilled`; the latest-orders list still shows recent orders of any status. Decided 2026-07-03 (see FSD §13). Alternative (all orders regardless of status) was the interim default during Phase 1 build-out; rejected once decided because a fresh submitted order's commission isn't earned yet — counting it would overstate the partner's numbers until the company actually fulfills it.
- **Frontend = native Web Components.** A thin set of shared components (data-table, form-field, modal, toast, status-badge, metric-card) reused across company and partner screens; pages call REST endpoints and render. No framework per fixed stack. State kept minimal (per-page fetch).
- **Import = Apache POI (xlsx) + opencsv (csv) in a dedicated import service.** Parse -> map columns -> validate row-by-row -> collect accepted/rejected -> persist accepted in one transaction, return a report. Alternative (client-side parsing) rejected — validation + dedupe belong server-side near the DB.
- **Layering per capability:** `resource` (REST) -> `service` (business rules) -> `repository`/entity, one package per capability, so partner/product/order/commission logic stays separable (maintainability NFR).

## Risks / Trade-offs

- [Keycloak setup overhead for 2 students] → Use Quarkus Dev Services / a committed realm export; document login users; timebox setup early in Phase 1.
- [Excel import complexity (mapping, dedupe, validation report) eats time] → Build incrementally: default column mapping + validation report first, then the mapping UI. Column mapping is MVP-essential (decision log: importer = real, full); seed a demo dataset as demo fallback.
- [Thesis narrative: research question originally leaned on the deferred external shop (B-01)] → Decided: reframe toward the internal Partner ↔ Company relationship; exact wording to confirm with supervisor.
- [Web Components with no framework can sprawl] → Fix a small shared-component library early; agree naming/conventions before feature work.
- [Data model owner changed (Julian left)] → Tobias owns schema, both sign off before ordering-core work starts.
- [Central price change vs historical orders] → Unit-price snapshot on OrderItem.
- [Two sources of truth for "active"] → Avoided: DB `Partner.active` is the single source; Keycloak not synced.

## Migration Plan

Greenfield — no data migration. Deployment for the demo: PostgreSQL + Keycloak via containers, Quarkus app connecting to both; committed Keycloak realm export and a seed/import dataset so the environment is reproducible. Rollback = redeploy previous build (no persistent-schema migration risk beyond Hibernate schema generation / simple migration scripts during the prototype).

## Open Questions

- Exact research-question wording (reframe direction decided; wording to confirm with supervisor).

### Resolved (2026-07-01)
- Optional Kunde fields: first name, e-mail, phone (optional) alongside mandatory last name + address.
- Affiliate identifier: deferred, not part of MVP.
- Schema management: Hibernate auto-generation, no Flyway (`drop-and-create` + seed for dev/test, `update` for demo/prod).

### Resolved (2026-07-02)
- Keycloak user provisioning: manual (realm export); import creates Partner records only.
- Dev topology: docker-compose Keycloak/Postgres for dev + demo; Dev Services for tests only.

### Resolved (2026-07-03)
- Dashboard totals: fulfilled orders only (see Decisions above).

### Resolved during implementation (2026-07-02)
- **Kunde scoping = per creating partner.** `Kunde` gets an `ownerPartner` FK (the partner who created it). Partner-facing endpoints (select/create during ordering) filter to the caller's own Kunden. Alternative (global Kunde pool shared across all partners) rejected — every other entity in the system is partner-scoped (orders, commission); a shared pool would let partner A see partner B's customer name/address in a plain dropdown with no ordering relationship, which conflicts with the NFR "consistent role-based visibility in all tested scenarios." Company sees Kunde data only indirectly, through order detail (company already sees all orders).
