## Why

The core of B-01: an end customer orders directly in a partner's shop without login, and the order enters the existing pipeline assigned to that partner (Full-Vision UC-A; decisions of 2026-10-15 in `Specification/ENDKUNDENSHOP_DECISIONS.md` §2: guest order, no online payment, new Kunde with origin "Shop", e-mail mandatory in the shop, order origin field, B-04 not a prerequisite).

## What Changes

- Cart in the public shop (browser storage, per shop), quantity changes, gross totals incl. USt.
- Checkout form: Nachname, address and e-mail mandatory; first name and phone optional; payment placeholder text ("Die Rechnung erhalten Sie mit der Lieferung."); button "Zahlungspflichtig bestellen".
- Public order endpoint: partner resolved from the shop identifier; prices, tax rates and availability re-read server-side; client prices ignored; idempotency key against double submission; rate limit; honeypot field.
- Each shop order creates a new `Kunde` owned by the shop's partner with origin "Shop"; no matching with existing customers.
- `Order` gets an origin (`INTERNAL`, `SHOP`); same status pipeline and commission as internal orders.
- Confirmation page with order number, summary and printable receipt; no real e-mail until B-04 exists.
- Partner and company order lists/details show the origin "Shop"; partner customer list shows origin.
- Not included: online payment, customer accounts (`add-shop-customer-accounts`), real e-mails (B-04), legal page texts and consent (`add-shop-legal-consent`, must be done before real publication).

## Capabilities

### New Capabilities

- `shop-checkout`: Cart, checkout form, public order submission, abuse protection, confirmation.

### Modified Capabilities

- `order-management`: Orders carry an origin; shop orders are assigned to the shop's partner.
- `customer-management`: Customers carry an origin; shop orders create new customers.

## Impact

Backend: public `POST /api/shop/{kennung}/orders`, `OrderService` entry for shop orders reusing validation/snapshot/commission, `Order.origin`, `Kunde.origin`, idempotency table, migration defaulting existing rows to `INTERNAL`. Frontend: shop cart, checkout, confirmation, print view; origin badges in portal lists. Depends on `add-article-tax-rate`, `add-shop-page-renderer`.
