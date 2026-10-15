## Context

Public access, rate limiting, renderer, builder and legal pages exist from earlier shop changes. Decisions: `Specification/ENDKUNDENSHOP_DECISIONS.md` §6.

## Goals / Non-Goals

**Goals:** simple, safe inquiry path to the partner.

**Non-Goals:** replies from PAPO, e-mail notifications (B-04), converting inquiries into orders, company access.

## Decisions

1. **`ShopInquiry` entity (shop, name, email, phone, message, articleId, createdAt, readAt, doneAt)**; partner access via `CurrentPartner` ownership check.
2. **Minimum fill time via signed render timestamp** delivered with the form (HMAC with server secret) to avoid trusting client clocks.
3. **Unread count endpoint** analogous to company unseen-order count (`order-notifications`).

## Risks / Trade-offs

- [Personal data without retention rule] → partner deletes; retention to be specified with the company, not invented.

## Migration Plan

Additive table.

## Open Questions

- Retention period for inquiries (company/legal).
