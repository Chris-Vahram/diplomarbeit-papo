## 1. Period presets — essential

- [ ] 1.1 Add pure `presetPeriod(kind, now)` for `week`/`month`/`year` to `frontend/src/lib/dashboard-data.js` (local time, week starts Monday, `to` = today 23:59:59.999); verify with an assert-based self-check for a Sunday, a Monday, the 1st of a month and 1 January.
- [ ] 1.2 Partner dashboard: add the three preset buttons to the existing period panel, open on "Dieser Monat", rename reset to "Gesamter Zeitraum" and show the preset name in the picker label; verify totals/trend/latest orders for each preset, custom Von/Bis and all-time (backend totals unchanged).
- [ ] 1.3 Company dashboard: same presets, default and "Gesamter Zeitraum"; verify totals, settlement zero rows and all-time backend totals.

## 2. Layout and navigation — essential

- [ ] 2.1 Move "Letzte Bestellungen" above metric and chart rows on the partner dashboard and give "Neue Bestellung" a `button-primary--hero` modifier (one minimal `base.css` rule, `plus` icon); verify desktop 1280 px and mobile 390 px.
- [ ] 2.2 Rename partner nav entries to "Meine Endkunden"/"Meine Artikel" and the `/partner/products` route title to "Meine Artikel"; verify sidebar, mobile nav and page title.

## 3. Completion

- [ ] 3.1 Run `node --check` on changed JS files, browser check on both dashboards (desktop/mobile, no console errors), `openspec validate apply-dashboard-navigation-feedback --strict`, `git diff --check` and `partnerportal-quality`; fix findings and update traceability/handoff.
