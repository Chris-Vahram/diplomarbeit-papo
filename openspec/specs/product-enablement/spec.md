# product-enablement Specification

## Purpose
Per-partner article enablement by the company; partners see and order only enabled articles, with a clear message when none are enabled.

## Requirements

### Requirement: Enabled-products list search, filter, and pagination
A partner's enabled-products list SHALL support searching by name and pagination, to remain usable when a partner has many enabled products.

#### Scenario: Partner searches enabled products by name
- **WHEN** a partner enters a search term in its enabled-products list
- **THEN** the system shows only enabled products whose name matches the search term

#### Scenario: Partner pages through enabled products
- **WHEN** a partner's enabled-products list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the partner navigate to further pages

### Requirement: Product image in the partner's product list
The partner's enabled-products list SHALL display a product image thumbnail per row, so partners can recognise articles visually when ordering.

#### Scenario: Enabled product with an image
- **WHEN** an enabled product has an image URL and the partner opens their product list
- **THEN** the system shows the product's image as a thumbnail in that product's row

#### Scenario: Enabled product without an image
- **WHEN** an enabled product has no image URL
- **THEN** the system shows a neutral placeholder in place of the thumbnail, not a broken-image indicator

### Requirement: Enable products per partner
The company SHALL be able to enable specific own products for specific own partners. Partner and product of an enablement SHALL belong to the caller's company.

#### Scenario: Enable a product
- **WHEN** the company enables a product for a partner
- **THEN** that product becomes visible and orderable for that partner

#### Scenario: Disable a product
- **WHEN** the company removes an enablement
- **THEN** the product is no longer visible or orderable for that partner

#### Scenario: Foreign partner or product
- **WHEN** a company user lists, enables or disables using a partner or product of another company
- **THEN** the request returns 404 and no enablement is created or removed

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
