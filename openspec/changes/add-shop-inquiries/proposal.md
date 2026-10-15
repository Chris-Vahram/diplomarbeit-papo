## Why

B-05: questions or quote requests from the shop shall reach the responsible partner automatically (Protokoll 17.06.: "Anfragen bei Shop: automatisch weiterleiten"; fields and protection decided 2026-10-15, `Specification/ENDKUNDENSHOP_DECISIONS.md` §6).

## What Changes

- New block "Anfrageformular" for the builder, placeable on any designable page (e.g. Kontakt).
- Public form: Name, E-Mail or Telefon (at least one), Nachricht (max. 2000), optional article reference, mandatory privacy consent.
- Spam protection without external services: honeypot, minimum fill time, rate limit.
- Inquiry assigned to the partner owning the shop; partner inbox "Anfragen" under "Mein Webshop" with unread count badge; mark as done; delete.
- Only the partner sees inquiries. No invented retention period. E-mail notification added once B-04 exists.

## Capabilities

### New Capabilities

- `shop-inquiries`: Inquiry form block, submission rules, spam protection, partner inbox.

### Modified Capabilities

None.

## Impact

Backend: `ShopInquiry` entity, public `POST /api/shop/{kennung}/inquiries`, partner inbox endpoints, unread count. Frontend: form block (renderer + builder properties), partner inbox page and badge. Depends on `add-shop-page-renderer`, `add-shop-builder-editor`, `add-shop-legal-consent` (privacy link).
