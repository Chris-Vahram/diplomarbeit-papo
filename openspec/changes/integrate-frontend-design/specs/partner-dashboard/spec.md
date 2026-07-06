## ADDED Requirements

### Requirement: Partner dashboard renders only backend-backed data
The redesigned partner dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field (e.g. `totalProductCount`, `topClients`, `monthlyRevenue`, `previous`). Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the partner opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Enabled-products indicator without a total count
- **WHEN** the backend does not provide a total product count
- **THEN** the enabled-products indicator either omits the ratio/ring or is fed a count that genuinely exists, never dividing by a missing value
