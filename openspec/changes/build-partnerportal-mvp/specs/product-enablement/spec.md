## ADDED Requirements

### Requirement: Enable products per partner
The company SHALL be able to enable specific products for specific partners.

#### Scenario: Enable a product
- **WHEN** the company enables a product for a partner
- **THEN** that product becomes visible and orderable for that partner

#### Scenario: Disable a product
- **WHEN** the company removes an enablement
- **THEN** the product is no longer visible or orderable for that partner

### Requirement: Restricted product visibility
Each partner SHALL view and order only the products enabled for that partner.

#### Scenario: Partner sees only enabled products
- **WHEN** a partner opens its enabled-products list
- **THEN** only products enabled for that partner are shown

#### Scenario: Partner cannot order non-enabled product
- **WHEN** a partner attempts to order a product not enabled for it
- **THEN** the system rejects the action

### Requirement: No enabled products handling
When a partner has no enabled products, the system SHALL show a clear message and prevent order creation.

#### Scenario: Partner with zero enabled products
- **WHEN** a partner with no enabled products opens the order-creation view
- **THEN** the system shows a clear message and does not allow creating an order
