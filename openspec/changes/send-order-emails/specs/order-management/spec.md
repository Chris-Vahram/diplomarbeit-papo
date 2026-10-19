## MODIFIED Requirements

### Requirement: Shipping confirmation on fulfilment
When the company advances an order to `fulfilled`, the system SHALL queue a real shipping confirmation e-mail to the end customer as defined in `order-emails` (only while shipping confirmations are enabled) and SHALL show the company a short notice whether it is being sent, cannot be sent for lack of an address, or sending is disabled. This satisfies the shipping-confirmation half of FR-17.

#### Scenario: Company sets an order to fulfilled
- **WHEN** the company changes an order's status to `fulfilled`
- **THEN** a shipping confirmation is queued for the end customer if shipping confirmations are enabled, and the company sees the delivery notice for that order

#### Scenario: No real e-mail is sent
- **WHEN** sending is disabled by configuration and an order is set to `fulfilled`
- **THEN** no real e-mail is dispatched and the notice states that sending is not active

### Requirement: Order confirmation
After a valid order submission the system SHALL display an on-screen confirmation with an order summary and SHALL queue the order e-mails defined in `order-emails`. The confirmation SHALL state whether a confirmation is sent to the end customer, no address exists, or sending is disabled.

#### Scenario: On-screen confirmation
- **WHEN** a partner submits a valid order
- **THEN** the system shows an on-screen summary and the e-mail notice for the end customer
