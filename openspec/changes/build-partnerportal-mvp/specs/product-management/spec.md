## ADDED Requirements

### Requirement: Company manages products
The company SHALL be able to create, view, edit, and manage products/articles centrally. Each product SHALL store at least name, price, description, and availability status, and optionally category and image reference.

#### Scenario: Create product
- **WHEN** the company submits a product with name, price, description, and availability
- **THEN** the system stores the product and shows a success confirmation

#### Scenario: Edit product price
- **WHEN** the company changes a product's price
- **THEN** the system saves the new price as the single central price used everywhere

### Requirement: Central product content control
Product price and description SHALL be controlled solely by the company. Partners SHALL NOT modify central product master data.

#### Scenario: Partner cannot edit product
- **WHEN** a partner attempts to modify a product's price or description
- **THEN** the system rejects the action (unauthorized / not editable)

### Requirement: Product availability
A product marked unavailable SHALL NOT be orderable, even if it is enabled for a partner.

#### Scenario: Order attempt on unavailable product
- **WHEN** a partner attempts to add an unavailable product to a new order
- **THEN** the system prevents adding it and shows a clear message

### Requirement: Product validation
The system SHALL validate required product fields before saving and prevent duplicate product identifiers where used.

#### Scenario: Missing required product field
- **WHEN** the company saves a product with a required field empty
- **THEN** the system rejects the save and shows an understandable validation message
