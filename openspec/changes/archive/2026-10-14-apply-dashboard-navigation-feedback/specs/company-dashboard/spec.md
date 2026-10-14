## MODIFIED Requirements

### Requirement: Company dashboard date-range filter
The company SHALL be able to restrict the dashboard totals and the per-partner settlement breakdown to a period. The period picker SHALL offer the presets "Diese Woche" (Monday of the current week through today), "Dieser Monat" (first day of the current month through today) and "Dieses Jahr" (1 January through today), a custom Von/Bis range (inclusive, either side optional) and "Gesamter Zeitraum". The dashboard SHALL open with "Dieser Monat" selected. Orders count by their order date; the fulfilled-only convention is unchanged. With "Gesamter Zeitraum", the dashboard SHALL show the backend's all-time totals exactly as returned (no client-side re-derivation). When a preset or custom range is active, totals SHALL be computed without floating-point rounding drift (e.g. integer-cent summation).

#### Scenario: Dashboard opens on current month
- **WHEN** the company opens the dashboard
- **THEN** "Dieser Monat" is selected and totals and settlement rows include only fulfilled orders from the first day of the current month through today

#### Scenario: Company chooses a preset
- **WHEN** the company selects "Diese Woche" or "Dieses Jahr"
- **THEN** totals and settlement rows cover only fulfilled orders from Monday of the current week, or from 1 January of the current year, through today, and the picker label shows the preset name

#### Scenario: Company filters by range
- **WHEN** the company sets a Von/Bis range on the dashboard
- **THEN** totals and settlement rows include only fulfilled orders whose order date lies within the range, and partners without matching orders show zero rows

#### Scenario: Filter reset
- **WHEN** the company selects "Gesamter Zeitraum"
- **THEN** the dashboard shows the backend's all-time totals, not a client-side recompute

#### Scenario: Ranged totals have no rounding drift
- **WHEN** a preset or range covers many fulfilled orders
- **THEN** the displayed revenue and commission totals equal the exact cent sum of the included orders, with no floating-point rounding error
