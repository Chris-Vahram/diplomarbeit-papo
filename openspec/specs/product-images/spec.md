# product-images Specification

## Purpose
Article images uploaded by the company, served by the backend, reusable from a gallery and stored with safe filenames.
## Requirements
### Requirement: Company uploads a product image file
The system SHALL allow a user with the company role to upload an image file, store it, and reference it from a product, without needing an external image URL.

#### Scenario: Successful raster upload
- **WHEN** the company uploads a PNG, JPEG, or WEBP image within the size limit
- **THEN** the system stores the image and returns a reference the company can assign to a product

#### Scenario: Upload rejected — disallowed type
- **WHEN** the company uploads a file that is not a PNG, JPEG, or WEBP image (for example an SVG, PDF, or a non-image file renamed to an image extension)
- **THEN** the system rejects the upload with a German error message and stores nothing

#### Scenario: Upload rejected — too large
- **WHEN** the company uploads an image larger than the allowed maximum size
- **THEN** the system rejects the upload with a German error message and stores nothing

#### Scenario: Partner cannot upload
- **WHEN** a user with the partner role attempts to upload a product image
- **THEN** the system denies the request

### Requirement: Product images are served by the backend
The system SHALL serve a stored product image's bytes at a stable, id-addressed URL with the correct image content type, so the image renders as a thumbnail wherever a product is listed.

#### Scenario: Authenticated user loads an image
- **WHEN** an authenticated user requests a stored product image by its id
- **THEN** the system returns the image bytes with the stored image content type

#### Scenario: Served content type is the verified image type
- **WHEN** a stored image is served
- **THEN** the response content type is the image type the system verified at upload, not a type taken from the uploader's filename

#### Scenario: Unknown image id
- **WHEN** a user requests an image id that does not exist
- **THEN** the system responds with a not-found result

### Requirement: Company picks from a gallery of uploaded images
The system SHALL let a company user list all previously-uploaded product images and assign any of them to a product, so one uploaded image can be reused across multiple products.

#### Scenario: Gallery lists uploaded images
- **WHEN** the company opens the image gallery
- **THEN** the system shows all previously-uploaded product images to choose from

#### Scenario: Reuse one image for several products
- **WHEN** the company assigns an already-uploaded gallery image to another product
- **THEN** both products reference the same stored image

#### Scenario: Partner cannot list the gallery
- **WHEN** a user with the partner role requests the image gallery
- **THEN** the system denies the request

### Requirement: Uploaded filename is stored safely
The system SHALL preserve the uploader's original filename for display in UTF-8 (including umlauts ä/ö/ü/ß) but SHALL NOT use it to build the storage location or the served URL.

#### Scenario: Umlaut filename
- **WHEN** the company uploads an image whose filename contains umlauts (for example `Prüfsiegel-Größe.png`)
- **THEN** the system stores and displays the filename correctly and still serves the image by its id, not by the filename

