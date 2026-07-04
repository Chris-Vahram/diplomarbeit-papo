## 1. Backend

- [x] 1.1 `CompanySettlementDto` (partner id, name, fulfilled order count, revenue, total commission) + `CompanyDashboardDto` (total revenue, total commission, fulfilled-order count, partner count, settlement list)
- [x] 1.2 `DashboardService.buildForCompany()`: two list queries (fulfilled orders, commission records) + `Map<orderId, CommissionRecord>` + in-memory group-by partner; zero rows for partners without fulfilled orders; fulfilled-only convention
- [x] 1.3 `GET /api/company/dashboard` (`@RolesAllowed("company")`) returning the combined DTO
- [x] 1.4 Tests: totals/settlement count only fulfilled orders (submitted/reviewed excluded); zero-row partner present; partner role gets 403; query count flat regardless of partner count (no N+1)

## 2. Frontend

- [x] 2.1 Rewrite `company-dashboard-page.js`: fetch `/company/dashboard`, metric cards (Umsatz, Provision, abgeschlossene Bestellungen, Partner) mirroring `partner-dashboard-page.js`
- [x] 2.2 Settlement `data-table` below the metrics (Partner, Abgeschlossene Bestellungen, Umsatz €, Provision €); if `add-list-search-pagination` is merged, set `.searchKeys=["partnerName"]` + `.pageSize`

## 3. Verification

- [x] 3.1 Browser-verify against seeded demo data: totals match fulfilled orders only; settlement rows correct incl. zero-row partner; umlaut partner names render correctly
- [x] 3.2 Full backend test suite green
