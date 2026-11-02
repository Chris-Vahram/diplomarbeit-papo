## MODIFIED Requirements

### Requirement: Order storage
The system SHALL store per order at least: order identifier, order date, assigned partner, end customer, ordered articles with quantities (order items, with a gross unit-price snapshot and a tax-rate snapshot), order status, derived gross order value, derived net order value and derived tax amount.

#### Scenario: Order record completeness
- **WHEN** an order is created
- **THEN** the stored order contains all listed fields and each order item keeps the gross unit price and tax rate at time of order

#### Scenario: Later price change does not alter orders
- **WHEN** the company changes an article's price or tax rate after an order was placed
- **THEN** the existing order's items, totals and commission remain unchanged
