## MODIFIED Requirements

### Requirement: Company manages products
The company SHALL be able to create, view, edit, and manage its own products/articles centrally. Each product SHALL store at least name, price, description, and availability status, and optionally category and image reference; category and image SHALL belong to the same company. Products of other companies SHALL be invisible and unchangeable.

#### Scenario: Create product
- **WHEN** the company submits a product with name, price, description, and availability
- **THEN** the system stores the product for the caller's company and shows a success confirmation

#### Scenario: Edit product price
- **WHEN** the company changes a product's price
- **THEN** the system saves the new price as the single central price used everywhere

#### Scenario: Foreign product
- **WHEN** a company user lists products or opens or edits a product of another company by id
- **THEN** it is absent from the list and the request returns 404 without change

#### Scenario: Foreign category or image
- **WHEN** a company user saves a product with a category or uploaded image of another company
- **THEN** the save is rejected like an unknown category or image and nothing is stored
