## MODIFIED Requirements

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
