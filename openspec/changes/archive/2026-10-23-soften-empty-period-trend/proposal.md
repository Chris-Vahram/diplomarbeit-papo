## Why

At the start of a period (e.g. "Dieser Monat" on the 2nd) there are often no fulfilled orders yet. The metric cards then show a red arrow with "-100,0 % ggü. Vorperiode", which reads like an alarm although the period has only just begun. Seen on both dashboards on 22.10.2026.

## What Changes

- When the current period has no fulfilled orders but the previous one has, the card shows a neutral note "Noch keine Abschlüsse im Zeitraum" instead of a red arrow and percentage.
- All other trend cases stay unchanged (real declines with at least one fulfilled order still show the red arrow and percentage).
- Applies to the company and partner dashboards (shared `trendInfo`).

## Capabilities

### Modified Capabilities

- `company-dashboard`: trend indicator rule for an empty current period.

## Impact

Frontend only: `frontend/src/lib/dashboard-data.js`, `frontend/src/components/metric-card.js`, `frontend/src/styles/base.css`. No backend, API or data change.
