## Context

Both dashboards already compute period totals client-side from the order list (`dashboard-data.js`: `filterByPeriod`, `fulfilledTotals` with integer-cent `sumCents`, `previousPeriod`, `trendInfo`). Today they open without a range and then show the backend's all-time totals. The period picker only offers Von/Bis plus "Zurücksetzen".

## Goals / Non-Goals

**Goals:** Month as default, three one-click presets, unchanged exactness of totals, latest orders first for partners, more prominent order action, possessive partner navigation labels.

**Non-Goals:** Backend changes, company trend arrows (B-08), persisting the chosen period across reloads, redesigning the dashboard layout beyond moving one table.

## Decisions

- **Preset definition (calendar, up to today):** week = Monday 00:00 of the current week through today 23:59:59.999; month = 1st of the current month through today; year = 1 January through today. Local time, consistent with `toLocalIsoDate`. One pure helper `presetPeriod(kind, now = new Date())` in `dashboard-data.js` returns `{ from, to }`; the pages reuse their existing `this.period` handling.
- **Default:** pages initialise `this.period = presetPeriod("month")` and track the active preset name for the label ("Dieser Monat" etc.). A custom Von/Bis keeps the existing date label.
- **All-time:** the reset action is renamed "Gesamter Zeitraum" and keeps `this.period = null`, so the existing all-time path (backend totals as returned, partner trailing-30-day trend) is unchanged.
- **Trend (partner only):** a preset is an explicit range, so the existing equal-length previous-period comparison applies unchanged. Company trend arrows remain out of scope (B-08).
- **Latest orders:** the table keeps its current semantics (newest five in the selected period, any status) and only moves above `metric-row`/`chart-row`. With the month default it may be empty early in a month; its existing empty message covers that.
- **Prominent button:** add `button-primary--hero` (larger padding/font, `plus` icon) as one minimal rule in `base.css`. No change to Tobias' tokens or other buttons.
- **Labels:** change only the two partner nav labels and the `/partner/products` route title. The page heading of the customer page is already "Meine Endkunden".

## Risks / Trade-offs

- [Risk] Users expect the old all-time view on open. Mitigation: "Gesamter Zeitraum" stays one click away and is labelled clearly.
- [Risk] Week/year boundaries off by one (Sunday, 1 January, month start). Mitigation: assert-based checks for these dates in the helper task.
- [Trade-off] Month-to-date compared with an equally long previous window, not with the full previous month. Accepted: it reuses the existing, tested comparison.

## Migration Plan

Pure frontend deploy; rollback is reverting the commit. No data impact.
