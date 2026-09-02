## MODIFIED Requirements

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
