## ADDED Requirements

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
