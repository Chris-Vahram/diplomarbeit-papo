# order-management Specification

## Purpose
TBD - created by archiving change close-fsd-order-gaps. Update Purpose after archive.
## Requirements
### Requirement: Shipping confirmation on fulfilment
When the company advances an order to `fulfilled`, the system SHALL render a simulated shipping confirmation in-app (no real e-mail send), consistent with the simulated order confirmation shown on submission. This satisfies the shipping-confirmation half of FR-17, which is currently missing.

#### Scenario: Company sets an order to fulfilled
- **WHEN** the company changes an order's status to `fulfilled`
- **THEN** the system shows a simulated shipping-confirmation view for that order (order reference, Kunde, items) marked clearly as simulated / not sent

#### Scenario: No real e-mail is sent
- **WHEN** a shipping confirmation is rendered
- **THEN** no real e-mail is dispatched; the confirmation exists only in-app

### Requirement: Inactive partners cannot receive new orders
The system SHALL prevent creation of new orders for an inactive partner on every order-creation path, including the company manual-order path. An attempt SHALL be rejected with a German validation error and SHALL NOT create an order or a commission record.

#### Scenario: Company creates a manual order for an inactive partner
- **WHEN** the company submits a manual order targeting a partner whose `active` flag is false
- **THEN** the system rejects the request with a German error message and creates no order and no commission record

#### Scenario: Company creates a manual order for an active partner
- **WHEN** the company submits a manual order targeting an active partner
- **THEN** the order is created normally

### Requirement: Existing Endkunde selection is deliberate
The shared order form SHALL require the user to choose an existing Endkunde explicitly and SHALL NOT silently preselect the first customer. This behavior SHALL apply to partner order creation and company manual-order creation.

#### Scenario: Existing-customer mode opens
- **WHEN** an order form opens in existing-customer mode
- **THEN** the customer field shows a disabled German prompt and no Endkunde id is selected

#### Scenario: Submission without an existing customer
- **WHEN** the user submits in existing-customer mode without choosing an Endkunde
- **THEN** the browser blocks submission and shows a German validation message asking for an Endkunde

#### Scenario: Explicit customer selection
- **WHEN** the user chooses an existing Endkunde and submits an otherwise valid order
- **THEN** the selected Endkunde id is included and order creation proceeds normally

#### Scenario: New-customer mode remains available
- **WHEN** the user switches to new-customer mode and supplies valid customer data
- **THEN** no existing Endkunde selection is required

### Requirement: Partner order history is newest first
The partner order-list API SHALL return the authenticated partner's orders in descending order so the newest order appears first while partner scoping remains unchanged.

#### Scenario: Partner has multiple orders
- **WHEN** the partner loads `Meine Bestellungen`
- **THEN** orders are returned newest first by descending order id

#### Scenario: Fresh order is visible first
- **WHEN** a partner completes a new order and returns to `Meine Bestellungen`
- **THEN** the new order appears as the first order in the list

#### Scenario: Partner isolation remains unchanged
- **WHEN** a partner loads their ordered history
- **THEN** no order belonging to another partner is returned

### Requirement: Order submission is guarded against duplicate double-click
The system SHALL prevent a duplicate order (and duplicate commission record) from being created when a partner submits an order form multiple times in quick succession, on every order-creation entry point available to the partner.

#### Scenario: Partner double-clicks submit
- **WHEN** a partner clicks the order-submit button twice in quick succession before the first request completes
- **THEN** only one order and one commission record are created

### Requirement: Order item quantity has a sane upper bound
Order item quantity SHALL be rejected with a German validation error if it exceeds a defined maximum, rather than being accepted and risking a numeric-overflow failure when the order total is computed and stored.

#### Scenario: Quantity exceeds the maximum
- **WHEN** an order item's quantity exceeds the defined maximum
- **THEN** the system rejects the request with a German validation error and creates no order

