## Why

Order and shipping confirmations are only simulated in-app (FR-17, B-04 `STUB`). The client asked for automated confirmations, and Chris confirmed the recipients on 25.08.2026: the end customer if an address exists, the responsible partner and the company. Real sending needs SMTP access from the company (Herr Bachl); the application part can be built and tested before that.

## What Changes

- New capability `order-emails`: real e-mails via the Quarkus mailer (SMTP), written to an outbox in the same transaction as the order or status change and sent by a scheduled job with retries.
- Order created → confirmation to the end customer (if e-mail present), notice to the partner (`Partner.email`), notice to the company (configured address).
- Order set to `fulfilled` → shipping confirmation to the end customer (if e-mail present).
- Duplicate prevention per order, mail type and recipient role; delivery status (pending, sent, failed, skipped) visible to the company per order, with manual retry for failed mails.
- Sending is off by default (`papo.mail.enabled=false`): mails are recorded as "nicht versendet" and nothing leaves the system. A redirect option sends every mail to one test inbox for the demo/staging VM.
- The simulated e-mail blocks in the UI are replaced by a short delivery notice.
- Dev: Mailpit container in `docker-compose.yml`; tests: Quarkus `MockMailbox`.
- Not included: e-mail templates editable by users, HTML design, attachments/PDF, inquiry mails (B-05), shop mails (B-01), personal partner-staff recipients.

## Capabilities

### New Capabilities

- `order-emails`: recipients, outbox, retries, duplicate prevention, enable/redirect configuration, privacy and status view.

### Modified Capabilities

- `order-management`: "Shipping confirmation on fulfilment" and "Order confirmation" send real mails instead of simulating them.
- `order-document-print`: the excluded on-screen block is the e-mail notice.
- `partner-order-cart`: the confirmation shows the e-mail notice instead of a simulated e-mail.

## Impact

Backend: `quarkus-mailer` and `quarkus-scheduler` (official Quarkus extensions), new `OrderEmail` entity/table (Hibernate `update`, additive), hooks in `OrderService` (create, status), company endpoints for status/retry, config properties, tests. Frontend: partner confirmation, company order details and fulfilment dialog. Deploy: SMTP settings and the company address as VM secrets/env, Mailpit only in dev. Docs: `docs/SETUP.md`, `docs/DATA_MODEL.md`, `Specification/phase2_backlog.md` (B-04). Real sending on the VM waits for SMTP data from the company.
