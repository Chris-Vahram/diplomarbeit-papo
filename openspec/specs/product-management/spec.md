# product-management Specification

## Purpose
Central company management of articles: content, price, category, availability, image and validation, with list search and pagination.

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

### Requirement: Controlled article category assignment
Company article creation and editing SHALL support one optional category identifier. Article responses SHALL retain the category display name and add its identifier. Legacy category-name writes SHALL resolve only known categories; simultaneous nonnull category name and identifier SHALL be rejected with 400. Omitting or clearing both SHALL remove the optional assignment under the existing full-update contract.

#### Scenario: Category selected by identifier
- **WHEN** the company saves an article with a known active category identifier
- **THEN** the article response contains that identifier and the current category name

#### Scenario: Legacy name compatibility
- **WHEN** a legacy client submits only a known category name
- **THEN** the same controlled assignment rules apply without creating a category

#### Scenario: Conflicting or unknown input
- **WHEN** both category inputs are nonnull or the selected category is unknown
- **THEN** the API rejects the request with 400 and no article change

#### Scenario: Category removed
- **WHEN** the company submits an article without a category
- **THEN** the article remains valid and its category fields are null

#### Scenario: Concurrent deactivation
- **WHEN** article assignment races with deactivation of the selected category
- **THEN** assignment either commits while the category is active or is rejected after deactivation, never bypassing the active-state rule

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
