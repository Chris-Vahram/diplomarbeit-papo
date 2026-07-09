## Context

`Product.imageUrl` is a plain string. `add-product-category-image` renders it as a thumbnail in both product lists via an `image: true` `data-table` column that escapes the URL into `src` and falls back to a placeholder on empty/error. Demo images are trusted local SVGs under `frontend/assets/products/`, served by the frontend static server (:3000). The backend (Quarkus, :8080) already handles multipart uploads in the import feature (`org.jboss.resteasy.reactive.multipart.FileUpload` + `@RestForm`, part of quarkus-rest — no extra dependency). The frontend is a separate static origin, so anything uploaded must be stored and served by the backend. Product management is company-only (`@RolesAllowed("company")`); schema is Hibernate auto-generated (no Flyway), drop-and-create in dev/test, update in demo/prod. Target hosting is a persistent-disk VPS (Oracle Cloud free tier).

## Goals / Non-Goals

**Goals:**
- Company can upload a raster image file for a product from the product form.
- Company can pick a previously-uploaded image from a gallery (one image reusable across products).
- External-URL entry keeps working unchanged (third option).
- Uploaded bytes persist across restarts and back up with the database.
- Uploads cannot inject markup/script or exhaust storage.

**Non-Goals:**
- Partner-side upload (partners never manage products).
- Multiple images per product, cropping/editing, server-side resizing/CDN.
- Migrating the existing demo SVG assets into the DB — they stay frontend-served.

## Decisions

**Store image bytes in PostgreSQL, not the filesystem.** A new `ProductImage` Panache entity holds `byte[] data` (`@Lob`, bytea), `contentType`, `filename` (original, display-only), `sizeBytes`, `uploadedAt`. Rationale over a filesystem dir on the VPS: one backup path (`pg_dump` captures images with the data), image+row stay transactionally consistent, and — decisively for this project — the dev/test **drop-and-create** cycle wipes images with the schema instead of leaving orphan files on disk that a filesystem approach would strand. Cost (DB growth, streaming through the app) is negligible at ~800 small images. No new dependency, no upload-dir/permission/nginx config, no path-traversal surface.

**Serve bytes id-addressed: `GET /api/product-images/{id}`.** The endpoint streams `data` with the stored `contentType` as an explicit `Content-Type`, a long `Cache-Control` (content is immutable per id), and `Content-Disposition: inline; filename="..."` using only a sanitized/id-derived name — the client filename is never used to build the path or URL. This endpoint is readable by any authenticated user (partners must load thumbnails of enabled products too); it exposes only image bytes, no product/partner data, so `company`+`partner` both allowed. Upload and gallery-list stay company-only.

**`Product` keeps `imageUrl` as the single value the frontend renders; add a nullable `imageId` FK to `ProductImage`.** When the company selects an uploaded/gallery image, the backend sets `product.image = <ProductImage>` and derives `imageUrl = "/api/product-images/{id}"`; when the company enters an external URL, `image = null` and `imageUrl` = that URL; empty clears both. The frontend `data-table` thumbnail path is unchanged — it just renders whatever `imageUrl` resolves to (relative internal path or absolute external URL). Keeping a derived `imageUrl` avoids touching the partner list, the DTO consumers, and the existing thumbnail code. The FK exists so the gallery can list reusable images and so a product's selected image is a real reference, not a fragile string match.

**Validation lives server-side, raster-only.** Accept `image/png`, `image/jpeg`, `image/webp`; **reject SVG and everything else** (an SVG served same-origin from the API could execute script — the trusted demo SVGs are frontend assets, not uploads). Verify by sniffing magic bytes, not just the multipart-declared content type (declared type is attacker-controlled). Enforce a max size (~2 MB) via `quarkus.http.limits.max-body-size` plus an explicit service-side check for a clean German message. All rejections return a German error the form surfaces.

**Gallery is a flat list of all `ProductImage`s.** `GET /api/product-images` (company-only) returns `{ id, filename, contentType, sizeBytes, uploadedAt }` (metadata, not bytes); the picker renders each as a thumbnail via the serve endpoint. No pagination initially (image count grows slowly, far below the ~800 articles); revisit only if it becomes large.

**Frontend: one `<image-chooser>` Web Component replacing the Bild-URL input.** Three modes in the product modal — *Hochladen* (file input → `POST` multipart → sets selection), *Aus Galerie wählen* (opens the gallery, click to select), *Externe URL* (the existing text field). A live preview shows the current selection. The component exposes the resulting value (an uploaded image id → derived internal URL, or an external URL string) to the form's submit payload. Native Custom Element, no framework, German labels/messages.

## Risks / Trade-offs

- **[Risk] Declared content type is spoofable** → sniff magic bytes for PNG/JPEG/WEBP signatures and reject on mismatch; never echo the uploaded bytes as anything but the sniffed image type.
- **[Risk] SVG/script upload → stored XSS when served same-origin** → SVG is not in the allowlist; only raster types accepted. Serve with an explicit raster `Content-Type` and `inline` disposition.
- **[Risk] Large uploads exhaust DB/heap** → hard size cap (~2 MB) enforced at the HTTP layer and in the service; bytes streamed on serve.
- **[Trade-off] Reusable images can be orphaned** when no product references them → acceptable; they remain in the gallery for reuse. No reference-counting/cleanup in scope (diploma scale).
- **[Trade-off] Deleting a `ProductImage` still referenced by products** → out of scope for this change (no delete-image UI); if added later, guard with a reference check.
- **[Trade-off] Dev drop-and-create wipes uploads** → intended and consistent with all other dev data; `DemoDataSeeder` can seed a couple of `ProductImage` rows from the existing demo assets so the gallery is non-empty out of the box.

## Migration Plan

Additive only: new table `ProductImage` + nullable `Product.image_id`, created by Hibernate schema update in demo/prod, drop-and-create in dev/test. No data migration — existing `imageUrl` strings (external or the seeded frontend-asset paths) keep working untouched. Rollback = revert the change; the new column/table are nullable/unreferenced by old code.

## Open Questions

- Should `DemoDataSeeder` seed the gallery from the existing `frontend/assets/products/*.svg` demo images? SVG is disallowed for *uploads*, but seeding trusted raster demo images (or leaving the gallery empty in dev) is a seed-data choice — resolve during implementation, default to seeding 1–2 small raster demo images so the picker demos with content.
