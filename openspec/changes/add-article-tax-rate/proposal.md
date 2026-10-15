## Why

`Product.price` has no defined tax meaning today; the codebase contains no VAT concept at all. The end-customer shop (B-01) must show consumer prices including USt, and the team decided on 2026-10-15 (provisional, see `Specification/ENDKUNDENSHOP_DECISIONS.md` §2) that the company enters **gross** prices and that commission is calculated on the **net** value. This change is the prerequisite for `add-shop-guest-checkout`.

## What Changes

- Define `Product.price` as the gross unit price (incl. USt) entered by the company.
- Add a per-article USt rate (`taxRatePercent`), default 20 %, selectable 20 / 13 / 10 / 0 %.
- Snapshot the tax rate on each `OrderItem` next to the existing unit-price snapshot; derive and store net and tax amounts per order.
- **BREAKING (semantics):** Commission is calculated as `commissionPercent` of the **net** order value instead of the (now gross) total. Existing `CommissionRecord`s are not recalculated.
- Partner and company portal show gross prices with the net value in small type below.
- Article import mapping lets the company declare the price column as net or gross; net prices are converted using the row's or default tax rate.
- Existing demo prices are interpreted as gross with 20 %.
- Not included: invoices, VAT reporting, per-customer tax exemptions, reverse charge.

## Capabilities

### New Capabilities

- `article-tax`: Per-article USt rate, gross price semantics and net/tax derivation rules.

### Modified Capabilities

- `product-management`: Products store a gross price and a tax rate.
- `commission`: Commission basis becomes the net order value.
- `order-management`: Order items snapshot the tax rate; orders store net and tax totals.
- `data-import`: Article import supports a net/gross price-column choice and an optional tax-rate column.

## Impact

Backend: `Product`, `ProductRequest/Dto`, `OrderItem`, `Order`, `OrderService`, `CommissionService`, `ProductImportDefinition`, `ImportService`, `DemoDataSeeder`, CSV export of orders/commissions. Frontend: company product form/list, partner catalog/cart, order details, print document, dashboards (revenue labels). Schema: new nullable-then-defaulted columns via Hibernate `update` on demo/prod; migration sets `taxRatePercent = 20` for existing rows. Provisional decision; may change after the company meeting.
