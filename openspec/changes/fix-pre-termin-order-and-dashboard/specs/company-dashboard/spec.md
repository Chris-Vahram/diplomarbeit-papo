## MODIFIED Requirements

### Requirement: Company dashboard date-range filter
The company SHALL be able to restrict the dashboard totals and the per-partner settlement breakdown to a date range (Von/Bis, inclusive, either side optional). Selected local calendar days SHALL be preserved exactly without shifting through UTC. Orders count by their order date; the fulfilled-only convention is unchanged. Without a range, the dashboard shows all-time values.

#### Scenario: Company filters by range
- **WHEN** the company sets a Von/Bis range on the dashboard
- **THEN** totals and settlement rows include only fulfilled orders whose order date lies within the range, and partners without matching orders show zero rows

#### Scenario: Selected calendar boundaries stay exact
- **WHEN** the company selects a local Von or Bis date in an Austrian UTC+1 or UTC+2 period
- **THEN** the filter compares against that exact `YYYY-MM-DD` calendar date and not the preceding UTC day

#### Scenario: Filter reset
- **WHEN** the company resets the range
- **THEN** the dashboard shows the all-time values again
