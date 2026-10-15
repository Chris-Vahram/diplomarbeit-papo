## Context

`partner-dashboard-page.js` already computes trends: explicit period → `previousPeriod(from, to)` + `fulfilledTotals` + `trendInfo`; no period → `trailingWindowTrend(orders)` (30 vs. 30 days). `company-dashboard-page.js` loads every order (`/company/orders`) and computes period totals the same way, but passes no `trend` attributes to `metric-card`.

## Goals / Non-Goals

**Goals:** Same trend semantics on both dashboards; exact cent sums; no new data source.

**Non-Goals:** Backend analytics, per-partner trends in the settlement table, sparklines, configurable comparison periods, comparing month-to-date with the full previous month.

## Decisions

- **Same rule as partner dashboard.** Explicit period: equal-length previous period. "Gesamter Zeitraum": trailing 30 days vs. prior 30 days, headline totals unchanged. Keeps one mental model across dashboards.
- **Average order value trend** = revenue/count of each window; `trendInfo` on the two averages. If the previous window has no fulfilled orders, "neu" (current > 0) or no arrow (both zero), like the other cards.
- **Extend `trailingWindowTrend`** to also return the averages' trend instead of duplicating window code in the company page.
- **Tone:** cards keep `tone="positive"`; the arrow colour comes from `trend` as on the partner side.

## Risks / Trade-offs

- [Trade-off] Month-to-date compared with an equal-length window, not the full previous month (same accepted trade-off as the partner dashboard).
- [Risk] Little demo history makes many arrows "neu". Accepted; demo seed already spreads orders over months.

## Migration Plan

Pure frontend; revert the commit to roll back.
