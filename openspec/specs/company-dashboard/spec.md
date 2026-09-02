# company-dashboard Specification

## Purpose
TBD - created by archiving change add-dashboard-date-filter. Update Purpose after archive.
## Requirements
### Requirement: Company dashboard date-range filter
The company SHALL be able to restrict the dashboard totals and the per-partner settlement breakdown to a date range (Von/Bis, inclusive, either side optional). Orders count by their order date; the fulfilled-only convention is unchanged. Without a range, the dashboard SHALL show the backend's all-time totals exactly as returned (no client-side re-derivation), not a default trailing window. When a range is set, totals SHALL be computed without floating-point rounding drift (e.g. integer-cent summation).

#### Scenario: Company filters by range
- **WHEN** the company sets a Von/Bis range on the dashboard
- **THEN** totals and settlement rows include only fulfilled orders whose order date lies within the range, and partners without matching orders show zero rows

#### Scenario: Filter reset
- **WHEN** the company resets the range
- **THEN** the dashboard shows the backend's all-time totals again, not a default trailing-period recompute

#### Scenario: Ranged totals have no rounding drift
- **WHEN** the company sets a range covering many fulfilled orders
- **THEN** the displayed revenue and commission totals equal the exact cent sum of the included orders, with no floating-point rounding error

### Requirement: Company dashboard renders only backend-backed data
The redesigned company dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field. Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the company opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Money and dates use the German formatter
- **WHEN** the dashboard shows money or dates
- **THEN** they use the shared de-AT formatter (`lib/format.js`), consistent with the rest of the app

### Requirement: Company-wide dashboard totals
The company SHALL see aggregate totals across all partners: total revenue, total commission, total fulfilled-order count, and total partner count. Totals SHALL count only fulfilled orders, consistent with the partner dashboard convention.

#### Scenario: Company views dashboard totals
- **WHEN** the company opens the company dashboard
- **THEN** the system shows total revenue, total commission, and total fulfilled-order count summed across all partners' fulfilled orders

#### Scenario: Non-fulfilled orders excluded from totals
- **WHEN** a partner has orders in `submitted` or `reviewed` status
- **THEN** those orders are not counted in the company dashboard totals

### Requirement: Per-partner settlement breakdown
The company SHALL see, per partner, the fulfilled-order count, revenue, and total commission for that partner.

#### Scenario: Company views settlement breakdown
- **WHEN** the company opens the settlement view
- **THEN** the system shows one row per partner with that partner's fulfilled-order count, revenue, and total commission

#### Scenario: Partner with no fulfilled orders
- **WHEN** a partner has no fulfilled orders
- **THEN** the settlement view shows that partner with zero order count, zero revenue, and zero commission

