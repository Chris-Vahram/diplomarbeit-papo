## Context

The partner dashboard (`DashboardService.buildForPartner`, `/api/partner/dashboard`) established the conventions: fulfilled-orders-only totals, `metric-row` of `<metric-card>`s, `<data-table>` for the list. `CommissionRecord` snapshots `orderValue`/`commissionPercentUsed`/`commissionValue` per order and links to `Partner`, so per-partner settlement is a group-by over existing data — no new entity. `company-dashboard-page.js` is a placeholder.

## Goals / Non-Goals

**Goals:**
- Company-wide totals (revenue, commission, fulfilled-order count, partner count), fulfilled orders only.
- Per-partner settlement rows (fulfilled count, revenue, commission), including zero rows for partners without fulfilled orders.
- Single round trip, no N+1 aggregation.

**Non-Goals:**
- No settlement *processing*/payout (B-07 stays deferred — this is read-only reporting).
- No CSV export here (that's `add-csv-export`).
- No date-range filtering (not requested; all-time totals like the partner dashboard).

## Decisions

**Reuse the `dashboard` package, not a new module.** `DashboardService.buildForCompany()` alongside `buildForPartner()` keeps all dashboard aggregation and the "fulfilled only" rule in one place. New `CompanyDashboardDto` (totals + `List<CompanySettlementDto>`) and a company-scoped endpoint `GET /api/company/dashboard` (`@RolesAllowed("company")`).

**One combined response (totals + settlement list).** Both render on the same page; a second endpoint would just add a round trip for data computed in the same pass.

**Aggregation avoids the partner-dashboard N+1 pattern.** `buildForPartner` calls `commissionService.findForOrder` per order — fine for one partner, explosive company-wide (~200 partners). `buildForCompany` loads all fulfilled orders and all `CommissionRecord`s in two list queries, builds `Map<orderId, CommissionRecord>`, groups by partner in memory. Partners without fulfilled orders come from `Partner.listAll()` and get zero rows.

**Frontend mirrors `partner-dashboard-page.js`.** Same structure: `metric-row` + `metric-card`s on top, `data-table` below (settlement rows: partner name, fulfilled count, revenue, commission — German column labels, € formatting with `toFixed(2)` like everywhere else).

## Risks / Trade-offs

- **[Trade-off]** Combined DTO couples totals + settlement in one response — accepted: same page, same read-only aggregation, one pass.
- **[Risk]** In-memory grouping loads all fulfilled orders — fine at diploma/demo scale (hundreds of orders); documented limit, same order of magnitude as the existing company order list.

## Migration Plan

No migration. Feature branch after `fix-audit-hardening`. Additive endpoint + rewritten placeholder page.

## Open Questions

None — settlement renders as a section on the dashboard page (single route), per earlier decision.
