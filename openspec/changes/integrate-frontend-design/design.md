## Context

`76bbe40 Frontend Design` is already on `main`. It is a large, working redesign (Light-DOM components preserved, as agreed), but built against an imagined richer dashboard API. The current backend DTOs are `CompanyDashboardDto(totalRevenue, totalCommission, fulfilledOrderCount, partnerCount, settlement)` and `PartnerDashboardDto(totalCommission, totalRevenue, fulfilledOrderCount, enabledProductCount, latestOrders)`. The order-list endpoints already return per-order `orderDate/orderValue/commissionValue/status`, so some widgets are client-side-derivable; others genuinely need new backend aggregation.

## Goals / Non-Goals

**Goals:**
- Demo-safe dashboards: nothing renders `NaN`/blank against the real backend.
- Keep Tobias's visual design and the period-picker.
- No new external runtime dependency on a live CDN.
- Reuse `lib/format.js` (de-AT) rather than re-hardcoded formatting.

**Non-Goals:**
- No redesign of the design — Tobias owns the visuals; this is reconciliation.
- No new deferred-feature backend work unless a widget is explicitly chosen BUILD.
- Not re-implementing the period-picker (adopt Tobias's).

## Decisions

**Per-widget CUT/BUILD matrix (the decision this change exists to record).** Each row is a team call; the "lean" column is the recommendation.

| Widget | Needs | Client-side possible? | Lean |
|---|---|---|---|
| Period-picker (Von/Bis) | order list (already fetched) | yes | **KEEP** |
| Bestellstatus breakdown/donut | order statuses | yes (count from orders) | **KEEP** (wire to client data) |
| "ggü. Vorperiode" trend | previous-period totals | only if client computes both periods from orders | CUT (or client-compute) |
| Top-Kunden | per-Kunde revenue aggregation | yes for partner (own orders) | CUT for MVP; optional client-side |
| Monthly revenue/order sparklines | monthly buckets | yes (bucket the order list client-side) | CUT (or client-compute) |
| Enabled-vs-total-products ring | `totalProductCount` | needs backend (partner doesn't get full catalog count) | CUT |

Note several "needs backend" widgets are actually client-side-derivable from the order list the dashboard can already fetch (same approach the superseded `add-dashboard-date-filter` used). So a middle path exists: **KEEP the widget but feed it client-side** instead of adding backend fields. The matrix should be resolved widget-by-widget with that third option in mind.

**Lucide: vendor + pin.** Replace `<script src="https://unpkg.com/lucide@latest">` with a locally committed, version-pinned Lucide (or inline the ~20 SVGs actually used — the icon set is small and fixed). No live-CDN dependency in a demo that must survive offline. Aligns with the same offline-safety rule chosen for product images.

**Supersede `add-dashboard-date-filter`.** Tobias's period-picker covers it. Archive that change as superseded; do not implement its tasks. Any correctness gap in his picker is folded into this change's verification.

**base.css kept as delivered.** The +2024-line design system is Tobias's deliverable; this change does not rewrite it, only removes rules for any widget that is CUT if they'd otherwise dangle.

## Risks / Trade-offs

- **[Risk]** Choosing BUILD for several widgets reopens deferred B-scope and adds backend work near the deadline → **Mitigation**: the matrix's lean is CUT/client-side; BUILD only by explicit team choice.
- **[Risk]** Cutting widgets Tobias built feels like wasted work → **Mitigation**: frame as MVP-now / Phase-2-later; the CSS/components stay, only the data wiring is deferred.
- **[Trade-off]** Vendoring Lucide adds a committed asset → acceptable and required for offline demo.

## Open Questions

1. **The CUT/BUILD/client-side call per widget row above — needs Chris + Tobias (Mo 2026-07-06 / at the Di client meeting).** Everything else in this change (Lucide, format reuse, period-picker verification, ensuring no `NaN`) proceeds regardless.
