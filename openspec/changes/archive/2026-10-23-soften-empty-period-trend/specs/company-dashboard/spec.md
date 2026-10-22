## MODIFIED Requirements

### Requirement: Company metric-card trend indicators
The company dashboard SHALL show an up/down trend arrow with a "ggü. Vorperiode" label on the Umsatz, Provision, Abgeschlossene Bestellungen and Ø Bestellwert metric cards. A preset or custom range SHALL be compared with the immediately preceding period of equal length; "Gesamter Zeitraum" SHALL keep the all-time totals and compare the last 30 days with the 30 days before. Comparisons SHALL count fulfilled orders only, in exact cents. When the current period has no fulfilled orders, the card SHALL show a neutral note instead of a downward arrow.

#### Scenario: Trend for a selected period
- **WHEN** the company views "Dieser Monat"
- **THEN** each of the four cards shows an arrow and percentage comparing the month so far with the equally long period immediately before it

#### Scenario: Trend with all-time totals
- **WHEN** the company selects "Gesamter Zeitraum"
- **THEN** the cards show the backend's all-time totals and arrows based on the last 30 days versus the 30 days before

#### Scenario: Previous period without orders
- **WHEN** the current period has fulfilled orders and the previous period has none
- **THEN** the cards show an upward arrow labelled as new instead of a percentage

#### Scenario: Nothing to compare
- **WHEN** neither period contains fulfilled orders
- **THEN** no trend arrow is shown and no `NaN` or `Infinity` appears

#### Scenario: Current period without fulfilled orders
- **WHEN** the current period has no fulfilled orders and the previous period has some
- **THEN** the cards show the neutral note "Noch keine Abschlüsse im Zeitraum" in secondary text, without an arrow and without "-100 %"
