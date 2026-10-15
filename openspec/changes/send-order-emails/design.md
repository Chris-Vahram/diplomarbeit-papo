## Context

`OrderService.createForPartner`, `createForPartnerId` (company manual order) and `updateStatus` are the only places where the relevant events happen. The frontend renders "Simulierte Bestätigungs-E-Mail" (`order-form.js`) and the FR-17 shipping dialog (`company-order-list-page.js`). `DemoDataSeeder` persists orders directly via Panache, not through `OrderService`. Mail infrastructure does not exist; the company will provide SMTP access later.

## Goals / Non-Goals

**Goals:** Reliable, non-duplicated order mails to the three confirmed recipients plus shipping confirmation; safe default (nothing sent until enabled); visible status; no personal data in logs.

**Non-Goals:** Editable templates, HTML layout, attachments, bounce handling, unsubscribe, B-01/B-05 mails, mails to personal partner employees, legal retention rules.

## Decisions

- **Stack.** `quarkus-mailer` (reactive mailer awaited with a 30 s bound on a worker thread) and German plain-text bodies built in Java (`OrderEmailContent`, no template engine needed for four short texts); `quarkus-scheduler` with a blocking (non-`Uni`) `@Scheduled(every = "30s", concurrentExecution = SKIP)` method. SMTP only, TLS required for external servers.
- **Outbox entity `OrderEmail`.** `order` FK, `type` (ORDER_CONFIRMATION, ORDER_NOTICE_PARTNER, ORDER_NOTICE_COMPANY, SHIPPING_CONFIRMATION), `recipientRole` (CUSTOMER, PARTNER, COMPANY), `recipientAddress` (snapshot; null for SKIPPED rows), `deliveryMode` (LIVE or REDIRECT, fixed at creation), `status` (PENDING, SENDING, SENT, FAILED, SKIPPED), `skipReason`, `attempts`, `nextAttemptAt`, `lastErrorCode` (allowlisted: SMTP_UNAVAILABLE, AUTH_FAILED, REJECTED, TIMEOUT, OTHER — never raw exception text), `createdAt`, `sentAt`. Unique (`order`, `type`, `recipientRole`). Rows are inserted in the same transaction as the order/status change.
- **Send loop, one row at a time.** (1) short transaction: conditional update PENDING→SENDING for a due row; (2) outside any transaction: re-check config and recipient, render from current order data, send and wait for the result with a timeout; (3) own transaction: SENT, or attempts+1 with retry after 1/5/15/60 minutes, then FAILED. Rows stuck in SENDING for more than 10 minutes return to PENDING. This is at-least-once: a crash after SMTP acceptance can repeat one mail. SENT means "vom Mailserver angenommen", not "im Postfach angekommen". Single VM; `ponytail:` note for `FOR UPDATE SKIP LOCKED` with several instances.
- **Checks before every send.** Sending disabled now, or `deliveryMode` no longer matches the current configuration (redirect added/removed) → SKIPPED (`CONFIG_CHANGED`), never sent to a different destination than planned. Recipient address of the role changed since queueing (e.g. customer e-mail corrected) → SKIPPED (`RECIPIENT_CHANGED`); the company can requeue explicitly (new attempt with the current address).
- **Safety configuration.** `papo.mail.enabled` (default false): while false, rows are created as SKIPPED (`DISABLED`) without address and are never sent later. `papo.mail.redirect-to`: every mail goes to this test inbox. `papo.mail.allow-real-recipients` (default false): the application refuses to start with sending enabled and no redirect unless this is true — so the demo VM with known demo logins can only send to a test inbox. `papo.mail.shipping-enabled` (default false; enabled per environment, see Decisions by Chris).
- **Recipients.** Customer: `Kunde.email`; partner: `Partner.email` (shared organisational address in the current stage); company: `papo.mail.company-recipient`. Addresses are trimmed; blank → no row; syntactically invalid or longer than 254 characters → SKIPPED (`INVALID_ADDRESS`). New customer/partner input gets the same e-mail validation (German message), so the order request validation changes only for malformed e-mail values.
- **Content.** German plain text, sender `papo.mail.from`, signed with `CompanyProfile.businessName`. Customer mails list articles and quantities **without prices** (net/gross for end customers is an open company question). Partner and company notices include order value and commission.
- **Event notice.** Create and status-change responses carry `customerEmailNotice` derived from the rows actually created for that event: QUEUED, REDIRECTED, NO_ADDRESS, INVALID_ADDRESS, DISABLED; absent in lists and for historical/seeded orders. One shared renderer covers partner creation, company manual creation and fulfilment.
- **Status and retry (company only).** `GET /api/company/orders/{id}/emails`: type, role, masked address (`a***@example.at`), status, reason, attempts, timestamps. `POST …/emails/{emailId}/retry`: conditional FAILED or RECIPIENT_CHANGED-SKIPPED → PENDING with attempts reset and immediate eligibility; 404 for another order's mail, 409 for any other status or while sending is disabled.
- **Order status concurrency.** Status transitions become a conditional update (`where id = :id and status = :expected`), and "seen" becomes a targeted update of the seen field only, so a concurrent `markSeen` cannot overwrite `FULFILLED` and a double fulfilment creates one shipping row. Existing forward-only transition rules stay.
- **Privacy/logging.** Logs contain only row id, type, status and error code. `quarkus.log.category."quarkus-mailer".level=WARN` in all profiles (mock mode would otherwise log full mails at INFO). SMTP credentials only via env/secrets. Retention: see Decisions by Chris (90 days for failed rows' addresses).
- **Dev/test/prod.** Dev: Mailpit in `docker-compose.yml` with ports bound to `127.0.0.1`, `quarkus.mailer.mock=false`, sending enabled without redirect only in dev. Tests: `MockMailbox` plus a controllable failing test sender and injectable clock for retry/timeout cases. Prod: new env entries mapped explicitly in `docker-compose.prod.yml`, placeholders in the env example, VM sync step documented. Seeder creates no mail rows.

## Risks / Trade-offs

- [Risk] Demo data has fictitious customer addresses. Mitigation: disabled by default; enabling without redirect requires `allow-real-recipients`.
- [Risk] Mail content shows live (edited) customer data. Accepted for content (live-edit decision 14.10.2026); recipient changes are caught by the pre-send check.
- [Risk] Spam classification of a new sender. Mitigation: company SMTP with SPF/DKIM for its domain — question for Herr Bachl.
- [Gate] Mail rows keep addresses until a retention/erasure rule is approved; real customer data is not allowed before that.

## Migration Plan

Additive table via Hibernate `update`. Deploy with `papo.mail.enabled=false`; behaviour on the VM is only the changed notice text. After SMTP data is available: set secrets, enable with `redirect-to` a test inbox, verify, then remove the redirect when real recipients are allowed. Rollback: disable via env, or revert the release (the table is ignored).

## Decisions by Chris (2026-10-15)

- **Completion mail:** `fulfilled` may trigger a neutrally worded mail ("Ihre Bestellung wurde abgeschlossen", not "versendet"); enabled per environment via `papo.mail.shipping-enabled` (VM: true). A real shipping confirmation with tracking is a later change.
- **Company recipient:** a functional mailbox, never a personal address; set via `papo.mail.company-recipient` and moved to the company profile with multi-tenancy. On the demo VM it is the test inbox.
- **Retention:** full addresses only while pending or failed; masked after sending; none for skipped rows. Failed rows lose the address 90 days after creation (chosen data-minimisation value, not a statutory period); after that they cannot be retried.
- **Sender:** `no-reply@paportal.at` via the SMTP account of the domain provider (SPF/DMARC for `paportal.at` already published).

## Open Questions

- SMTP credentials (the Vaultwarden send links had expired) and a test inbox for the demo VM (Herr Bachl).
- Whether real end customers may receive mails from the demo VM (until then: redirect enforced).

