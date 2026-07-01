## Context

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

- **Backend = Quarkus + Hibernate ORM (Panache) + REST (RESTEasy Reactive).** Rationale: fixed stack; Panache keeps entity/repository code small for a 2-person team. Alternative (plain JDBC) rejected — more boilerplate at ~8-entity scale.
- **DB = PostgreSQL, normalized.** Entities: `Partner` (incl. `commissionPercent`, `active`), `Product`, `ProductEnablement` (partner×product), `Kunde`, `Order`, `OrderItem`, `CommissionRecord`. Order value derived from OrderItems; **store a unit-price snapshot on OrderItem** so historical orders/commissions keep their value if the central product price later changes. Alternative (recompute from live price) rejected — corrupts historical values.
- **Commission = field on Partner, not an entity.** `Partner.commissionPercent` set by the company via dropdown 5/10/15 + optional custom value. A separate CommissionLevel table was considered and rejected — the 3 presets act as the "level" label and a custom value must be allowed, so a plain constrained numeric field is simpler. Commission computed once at order creation and persisted as `CommissionRecord`.
- **Auth = Keycloak, backend-enforced. Active-state = DB.** Roles `company`/`partner` in a Keycloak realm; Quarkus OIDC validates tokens; every endpoint annotated with the required role. Partner identity linked to a `Partner` record via a Keycloak subject/username claim. Business inactivity lives in `Partner.active` and is checked server-side after authentication (login + order creation). Alternative (sync Keycloak `enabled`) rejected — avoids two sources of truth and keeps Keycloak simple.
- **Frontend = native Web Components.** A thin set of shared components (data-table, form-field, modal, toast, status-badge, metric-card) reused across company and partner screens; pages call REST endpoints and render. No framework per fixed stack. State kept minimal (per-page fetch).
- **Import = Apache POI (xlsx) + opencsv (csv) in a dedicated import service.** Parse -> map columns -> validate row-by-row -> collect accepted/rejected -> persist accepted in one transaction, return a report. Alternative (client-side parsing) rejected — validation + dedupe belong server-side near the DB.
- **Layering per capability:** `resource` (REST) -> `service` (business rules) -> `repository`/entity, one package per capability, so partner/product/order/commission logic stays separable (maintainability NFR).

## Risks / Trade-offs

- [Keycloak setup overhead for 2 students] → Use Quarkus Dev Services / a committed realm export; document login users; timebox setup early in Phase 1.
- [Excel import complexity (mapping, dedupe, validation report) eats time] → Ship a basic-but-real importer first (fixed column order + validation report), add flexible column mapping only if time allows; seed a demo dataset as fallback.
- [Thesis narrative: research question originally leaned on the deferred external shop (B-01)] → Decided: reframe toward the internal Partner ↔ Company relationship; exact wording to confirm with supervisor.
- [Web Components with no framework can sprawl] → Fix a small shared-component library early; agree naming/conventions before feature work.
- [Data model owner changed (Julian left)] → Tobias owns schema, both sign off before ordering-core work starts.
- [Central price change vs historical orders] → Unit-price snapshot on OrderItem.
- [Two sources of truth for "active"] → Avoided: DB `Partner.active` is the single source; Keycloak not synced.

## Migration Plan

Greenfield — no data migration. Deployment for the demo: PostgreSQL + Keycloak via containers, Quarkus app connecting to both; committed Keycloak realm export and a seed/import dataset so the environment is reproducible. Rollback = redeploy previous build (no persistent-schema migration risk beyond Hibernate schema generation / simple migration scripts during the prototype).

## Open Questions

- Optional Kunde fields to expose on the order form beyond mandatory last name + address.
- Whether to expose an `affiliate identifier` field in the MVP (FSD open question) or defer.
- Whether schema is managed by Hibernate auto-generation or explicit migration scripts (Flyway) for the prototype.
- Exact research-question wording (reframe direction decided; wording to confirm with supervisor).
