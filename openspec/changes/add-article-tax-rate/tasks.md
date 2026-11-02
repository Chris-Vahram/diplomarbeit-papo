## 1. Data model and migration (essential)

- [x] 1.1 Add `taxRatePercent` to `Product` with allowed-set validation and default 20; extend `ProductRequest`/`ProductDto`
- [x] 1.2 Add `taxRatePercentSnapshot` to `OrderItem`; add `netValue` and `taxValue` to `Order`
- [x] 1.3 Implement shared net/tax derivation helper (BigDecimal, HALF_UP) with unit tests incl. 0 % and rounding edges
- [x] 1.4 Implement idempotent `ArticleTaxMigrationService` (default rate, backfill order net/tax) with test

## 2. Orders and commission (essential)

- [x] 2.1 `OrderService` snapshots tax rate and stores gross/net/tax totals
- [x] 2.2 `CommissionService` uses net order value as basis; test "120 gross / 20 % / 10 % → 10.00"
- [x] 2.3 Test that price/rate changes after ordering leave orders and commissions unchanged

## 3. Import (essential)

- [x] 3.1 Add optional tax-rate field and net/gross price-column option to article import mapping
- [x] 3.2 Convert net to gross before validation; reject invalid rates with report reason; tests

## 4. Frontend (essential)

- [x] 4.1 Company article form: tax-rate select (20/13/10/0), label price as "Bruttopreis (inkl. USt)"
- [x] 4.2 Partner catalog, cart, order details and print document: gross with net below
- [x] 4.3 Import page: price-column net/gross choice
- [x] 4.4 Order/commission CSV export includes net and tax columns
- [x] 4.5 Public shop catalogue: replace `PublicArticleDto.DEFAULT_TAX_RATE_PERCENT` with the article's `taxRatePercent`; adjust `PublicShopContentTest` (interim state from `add-shop-page-renderer`)

## 5. Verification

- [ ] 5.1 Backend tests and package green; `git diff --check`
- [x] 5.2 Browser check desktop/mobile of catalog, cart, article form
- [ ] 5.3 Update `REQUIREMENTS_TRACEABILITY.md` and data-model documentation
