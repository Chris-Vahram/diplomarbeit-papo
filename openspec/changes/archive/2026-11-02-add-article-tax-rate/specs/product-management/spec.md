## MODIFIED Requirements

### Requirement: Company manages products
The company SHALL be able to create, view, edit, and manage products/articles centrally. Each product SHALL store at least name, gross price (incl. USt), tax rate, description, and availability status, and optionally category and image reference.

#### Scenario: Create product
- **WHEN** the company submits a product with name, gross price, tax rate, description, and availability
- **THEN** the system stores the product and shows a success confirmation

#### Scenario: Edit product price
- **WHEN** the company changes a product's gross price or tax rate
- **THEN** the system saves the new values as the single central price used everywhere; existing orders keep their snapshots
