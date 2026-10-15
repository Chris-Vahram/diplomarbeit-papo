## 1. Backend — essential

- [x] 1.1 Write failing tests first (`MockMailbox` plus a controllable failing sender and clock): claim/send/commit per row incl. SMTP-accepted-then-commit-failure, stuck SENDING recovery, disabled/redirect changes while pending (CONFIG_CHANGED), refusal to start without redirect unless real recipients allowed, recipient corrected while pending (RECIPIENT_CHANGED), invalid/blank addresses, concurrent fulfilment and concurrent markSeen, concurrent retries, shipping disabled by default, framework mailer logs free of addresses/bodies; recipients for partner and company-created orders, customer without e-mail, shipping on `fulfilled`, repeated `fulfilled` without duplicate, rollback creates no row, disabled mode records SKIPPED and never sends later, redirect address, retry schedule up to FAILED, company status/retry endpoints incl. foreign mail 404 / non-failed 409 / partner 403 / anonymous 401, masked addresses, customer mails without prices, no addresses in logs.
- [x] 1.2 Add `quarkus-mailer` and `quarkus-scheduler`, `OrderEmail` entity with unique (order, type, recipientRole), outbox writes in `OrderService` create/status, conditional status/seen updates, blocking scheduled sender with per-row claim and backoff, e-mail validation for new customer/partner input, plain-text bodies in Java, config `papo.mail.enabled|from|company-recipient|redirect-to|allow-real-recipients|shipping-enabled`, mailer log level WARN, event-specific `customerEmailNotice` in create/status responses; make 1.1 pass. Seeder creates no mail rows.

## 2. Frontend — essential

- [x] 2.1 Replace the simulated e-mail blocks (partner confirmation, company fulfilment dialog) with the delivery notice; add the company mail status list with retry in the order details; keep print output free of the notice; verify desktop 1280 px and mobile 390 px.

## 3. Dev and deploy — essential

- [x] 3.1 Add Mailpit (ports on 127.0.0.1) to `docker-compose.yml` and dev config; map the new env entries in `docker-compose.prod.yml` and the env example and document the VM sync; document SMTP env/secrets, disabled default and redirect in `docs/SETUP.md`; keep `papo.mail.enabled=false` for the VM; update `docs/DATA_MODEL.md` and B-04 in `Specification/phase2_backlog.md`.

## 4. Completion — essential

- [x] 4.1 Run focused tests, full `./mvnw test`, `./mvnw package`, `node --check`, `openspec validate send-order-emails --strict`, `git diff --check`, partnerportal-quality and an independent security/Java review; manual Mailpit check of all four mail types with umlauts.
- [ ] 4.2 Decisions taken 2026-10-15 (neutral completion mail, functional company mailbox, 90-day address removal for failed mails). After SMTP data and a test inbox from the company: set VM secrets, enable with `redirect-to` a test inbox, verify delivery, decide with Chris when real recipients are allowed.
