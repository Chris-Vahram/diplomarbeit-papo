## ADDED Requirements

### Requirement: Checkout for logged-in customers
When a customer is logged in, the checkout SHALL be prefilled from the customer profile, and the created order and Kunde SHALL be linked to the account subject. The guest checkout SHALL remain unchanged for visitors who are not logged in.

#### Scenario: Linked order
- **WHEN** a logged-in customer places an order
- **THEN** the order appears in the customer's "Mein Konto" order list and in the partner's order list with origin "Shop"
