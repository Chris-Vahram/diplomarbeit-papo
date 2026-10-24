## MODIFIED Requirements

### Requirement: Product images are served by the backend
The system SHALL serve a stored product image's bytes at a stable, id-addressed URL with the correct image content type, so the image renders as a thumbnail wherever a product is listed. An image SHALL be served only to company and partner users of the company that owns it.

#### Scenario: Authenticated user loads an image
- **WHEN** an authenticated user of the owning company requests a stored product image by its id
- **THEN** the system returns the image bytes with the stored image content type

#### Scenario: Served content type is the verified image type
- **WHEN** a stored image is served
- **THEN** the response content type is the image type the system verified at upload, not a type taken from the uploader's filename

#### Scenario: Unknown image id
- **WHEN** a user requests an image id that does not exist
- **THEN** the system responds with a not-found result

#### Scenario: Image of another company
- **WHEN** a company or partner user of company B requests an image id owned by company A
- **THEN** the system responds with the same not-found result

### Requirement: Company picks from a gallery of uploaded images
The system SHALL let a company user list all product images previously uploaded by the own company and assign any of them to an own product, so one uploaded image can be reused across multiple products of that company.

#### Scenario: Gallery lists uploaded images
- **WHEN** the company opens the image gallery
- **THEN** the system shows all product images previously uploaded by that company and none of another company

#### Scenario: Reuse one image for several products
- **WHEN** the company assigns an already-uploaded gallery image to another product
- **THEN** both products reference the same stored image

#### Scenario: Partner cannot list the gallery
- **WHEN** a user with the partner role requests the image gallery
- **THEN** the system denies the request
