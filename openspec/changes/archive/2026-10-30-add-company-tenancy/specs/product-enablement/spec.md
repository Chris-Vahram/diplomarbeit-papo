## MODIFIED Requirements

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
