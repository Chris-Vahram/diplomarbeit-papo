## Why

The 2026-07-09 review found five correctness defects in the shipped MVP: two order-flow bugs (duplicate orders from a double-click, a quantity value that can overflow the money column instead of failing validation), one data-integrity inconsistency (partner-number duplicate detection disagrees between import and manual create/update), one import parsing bug (a blank XLSX header cell silently shifts every later column), and one dashboard bug (partner/company dashboard headline totals default to a trailing-6-month window and re-sum in JS floats, instead of the archived `partner-dashboard`/`company-dashboard` spec's "without a range, all-time values" rule).

## What Changes

- Disable the order-submit button while the request is in flight on both partner order-creation paths (`partner-dashboard-page.js`, `partner-order-list-page.js`), matching the guard the company's manual-order flow already has, so a double-click cannot create duplicate orders and duplicate commission records.
- Add an upper bound to `OrderItemRequest.quantity` (`@Max`) so an extreme quantity is rejected with a German validation error instead of risking a numeric-column overflow.
- Make manual partner create/update duplicate-identifier checking case-insensitive (`PartnerService.assertPartnerNumberFree`), matching the case-insensitive check the Excel/CSV importer already applies, so `"P-1001"` and `"p-1001"` can no longer coexist depending on which path created them.
- Fix `ImportedFileParser`'s XLSX header read: iterate header cells by index up to the sheet's last column (including blanks) instead of skipping blank cells, so a blank header cell can no longer shift later columns' values.
- Fix partner/company dashboard headline totals (Provision/Umsatz/Bestellanzahl) to match the already-archived date-range-filter spec: **no range set → all-time totals**, sourced directly from the existing backend all-time totals endpoints (`/api/partner/dashboard`, `/api/company/dashboard`) instead of a client-side trailing-6-month recompute. When the partner/company explicitly sets a Von/Bis range, totals continue to be computed from the range-filtered order list (as today), but summed via integer-cent arithmetic instead of naive JS floating-point addition, removing the rounding-drift risk. The month chart and the "previous period" trend arrows are unaffected (they are not covered by the archived totals rule).

No new dependencies, no new backend API surface. All user-facing text stays German with correct umlauts.

## Capabilities

### Modified Capabilities
- `order-management`: order submission must be guarded against duplicate double-click submits on both partner order-creation entry points; order item quantity must be bounded.
- `partner-management`: partner-number duplicate detection must be case-insensitive on every creation/update path, not only on import.
- `data-import`: XLSX header parsing must not skip blank header cells, preventing a silent column-value shift.
- `partner-dashboard`: headline totals (commission, revenue, fulfilled-order count) must show all-time values when no date range is set, per the existing "without a range, all-time values" requirement — closing a gap where the current implementation defaults to a trailing-6-month window instead.
- `company-dashboard`: same all-time-by-default correction for the company dashboard's headline totals.

## Impact

- Frontend: `frontend/src/pages/partner-dashboard-page.js`, `frontend/src/pages/partner-order-list-page.js`, `frontend/src/pages/company-dashboard-page.js`, `frontend/src/lib/dashboard-data.js`.
- Backend: `backend/.../order/OrderItemRequest.java`, `backend/.../partner/PartnerService.java`, `backend/.../importer/ImportedFileParser.java`.
- No schema changes, no new backend endpoints, no new dependencies.
