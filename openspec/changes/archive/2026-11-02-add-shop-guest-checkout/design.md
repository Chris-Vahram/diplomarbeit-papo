## Context

`OrderService.createForPartner(Partner, OrderRequest)` already enforces enablement, availability, quantity bounds, unit-price snapshot and commission creation; `Kunde` is required on every order and scoped to a partner. Public access and rate limiting exist since `add-partner-shop-foundation`. Decisions: `Specification/ENDKUNDENSHOP_DECISIONS.md` §2.

## Goals / Non-Goals

**Goals:** a safe public order path that reuses the internal order logic; clear origin markers.

**Non-Goals:** payment, accounts, real mail, customer merge UI, stock reservation.

## Decisions

1. **New `ShopOrderService` building a `Kunde` (origin SHOP) and delegating to the shared order-creation core** extracted from `createForPartner`, so validation, snapshots and commission are identical. Alternative: separate implementation — rejected to avoid divergence.
2. **Idempotency table `shop_order_request(key, shop_id, order_id, created_at)`**, unique (shop_id, key), entries older than 24 h cleaned by a scheduled job. Client generates a UUID per checkout attempt.
3. **Cart in `localStorage` keyed by shop identifier**, wrapped in try/catch; only article ids and quantities are stored (no personal data), which keeps it "technically necessary" for the consent change.
4. **Honeypot plus rate limit; no CAPTCHA** (decided: no external services).
5. **Origin enums with migration defaults** via idempotent startup service.

## Risks / Trade-offs

- [Spam orders reach the company] → company reviews every order (status reviewed) before fulfilment; rate limit, honeypot.
- [Duplicate Kunde records] → accepted by decision; merge later.
- [Legal publication before legal texts exist] → shops must not be released in production before `add-shop-legal-consent`; release action shows a warning while legal texts are placeholders.

## Migration Plan

Additive columns with defaults; rollback leaves extra columns unused.

## Open Questions

- Payment terms text final wording (company).
