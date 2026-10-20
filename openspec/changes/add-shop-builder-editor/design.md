## Context

Builds on `add-shop-page-renderer` (JSON snapshots, block registry, shared renderer). Partner login is currently one shared account per partner company, so concurrent edits are realistic. Existing image upload: `ProductImageValidator`, `UploadLimits`. Decisions: `Specification/ENDKUNDENSHOP_DECISIONS.md` §4. Tobias implements.

## Goals / Non-Goals

**Goals:** a builder a non-technical partner can use; safe server-side writes; no lost work.

**Non-Goals:** assistant checks, more templates, real-time collaboration, editing on phones, per-article page design.

## Decisions

1. **Whole-page draft save (PUT draft snapshot with `version`)** instead of per-block endpoints: simpler, atomic, validator runs on the full page. Debounced autosave (1.5 s). Optimistic locking via `@Version` on `ShopPage` and `ShopTheme`.
2. **Undo/redo client-side** as a bounded stack (50 steps) of draft snapshots; no server history beyond the previous published version.
3. **Preview renders in an `<iframe>` of `shop.html?preview=…`** fed by `postMessage` with the draft, so the public CSS applies unchanged and editor CSS cannot leak. Desktop/mobile toggle changes iframe width.
4. **Properties panel generated from a client-side form description per block type** that mirrors the server schema; server validation remains authoritative.
5. **Drag & drop with native HTML5 DnD plus explicit buttons**; no library (fixed stack).
6. **Assets: own table `ShopAsset` (shop, bytes or file path, content type, size, uploadedAt, rightsConfirmed)**; quota sum query; reuse `ProductImageValidator` with WebP added. Implemented: block image fields reference only the shop's own `ShopAsset`s (own id space, so no ambiguity with article images); article images stay on `/api/shop/<kennung>/images/{id}`, shop images are served on `/api/shop/<kennung>/assets/{id}` only while a published online page shows them. Drafts are validated leniently (required fields and article/category/link references only on publish) so autosave never loses half-filled blocks.
7. **Entry and full-screen editor** (Tobias, 20.10.2026: first layout was overloaded): "Mein Webshop" shows the status box, miniatures of all designable pages (the same `shop.html?preview` renderer, scaled down, current editing state) and "Shop bearbeiten". The builder opens full screen on `#/partner/shop/editor[?page=<id>]` without the portal navigation; "Zurück" saves pending changes and returns.

## Risks / Trade-offs

- [Builder scope is large] → Split tasks so page/block editing ships before design tab polish; assistant is a separate change.
- [Shared login overwrites] → optimistic locking + clear reload message.
- [Large images slow shops] → 5 MB limit; resizing deferred, assistant warns about large images.

## Migration Plan

New endpoints and table only. Rollback: previous image; drafts remain stored.

## Open Questions

- Exact properties per block (refined during implementation with Tobias's templates).
