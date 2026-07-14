## Why

The 2026-07-09 review found three query-efficiency defects in already-built code: order-list endpoints trigger N+1 queries per order (items, partner, kunde all loaded separately), the partner dashboard repeats the same N+1 pattern for commission lookups and loads a partner's entire order history just to show the latest 5, and the frontend re-downloads the full company order list (with every item and Kunde object) on every route render solely to count unseen orders. None of these change what the app shows the user — only how expensively it computes it — so this change alters no observable behavior.

## What Changes

- `OrderService.listAll()` and `listForPartner(Partner)` are rewritten to a single `join fetch` query (items, partner, kunde) instead of triggering a separate query per order for each association. One query total instead of `1 + N + N + N`.
- `DashboardService.buildForPartner` uses the existing batch `commissionService.findForOrders(...)` (already used by `buildForCompany`) instead of calling `findForOrder` once per fulfilled order and again per latest order. The latest-orders lookup is changed from "load all of the partner's orders, then `.limit(5)` in a Java stream" to a DB-side limit so only 5 rows are ever fetched.
- A new lightweight endpoint `GET /api/company/orders/unseen-count` (role `company`) returns just the unseen-order count. `refreshOrdersBadge()` in `frontend/src/lib/router.js` calls this instead of fetching and counting the entire `/company/orders` payload on every route render.

Decided: **join fetch only, not backend pagination** — no change to the order-list API contract; the frontend keeps its existing fetch-once-paginate-client-side pattern (`data-table.js`). The one new endpoint is purely additive (a count, not a behavior change to unseen-order tracking itself).

No new dependencies. No user-facing text changes.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `order-notifications`: the unseen-order count becomes available via a dedicated endpoint instead of only being derivable from the full order list. This is the one genuinely new piece of API surface in this change; the join-fetch and dashboard-batch fixes alter no observable behavior and have no spec delta.

## Impact

- Backend: `OrderService.java` (`listAll`, `listForPartner`), `DashboardService.java` (`buildForPartner`), new endpoint in `OrderResource`/`CompanyOrderResource` (wherever `/company/orders` is currently exposed) for the unseen count.
- Frontend: `frontend/src/lib/router.js` (`refreshOrdersBadge`).
- No schema changes, no new dependencies, no API contract changes to existing endpoints.
