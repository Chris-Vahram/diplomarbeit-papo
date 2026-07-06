## Context

`76bbe40 Frontend Design` is already merged on `main`. It's a large working redesign (Light-DOM components preserved), but built against an imagined richer dashboard API. Current DTOs: `CompanyDashboardDto(totalRevenue, totalCommission, fulfilledOrderCount, partnerCount, settlement)`, `PartnerDashboardDto(totalCommission, totalRevenue, fulfilledOrderCount, enabledProductCount, latestOrders)`. The order-list endpoints already return per-order `orderDate/orderValue/commissionValue/status/kunde`, so most "missing" widgets are actually client-side-derivable from a list the dashboard can fetch itself — the same pattern the superseded `add-dashboard-date-filter` used.

## Goals / Non-Goals

**Goals:**
- Demo-safe dashboards: no `NaN`/blank widget against the real backend.
- Keep Tobias's visuals, including the charts, by feeding them client-side.
- No live-CDN runtime dependency.
- Dashboards that actually show data in a demo (seeded historical orders).

**Non-Goals:**
- No redesign of the visuals (Tobias owns them).
- No new deferred-feature backend aggregation. Everything kept is computed from the existing order list.
- Not re-implementing the period-picker (adopt Tobias's).

## Decisions

**Resolved widget matrix (Chris, 2026-07-06):**

| Widget | Data source | Verdict |
|---|---|---|
| Period-picker (Von/Bis) | order list (client) | KEEP as delivered; verify it filters |
| Monthly revenue/orders chart | bucket order list by `orderDate` (client) | KEEP, wire client-side |
| Bestellstatus-Donut | count order list by `status` (client) | KEEP, wire client-side |
| Top-Kunden (partner) | group partner's orders by Kunde (client) | KEEP, wire client-side |
| Enabled-vs-total products ring | needs a catalog total | OPTIONAL: keep only if `totalProductCount` added (trivial `Product.count()`), else drop the ratio |
| "ggü. Vorperiode" trend | previous-period compare | **DEFER → B-08** (remove from cards for now) |

**Client-side aggregation, not new backend fields.** Each kept chart is computed in JS from the order list the dashboard fetches (company: `/company/orders`; partner: `/partner/orders`). Rationale: zero backend/DTO churn, no N+1, data volume is trivial at demo scale, and it matches the already-accepted client-side approach. The backend DTOs stay as they are (the only optional exception is `totalProductCount` if the products-ring is kept).

**Lucide: vendor + pin.** Replace the `unpkg.com/lucide@latest` script with a locally committed pinned build, or inline the fixed ~20-icon set. Offline-safe, reproducible — same offline-safety rule chosen for product images.

**Demo orders in `DemoDataSeeder`.** Seed ~30 orders across the seeded partners, several Kunden, spread over recent months, mixed statuses (some fulfilled). Dev/test only (existing `LaunchMode.NORMAL` guard). Makes the whole company side + charts demoable; without it the monthly chart is a single bar and settlement is all zeros.

**Supersede `add-dashboard-date-filter`.** Tobias's period-picker covers it; archive as superseded, don't implement. Any correctness gap folds into this change's verification.

**base.css kept as delivered.** Only remove rules that dangle after the Vorperiode trend (and optionally the products-ring) are stripped.

## Risks / Trade-offs

- **[Risk]** Client-side charts drift from server aggregates → **Mitigation**: the headline metric cards (revenue/commission/counts) stay server-DTO-driven; only the extra charts are client-computed, and the fulfilled-only convention is reused where relevant.
- **[Trade-off]** Seeding historical orders adds seed code → accepted; it's the single highest-value thing for the diploma demo.
- **[Trade-off]** Vendoring Lucide commits an asset → required for offline demo.

## Open Questions

None (matrix resolved). One small pick left to implementation time: keep the products-ring (add trivial `totalProductCount`) or drop its ratio.
