## Context

The MVP build (`build-partnerportal-mvp`, 46/53 tasks) shipped working screens for both areas. This change is a usability/consistency pass plus a handful of features the company needs operationally (order details, manual order creation, unseen-order visibility, filters) discovered by exercising the live UI. It touches both backend (Quarkus/Hibernate/Postgres) and frontend (native Web Components, `base.css`), per the fixed stack — no new dependencies.

Two existing pieces do a lot of the work already:
- `modal-dialog.js` — generic open/close overlay component, unused by the order pages today.
- `--status-*-bg/--status-*-text` CSS custom properties in `base.css` — already used by `status-badge.js` and the dashboard's status donut, just not yet applied to the order-table status control.
- `OrderService.createForPartner(Partner partner, OrderRequest request)` already takes `Partner` as a parameter rather than resolving it internally — the company's manual-creation endpoint can call it directly with a company-selected partner instead of duplicating order-creation logic.

## Goals / Non-Goals

**Goals:**
- Structured address (street, house number, postal code, city, country) for Kunde and Partner, consistently displayed everywhere an address currently renders as one string.
- Consistent naming ("Partnerportal", "Partnerverwaltung", "Endkunde") and nav ordering (Bestellungen, Partner, Produkte) across both areas.
- Styled table search input and a colored status control, using existing design tokens.
- Order-details overlay, company-side manual order creation, unseen-order badge, and date/status/category/active filters on the relevant tables.

**Non-Goals:**
- No partner-side notifications/badges (company-only, per proposal).
- No data migration for existing single-string addresses — dev/test/demo reseed (no Flyway, per project convention).
- No new external dependencies, no framework introduction, no change to Keycloak roles.
- No change to order status workflow rules (forward-only submitted→reviewed→fulfilled stays as-is).

## Decisions

**Address as a JPA `@Embeddable`.** Both `Kunde` and `Partner` need the identical five fields (street, houseNumber, postalCode, city, country). An `@Embeddable Address` class embedded in both entities avoids duplicating five columns/getters twice and gives one place to add a `toDisplayString()`-equivalent formatting helper (used by DTOs). Alternative considered: plain duplicated fields on each entity — rejected, pure duplication with no upside since the shape is identical everywhere it's used (forms, DTOs, requests, CSV import mapping).

**All five address parts mandatory.** Matches the "Recommended" scope agreed in exploration (street+number, postal code, city, country). `KundeRequest`/`PartnerRequest`/`PartnerContactRequest` validate each part `@NotBlank`, replacing the single `@NotBlank address` check.

**No address migration — reseed instead.** Per project convention (dev/test = drop-and-create + seeder; demo/prod = Hibernate `update`) and the confirmed decision that no real demo data needs preserving, the old `address` column is simply dropped and `DemoDataSeeder` regenerates data in the new shape. This is a **BREAKING** change for any existing row data, called out in the proposal.

**Unseen/seen as a plain boolean on `Order`, not a separate table.** `Order.seenByCompany` (default `false`). Only one company role exists (not per-user accounts), so there is no need to track *which* company user saw it — a single flag is the correct level of complexity. Alternative considered: a join table for per-user seen-state — rejected as unnecessary given there's exactly one "company" viewer concept in this system.

**Seen transition happens server-side on detail fetch, not client-side on list render.** A `PUT /api/company/orders/{id}/seen` (or folding the mark-seen into the existing detail-fetch call) flips the flag when the company actually opens that order's overlay — matches the agreed "opening details" trigger exactly, and avoids the list page silently clearing badges the company never actually looked at.

**Badge count via existing list data, no polling/websocket.** The unseen count is derivable from the already-fetched company order list (`orders.filter(o => !o.seenByCompany).length`); the sidebar re-renders on each route change already (`renderNav()` in `router.js`), so the badge recomputes on navigation without new infrastructure. No push/poll mechanism needed at this scale (~200 partners' worth of orders).

**Manual company order creation reuses `OrderService.createForPartner`.** `CompanyOrderResource` gains `POST /api/company/orders` accepting `{ partnerId, kundeId | newKunde, items }`; it resolves `Partner` by id (404 if missing) and calls the same `createForPartner(partner, request)` used by the partner's own flow — so validation (enablement, availability, commission snapshot) is identical, not reimplemented. The frontend reuses `partner-order-new-page.js`'s rendering logic (article-line UI, Kunde radio/new-Kunde block) with an added partner-select step, rather than writing a parallel form from scratch.

**Filters are client-side, consistent with today's search/pagination.** `data-table.js` already does client-side search over `.searchKeys` and client-side pagination; the new filters (date range, status, category, active/inactive) are additional client-side predicates composed with the existing search, not new backend query parameters. This matches current architecture and the client's data scale (~200 partners, ~800 products, order volumes well within client-side-filter territory). Alternative considered: server-side filtering via query params — rejected as unnecessary complexity at this scale and inconsistent with the existing search implementation.

**Address split is the only backend-touching item beyond order-management; both are done in one change.** The user explicitly chose to do the full-stack scope now rather than deferring backend-touching items, overriding the Chris/Tobias split for this change.

## Risks / Trade-offs

- [Dropping the `address` column loses any existing row data] → Mitigated: confirmed safe to reseed; called out as **BREAKING** in the proposal; only affects dev/test/demo environments, no production data exists yet.
- [Client-side filtering loads the full list before filtering, same as today's search] → Acceptable at current/expected data volume (~200 partners, ~800 products); revisit if catalog/partner count grows an order of magnitude.
- [Reusing `partner-order-new-page.js` UI logic for the company's manual-creation flow risks entangling partner-only and company-only concerns in one file] → Mitigate by extracting the shared article-line/Kunde-picker rendering into functions callable from both pages rather than subclassing or duplicating the whole page.
- [Seen-state flip tied to "opening the overlay" could be bypassed if the company never opens details, leaving orders permanently "unseen" even if handled via the status dropdown alone] → Accepted: matches the explicitly chosen trigger; a future iteration could also mark-seen on status change if this proves confusing in practice.

## Open Questions

None outstanding — scope, address shape, seen-trigger, filter fields, and badge scope were all resolved during exploration.
