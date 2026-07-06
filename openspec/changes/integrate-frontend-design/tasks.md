## 1. Demo-safety + hygiene (MVP-critical)

- [x] 1.1 Vendor + pin Lucide: replace `https://unpkg.com/lucide@latest` in `index.html` with a locally committed, version-pinned copy (or inline the ~20 SVGs actually used); confirm icons render with no network
- [x] 1.2 Remove the "ggü. Vorperiode" trend from both dashboards' metric cards (defer → B-08); drop the now-unused `dashboard.previous` reads and any dangling `base.css`/`metric-card` trend rules
- [x] 1.3 Sweep both redesigned dashboards + `partner-order-list-page.js` for hardcoded `toFixed`/ISO dates → route through `lib/format.js` (de-AT)
- [x] 1.4 Verify Tobias's period-picker actually filters totals/lists (Von/Bis inclusive, reset restores all-time); fix if it only styles

## 2. Wire the kept charts client-side (keeps Tobias's visuals, no backend change)

- [x] 2.1 Company dashboard: fetch `/company/orders`; compute monthly revenue/orders buckets (by `orderDate`) + Bestellstatus-Donut (count by `status`) from it; feed the existing chart markup
- [x] 2.2 Partner dashboard: fetch `/partner/orders`; compute monthly chart, status donut, and Top-Kunden (group own orders by Kunde, sum `orderValue`, top N) from it
- [x] 2.3 Products-ring pick: either drop the enabled-vs-total ratio, or add a trivial `totalProductCount` (`Product.count()`) to `PartnerDashboardDto` + `DashboardService` and keep the ring
- [x] 2.4 Confirm no dashboard reads a non-existent DTO field anymore (grep `monthLabels|monthlyRevenue|monthlyOrderCount|previous|topClients|totalProductCount`)

## 3. Demo data

- [x] 3.1 `DemoDataSeeder`: seed ~30 orders across the seeded partners + several Kunden, spread over recent months, mixed statuses (enough fulfilled to populate settlement/charts); dev/test only (existing `LaunchMode.NORMAL` guard); reuse `OrderService`/commission creation so `CommissionRecord`s exist

## 4. Verification

- [x] 4.1 Browser-verify both dashboards against the seeded backend: no `NaN`/blank; monthly chart + status donut + Top-Kunden populated; period-picker filters; umlaut names + de-AT formatting throughout
- [x] 4.2 Offline check: no live-CDN request on load; full backend test suite green (add a test only if 2.3 adds `totalProductCount`)

## 5. Bookkeeping

- [x] 5.1 Archive `add-dashboard-date-filter` as superseded by the period-picker (do not implement its tasks)
