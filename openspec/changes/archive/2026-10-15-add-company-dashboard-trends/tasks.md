## 1. Implementation — essential

- [x] 1.1 Extend `trailingWindowTrend` (and add a small average-order-value helper if needed) in `frontend/src/lib/dashboard-data.js`; extend `tools/check_dashboard_presets.mjs` or add one assert check for the average trend (zero/zero, zero/positive, normal).
- [x] 1.2 Pass `trend`/`trend-label` to the four company metric cards for preset, custom and all-time views, reusing the partner-dashboard logic.

## 2. Completion — essential

- [x] 2.1 Browser check desktop 1280 px and mobile 390 px for "Dieser Monat", "Dieses Jahr", custom range and "Gesamter Zeitraum" (values match the previous totals, no `NaN`, no console errors); `node --check`, `openspec validate add-company-dashboard-trends --strict`, `git diff --check`, partnerportal-quality; set B-08 to done in `Specification/phase2_backlog.md`.
