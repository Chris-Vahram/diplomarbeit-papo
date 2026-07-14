## 1. Double-submit guard (frontend)

- [x] 1.1 In `partner-dashboard-page.js`'s order-submit handler, disable the submit button before the request and re-enable it in the catch block (mirror `company-order-list-page.js#handleManualSubmit`)
- [x] 1.2 Apply the same guard in `partner-order-list-page.js`'s order-submit handler
- [x] 1.3 Confirm the button re-enables on a rejected/failed submit (error path still lets the partner retry) (both catch blocks re-enable `submitButton`)

## 2. Order item quantity upper bound (backend)

- [x] 2.1 Add `@Max(10000)` (with a German validation message) to `OrderItemRequest.quantity`
- [x] 2.2 Add/extend a backend test: an order item quantity above the max is rejected with a validation error, no order created (`partnerOrderWithExcessiveQuantityIsRejected`)

## 3. Case-insensitive partner-number dedup (backend)

- [x] 3.1 In `PartnerService.assertPartnerNumberFree`, compare partner numbers case-insensitively, matching `PartnerImportDefinition`'s `lower(...)` check
- [x] 3.2 Add/extend a backend test: creating/updating a partner with an identifier differing only in case from an existing one is rejected as a duplicate (`companyCreateRejectsDuplicatePartnerNumberDifferingOnlyByCase`)

## 4. XLSX header parsing fix (backend)

- [x] 4.1 In `ImportedFileParser.parseXlsx`, replace the `for (Cell cell : headerRow)` loop with an index loop over `0..headerRow.getLastCellNum()-1` using `getCell(i, CREATE_NULL_AS_BLANK)`, matching how data rows are read
- [x] 4.2 Add/extend a backend test: a blank header cell mid-row does not shift later columns' values (`xlsxWithBlankHeaderCellDoesNotShiftLaterColumns`, builds a real XLSX via POI)

## 5. Dashboard headline totals (frontend)

- [x] 5.1 In `lib/dashboard-data.js`, change `fulfilledTotals()` to sum `orderValue`/`commissionValue` in integer cents and divide by 100 at the end, instead of naive float `reduce`
- [x] 5.2 In `partner-dashboard-page.js`, when no explicit Von/Bis range is set, source the headline KPI cards (commission, revenue, fulfilled-order count) directly from the already-fetched `/api/partner/dashboard` response instead of `defaultPeriod()` + client-side recompute (`this.period = null` is now the default/reset state; `defaultPeriod()` removed as dead code)
- [x] 5.3 When an explicit range is set, keep computing from the range-filtered order list (via the fixed `fulfilledTotals()`), unchanged UI behavior otherwise
- [x] 5.4 Apply the equivalent fix in `company-dashboard-page.js` against `/api/company/dashboard`
- [x] 5.5 Confirm the month chart and "previous period" trend arrows are unaffected (still trailing-window based, per design.md Non-Goals) (`trailingMonths(6)`/`monthlyRevenue(this.orders, keys)` still operate on unfiltered `this.orders`, confirmed by grep)

## 6. Verify

- [x] 6.1 Manually confirm: double-clicking order-submit on both partner entry points creates exactly one order and one commission record — **bug found + FIXED 2026-07-14**: the full order-new page (`partner-order-new-page.js` handleSubmit) was missing the double-submit guard the dashboard quick-order already had; a fast double-submit created TWO orders. Added the same `submitButton.disabled=true` guard; re-verified — double-submit now creates exactly one order (button disables synchronously, second click no-ops).
- [x] 6.2 Manually confirm: partner dashboard with no range set shows the same totals as the backend's all-time DTO; setting a range scopes correctly — verified 2026-07-14: no-range UI €247,22/€2.472,20/11 == DTO; June range → €446,30/€44,63/2 (scoped, integer-cent)
- [x] 6.3 Manually confirm: company dashboard shows the same behavior — verified 2026-07-14: no-range == DTO (dynamically after a status change too); June range → €533,30/3 (scoped)
- [x] 6.4 Run the backend test suite (67/67 pass, BUILD SUCCESS; includes 3 new tests: excessive quantity, case-insensitive dedup, XLSX blank-header)
- [x] 6.5 Run `openspec validate order-correctness-hardening` (valid)
