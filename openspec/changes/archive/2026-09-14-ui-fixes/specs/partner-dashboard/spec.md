## ADDED Requirements

### Requirement: Metric-card trend basis without an explicit range
When no Von/Bis range is set, the Provision-, Umsatz-, and Abgeschlossene-Bestellungen metric cards SHALL still show an up/down trend arrow, computed from a fixed trailing-30-days-vs-prior-30-days comparison of fulfilled orders. This comparison SHALL NOT change the displayed headline totals, which remain the backend's all-time totals as required by "Partner dashboard date-range filter". When an explicit range is set, the trend arrow SHALL continue to compare that range against the immediately preceding period of equal length, unchanged.

#### Scenario: No range set still shows a trend arrow
- **WHEN** the partner opens the dashboard without setting a Von/Bis range
- **THEN** each of the three trailing metric cards shows an up or down trend arrow based on the last 30 days versus the 30 days before that
- **AND** the displayed commission, revenue, and fulfilled-order-count totals remain the backend's all-time totals, unaffected by the trailing-30-day window

#### Scenario: No fulfilled orders in either trailing window
- **WHEN** the partner has no fulfilled orders in the last 60 days
- **THEN** no misleading trend arrow is shown for that metric (falls back to the existing "no meaningful comparison" no-arrow state)
