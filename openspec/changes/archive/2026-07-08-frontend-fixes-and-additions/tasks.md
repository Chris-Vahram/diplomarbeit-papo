## 1. Backend: structured address

- [x] 1.1 Create `Address` `@Embeddable` (street, houseNumber, postalCode, city, country — all `@NotBlank`-validated at the request-record level) in a shared package (e.g. `at.papo.partnerportal.common`)
- [x] 1.2 Replace `Kunde.address` (String) with an embedded `Address`; update `KundeRequest`, `KundeDto`, `KundeService` accordingly
- [x] 1.3 Replace `Partner.address` (String) with an embedded `Address`; update `PartnerRequest`, `PartnerContactRequest`, `PartnerDto`, `PartnerService` accordingly
- [x] 1.4 Update `PartnerImportDefinition` (CSV/XLSX column mapping) to map the new address columns instead of one combined column
- [x] 1.5 Update `DemoDataSeeder` to generate addresses in the new structured shape
- [x] 1.6 Update all backend tests referencing `.address` (`KundeResourceTest`, `PartnerResourceTest`, `OrderResourceTest`, `PartnerImportResourceTest`, `CompanyDashboardResourceTest`, `DashboardResourceTest`, `CsvExportResourceTest`)
- [x] 1.7 Confirm dev/test schema drop-and-recreate picks up the new columns cleanly (no migration script needed, per design.md — no Flyway/migration files exist, Hibernate handles it)

## 2. Backend: order-notifications (unseen/seen)

- [x] 2.1 Add `Order.seenByCompany` (boolean, default `false`)
- [x] 2.2 Add `OrderDto.seenByCompany` field
- [x] 2.3 Add `PUT /api/company/orders/{id}/seen` (or fold into detail fetch) in `CompanyOrderResource`/`OrderService` to flip the flag transactionally
- [x] 2.4 Confirm newly created orders (`OrderService.createForPartner`) default to unseen

## 3. Backend: manual order creation by company

- [x] 3.1 Add `POST /api/company/orders` to `CompanyOrderResource` accepting `{ partnerId, kundeId | newKunde, items }`
- [x] 3.2 Resolve `Partner` by `partnerId` (404 if missing/not found) and delegate to the existing `OrderService.createForPartner(partner, request)` — no duplicated order-creation logic
- [x] 3.3 Add an endpoint (or extend an existing one) for the company to fetch a given partner's Kunden (`GET /api/company/partners/{id}/customers` or similar), scoped exactly like the partner's own `/partner/customers`
- [x] 3.4 Add/adjust tests: company creates an order for a partner; company cannot select another partner's Kunde; validation errors match partner-created-order behavior

## 4. Frontend: navigation & labeling

- [x] 4.1 `router.js`: replace `appRole.textContent` area text with static "Partnerportal" in both areas
- [x] 4.2 `router.js`: reorder both nav arrays to Bestellungen, Partner(-verwaltung), Produkte (+ Datenimport/Profil staying after, per current placement)
- [x] 4.3 `router.js` + `company-partner-list-page.js`: rename "Partner" label/heading to "Partnerverwaltung" (company area only)
- [x] 4.4 Rename "Kunde" → "Endkunde" in all partner-area and company-area UI text: `partner-order-new-page.js`, `partner-order-list-page.js`, `partner-dashboard-page.js`, `company-partner-list-page.js` (if referenced), `company-order-list-page.js`, and any new order-details/manual-creation UI

## 5. Frontend: address form fields

- [x] 5.1 Extend or wrap `form-field.js` (or compose multiple `form-field` instances) to render street/houseNumber/postalCode/city/country as a grouped address sub-form
- [x] 5.2 Update Kunde forms (`partner-order-new-page.js` new-Kunde block) to use the split address fields and submit the structured shape
- [x] 5.3 Update Partner forms (`company-partner-list-page.js` create/edit, `partner-profile-page.js` self-edit) to use the split address fields
- [x] 5.4 Add an address-formatting helper (e.g. in `format.js`) to compose the structured parts back into a single display string for read-only contexts (Kunde dropdown options, order confirmation, order details)

## 6. Frontend: table search, filters, status control (CSS + component work)

- [x] 6.1 Style `.data-table__search` in `base.css` to match the design system (spacing, border, focus state) — already matched the design system from an earlier commit, verified only
- [x] 6.2 Extend `data-table.js` with an optional filter slot/config (e.g. `.filters = [{ key, type: "daterange"|"select"|"multiselect", options }]`) rendered next to the search input, composing with existing `_filteredRows()`
- [x] 6.3 Wire order-date-range + status filters into `company-order-list-page.js` and `partner-order-list-page.js`
- [x] 6.4 Wire active/inactive filter into `company-partner-list-page.js`
- [x] 6.5 Wire category (+ availability, company-side) filters into `company-product-list-page.js` and `partner-product-list-page.js`
- [x] 6.6 Replace the bare `<select>` status control in `company-order-list-page.js` with a styled, color-coded control reusing `--status-*-bg/--status-*-text` custom properties from `base.css`

## 7. Frontend: order-details overlay

- [x] 7.1 Add a reusable order-details rendering function (items, quantities, prices, Endkunde, partner, status) usable by both areas
- [x] 7.2 Wire row-click on `company-order-list-page.js`'s table to open a `modal-dialog` with the order details; call the mark-seen endpoint when opening an unseen order
- [x] 7.3 Wire row-click on `partner-order-list-page.js`'s table to open the same overlay (read-only, no partner-selection field, no seen/unseen concept)

## 8. Frontend: unseen-order badge

- [x] 8.1 Compute unseen count from the company order list already fetched (`orders.filter(o => !o.seenByCompany).length`)
- [x] 8.2 Render an iOS-style badge on the "Bestellungen" sidebar nav item in `renderNav()` (router.js), hidden when count is 0
- [x] 8.3 Refresh the badge after an order is marked seen (overlay open) and after `render()`/navigation, without a full page reload

## 9. Frontend: partner dashboard quick action

- [x] 9.1 Add a prominent "Neue Bestellung" button to `partner-dashboard-page.js` opening the same order-creation overlay used on `partner-order-list-page.js` (reused via `order-form.js` helpers), not a link to the separate `#/partner/orders/new` page

## 10. Frontend: company manual order creation

- [x] 10.1 Add "Bestellung manuell anlegen" button to `company-order-list-page.js`
- [x] 10.2 Build the manual-creation flow (modal or dedicated view): partner select → that partner's Endkunde (existing/new) → articles/quantities, reusing the article-line and Kunde-picker rendering extracted from `partner-order-new-page.js` (see design.md risk note on shared logic)
- [x] 10.3 Submit to `POST /api/company/orders`; show the same on-screen + simulated-e-mail confirmation pattern as the partner flow

## 11. Verification

- [x] 11.1 Run backend test suite; fix any remaining `.address` references
- [x] 11.2 Manually exercise: create/edit Kunde and Partner with split address; place a partner order; open order details in both areas; create a manual company order; confirm badge appears/clears; apply each new filter
- [x] 11.3 Check both areas at narrow (mobile off-canvas nav) and wide viewport for the new sidebar badge, styled search/filters, and status control
