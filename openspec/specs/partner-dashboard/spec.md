# partner-dashboard Specification

## Purpose
TBD - created by archiving change add-dashboard-date-filter. Update Purpose after archive.
## Requirements
### Requirement: Partner dashboard date-range filter
The partner SHALL be able to restrict their dashboard totals (commission, revenue, fulfilled-order count) and the latest-orders list to a date range (Von/Bis, inclusive, either side optional). Selected local calendar days SHALL be preserved exactly without shifting through UTC. Orders count by their order date; totals keep the fulfilled-only convention, the latest-orders list keeps its any-status convention. Without a range, the dashboard shows all-time values.

#### Scenario: Partner filters by range
- **WHEN** the partner sets a Von/Bis range on their dashboard
- **THEN** commission, revenue, and fulfilled-order count include only fulfilled orders in the range, and the latest-orders list shows only orders in the range

#### Scenario: Selected calendar boundaries stay exact
- **WHEN** the partner selects a local Von or Bis date in an Austrian UTC+1 or UTC+2 period
- **THEN** the filter compares against that exact `YYYY-MM-DD` calendar date and not the preceding UTC day

#### Scenario: Enabled products unaffected
- **WHEN** a range is active
- **THEN** the enabled-product count stays unchanged (not date-dependent)

### Requirement: Partner dashboard renders only backend-backed data
The redesigned partner dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field (e.g. `totalProductCount`, `topClients`, `monthlyRevenue`, `previous`). Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the partner opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Enabled-products indicator without a total count
- **WHEN** the backend does not provide a total product count
- **THEN** the enabled-products indicator either omits the ratio/ring or is fed a count that genuinely exists, never dividing by a missing value

