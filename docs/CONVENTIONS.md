# Conventions — Partnerportal

## Backend (Quarkus, Java)

### Package layout
One package per capability under `at.papo.partnerportal`, each layered `resource` (REST) -> `service` (business rules) -> entity/repository (Panache):

```
at.papo.partnerportal.partner        # Partner CRUD, commissionPercent, active flag
at.papo.partnerportal.product        # Product CRUD, availability
at.papo.partnerportal.enablement     # ProductEnablement (partner x product)
at.papo.partnerportal.customer       # Kunde
at.papo.partnerportal.order          # Order, OrderItem, status workflow, confirmations
at.papo.partnerportal.commission     # CommissionRecord
at.papo.partnerportal.dashboard      # Partner dashboard aggregation
at.papo.partnerportal.importer       # Excel/CSV import
at.papo.partnerportal.security       # Keycloak/Partner linking, role checks shared across capabilities
```

Within a capability package: `XxxResource` (REST), `XxxService` (logic), `Xxx` (Panache entity), `XxxRepository` only if repository pattern is needed over active-record.

### REST paths
- Company-facing: `/api/company/<resource>` (e.g. `/api/company/partners`, `/api/company/products`)
- Partner-facing: `/api/partner/<resource>` (e.g. `/api/partner/orders`, `/api/partner/dashboard`)
- Shared/no role prefix only for truly role-agnostic reads (none expected in MVP — prefer explicit prefixes for FR-20 clarity).
- Standard REST verbs: `GET` (list/read), `POST` (create), `PUT` (update), no `DELETE` unless a capability spec calls for it.

### Access control
Every resource method annotated `@RolesAllowed("company")` or `@RolesAllowed("partner")` — no unannotated protected endpoints (FR-20). Partner-scoped endpoints additionally filter by the caller's linked `Partner` id server-side, never trust a client-supplied partner id.

## Frontend (native Web Components)

### Naming
- Custom element tag names: kebab-case, capability-first: `<partner-list-page>`, `<product-form-modal>`.
- Shared/reusable components live in `frontend/src/components/`: `data-table`, `form-field`, `modal-dialog`, `toast-notice`, `status-badge`, `metric-card`.
- Page-level components (one per route) live in `frontend/src/pages/`, named `<area>-<thing>-page.js` (e.g. `company-dashboard-page.js`, `partner-order-page.js`).

### Structure
```
frontend/
  index.html          # shell: header, #app-root mount point, #toast-root
  src/
    lib/               # router.js, auth.js, api.js — no UI, no framework
    components/        # shared Web Components, reused across company/partner areas
    pages/             # one Custom Element per route
    styles/            # base.css — shared tokens + component styles
```

### Routing
`src/lib/router.js` is a minimal hash router (`#/company/dashboard`, `#/partner/dashboard`, ...). Each route declares its required role; the router redirects unauthenticated/wrong-role access to `/login` or the caller's own dashboard (FR-20 deep-link protection is enforced again server-side — the frontend guard is UX only, not the security boundary).

### State
No global store. Each page component fetches its own data via `apiFetch()` (`src/lib/api.js`) on `connectedCallback`. Keep state local to the component instance.

### Components
Light DOM (no Shadow DOM) — keeps `base.css` reusable across all components without per-component style duplication, acceptable trade-off for a small non-public component set.

## Cross-cutting

- Company vs partner areas are visually distinct: company screens get `.area--company` (blue accent), partner screens `.area--partner` (green accent) — see `base.css`.
- Validation/error messages: German, plain language, no raw stack traces or HTTP codes shown to the user.
- Commit realm/schema/seed changes together with the code that needs them (e.g. a new entity field ships with its Hibernate mapping in the same commit).
