## 0. Decision gate (blocks group 2)

- [ ] 0.1 Chris + Tobias resolve the CUT / BUILD / client-side call per widget row in `design.md` (Vorperiode trend, Top-Kunden, monthly sparklines, products ring, status donut). Record the outcome in this change before implementing group 2.

## 1. Integration hygiene (proceeds regardless of the decision)

- [ ] 1.1 Vendor + pin Lucide: replace `https://unpkg.com/lucide@latest` in `index.html` with a locally committed, version-pinned copy (or inline the ~20 SVGs actually used); verify icons render offline
- [ ] 1.2 Audit both redesigned dashboards + `partner-order-list-page.js` for hardcoded `toFixed`/ISO dates; route money/dates through `lib/format.js` (de-AT)
- [ ] 1.3 Verify Tobias's period-picker actually filters totals/lists correctly against real orders (Von/Bis inclusive, reset restores all-time); fix if it only styles without filtering

## 2. Reconcile widgets to backend (per the group-0 decision)

- [ ] 2.1 For each CUT widget: remove it from the dashboard JS and any dangling `base.css`/`metric-card` rules; confirm no remaining reference to the missing DTO field
- [ ] 2.2 For each KEEP-client-side widget: feed it from the already-fetched order list (no backend change); e.g. Bestellstatus breakdown counted from order statuses, monthly buckets from `orderDate`
- [ ] 2.3 For each BUILD widget (only if chosen): extend the relevant DTO + `DashboardService` aggregation and add a backend test; no N+1

## 3. Verification

- [ ] 3.1 Browser-verify both dashboards against the seeded/real backend: no `NaN`/blank charts, period-picker filters correctly, umlaut names render, German number/date formatting throughout
- [ ] 3.2 Confirm no live-CDN request on load (offline check); full backend test suite green
