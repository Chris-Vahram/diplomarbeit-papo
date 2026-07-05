## ADDED Requirements

### Requirement: Product image in the company product list
The company product list SHALL display a product image thumbnail per row, so articles are visually recognisable at catalogue scale.

#### Scenario: Product with an image
- **WHEN** a product has an image URL and the company opens the product list
- **THEN** the system shows the product's image as a thumbnail in that product's row

#### Scenario: Product without an image
- **WHEN** a product has no image URL
- **THEN** the system shows a neutral placeholder in place of the thumbnail, not a broken-image indicator
