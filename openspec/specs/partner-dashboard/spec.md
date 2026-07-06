# partner-dashboard Specification

## Purpose
TBD - created by archiving change add-dashboard-date-filter. Update Purpose after archive.
## Requirements
### Requirement: Partner dashboard date-range filter
The partner SHALL be able to restrict their dashboard totals (commission, revenue, fulfilled-order count) and the latest-orders list to a date range (Von/Bis, inclusive, either side optional). Orders count by their order date; totals keep the fulfilled-only convention, the latest-orders list keeps its any-status convention. Without a range, the dashboard shows all-time values.

#### Scenario: Partner filters by range
- **WHEN** the partner sets a Von/Bis range on their dashboard
- **THEN** commission, revenue, and fulfilled-order count include only fulfilled orders in the range, and the latest-orders list shows only orders in the range

#### Scenario: Enabled products unaffected
- **WHEN** a range is active
- **THEN** the enabled-product count stays unchanged (not date-dependent)

