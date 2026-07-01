## Context

HTL Diplomarbeit, 2-person team (Chris, Tobias). No codebase yet. Authoritative requirements: `functional_specification_document_partnerportal_final.md` (v2) and the capability specs in this change. Reference scale ~200 partners / ~800 articles, data loaded via Excel/CSV import. Fixed stack (no alternatives): HTML + CSS + native Web Components/Custom Elements (frontend), Quarkus/Java (backend), PostgreSQL (DB), Keycloak (auth). Deferred features B-01..B-07 are out of scope (`phase2_backlog.md`).

## Goals / Non-Goals

**Goals:**
- Demonstrate the full internal chain Company -> Partner -> End Customer via partner-driven ordering.
- Clean, layered Quarkus backend with a normalized PostgreSQL schema, separable per capability.
- Keycloak-enforced role separation (company, partner) on every protected endpoint.
- A small reusable set of Web Components for the UI, no framework.
- Realistic scope for two students within the diploma timeline.

**Non-Goals:**
- External end-customer shop / direct ordering (B-01), shop customization (B-02).
- Auto commission level-up (B-03), real e-mail (B-04), shop inquiry forwarding (B-05), BMD (B-06), payout (B-07).
- Real payment, full inventory/shipping, accounting.

## Decisions

- **Backend = Quarkus + Hibernate ORM (Panache) + REST (JAX-RS/RESTEasy Reactive).** Rationale: fixed stack; Panache keeps entity/repository code small for a 2-person team. Alternative (plain JDBC) rejected — more boilerplate at 8-entity scale.
- **DB = PostgreSQL, normalized.** Entities: `Partner`, `Product`, `ProductEnablement` (partner×product), `Kunde`, `Order`, `OrderItem`, `CommissionRecord`, `CommissionLevel`. Order value derived from OrderItems; **store a unit-price snapshot on OrderItem** so historical orders keep their price if the central product price later changes. Alternative (recompute from live product price) rejected — corrupts historical order/commission values.
- **Auth = Keycloak, backend-enforced.** Roles `company`, `partner` mapped to a Keycloak realm; Quarkus OIDC validates tokens; every endpoint annotated with the required role. Partner identity linked to a `Partner` record via a Keycloak subject/username claim. `active` flag on Partner is checked server-side on login and on order creation (Keycloak alone does not know business-inactivity).
- **Frontend = native Web Components.** A thin set of shared components (data-table, form-field, modal, toast, status-badge, metric-card) reused across company and partner screens; pages call REST endpoints and render. No framework per fixed stack. State kept minimal (per-page fetch).
- **Import = Apache POI (xlsx) + opencsv (csv) in a dedicated import service.** Parse -> map columns -> validate row-by-row -> collect accepted/rejected -> persist accepted in one transaction, return a report. Alternative (client-side parsing) rejected — validation + dedupe belong server-side near the DB.
- **Commission computed at order creation** and persisted as a `CommissionRecord` using the partner's current `CommissionLevel` percentage on the order value. Rationale: traceability, matches spec; avoids recomputation drift.
- **Confirmations simulated:** backend returns a rendered confirmation payload; frontend shows on-screen summary + an in-app "email preview" component. No SMTP (B-04).
- **Layering per capability:** `resource` (REST) -> `service` (business rules) -> `repository`/entity, one package per capability, so partner/product/order/commission logic stays separable (maintainability NFR).

## Risks / Trade-offs

- [Keycloak setup overhead for 2 students] → Use Quarkus Dev Services / a single pre-configured realm export committed to the repo; document the login users; timebox setup early in Phase 1.
- [Excel import complexity (mapping, dedupe, validation report) eats time] → Ship a basic-but-real importer first (fixed column order + validation report), add flexible column mapping only if time allows; seed a demo dataset as fallback for the demo.
- [Thesis narrative gap: research question leans on the deferred external shop (B-01)] → Re-tune the research question toward internal partner-driven ordering/coordination; confirm with supervisor (tracked as open question).
- [Web Components with no framework can sprawl] → Fix a small shared-component library early; agree naming/conventions before feature work.
- [Data model owner changed (Julian left)] → Tobias owns schema, both sign off before Phase 2 feature work starts.
- [Central price change vs historical orders] → Unit-price snapshot on OrderItem (see Decisions) prevents retroactive value/commission changes.

## Migration Plan

Greenfield — no data migration. Deployment for the demo: PostgreSQL + Keycloak via containers, Quarkus app connecting to both; committed Keycloak realm export and a seed/import dataset so the environment is reproducible. Rollback = redeploy previous build (no persistent-schema migration risk during the prototype beyond Hibernate schema generation / simple migration scripts).

## Open Questions

- Exact commission percentages for the 3 levels (blocks final dashboard/commission numbers).
- Optional Kunde fields to expose on the order form beyond mandatory last name + address.
- Whether to expose an `affiliate identifier` field in the MVP (FSD open question) or defer.
- Whether schema is managed by Hibernate auto-generation or explicit migration scripts (Flyway) for the prototype.
