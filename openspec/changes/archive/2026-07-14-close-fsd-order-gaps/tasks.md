## 1. Inactive-partner guard (backend)

- [x] 1.1 In `OrderService.createForPartnerId`, check the target partner's `active` flag before creating the order
- [x] 1.2 When the partner is inactive, throw the project's `ApiException` with a German message; ensure no order and no commission record are created
- [x] 1.3 Add/extend a backend test: company manual order for an inactive partner is rejected; for an active partner it succeeds (`companyCannotCreateManualOrderForInactivePartner`, existing `companyCreatesOrderForPartner` already covers the active case)

## 2. Shipping confirmation on fulfilment (frontend)

- [x] 2.1 In `company-order-list-page.js`, on a successful status change to `fulfilled`, render a simulated shipping confirmation ("Versandbestätigung"), reusing the existing confirmation/simulated-email presentation (`renderOrderConfirmationHtml(order, { kind: "shipping" })`)
- [x] 2.2 Label it clearly as a shipping confirmation, simulated / not sent, distinct from the order confirmation (own heading, own simulated-email heading/subject/body)
- [x] 2.3 Confirm no confirmation is shown for the submitted→reviewed transition (only on fulfilled) (`if (status === "FULFILLED")` guard)

## 3. Spec sync

- [x] 3.1 Confirm the order-management delta requirements (shipping confirmation, inactive-partner guard) match the implemented behavior (both scenarios match: `kind: "shipping"` confirmation on fulfilled; 409 rejection on inactive-partner manual order)
- [x] 3.2 Correct the divergence noted in the review (task 4.4 in build-partnerportal-mvp) so spec and code agree — left `build-partnerportal-mvp/tasks.md` 4.4 unedited (accurate historical record: it only ever built the order confirmation); the shipping-confirmation half is now recorded as done here, in this change, closing the gap without rewriting history

## 4. Verify

- [x] 4.1 Manually confirm: advancing an order to fulfilled shows the simulated shipping confirmation in-app; no real send — verified 2026-07-14 (Playwright): order→Abgeschlossen popped "Versandbestätigung für Bestellung Nr. 19" with "Simulierte Versandbestätigung (nicht wirklich versendet)"
- [x] 4.2 Manually confirm: company cannot create a manual order for a deactivated partner (German error, nothing persisted) — verified 2026-07-14: deactivated partner 3 → POST /api/company/orders returned 409 "Partner ist inaktiv und kann keine neuen Bestellungen erhalten."; order count 21→21 (nothing persisted)
- [x] 4.3 Run the backend test suite (65/65 pass, BUILD SUCCESS; includes the new inactive-partner test)
- [x] 4.4 Run `openspec validate close-fsd-order-gaps` (valid)
