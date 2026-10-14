## Why

The meeting on 12.10.2026 (`Documentation/Besprechungsprotokolle/Besprechungsprotokoll_12.10.md`) confirmed the ordering process and asked for small usability changes: dashboards should open on the current month with quick week/month/year choices, partners should see their latest orders first, "Neue Bestellung" should be the most prominent action, and the partner sidebar should speak in possessive terms ("Meine Endkunden", "Meine Artikel").

## What Changes

- Add period presets "Diese Woche", "Dieser Monat" and "Dieses Jahr" to the existing period picker on both the company and the partner dashboard. Calendar-based up to today; the week starts on Monday.
- Open both dashboards on "Dieser Monat" instead of all-time values.
- Keep the custom Von/Bis range. Rename the reset action to "Gesamter Zeitraum"; it still shows the backend's exact all-time totals.
- Move the partner dashboard's "Letzte Bestellungen" table above the metric and chart rows.
- Make the partner dashboard's "Neue Bestellung" button visibly larger and more prominent through one modifier class.
- Rename partner sidebar entries "Endkunden" → "Meine Endkunden" and "Artikel" → "Meine Artikel" (including the page title of the partner article list).
- No backend change, no new dependency, no company-dashboard trend arrows (those stay B-08).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-dashboard`: default period, presets, "Gesamter Zeitraum", latest-orders position and prominent order action.
- `company-dashboard`: default period, presets and "Gesamter Zeitraum".
- `frontend-shell`: partner navigation labels "Meine Endkunden" and "Meine Artikel".

## Impact

Frontend only: `frontend/src/lib/dashboard-data.js`, `frontend/src/pages/partner-dashboard-page.js`, `frontend/src/pages/company-dashboard-page.js`, `frontend/src/lib/router.js`, `frontend/src/styles/base.css`. Tobias owns the visual design; the button rule is a minimal modifier he may restyle.
