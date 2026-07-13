## 1. Backend: lastOrderDate in the Kunde list

- [ ] 1.1 Add nullable `LocalDate lastOrderDate` to `KundeDto` (new factory/overload taking the date; keep `KundeDto.from(Kunde)` passing `null` so order details and the create response are untouched).
- [ ] 1.2 Rewrite `KundeService.listForPartner()` as one grouped JPQL query — `select k, max(o.orderDate) from Kunde k left join Order o on o.kunde = k where k.ownerPartner = :partner group by k` — mapping each tuple to a `KundeDto` with the aggregated date; no per-Kunde queries.
- [ ] 1.3 Confirm `KundeResource.list()` and the company on-behalf picker (`PartnerResource#customers`) need no signature change and both still return partner-scoped data with the new field.

## 2. Backend tests (RestAssured)

- [ ] 2.1 `lastOrderDate` populated: a partner with a Kunde that has orders returns that Kunde's most-recent `orderDate`; a Kunde with no orders returns `lastOrderDate = null`.
- [ ] 2.2 Cross-partner scoping: partner A's customer list never contains partner B's Kunden, and the dates reflect only A's orders.
- [ ] 2.3 Single-query / no-N+1: assert the customer list is served without per-Kunde order queries (query-count assertion or log check), so the efficiency-review guarantee holds.

## 3. Frontend: "Meine Endkunden" page

- [ ] 3.1 Create `pages/partner-customer-list-page.js` (skeleton like `partner-product-list-page.js`, `area--partner`): heading "Meine Endkunden", `data-table` with columns Name (default-escaped), Adresse (`formatAddress`), "Letzte Bestellung" (`html: true`, cell = `formatDate(lastOrderDate)` or "Noch keine Bestellung", computed parts only); `searchKeys` on the name, `pageSize` 20, empty message "Noch keine Endkunden angelegt.".
- [ ] 3.2 Add dormancy derivation with module constant `DORMANT_THRESHOLD_DAYS = 60`: per row `aktivitaet = "inaktiv"` when the last order is **strictly older than** the threshold or `lastOrderDate` is `null`, else `"aktiv"`; wire a select filter "Aktivität" with options "Länger keine Bestellung" / "Kürzlich bestellt". Compute dormancy on **whole calendar days** — diff date-only values (e.g. `Date.UTC(y,m,d)` day numbers of `lastOrderDate` vs today), never `Date.now() - Date.parse(iso)` in milliseconds — so boundary dates aren't misclassified by timezone/DST.
- [ ] 3.3 Mark dormant rows: `rowClass: "data-table__row--dormant"` plus badge `<span class="dormant-badge">Inaktiv</span>` in the date cell, both driven by the same threshold as the filter.
- [ ] 3.4 Page-local ordering: pre-sort rows ascending by `lastOrderDate` (null/never-ordered first) before assigning them to the table, so dormant Endkunden are on top by default. No clickable column sorting and no `data-table` change (generalized sorting deferred — see design.md).
- [ ] 3.5 Register the page in `lib/router.js`: route `"/partner/customers"` (role `partner`, title "Meine Endkunden"), sidebar link "Endkunden" with a `NAV_ICONS` entry (lucide `contact`) between "Meine Bestellungen" and "Produkte", and the lazy import in the `DOMContentLoaded` block.
- [ ] 3.6 Style `.data-table__row--dormant` and `.dormant-badge` in `styles/base.css`, next to the existing `.data-table__row--unseen` block, as a subtle tint/badge consistent with the design system.

## 4. Verify (dev stack, browser)

- [ ] 4.1 Start the dev stack, log in as `partner-demo`, open "Meine Endkunden": the "Letzte Bestellung" column shows correct `dd.MM.yyyy` dates matching the seeded orders; an Endkunde without orders shows "Noch keine Bestellung".
- [ ] 4.2 The list is ordered oldest-first by default: never-ordered Endkunden appear first, then oldest-to-newest last order (no clickable sort needed).
- [ ] 4.3 Set the "Aktivität" filter to "Länger keine Bestellung": only Endkunden strictly past the 60-day threshold plus never-ordered Endkunden remain; each shows the "Inaktiv" badge and row highlight; "Alle" restores the full list. Boundary-check with seeded orders at 59 / 60 / 61 days old — 59 and 60 are "aktiv", 61 is "inaktiv" (strict "older than 60").
- [ ] 4.4 Create/seed an Endkunde with umlauts (e.g. "Größbacher") and confirm the name renders correctly (ä/ö/ü/ß intact, no injected markup) in the table, in search, and in the dormant filter view.
- [ ] 4.5 Log in as a second partner (or the demo company on-behalf view) and confirm the first partner's Endkunden never appear on the page or in the API response; direct navigation to `#/partner/customers` as company/anonymous redirects away.
- [ ] 4.6 Regression: order creation (Endkunde picker) and company order details still work with the extended DTO; the shared `data-table` is unchanged (no sorting added); backend log shows a single SQL query for the customer list (no N+1).
