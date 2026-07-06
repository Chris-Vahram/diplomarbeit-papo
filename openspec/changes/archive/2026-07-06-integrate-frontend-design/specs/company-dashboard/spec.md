## ADDED Requirements

### Requirement: Company dashboard renders only backend-backed data
The redesigned company dashboard SHALL render only values the backend actually provides; no metric, chart, or ring may display `NaN`, `undefined`, or an empty chart caused by a missing DTO field. Widgets whose data the backend does not provide SHALL either be removed or fed from data already available client-side.

#### Scenario: Dashboard opened against the real backend
- **WHEN** the company opens the redesigned dashboard
- **THEN** every visible metric and chart shows a real value (or an explicit empty-state), never `NaN` or a broken chart

#### Scenario: Money and dates use the German formatter
- **WHEN** the dashboard shows money or dates
- **THEN** they use the shared de-AT formatter (`lib/format.js`), consistent with the rest of the app
