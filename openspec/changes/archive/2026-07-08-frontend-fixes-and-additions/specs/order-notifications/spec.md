## ADDED Requirements

### Requirement: Unseen-order tracking
The system SHALL track, per order, whether the company has seen it. New orders start unseen. An order transitions to seen when the company opens that order's detail view. Partners have no notion of seen/unseen for their own orders.

#### Scenario: New order starts unseen
- **WHEN** a partner submits a new order
- **THEN** the stored order is marked unseen for the company

#### Scenario: Opening details marks an order seen
- **WHEN** the company opens the order-details overlay for an unseen order
- **THEN** the system marks that order seen and it no longer counts toward the unseen count

#### Scenario: Seen state does not affect partners
- **WHEN** a partner views their own order list or an order's status
- **THEN** the seen/unseen state is not shown or relevant to the partner

### Requirement: Sidebar unseen-order badge
The company-area sidebar SHALL show a count of unseen orders as a badge on the "Bestellungen" navigation item, styled like a mobile app's notification-count badge. The badge SHALL be hidden when the unseen count is zero.

#### Scenario: Unseen orders exist
- **WHEN** the company has one or more unseen orders
- **THEN** the sidebar "Bestellungen" item shows a badge with the unseen count

#### Scenario: No unseen orders
- **WHEN** the company has zero unseen orders
- **THEN** the sidebar "Bestellungen" item shows no badge

#### Scenario: Badge updates after viewing
- **WHEN** the company opens an unseen order's details, reducing the unseen count
- **THEN** the sidebar badge updates to reflect the new count without a full page reload
