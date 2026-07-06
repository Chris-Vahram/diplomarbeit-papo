## Why

Tobias delivered the visual design as working code, not just tokens: commit `76bbe40 Frontend Design` (on `main`) rewrites `base.css` (+2024 lines), `metric-card`, `router`, both dashboards, and the partner order list, and adds a Lucide icon dependency. This is FD.1–FD.4 landing at once. It needs a reconciliation pass before it's demo-safe, because parts were built against a richer backend than exists — Tobias flagged "passt mim backend noch nicht ganz zam" (2026-07-05). This change makes the redesign demo-safe and keeps the good parts, without turning deferred features into unplanned backend work.

## What Changes

**1. Demo-safety (MVP-critical): no dashboard may render NaN/blank.** The redesigned dashboards read DTO fields the backend does not return (`monthLabels`, `monthlyRevenue`, `monthlyOrderCount`, `previous`, `topClients`, `totalProductCount`) → `undefined`/`NaN` at runtime. Every widget must either be fed real data or removed.

**2. Keep the cheap widgets by feeding them client-side.** The order-list endpoints (`/company/orders`, `/partner/orders`) already return per-order `orderDate/orderValue/commissionValue/status/kunde` — enough to compute, in JS, without any backend change: the **period-picker** (already works), a **monthly revenue/orders chart** (bucket by `orderDate`), a **Bestellstatus-Donut** (count by status), and **Top-Kunden** (group the partner's own orders by Kunde). These keep Tobias's visuals at near-zero cost.

**3. Kill the external CDN.** `index.html` loads Lucide from `https://unpkg.com/lucide@latest` — external and **unpinned**. Vendor a pinned copy locally (or inline the ~20 SVGs used) so the demo survives offline and is reproducible.

**4. Seed historical demo orders.** `DemoDataSeeder` seeds zero orders today, and live orders all get today's date — so the whole company side (order list, settlement, dashboards, charts) is empty/flat in a demo. Seed ~30 orders spread across months, Kunden, and partners (dev/test only, same guard as existing seed) so the app demos alive. Independently valuable beyond the charts.

**5. Formatting + picker hygiene.** Route any hardcoded `toFixed`/ISO dates in the redesigned pages through `lib/format.js` (de-AT); verify the period-picker actually filters (not just styles).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `company-dashboard`: dashboard UI reconciled with backend; period filtering; client-side charts.
- `partner-dashboard`: same.

## Impact

- Frontend: `index.html` (Lucide), both dashboard pages, possibly `metric-card`/`base.css` (drop rules for anything removed). Backend: `DemoDataSeeder` (historical orders); a trivial `totalProductCount`/`Product.count()` only if the products-ring is kept (optional).
- **Supersedes `add-dashboard-date-filter`** (Tobias's period-picker replaces it) — that change should be archived as superseded, not implemented.
- **Deferred, not in this change:** the "ggü. Vorperiode" trend indicators → logged as **B-08** in `phase2_backlog.md` (needs period-compare logic + meaningful history; marginal for an internal tool).

## Decision status

Resolved with Chris (2026-07-06): keep period-picker + client-side monthly chart / status donut / Top-Kunden, vendor+pin Lucide, seed demo orders, defer only the Vorperiode trend (B-08). No open questions block implementation. Implementation deferred until Chris says go.
