## ADDED Requirements

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
