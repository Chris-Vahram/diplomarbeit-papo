## MODIFIED Requirements

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
