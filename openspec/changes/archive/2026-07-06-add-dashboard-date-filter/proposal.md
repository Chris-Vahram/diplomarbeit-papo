## Why

From Tobi's dashboard mockups, agreed Chris↔Tobi 2026-07-05: both dashboards should let the user narrow totals to a date range (Zeitraum). Originally parked as out-of-scope because it looked like backend work — Tobi spotted that it isn't: the order-list endpoints already return `orderDate`, `orderValue`, `commissionValue`, and `status` per order, so range-filtered totals can be computed entirely client-side.

## What Changes

- Company dashboard and partner dashboard gain a Von/Bis date-range filter (native date inputs, German labels, "Zurücksetzen").
- With a range active, metric cards and lists recompute client-side from the existing order-list endpoints (`/company/orders`, `/partner/orders`): fulfilled-only totals within range, per-partner settlement rows within range (zero rows preserved), partner's latest orders within range.
- No range (default) = exactly today's server-computed all-time numbers.
- Range semantics: an order counts by its **order date** (inclusive bounds; either side may be open). There is no status-change timestamp on orders, so "fulfilled in range" means "fulfilled order whose order date is in range" — documented, matches the only data available.
- Non-date-dependent metrics stay untouched: partner count, enabled-product count.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `company-dashboard`: totals + settlement filterable by date range.
- `partner-dashboard`: totals + latest orders filterable by date range.

## Impact

- Frontend only: new shared `date-range-filter` component; `company-dashboard-page.js` and `partner-dashboard-page.js` additionally fetch their order lists and aggregate client-side when a range is set.
- No backend changes, no new endpoints, no schema changes.
- Explicitly still out of scope: "ggü. Vorperiode" trends, Top-Kunden, Bestellstatus-Pie (need data that doesn't exist).
