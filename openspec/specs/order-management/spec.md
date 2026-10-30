# order-management Specification

## Purpose
Partner-driven orders for end customers with automatic partner assignment, the status flow submitted -> reviewed -> fulfilled, shipping confirmation, and guards against inactive partners, double submission and unrealistic quantities.

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

### Requirement: Partner-driven order creation
A partner SHALL be able to create an order for a selected/created end customer containing one or more enabled articles, each with a quantity.

#### Scenario: Create multi-line order
- **WHEN** a partner selects an end customer, adds several enabled articles with quantities, and submits
- **THEN** the system stores the order with one order item per article line and computes the order value

#### Scenario: Invalid order submission
- **WHEN** a partner submits an order with no articles or an invalid quantity
- **THEN** the system rejects the submission and shows an understandable validation message

### Requirement: Automatic order-to-partner assignment
The system SHALL automatically assign each order to the partner who created it.

#### Scenario: Order assigned to creating partner
- **WHEN** a partner submits a valid order
- **THEN** the stored order's assigned partner is that partner, with no manual assignment step

### Requirement: Order storage
The system SHALL store per order at least: order identifier, order date, assigned partner, end customer, ordered articles with quantities (order items, with a unit-price snapshot), order status, and derived order value.

#### Scenario: Order record completeness
- **WHEN** an order is created
- **THEN** the stored order contains all listed fields and each order item keeps the unit price at time of order

### Requirement: Order visibility
The system SHALL show the company all orders of its own partners and only its own orders to each partner. Orders of another company's partners SHALL be invisible and unchangeable for the company.

#### Scenario: Company sees all orders
- **WHEN** the company opens the order list
- **THEN** orders from all of its own partners are shown with their assigned partner, and no order of another company

#### Scenario: Partner sees only own orders
- **WHEN** a partner opens the order list
- **THEN** only orders assigned to that partner are shown

#### Scenario: Foreign order by id
- **WHEN** a company user changes the status of, marks as seen, or lists mails of an order of another company
- **THEN** the request returns 404 and the order is unchanged

#### Scenario: Manual order for foreign partner
- **WHEN** a company user creates a manual order for a partner of another company
- **THEN** the request returns 404 and no order is created

### Requirement: Order status workflow
The system SHALL support order statuses `submitted`, `reviewed`, and `fulfilled`. Only the company SHALL change status; partners SHALL view it.

#### Scenario: Company advances status
- **WHEN** the company moves an order from submitted to reviewed to fulfilled
- **THEN** the system saves each status change

#### Scenario: Partner cannot change status
- **WHEN** a partner attempts to change an order status
- **THEN** the system rejects the action; the partner can only view status

### Requirement: Order confirmation
After a valid order submission the system SHALL display an on-screen confirmation with an order summary and render a simulated order/shipping confirmation e-mail in-app (no real send).

#### Scenario: On-screen confirmation
- **WHEN** a partner submits a valid order
- **THEN** the system shows an on-screen summary and a simulated confirmation e-mail view
