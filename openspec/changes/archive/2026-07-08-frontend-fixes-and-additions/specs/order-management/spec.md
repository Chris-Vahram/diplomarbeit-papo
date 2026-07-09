## MODIFIED Requirements

### Requirement: Order storage
The system SHALL store per order at least: order identifier, order date, assigned partner, end customer, ordered articles with quantities (order items, with a unit-price snapshot), order status, derived order value, and a company-facing seen/unseen flag (see `order-notifications`).

#### Scenario: Order record completeness
- **WHEN** an order is created
- **THEN** the stored order contains all listed fields and each order item keeps the unit price at time of order, and the order starts unseen for the company

### Requirement: Order visibility
The system SHALL show all orders to the company and only its own orders to each partner. The company order list SHALL support filtering by order-date range and by status, in addition to existing search.

#### Scenario: Company sees all orders
- **WHEN** the company opens the order list
- **THEN** orders from all partners are shown with their assigned partner

#### Scenario: Partner sees only own orders
- **WHEN** a partner opens the order list
- **THEN** only orders assigned to that partner are shown

#### Scenario: Company filters orders by date range
- **WHEN** the company sets a Von/Bis order-date range on the order list
- **THEN** only orders whose order date falls in that range are shown

#### Scenario: Company filters orders by status
- **WHEN** the company selects one or more statuses to filter by
- **THEN** only orders with a matching status are shown

#### Scenario: Partner filters own orders by date range and status
- **WHEN** a partner sets a date range or status filter on their own order list
- **THEN** only their own orders matching that filter are shown

## ADDED Requirements

### Requirement: Order-details overlay
The system SHALL let the company (and the partner, for their own orders) open a detail overlay for a single order showing its end customer, assigned partner, ordered articles with quantities and prices, order value, and status, without navigating away from the order list.

#### Scenario: Company opens order details
- **WHEN** the company clicks an order row in the order list
- **THEN** an overlay shows that order's end customer, partner, articles, quantities, prices, order value, and status

#### Scenario: Partner opens own order details
- **WHEN** a partner clicks a row in their own order list
- **THEN** an overlay shows that order's details in the same way, without a partner-selection field

### Requirement: Manual order creation by the company
The company SHALL be able to create an order on behalf of a partner. The company first selects an existing partner, then selects or creates an end customer belonging to that partner (never another partner's end customers), then adds articles enabled for that partner with quantities, matching the constraints of partner-driven order creation.

#### Scenario: Company creates an order for a partner
- **WHEN** the company selects a partner, selects or creates that partner's end customer, adds one or more of that partner's enabled articles with quantities, and submits
- **THEN** the system stores the order assigned to the selected partner, with the same validation and confirmation behavior as a partner-created order

#### Scenario: Company cannot pick another partner's end customer
- **WHEN** the company selects a partner during manual order creation
- **THEN** the end-customer selection only offers end customers owned by that selected partner
