## 1. Order-list join fetch (backend)

- [x] 1.1 Rewrite `OrderService.listForPartner(Partner)` to a single `select distinct o from Order o join fetch o.items join fetch o.partner join fetch o.kunde where o.partner = ?1` query
- [x] 1.2 Rewrite `OrderService.listAll()` to the equivalent join-fetch query without the partner filter
- [x] 1.3 Confirm ordering is preserved (added explicit `order by o.id` matching prior default DB-natural/insertion order; frontend re-sorts descending itself where needed)
- [x] 1.4 Run `OrderResourceTest` to confirm no duplicate/missing orders from the join (the `distinct` clause is what prevents this) (13/13 pass)

## 2. Dashboard batch lookup + DB-side limit (backend)

- [x] 2.1 In `DashboardService.buildForPartner`, replace the per-fulfilled-order `commissionService.findForOrder(order)` calls with `commissionService.findForOrders(fulfilledOrders)` + map lookup, mirroring `buildForCompany`
- [x] 2.2 Change the latest-orders query to fetch only `LATEST_ORDERS_LIMIT` rows at the DB level (Panache `.page(0, LATEST_ORDERS_LIMIT).list()`) instead of loading all orders and slicing with `.limit()` in Java
- [x] 2.3 Fetch commissions for the (now DB-limited) latest orders via the batch method too, not per-order `findForOrder`

## 3. Unseen-count endpoint (backend + frontend)

- [x] 3.1 Add `GET /api/company/orders/unseen-count` to `CompanyOrderResource`, `@RolesAllowed("company")`, returning an integer count of orders with `seenByCompany = false`
- [x] 3.2 In `frontend/src/lib/router.js`, change `refreshOrdersBadge()` to call the new endpoint instead of fetching and counting the full `/company/orders` list
- [x] 3.3 Add a backend test for the new endpoint (company role required; returns the correct count) (`unseenCountEndpointRequiresCompanyRoleAndReturnsCorrectCount`)

## 4. Verify

- [x] 4.1 Manually confirm: company order list and partner order list still show the same orders/items/data as before (no regression from the join-fetch rewrite) — verified 2026-07-14: company 21 orders + partner 15 orders, each with populated items array (join-fetch)
- [x] 4.2 Manually confirm: partner dashboard "latest orders" still shows the correct 5 most recent orders with correct commission values — verified 2026-07-14: "Letzte Bestellungen" shows exactly the 5 newest (ids 15→11, mixed status) with commission values, matching the DTO
- [x] 4.3 Manually confirm: the sidebar unseen-order badge count still updates correctly after opening an order's details — verified 2026-07-14: opening order Nr.21 details dropped unseen-count 21→20 and the sidebar badge "Bestellungen 21"→"20"
- [x] 4.4 Run the backend test suite (65/65 pass, BUILD SUCCESS; includes the new unseen-count endpoint test)
- [x] 4.5 Run `openspec validate order-list-efficiency` (valid)
