## ADDED Requirements

### Requirement: Company dashboard date-range filter
The company SHALL be able to restrict the dashboard totals and the per-partner settlement breakdown to a date range (Von/Bis, inclusive, either side optional). Orders count by their order date; the fulfilled-only convention is unchanged. Without a range, the dashboard shows all-time values.

#### Scenario: Company filters by range
- **WHEN** the company sets a Von/Bis range on the dashboard
- **THEN** totals and settlement rows include only fulfilled orders whose order date lies within the range, and partners without matching orders show zero rows

#### Scenario: Filter reset
- **WHEN** the company resets the range
- **THEN** the dashboard shows the all-time values again
