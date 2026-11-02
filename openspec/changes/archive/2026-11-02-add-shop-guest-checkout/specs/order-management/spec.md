## ADDED Requirements

### Requirement: Order origin
Every order SHALL have an origin `INTERNAL` (entered in the portal) or `SHOP` (placed in a partner shop). Existing orders SHALL be migrated to `INTERNAL`. Company and partner order lists and details SHALL show the origin; lists SHALL be filterable by origin.

#### Scenario: Shop order visible to partner
- **WHEN** a shop order is placed in the partner's shop
- **THEN** it appears in the partner's order list with the badge "Shop" and status submitted

## MODIFIED Requirements

### Requirement: Automatic order-to-partner assignment
The system SHALL automatically assign each internal order to the partner who created it and each shop order to the partner owning the shop it was placed in.

#### Scenario: Order assigned to creating partner
- **WHEN** a partner submits a valid order
- **THEN** the stored order's assigned partner is that partner, with no manual assignment step

#### Scenario: Shop order assigned to shop owner
- **WHEN** a visitor places an order in shop "huemer-elektro-gmbh"
- **THEN** the order is assigned to the partner owning that shop and a commission record is created as for internal orders
