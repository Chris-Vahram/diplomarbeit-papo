## ADDED Requirements

### Requirement: Order e-mail recipients
When an order is created by a partner or by the company, the system SHALL queue an order confirmation to the end customer if the customer has an e-mail address, an order notice to the partner's e-mail address if present, and an order notice to the configured company address if present. When an order reaches `fulfilled` and shipping confirmations are enabled, the system SHALL queue a shipping confirmation to the end customer if an address is present.

#### Scenario: Order with customer e-mail
- **WHEN** a partner places an order for an end customer with an e-mail address
- **THEN** a confirmation for the customer, a notice for the partner and a notice for the company are queued

#### Scenario: Customer without e-mail
- **WHEN** the end customer has no e-mail address
- **THEN** no customer mail is queued and the partner and company notices are still queued

#### Scenario: Shipping confirmation
- **WHEN** shipping confirmations are enabled and the company sets an order to `fulfilled`
- **THEN** a shipping confirmation to the end customer is queued if an address exists

#### Scenario: Malformed address
- **WHEN** a stored recipient address is blank or not a valid e-mail address
- **THEN** a blank address creates no mail, a malformed one is recorded as not sent with the reason "ungültige Adresse", and the other mails are unaffected

### Requirement: Order e-mail content
Mails SHALL be German plain text signed with the company name. End-customer mails SHALL list articles and quantities without prices; partner and company notices SHALL include order value and commission.

#### Scenario: Customer confirmation without prices
- **WHEN** an order confirmation for an end customer is rendered
- **THEN** it lists articles and quantities with correct umlauts and contains no prices

### Requirement: Reliable and non-duplicated delivery
Mails SHALL be recorded in the same transaction as the triggering order change and sent asynchronously, at least once. At most one mail record SHALL exist per order, mail type and recipient role. "Sent" SHALL mean accepted by the mail server. Failed sends SHALL be retried with increasing delay and marked as failed after the last attempt. A rolled-back order change SHALL NOT produce mail.

#### Scenario: SMTP temporarily unavailable
- **WHEN** sending fails
- **THEN** the mail stays pending and is retried later, and after the last attempt it is marked failed

#### Scenario: Repeated fulfilment
- **WHEN** two fulfilment requests for the same order arrive at the same time
- **THEN** exactly one succeeds, one shipping confirmation record exists, and a concurrent "seen" update does not reset the status

#### Scenario: Recipient changed before sending
- **WHEN** the customer's e-mail address is corrected while a mail to the old address is still pending
- **THEN** that mail is not sent and is shown as "Empfänger geändert" until the company requeues it

#### Scenario: Failed order creation
- **WHEN** order creation is rejected or rolled back
- **THEN** no mail is queued

### Requirement: Sending can be disabled and redirected
Sending SHALL be disabled unless explicitly enabled by configuration. While disabled, mails SHALL be recorded as not sent without storing the address and SHALL NOT be sent later. A configured redirect address SHALL receive every mail instead of the original recipient. Pending mails SHALL NOT be sent when the configuration changed since queueing. Enabling sending without a redirect SHALL require an explicit permission for real recipients.

#### Scenario: Disabled sending
- **WHEN** an order is placed while sending is disabled
- **THEN** the mails are recorded as not sent and no SMTP connection is made

#### Scenario: Configuration changed while pending
- **WHEN** sending is disabled or the redirect is added or removed while mails are pending
- **THEN** those mails are recorded as not sent and are not delivered to any address

#### Scenario: Real recipients need explicit permission
- **WHEN** sending is enabled without redirect and real recipients are not explicitly allowed
- **THEN** the application refuses to start with a clear configuration error

#### Scenario: Redirect for the demo VM
- **WHEN** a redirect address is configured
- **THEN** every mail is delivered only to that address and names the original recipient role in the text

### Requirement: Delivery status for the company
The company SHALL see per order which mails exist, their recipient role, masked address, status and attempts, and SHALL be able to retry a failed mail. Partners SHALL NOT access this status. The partner order confirmation SHALL state whether a customer confirmation is being sent, whether no address exists, or whether sending is disabled.

#### Scenario: Company retries a failed mail
- **WHEN** the company retries a failed mail of an order
- **THEN** the mail is queued again and its status shows pending

#### Scenario: Retry of a foreign or non-failed mail
- **WHEN** the company retries a mail of another order, a mail that is not failed or recipient-changed, or any mail while sending is disabled
- **THEN** the request is rejected with 404 or 409 and nothing changes

#### Scenario: Concurrent retries
- **WHEN** two retry requests for the same failed mail arrive at the same time
- **THEN** the mail is queued once

#### Scenario: Partner cannot read delivery status
- **WHEN** a partner or an unauthenticated user calls the status or retry endpoint
- **THEN** access is denied with 403 or 401

### Requirement: Mail privacy
Logs SHALL NOT contain e-mail addresses, names or mail bodies, including framework mailer logs. Stored errors SHALL be short error codes. Full addresses SHALL be kept only while a mail is pending or failed and SHALL be removed from failed mails 90 days after creation. SMTP credentials SHALL come only from environment or secrets and SHALL NOT be stored in Git. Addresses SHALL be masked in the company status view.

#### Scenario: Old failed mail loses its address
- **WHEN** a failed mail is older than 90 days
- **THEN** its full address is removed and it can no longer be retried

#### Scenario: Send failure is logged
- **WHEN** a mail fails to send
- **THEN** the log entry contains only the mail id, type and status, not the address or content
