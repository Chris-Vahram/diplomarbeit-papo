## Why

B-08 is only half done: the partner dashboard shows "ggü. Vorperiode" trend arrows, the company dashboard does not. The company is the main user of revenue and commission figures, so it should see the same comparison. Everything needed is already loaded client-side.

## What Changes

- Company dashboard metric cards (Umsatz, Provision, Abgeschlossene Bestellungen, Ø Bestellwert) show an up/down trend arrow with "+x,x % ggü. Vorperiode".
- With a preset or custom range: compare against the immediately preceding period of equal length (same rule as the partner dashboard).
- With "Gesamter Zeitraum": headline totals stay the backend's all-time values; the arrows compare the last 30 days with the 30 days before.
- No arrow when there is nothing meaningful to compare (both periods zero).
- Reuse `previousPeriod`, `fulfilledTotals`, `trendInfo` and `trailingWindowTrend` from `dashboard-data.js`. No backend change.
- B-08 moves from `STUB` to done in `phase2_backlog.md` after implementation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `company-dashboard`: adds metric-card trend indicators.

## Impact

Frontend only: `frontend/src/pages/company-dashboard-page.js`, possibly `frontend/src/lib/dashboard-data.js` for an average-order-value helper. Docs: `Specification/phase2_backlog.md` (B-08 status).
