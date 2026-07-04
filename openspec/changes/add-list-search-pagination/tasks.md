## 1. Component

- [x] 1.1 `data-table.js`: optional `.searchKeys` (array of data-field keys) + `.pageSize` (number); search input + pager ("Seite X von Y", German labels) rendered when set; filter = case-insensitive substring on raw values at `.searchKeys` (never rendered HTML); search resets to page 1
- [x] 1.2 Regression check: unset properties = identical rendering to before (partner dashboard, both order lists); delegated edit/status/checkbox listeners still fire after search/page re-render

## 2. Wire into lists

- [x] 2.1 `company-product-list-page.js`: `.searchKeys = ["name"]`, `.pageSize`
- [x] 2.2 `company-partner-list-page.js`: `.searchKeys = ["name"]`, `.pageSize` (partner number optional second key)
- [x] 2.3 `partner-product-list-page.js` (enabled products): `.searchKeys = ["name"]`, `.pageSize`

## 3. Verification

- [x] 3.1 Browser-verify with seeded + imported bulk data: search narrows correctly incl. umlauts ("müller" finds "Müller"), paging navigates, CRUD/enablement flows on filtered pages still work
- [x] 3.2 Backend test suite still green (no backend changes expected — sanity run)
