## ADDED Requirements

### Requirement: Shop image upload
The partner SHALL upload images for the shop as PNG, JPEG or WebP, verified by content, max. 5 MB per file. SVG and other formats SHALL be rejected. The partner MUST confirm "Ich habe die Rechte an diesem Bild." before upload.

#### Scenario: SVG rejected
- **WHEN** the partner uploads an SVG file
- **THEN** the system rejects it with "Bitte PNG, JPG oder WebP verwenden."

#### Scenario: Rights confirmation required
- **WHEN** the partner uploads without confirming the rights checkbox
- **THEN** the upload is not sent

### Requirement: Storage quota
Each shop SHALL have a storage quota of 50 MB for its images. The editor SHALL show used and free storage. An upload exceeding the quota SHALL be rejected.

#### Scenario: Quota exceeded
- **WHEN** an upload would exceed 50 MB in total
- **THEN** the system rejects it with "Speicherplatz voll – bitte nicht verwendete Bilder löschen."

### Requirement: Asset ownership and deletion
Shop images SHALL belong to exactly one shop and SHALL only be referenced by that shop's blocks. The partner SHALL delete unused images; deleting an image used by any version of a page (draft, published or the restorable previous version) SHALL be refused with the list of pages using it. The library SHALL show the same use. The public shop SHALL deliver a shop image only while a published online page of that shop shows it.

#### Scenario: Image in use
- **WHEN** the partner deletes an image used on the published Startseite
- **THEN** the system refuses and names "Startseite"

#### Scenario: Draft-only image
- **WHEN** an image is used only in the draft of the Startseite
- **THEN** the library shows "Verwendet auf: Startseite", deleting it is refused, and the public image URL returns 404
