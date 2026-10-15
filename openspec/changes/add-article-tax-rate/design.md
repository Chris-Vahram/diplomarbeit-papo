## Context

`Product.price` (BigDecimal, scale 2) is used as-is for `OrderItem.unitPriceSnapshot`, `Order.orderValue` and the commission basis. No VAT concept exists anywhere in backend, frontend or specs. The team decided provisionally on 2026-10-15 (`Specification/ENDKUNDENSHOP_DECISIONS.md` §2): company enters gross prices, each article has a tax rate, commission is on net. Schema is Hibernate-managed (`update` on demo/prod, no Flyway); existing migrations run as startup services (e.g. `CategoryMigrationService`).

## Goals / Non-Goals

**Goals:**
- Unambiguous gross price semantics plus per-article tax rate.
- Correct net commission basis for all new orders.
- Snapshots so later price/rate changes never alter past orders.
- Import can accept net or gross source prices.

**Non-Goals:**
- Invoices, VAT returns, reverse charge, tax-exempt customers, foreign tax rates.
- Recalculating historic commission records.

## Decisions

1. **Tax rate as `BigDecimal taxRatePercent` on `Product`, validated against {20, 13, 10, 0}.** Alternative: a `TaxRate` entity — rejected as overhead for four fixed Austrian rates; a validator constant is enough and can become an entity later.
2. **Gross is stored, net is derived.** The company thinks in shop prices; storing gross avoids rounding drift on the displayed consumer price. Net = gross / (1 + r/100), HALF_UP to 2 decimals. Alternative: store net and derive gross — rejected because the decided input is gross.
3. **Snapshot on `OrderItem.taxRatePercentSnapshot`; `Order` stores `orderValue` (gross, unchanged meaning for existing code), `netValue`, `taxValue`.** Totals are computed from unrounded line values and rounded once, so net + tax always equals gross.
4. **Commission basis = `Order.netValue`.** `CommissionRecord` already stores its basis; only the value passed changes. Historic records keep their stored basis.
5. **Startup migration `ArticleTaxMigrationService`** sets `taxRatePercent = 20` where null and backfills `netValue/taxValue` for existing orders assuming 20 % (display only; commissions untouched). Idempotent, same pattern as `CategoryMigrationService`.
6. **Import:** a mapping-level option "Preisspalte enthält: Brutto | Netto" plus optional `taxRate` field in `ProductImportDefinition`. Conversion happens before validation so the report shows the stored gross value.

## Risks / Trade-offs

- [Dashboards mix old (gross-based) and new commission values] → Document in release notes; demo data only, no real customers yet.
- [Company later decides prices are net] → Only the input/display layer and migration assumption change; snapshots carry the rate, so derivation stays correct.
- [Rounding disputes] → Single rounding rule (HALF_UP, per total) covered by unit tests with edge values (0.01, 0.005 boundaries).

## Migration Plan

Deploy via normal pipeline; Hibernate adds nullable columns, the migration service fills defaults on first start. Rollback: previous image ignores the new columns; no destructive change.

## Open Questions

- Company confirmation of gross input and net commission basis (pending meeting).
