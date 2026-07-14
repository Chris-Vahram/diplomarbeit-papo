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

