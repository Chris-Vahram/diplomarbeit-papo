# company-dashboard Specification

## Purpose
Company landing dashboard: period filter, company-wide totals and per-partner settlement breakdown from backend data only, and trend indicators against the previous period.

## Requirements

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

### Requirement: Company dashboard renders only backend-backed data
The redesigned company dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field. Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the company opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Money and dates use the German formatter
- **WHEN** the dashboard shows money or dates
- **THEN** they use the shared de-AT formatter (`lib/format.js`), consistent with the rest of the app

### Requirement: Company-wide dashboard totals
The company SHALL see aggregate totals across all partners: total revenue, total commission, total fulfilled-order count, and total partner count. Totals SHALL count only fulfilled orders, consistent with the partner dashboard convention.

#### Scenario: Company views dashboard totals
- **WHEN** the company opens the company dashboard
- **THEN** the system shows total revenue, total commission, and total fulfilled-order count summed across all partners' fulfilled orders

#### Scenario: Non-fulfilled orders excluded from totals
- **WHEN** a partner has orders in `submitted` or `reviewed` status
- **THEN** those orders are not counted in the company dashboard totals

### Requirement: Per-partner settlement breakdown
The company SHALL see, per partner, the fulfilled-order count, revenue, and total commission for that partner.

#### Scenario: Company views settlement breakdown
- **WHEN** the company opens the settlement view
- **THEN** the system shows one row per partner with that partner's fulfilled-order count, revenue, and total commission

#### Scenario: Partner with no fulfilled orders
- **WHEN** a partner has no fulfilled orders
- **THEN** the settlement view shows that partner with zero order count, zero revenue, and zero commission

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
