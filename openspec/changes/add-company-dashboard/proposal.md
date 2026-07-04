## Why

Optional enhancements 9.1 + 9.2 from the MVP build's task list, selected by Chris. The company's dashboard is still a literal placeholder (`<p>Platzhalter…</p>`), and the company has no aggregate view of business across partners — the partner side already has one. 9.1 (company totals) and 9.2 (per-partner settlement breakdown) share the same aggregation and land on the same page, so they ship as one change.

## What Changes

- Replace the `company-dashboard-page` placeholder with a real company-wide overview: total revenue, total commission, fulfilled-order count, partner count — fulfilled orders only, mirroring the partner dashboard convention.
- Per-partner settlement table on the same page: one row per partner with fulfilled-order count, revenue, total commission (read-only reporting; payout/settlement *processing* stays deferred as B-07).
- One new endpoint `GET /api/company/dashboard` returning totals + settlement list in a single response, aggregated server-side without N+1.

## Capabilities

### New Capabilities

- `company-dashboard`: company-wide aggregate totals + per-partner settlement breakdown.

### Modified Capabilities

None.

## Impact

- Backend: `dashboard` package gains `CompanyDashboardDto`, `CompanySettlementDto`, `DashboardService.buildForCompany()`, company-scoped resource path. No schema changes.
- Frontend: rewrite `company-dashboard-page.js` (metric cards + settlement `data-table`).
- **Depends on `fix-audit-hardening`** (forward-only status keeps fulfilled totals stable; escaped `data-table`). Benefits from `add-list-search-pagination` (settlement table search) but works without it.
- The settlement CSV export button is part of `add-csv-export`, not this change.
