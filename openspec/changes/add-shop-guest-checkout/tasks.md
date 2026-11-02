## 1. Backend (essential)

- [x] 1.1 Add `Order.origin` and `Kunde.origin` with migration defaults
- [x] 1.2 Extract shared order-creation core from `OrderService`; keep internal behaviour unchanged (existing tests green)
- [x] 1.3 `ShopOrderService` + `POST /api/shop/{kennung}/orders` (server prices, enablement, availability, quantity bound)
- [x] 1.4 Idempotency table and 24 h cleanup; honeypot; rate limit
- [x] 1.5 Tests: manipulated price, foreign article, unreachable shop, double submit, honeypot, commission created

## 2. Shop frontend (essential)

- [x] 2.1 Cart (localStorage per shop), quantity edit, unavailable lines, gross totals and USt per rate
- [x] 2.2 Checkout form with validation, payment note, "Zahlungspflichtig bestellen"
- [x] 2.3 Confirmation page and printable receipt

## 3. Portal (essential)

- [ ] 3.1 Origin badge and filter in partner and company order lists; origin in customer list

## 4. Verification

- [ ] 4.1 Tests + package; `git diff --check`
- [ ] 4.2 Browser end-to-end desktop/mobile: shop order → partner sees it → company advances status
- [ ] 4.3 Update traceability and B-01 status
