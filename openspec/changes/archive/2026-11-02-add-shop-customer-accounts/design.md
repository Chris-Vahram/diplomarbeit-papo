## Context

Portal auth: realm `partnerportal`, ROPC direct grant from the SPA. Shop entry is separate (`shop.html`) and must not load portal tokens. Decision: separate realm `papo-shop` in the same Keycloak (`Specification/ENDKUNDENSHOP_DECISIONS.md` §3). E-mail verification needs SMTP (B-04).

## Goals / Non-Goals

**Goals:** strict identity separation; standard, secure flows; data rights for customers.

**Non-Goals:** social login, accounts shared across companies' legal contexts beyond identity, loyalty features, cross-shop order history.

## Decisions

1. **Quarkus OIDC multi-tenancy**: default tenant `partnerportal` for existing paths, tenant `papo-shop` resolved by path `/api/shop-customer/*`. Path-based resolution ensures portal endpoints never validate shop tokens.
2. **Authorization Code + PKCE redirect flow** in the shop (no ROPC), so registration, verification and reset use Keycloak pages styled with a neutral PAPO theme.
3. **`ShopCustomerAccount(sub, profile fields)`**; `Kunde.accountSub` and `Order` link via Kunde. Orders view filtered by `sub` and shop.
4. **Deletion** removes Keycloak user (admin API with a dedicated service account limited to the `papo-shop` realm) and profile; Kunde/Order records stay as business records.
5. **Brute-force detection and password policy** enabled in the realm export.

## Risks / Trade-offs

- [Account deletion vs. business record retention] → orders keep order-time data; exact retention rules must come from the company, not invented.
- [Identity is platform-wide while legal context is per company] → profile data is used only for prefilling; orders remain per partner/company.

## Migration Plan

New realm import; new tenant config. Rollback: disable login button; guest checkout unaffected.

## Open Questions

- Retention of customer data in orders after account deletion (company/legal).
- Should one account work in shops of different companies after multi-tenancy?
