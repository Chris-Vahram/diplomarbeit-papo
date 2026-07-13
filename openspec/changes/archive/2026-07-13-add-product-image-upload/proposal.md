## Why

Today a product image can only be set by pasting an external `imageUrl` into the company product form — the company has no way to upload an actual image file or reuse one it uploaded before. For a hardware catalogue (~800 articles) whose data arrives by Excel/CSV, most articles have no usable public image URL, so the thumbnail column (shipped in `add-product-category-image`) stays empty in practice. Uploading a file and picking from previously-uploaded images makes the catalogue actually show pictures. Requested by Tobias (2026-07-09); note this is a team enhancement, not a client-meeting requirement.

## What Changes

- The company can **upload an image file** for a product directly in the product form, instead of only pasting a URL.
- The company can **pick a previously-uploaded image** from a gallery of all uploaded product images (one image is reusable across many products).
- The existing **external-URL field stays** as a third option — client-entered absolute URLs keep working unchanged.
- Uploaded images are **stored in PostgreSQL** (bytes in the DB) and **served by the backend** at a stable id-addressed URL; a product's `imageUrl` points at that URL for internal images. No filesystem storage.
- Uploads are **company-only** and **validated**: raster images only (`image/png`, `image/jpeg`, `image/webp`), SVG rejected, size-capped, with German error messages.
- The product list thumbnail rendering is unchanged — internal images simply resolve through the new serve endpoint.

## Capabilities

### New Capabilities
- `product-images`: uploading, storing, listing (gallery), and serving product image files owned by the company; validation and access rules for uploads and retrieval.

### Modified Capabilities
- `product-management`: the company product form's image field becomes an image chooser (upload new · pick from gallery · paste external URL) with a preview, replacing the single Bild-URL text input.

## Impact

- **Backend:** new `ProductImage` entity (bytea bytes, contentType, filename, size, uploadedAt) + `ProductImageResource`/service — upload (`POST`, multipart via the existing `@RestForm FileUpload` pattern from the import feature, no new dependency), gallery list (`GET`), and byte-serving (`GET /api/product-images/{id}`). `Product` gains a nullable link to the selected `ProductImage` (and continues to expose `imageUrl` for the frontend). Hibernate auto-generates the new table (no Flyway), consistent with the existing schema strategy.
- **Frontend:** `company-product-list-page.js` product modal gains an image-chooser control (native Web Component) with upload, gallery pick, URL entry, and a live preview. German labels/validation. Partner product list and the `data-table` image column are unchanged.
- **APIs:** `/api/product-images` (new); `/api/company/products` request/response gains the image reference.
- **Security:** company-role-gated upload/gallery; content-type allowlist (raster only, no SVG); max upload size; id-addressed serving with an explicit stored Content-Type (never trusts the client filename for the path). Umlauts in the original filename are stored/displayed in UTF-8 but never used to build a URL or path.
- **Out of scope:** partner-side upload (partners never manage products), image editing/cropping, per-product multiple images, CDN/thumbnail resizing.
