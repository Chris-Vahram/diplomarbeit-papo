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

### Requirement: Company sets a product image via upload, gallery, or URL
The company product form SHALL let the company set a product's image by uploading a new file, by picking a previously-uploaded image from the gallery, or by entering an external image URL, and SHALL show a preview of the current selection.

#### Scenario: Set image by upload
- **WHEN** the company uploads an image while creating or editing a product
- **THEN** the product uses that uploaded image and the form shows a preview of it

#### Scenario: Set image from the gallery
- **WHEN** the company picks a previously-uploaded image from the gallery
- **THEN** the product uses that image and the form shows a preview of it

#### Scenario: Set image by external URL
- **WHEN** the company enters an external image URL
- **THEN** the product uses that URL and the form shows a preview of it

#### Scenario: No image
- **WHEN** the company leaves the image unset (no upload, no gallery pick, no URL)
- **THEN** the product has no image and its list row shows the neutral placeholder

#### Scenario: Selected image appears in the product lists
- **WHEN** a product's image is set by upload or gallery pick
- **THEN** the company product list and the partner enabled-products list show that image as the row thumbnail

