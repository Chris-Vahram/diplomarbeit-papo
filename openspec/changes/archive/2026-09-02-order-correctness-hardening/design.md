## Context

Five independent defects from the 2026-07-09 review, grouped into one change because they're all small, low-risk correctness fixes to already-shipped code with no cross-dependencies between them.

The dashboard item required checking prior art: `openspec/specs/partner-dashboard/spec.md` and `openspec/specs/company-dashboard/spec.md` (archived 2026-07-06, change `add-dashboard-date-filter`) already specify "without a range, the dashboard shows all-time values." The backend already exposes exact all-time totals via `/api/partner/dashboard` and `/api/company/dashboard` (`PartnerDashboardDto`/`CompanyDashboardDto`, computed with `BigDecimal`). The current frontend ignores these fields for headline KPIs and instead always recomputes from the full order list with a default trailing-6-month window — this violates the archived "without a range = all-time" rule and was the source of today's earlier back-and-forth on what the "correct" behavior is. The archived spec is authoritative; this change closes the gap between it and the shipped frontend rather than introducing new behavior.

## Goals / Non-Goals

**Goals:**
- Prevent duplicate order submission from a double-click.
- Prevent a quantity value from bypassing validation and risking a numeric-column overflow.
- Make partner-number duplicate detection consistent across all creation paths.
- Fix the XLSX header-parsing column-shift bug.
- Bring dashboard headline totals back in line with the archived date-range-filter spec (all-time by default, exact-cent range totals).

**Non-Goals:**
- No new backend date-range query API. The existing frontend design (full order list + client-side range filtering) already satisfies the archived spec's *ranged* scenario; only the *no-range* default and the summation method are wrong. Adding a backend range endpoint would be new API surface for a hardening change and is out of scope here.
- No change to the month chart or "previous period" trend-arrow behavior (`trailingMonths`, `previousPeriod`, `trendInfo`) — not covered by the archived totals requirement.
- No redesign of the period-picker UI itself (Tobias's design stays as-is).

## Decisions

- **No-range headline totals: use the backend DTOs directly.** `partner-dashboard-page.js`/`company-dashboard-page.js` already fetch `/api/partner/dashboard` / `/api/company/dashboard` (for `enabledProductCount`/`totalProductCount`/settlement rows); extend that to also read `totalCommission`, `totalRevenue`, `fulfilledOrderCount` for the headline KPI cards when no explicit range is active. Zero new requests, zero JS math — the values are already `BigDecimal`-exact server-side.
- **Ranged totals: keep client-side filtering, fix the summation.** Per the Non-Goals above, a new backend range endpoint is out of scope. `fulfilledTotals()` in `lib/dashboard-data.js` is changed to sum in integer cents (`Math.round(value * 100)` per order, sum as integers, divide by 100 at the end) instead of naive `reduce((sum, o) => sum + o.orderValue, 0)`. This satisfies "no floating-point rounding drift" without new API surface. Alternative considered: a new `?from=&to=` backend endpoint returning `BigDecimal`-exact ranged totals — more correct in principle, but bigger scope (new query params, service logic, tests) for a hardening change; deferred unless the integer-cent fix proves insufficient in practice.
- **Double-submit guard: mirror the existing company pattern.** `company-order-list-page.js` already disables its submit button during the request (`handleManualSubmit`); the same one-line pattern (`submitButton.disabled = true` before the request, `= false` in the catch) is applied to the two partner-side submit handlers.
- **Quantity upper bound: `@Max(10000)`** on `OrderItemRequest.quantity`. Rationale: comfortably above any plausible real order line (reference client sells hardware/Baustoffe in units, not bulk-thousands) while categorically preventing the numeric(10,2) overflow path. Exact value is a judgment call, not derived from a stated business rule.
- **Partner-number dedup: case-insensitive everywhere.** `PartnerService.assertPartnerNumberFree` gets the same `lower(...)` comparison `PartnerImportDefinition` already uses for import, so both paths agree.
- **XLSX header fix: index-based iteration.** Replace `for (Cell cell : headerRow)` (which skips null cells) with an index loop `0..headerRow.getLastCellNum()-1` using `getCell(i, CREATE_NULL_AS_BLANK)`, mirroring how data rows are already read (`sheetRow.getCell(i)`).

## Risks / Trade-offs

- [The trailing-6-month default was itself a deliberate 2026-07-06 workaround (`integrate-frontend-design`) because a tighter default window showed an empty dashboard with sparse demo data] → Not reopened by this change: switching the *headline KPI totals* to the backend's true all-time values is strictly less likely to be empty than any trailing window (it's non-empty for any partner with ever one fulfilled order), so this fix improves on the exact axis that motivated the original workaround. The month chart keeps its own independent trailing-6-month window (Non-Goals) and is unaffected.
- [Switching no-range totals to the backend DTO could disagree with the range-filtered client computation in edge cases (e.g. a fulfilled order with a null `orderDate` would be counted in the backend all-time total but excluded from any date-filtered range)] → Acceptable; this is the correct, spec-intended distinction between "all-time" and "in this range," not a bug.
- [Integer-cent summation assumes `orderValue`/`commissionValue` arrive as JSON numbers with at most 2 decimal places, matching the backend's `numeric(10,2)` columns] → Already the case; no format change needed.
- [Raising the quantity cap to exactly 10000 is a judgment call] → Low risk; can be revisited without a spec change if the company reports it's ever too low in practice.

## Migration Plan

Code-only, backward compatible, no data migration, no new dependencies. Rollback is reverting the change.
