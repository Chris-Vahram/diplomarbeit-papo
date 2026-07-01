## ADDED Requirements

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
The system SHALL store per order at least: order identifier, order date, assigned partner, end customer, ordered articles with quantities (order items), order status, and derived order value.

#### Scenario: Order record completeness
- **WHEN** an order is created
- **THEN** the stored order contains all listed fields

### Requirement: Order visibility
The system SHALL show all orders to the company and only its own orders to each partner.

#### Scenario: Company sees all orders
- **WHEN** the company opens the order list
- **THEN** orders from all partners are shown with their assigned partner

#### Scenario: Partner sees only own orders
- **WHEN** a partner opens the order list
- **THEN** only orders assigned to that partner are shown

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
