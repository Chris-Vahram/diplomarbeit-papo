## Why

Two client asks converge on the same screen. The 10.07.2026 meeting: adding a product to an order must be "übersichtlicher (Suchleiste, Filter, ...)" — today each article line is a bare `<select>` over up to ~800 articles, unusable at reference scale and hostile to the non-technical partners the client emphasized. And the 2026-07-09 redesign review identified the highest-leverage missing piece (Q1): the order form shows no money at all before submit — no unit price, no running total, no commission. The partner, whose motivation is commission, types quantities blind. All data needed is already delivered to the browser (`ProductDto` carries price/category/availability; `GET /api/partner/profile` carries `commissionPercent`), so this is a frontend-only change.

## What Changes

- The per-line product `<select>` is replaced by a searchable product picker (combobox): type-to-filter over the enabled products by name and category, showing name, category, unit price, and availability per suggestion; unavailable products are visibly disabled and cannot be chosen; editing the text after a selection clears it. Full keyboard operation, escaped output.
- Each article line shows the unit price and its live line total (Menge × Einzelpreis).
- A **live order summary** panel fills in as the partner works — article lines, Gesamtwert, and a commission preview line ("Ihre Provision (X %)"), the commission computed to match the backend to the cent. It is an estimate; the post-submit confirmation is authoritative. (The document/letterhead "Belegvorschau" styling from Direction B is deferred to a separate follow-up so this change ships the client's search/filter ask directly.)
- All money aggregation in integer cents (project rule), rendered de-AT.
- The company's manual-order flow reuses the same shared form and gains the picker + totals; its product data now carries price/category (currently dropped), and its commission line shows the selected partner's percentage.

## Capabilities

### New Capabilities
- `order-form-experience`: What the order-creation form must provide while composing an order — product search/filtering, live line and order totals, and a commission preview matching the backend.

### Modified Capabilities
<!-- None. Order submission payloads, validation, and backend behavior are unchanged; published specs (openspec/specs/) contain no order-form capability. -->

## Impact

- **Frontend only:** `frontend/src/lib/order-form.js` (picker, lines, live totals, summary), `frontend/src/pages/partner-order-new-page.js` + `frontend/src/pages/company-order-list-page.js` (wiring, profile fetch, carry price/category in the company picker data), `frontend/src/styles/base.css` (summary styling).
- **No backend change.** No REST, entity, or authorization change. Order request payload contract (`productId`/`quantity` lines) stays identical.
- **Depends on `self-host-fonts`** (offline fonts, split out on review) landing first — the monospace amounts assume local Fira Code.
- Serves the open a11y findings on the way: combobox keyboard navigation and an `aria-live` total.
