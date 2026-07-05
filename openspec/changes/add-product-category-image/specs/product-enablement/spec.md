## ADDED Requirements

### Requirement: Product image in the partner's product list
The partner's enabled-products list SHALL display a product image thumbnail per row, so partners can recognise articles visually when ordering.

#### Scenario: Enabled product with an image
- **WHEN** an enabled product has an image URL and the partner opens their product list
- **THEN** the system shows the product's image as a thumbnail in that product's row

#### Scenario: Enabled product without an image
- **WHEN** an enabled product has no image URL
- **THEN** the system shows a neutral placeholder in place of the thumbnail, not a broken-image indicator
