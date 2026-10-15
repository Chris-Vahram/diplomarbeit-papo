## Why

The decision of 2026-09-25 allows optional end-customer accounts so returning customers need not re-enter their data. On 2026-10-15 the team decided a separate Keycloak realm for end customers and to build accounts as the last shop stage (`Specification/ENDKUNDENSHOP_DECISIONS.md` §3). Guest checkout stays available.

## What Changes

- Separate Keycloak realm `papo-shop` (same Keycloak instance) with a public client for the shop entry using Authorization Code + PKCE; self-registration, e-mail verification and password reset by Keycloak.
- Optional login/registration in the shop; checkout prefilled from the account profile (name, address, e-mail, phone).
- "Mein Konto" in the shop: edit profile, see own orders placed in this shop with status, export own data, delete account.
- Orders by logged-in customers create the partner's `Kunde` as in guest checkout and link it to the account subject.
- Backend accepts tokens of the `papo-shop` realm only on the customer namespace; portal endpoints never accept them.
- Requires real e-mail sending (B-04) for verification and reset.

## Capabilities

### New Capabilities

- `shop-customer-accounts`: Realm separation, registration/login, profile, own orders, data export, account deletion.

### Modified Capabilities

- `shop-checkout`: Checkout can be prefilled for logged-in customers; guest checkout remains.

## Impact

Keycloak: new realm export `papo-shop` (registration, verify e-mail, reset, password policy, brute-force protection, SMTP from deployment secrets). Backend: second OIDC tenant for `/api/shop-customer/*`, `ShopCustomerAccount` profile entity, link from `Kunde`/`Order` to account subject. Frontend: shop login via redirect flow, account pages. Depends on `add-shop-guest-checkout` and B-04.
