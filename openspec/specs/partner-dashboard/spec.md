# partner-dashboard Specification

## Purpose
TBD - created by archiving change add-dashboard-date-filter. Update Purpose after archive.

## Requirements

### Requirement: Partner dashboard date-range filter
The partner SHALL be able to restrict their dashboard totals (commission, revenue, fulfilled-order count) and the latest-orders list to a date range (Von/Bis, inclusive, either side optional). Orders count by their order date; totals keep the fulfilled-only convention, the latest-orders list keeps its any-status convention. Without a range, the dashboard SHALL show the backend's all-time totals exactly as returned (no client-side re-derivation), not a default trailing window. When a range is set, totals SHALL be computed without floating-point rounding drift (e.g. integer-cent summation).

#### Scenario: Partner filters by range
- **WHEN** the partner sets a Von/Bis range on their dashboard
- **THEN** commission, revenue, and fulfilled-order count include only fulfilled orders in the range, and the latest-orders list shows only orders in the range

#### Scenario: Enabled products unaffected
- **WHEN** a range is active
- **THEN** the enabled-product count stays unchanged (not date-dependent)

#### Scenario: No range set shows true all-time totals
- **WHEN** the partner opens the dashboard without setting a Von/Bis range
- **THEN** commission, revenue, and fulfilled-order count show the backend's all-time totals, not a default trailing-period recompute

#### Scenario: Ranged totals have no rounding drift
- **WHEN** the partner sets a range covering many fulfilled orders
- **THEN** the displayed revenue and commission totals equal the exact cent sum of the included orders, with no floating-point rounding error

### Requirement: Partner dashboard renders only backend-backed data
The redesigned partner dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field (e.g. `totalProductCount`, `topClients`, `monthlyRevenue`, `previous`). Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the partner opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Enabled-products indicator without a total count
- **WHEN** the backend does not provide a total product count
- **THEN** the enabled-products indicator either omits the ratio/ring or is fed a count that genuinely exists, never dividing by a missing value

### Requirement: Partner company in dashboard heading
The partner dashboard SHALL show “Dashboard der Firma <name>” for the authenticated partner. While loading or after a failed profile request it SHALL keep a neutral heading and provide a read-only retry. Existing dashboard metrics SHALL remain independently available.

#### Scenario: Company identified on dashboard
- **WHEN** the own profile is successfully loaded on the partner dashboard
- **THEN** its name appears in the dashboard heading as plain text

#### Scenario: Changed profile name
- **WHEN** a partner saves a changed name and next enters the dashboard
- **THEN** the dashboard loads and displays the updated own name

#### Scenario: Long name on mobile
- **WHEN** the company name is long on a narrow screen
- **THEN** it wraps without covering dashboard controls
