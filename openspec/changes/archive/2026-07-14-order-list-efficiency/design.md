## Context

`order.items` is `@OneToMany` (LAZY by Hibernate default); `order.partner`/`order.kunde` are `@ManyToOne` (EAGER by default, but without `join fetch` Hibernate still issues one query per association per row rather than a single joined query). `OrderDto.from(order, commission)` touches `order.partner.id/name`, `order.kunde`, and `order.items` for every order, so `listAll()`/`listForPartner()` cost `1 + 3N` queries for N orders today. `DashboardService.buildForPartner` separately calls the single-order `commissionService.findForOrder(order)` in two places even though the batch `findForOrders(List<Order>)` already exists and is used in `buildForCompany`. `refreshOrdersBadge()` (`router.js`) runs on every route change and re-fetches all of `/company/orders` (full item/Kunde payload) just to count `!seenByCompany`.

## Goals / Non-Goals

**Goals:**
- Reduce order-list queries from `1 + 3N` to 1.
- Reuse the existing batch commission lookup in the dashboard; fetch only 5 rows for "latest orders," not all of them.
- Stop re-downloading the full order list on every route render just to show a badge count.

**Non-Goals:**
- No backend pagination for order-list endpoints (explicit decision — see proposal.md). The existing client-side pagination (`data-table.js`) already handles display; the problem was query cost per row, not payload size at the client.
- No change to what data any endpoint returns, only how cheaply it's computed. This is why the Capabilities section in proposal.md is empty — no spec delta needed for a change with no observable behavior difference.
- No caching layer, no new dependency (e.g. no Caffeine/Redis) — out of proportion for this MVP's scale (~200 partners).

## Decisions

- **`join fetch` with `distinct`.** `Order.find("select distinct o from Order o join fetch o.items join fetch o.partner join fetch o.kunde where o.partner = ?1 order by o.id desc", partner).list()` (and the equivalent without the `where` for `listAll`). Joining a `@OneToMany` collection produces one result row per item, so `distinct` on the entity is required to collapse duplicate `Order` rows back to one per order — standard JPA pattern for this exact case. Panache's `Order.find(...)` accepts a full HQL query (recognized by the leading `select`), so this doesn't require dropping to a raw `EntityManager` query.
- **Batch commission lookup in `buildForPartner`.** Replace both `commissionService.findForOrder(order)` call sites with a single `commissionService.findForOrders(...)` map lookup, mirroring `buildForCompany`'s existing pattern exactly.
- **DB-side limit for latest orders.** Panache's `.page(0, LATEST_ORDERS_LIMIT).list()` (or `firstResult`/`maxResults` equivalent) instead of loading the full ordered list and slicing in Java.
- **New `GET /api/company/orders/unseen-count` endpoint** returning a plain integer count (`Order.count("seenByCompany = false")` or equivalent), `@RolesAllowed("company")`. `refreshOrdersBadge()` calls this instead of `/company/orders`. Alternative considered: keep fetching the full list but rely on (1)'s join-fetch fix to make it cheaper — rejected, because the endpoint is called on *every* route render regardless of whether the company is looking at orders at all; even a cheap full-list fetch is wasted work compared to a dedicated count.

## Risks / Trade-offs

- [`join fetch` on a collection + `distinct` is a well-known JPA pattern but easy to get subtly wrong (e.g. forgetting `distinct` reintroduces duplicate parent objects)] → Covered by the existing `OrderResourceTest` suite, which already asserts on returned order counts/contents; a forgotten `distinct` would surface as duplicated orders in those tests.
- [A dedicated unseen-count endpoint is one more endpoint to keep in sync with the "unseen" definition if that definition ever changes] → Low risk; the definition (`seenByCompany = false`) is a single boolean column, unlikely to drift.

## Migration Plan

Code-only, backward compatible, no data migration, no dependency changes. Rollback is reverting the change.
