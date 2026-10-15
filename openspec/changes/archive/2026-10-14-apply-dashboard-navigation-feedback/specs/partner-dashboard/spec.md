## MODIFIED Requirements

### Requirement: Partner dashboard date-range filter
The partner SHALL be able to restrict their dashboard totals (commission, revenue, fulfilled-order count) and the latest-orders list to a period. The period picker SHALL offer the presets "Diese Woche" (Monday of the current week through today), "Dieser Monat" (first day of the current month through today) and "Dieses Jahr" (1 January through today), a custom Von/Bis range (inclusive, either side optional) and "Gesamter Zeitraum". The dashboard SHALL open with "Dieser Monat" selected. Orders count by their order date; totals keep the fulfilled-only convention, the latest-orders list keeps its any-status convention. With "Gesamter Zeitraum", the dashboard SHALL show the backend's all-time totals exactly as returned (no client-side re-derivation). When a preset or custom range is active, totals SHALL be computed without floating-point rounding drift (e.g. integer-cent summation).

#### Scenario: Dashboard opens on current month
- **WHEN** the partner opens the dashboard
- **THEN** "Dieser Monat" is selected and commission, revenue, fulfilled-order count and the latest-orders list cover only orders from the first day of the current month through today

#### Scenario: Partner chooses a preset
- **WHEN** the partner selects "Diese Woche" or "Dieses Jahr"
- **THEN** the figures and the latest-orders list cover only orders from Monday of the current week, or from 1 January of the current year, through today, and the picker label shows the preset name

#### Scenario: Partner filters by range
- **WHEN** the partner sets a Von/Bis range on their dashboard
- **THEN** commission, revenue, and fulfilled-order count include only fulfilled orders in the range, and the latest-orders list shows only orders in the range

#### Scenario: Enabled products unaffected
- **WHEN** a preset or range is active
- **THEN** the enabled-product count stays unchanged (not date-dependent)

#### Scenario: No range set shows true all-time totals
- **WHEN** the partner selects "Gesamter Zeitraum"
- **THEN** commission, revenue, and fulfilled-order count show the backend's all-time totals, not a client-side recompute

#### Scenario: Ranged totals have no rounding drift
- **WHEN** a preset or range covers many fulfilled orders
- **THEN** the displayed revenue and commission totals equal the exact cent sum of the included orders, with no floating-point rounding error

### Requirement: Metric-card trend basis without an explicit range
When "Gesamter Zeitraum" is selected, the Provision-, Umsatz-, and Abgeschlossene-Bestellungen metric cards SHALL still show an up/down trend arrow, computed from a fixed trailing-30-days-vs-prior-30-days comparison of fulfilled orders. This comparison SHALL NOT change the displayed headline totals, which remain the backend's all-time totals as required by "Partner dashboard date-range filter". When a preset or custom range is active, the trend arrow SHALL compare that period against the immediately preceding period of equal length.

#### Scenario: No range set still shows a trend arrow
- **WHEN** the partner selects "Gesamter Zeitraum"
- **THEN** each of the three trailing metric cards shows an up or down trend arrow based on the last 30 days versus the 30 days before that
- **AND** the displayed commission, revenue, and fulfilled-order-count totals remain the backend's all-time totals, unaffected by the trailing-30-day window

#### Scenario: Preset compared with previous equal-length period
- **WHEN** "Dieser Monat" is selected
- **THEN** each trend arrow compares the month-to-date values with the immediately preceding period of the same length

#### Scenario: No fulfilled orders in either trailing window
- **WHEN** the partner has no fulfilled orders in the last 60 days
- **THEN** no misleading trend arrow is shown for that metric (falls back to the existing "no meaningful comparison" no-arrow state)

## ADDED Requirements

### Requirement: Latest orders and order action are prominent
The partner dashboard SHALL show the "Letzte Bestellungen" list above the metric cards and charts, and SHALL present "Neue Bestellung" as the visually most prominent action on the dashboard.

#### Scenario: Partner opens the dashboard
- **WHEN** the partner opens the dashboard on desktop or mobile
- **THEN** the "Letzte Bestellungen" list appears before the metric cards and charts
- **AND** the "Neue Bestellung" button is larger than the other dashboard controls and opens the new-order page
