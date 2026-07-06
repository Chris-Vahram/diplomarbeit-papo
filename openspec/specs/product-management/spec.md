# product-management Specification

## Purpose
TBD - created by archiving change add-list-search-pagination. Update Purpose after archive.
## Requirements
### Requirement: Product list search, filter, and pagination
The company product list SHALL support searching by name, filtering by availability, and pagination, to remain usable at the client's catalog scale (~800 articles).

#### Scenario: Company searches products by name
- **WHEN** the company enters a search term in the product list
- **THEN** the system shows only products whose name matches the search term

#### Scenario: Company pages through products
- **WHEN** the product list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the company navigate to further pages

### Requirement: Product image in the company product list
The company product list SHALL display a product image thumbnail per row, so articles are visually recognisable at catalogue scale.

#### Scenario: Product with an image
- **WHEN** a product has an image URL and the company opens the product list
- **THEN** the system shows the product's image as a thumbnail in that product's row

#### Scenario: Product without an image
- **WHEN** a product has no image URL
- **THEN** the system shows a neutral placeholder in place of the thumbnail, not a broken-image indicator

