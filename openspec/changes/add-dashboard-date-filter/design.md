## Context

Dashboards render server-aggregated DTOs (`/company/dashboard`, `/partner/dashboard`) with all-time, fulfilled-only totals. The order-list endpoints (`/company/orders`, `/partner/orders`) return every order with `orderDate` (ISO string), `orderValue`, `commissionValue`, `partnerId`/`partnerName`, `status` — enough to recompute every date-dependent number client-side. Orders carry no status-change timestamps, only `orderDate`.

## Goals / Non-Goals

**Goals:**
- Von/Bis range on both dashboards; filtered numbers consistent with the fulfilled-only convention.
- Default (no range) shows the server aggregates unchanged — zero behavior change until the user filters.
- No backend work.

**Non-Goals:**
- No "ggü. Vorperiode" trends, Top-Kunden, Bestellstatus-Pie (data doesn't exist; still parked).
- No persistence of the chosen range (resets on navigation).
- No CSV-export filtering (exports stay all-time).
- No custom calendar widget — native `<input type="date">`; visual polish is FD.3's job.

## Decisions

**Client-side aggregation over new backend query params.** Tobi's observation, Chris confirmed: the raw per-order data is already delivered to these roles by existing endpoints, so `?from=&to=` server params would duplicate aggregation logic for zero data-volume benefit at this scale (~hundreds of orders). The company dashboard already loads all orders elsewhere (order list page); the partner sees only their own.

**Shared `<date-range-filter>` component (Light DOM, like all components).** Two `<input type="date">` (Von/Bis) + "Zurücksetzen", emits a `range-change` event with `{ from, to }` (ISO strings or null). Used identically on both dashboards; two hand-rolled copies would drift.

**Filtering = ISO string comparison.** `orderDate` is `YYYY-MM-DD`; lexicographic compare equals chronological compare, inclusive bounds, open side = no bound. No Date parsing, no timezone edge cases.

**Data flow per dashboard:**
- *Company:* keep `/company/dashboard` for partner names/count and the unfiltered view; additionally fetch `/company/orders` once on load. Range active → totals + per-partner settlement recomputed from fulfilled orders in range; partners without matching orders keep zero rows (names from the settlement list). Partner-count card never changes.
- *Partner:* keep `/partner/dashboard` for enabled-product count and the unfiltered view; additionally fetch `/partner/orders` once. Range active → commission/revenue/fulfilled-count from fulfilled orders in range; "Letzte Bestellungen" shows the 5 newest orders (any status) within range, matching the existing any-status convention for that list.

**Semantics documented in UI-adjacent comment:** an order counts by `orderDate`. A "fulfilled orders in range" figure means fulfilled-now orders ordered within the range — the only semantic the data supports (no fulfillment timestamp). Acceptable for a reporting prototype; a fulfillment timestamp would be a backend/Phase-2 addition if the client ever needs it.

## Risks / Trade-offs

- **[Risk]** Filtered client-side numbers could diverge from server aggregates → **Mitigation**: same fulfilled-only rule, and the default view still uses the server numbers; browser-verify compares an unbounded range against the server view (must be identical).
- **[Trade-off]** Company dashboard now fetches all orders on load (second request) → negligible at demo scale, same payload the order-list page already loads.
- **[Trade-off]** `orderDate`-based counting can surprise if an old order gets fulfilled later → documented; no alternative without new backend data.
