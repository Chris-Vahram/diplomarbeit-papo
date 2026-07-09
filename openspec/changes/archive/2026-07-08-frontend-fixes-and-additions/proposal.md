## Why

The Phase 1 frontend build (`build-partnerportal-mvp`, 46/53 tasks done) shipped the core screens, but a round of usability review against the live UI surfaced a batch of rough edges and gaps: unsplit address fields, unstyled table controls, inconsistent naming (Partner/Partnerverwaltung, Kunde/Endkunde), and missing operational features the company needs day-to-day (seeing order details without leaving the list, knowing which orders are new, filtering large tables, creating an order on a partner's behalf). Fixing these now — before the design is handed to Tobias for final CSS polish and before the client sees more demos — avoids compounding rework later.

## What Changes

**Fixes**
- Split the single `address` field (Kunde, Partner) into street, house number, postal code, city, country — in entities, DTOs, request validation, forms, and every place a combined address is displayed. **BREAKING**: existing single-string address data is discarded; dev/test/demo reseed in the new shape (no migration script, consistent with the project's no-Flyway/reseed convention).
- Sidebar subheader ("Partnerbereich"/"Firmenbereich") replaced with static "Partnerportal" in both areas.
- Sidebar nav order changed to: Bestellungen, Partner(-verwaltung), Produkte (both areas, dropping the area-specific ordering used today).
- Company nav/page label "Partner" renamed to "Partnerverwaltung".
- "Kunde" renamed to "Endkunde" everywhere in the UI (partner area and company area), including column headers, form labels, confirmation text.
- Table search input restyled to match the design system (spacing, borders, focus state) — `data-table.js` markup unchanged, CSS only.
- Company order table's status control restyled: colored per status (reusing the existing `--status-*-bg/text` custom properties already used by `status-badge`) instead of a bare `<select>`.

**New Features**
- Order-details overlay: clicking an order row opens a `modal-dialog` showing full order info (items, quantities, prices, Endkunde, partner). Uses data already returned by the existing list endpoints — no new fetch.
- Partner dashboard gets a prominent "Neue Bestellung" button that links directly to the new-order flow.
- New-order indicator: orders track an unseen/seen state (company-side only); the sidebar "Bestellungen" nav item shows an iOS-style unread-count badge. An order is marked seen when its details overlay is opened.
- Company can create an order manually: a "Bestellung manuell anlegen" button on the company Bestellungen page opens a flow to pick an existing partner, then that partner's Endkunde (existing or new) and articles/quantities — mirroring the partner's own order-creation flow, scoped to the partner's own Endkunden.
- Table filters: order tables (both areas) get date-range + status filters; company partner table gets an active/inactive filter; product tables (both areas) get a category filter, company product table also gets an available/unavailable filter. All filters sit next to the existing search input.

## Capabilities

### New Capabilities
- `order-notifications`: unseen/seen tracking per order and the company-side sidebar badge that surfaces it.

### Modified Capabilities
- `order-management`: order-details overlay, manual order creation by the company (explicit partner selection), date-range + status filters, "seen" transition on detail view. (Base spec for this capability is still pending its first archive inside `build-partnerportal-mvp`; this change's deltas apply on top of that pending spec.)
- `customer-management`: Kunde address split into structured fields; "Kunde" terminology renamed to "Endkunde" in all user-facing text. (Same pending-archive note as above.)
- `partner-management`: Partner address split into structured fields; partner table active/inactive filter; "Partner" nav label renamed to "Partnerverwaltung".
- `product-management`: category + available/unavailable filters on the company product table.
- `product-enablement`: category filter on the partner product table.
- `partner-dashboard`: "Neue Bestellung" quick-action button.

## Impact

- **Backend**: `Kunde`, `Partner` entities (address columns), `KundeRequest`/`KundeDto`, `PartnerRequest`/`PartnerDto`, `PartnerContactRequest`; `Order` entity (`seenByCompany` flag); `CompanyOrderResource` (new create endpoint, mark-seen endpoint, filter query params); `OrderService`; `DemoDataSeeder` (new address shape); existing tests referencing `.address` (Kunde/Partner/Order/import/dashboard/CSV export tests) need updating.
- **Frontend**: `router.js` (nav order, labels, badge), `form-field.js`/new address sub-fields, `data-table.js` (filter slot, styled search), `status-badge.js`/new status-select styling, `modal-dialog.js` reuse for order details, all pages referencing "Kunde", `partner-dashboard-page.js`, `company-order-list-page.js`, `partner-order-list-page.js`, `partner-order-new-page.js` (reused for company's manual creation flow), `company-partner-list-page.js`, product list pages, `base.css`.
- **Depends on**: `build-partnerportal-mvp` reaching a stable state for `order-management`/`customer-management`; this change assumes those capabilities' current (pending) shape as its starting point.
- **Out of scope**: partner-side notifications/badges, real email, any B-01..B-07 backlog item.
