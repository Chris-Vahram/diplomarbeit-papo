## Why

Partners without technical knowledge shall design their own shop in a clean GUI (Protokoll 12.10.2026: "Homepage Generator/Baukasten", "Mein Webshop"; decisions in `Specification/ENDKUNDENSHOP_DECISIONS.md` §4). The data model and renderer exist after `add-shop-page-renderer`; this change adds the editor that writes drafts, manages pages and the theme, and publishes. Tobias implements it.

## What Changes

- "Mein Webshop" shows status and page miniatures with "Shop bearbeiten"; the builder opens full screen ("Zurück") with collapsible palette/block-list sidebar, live preview (shared renderer, desktop/phone) and collapsible properties panel.
- Editing directly in the preview: texts in place, block toolbar (move, size, duplicate, delete), drag & drop from the palette with hover previews.
- Separate tab "Design" for the theme (free colour pickers, curated font pair), with own draft and "Design veröffentlichen".
- Page management: add empty page or page from template, rename, menu name, URL segment, order in navigation, delete free pages; mandatory pages always online; pages linked by published buttons keep their address and stay online.
- Block editing: add, configure, duplicate, delete, reorder via drag & drop and up/down buttons; colour override per block with one-click "Auf Design zurücksetzen".
- Publish per page; restore last published version; autosave of drafts; undo/redo within the session.
- Simple SEO fields per page (title, description) with plain-language explanation.
- Shop image library: upload PNG/JPG/WebP via the existing image validator, 50 MB quota per shop shown in the editor, rights confirmation on upload.
- Optimistic locking so two people on the shared partner login do not overwrite each other silently.
- Editor for desktop and tablet; on phones a hint.
- Not included: design assistant checks (`add-shop-design-assistant`), more templates (`add-shop-templates`).

## Capabilities

### New Capabilities

- `shop-builder`: Editor behaviour, page management, block editing, theme tab, publishing, restore, autosave, undo, SEO fields, concurrency, device support.
- `shop-assets`: Partner-uploaded shop images, formats, quota, rights confirmation, deletion.

### Modified Capabilities

None.

## Impact

Backend: partner-only endpoints under `/api/partner/shop/…` (pages, blocks, theme, publish, restore, assets) with version checks; asset storage reusing `ProductImageValidator`. Frontend: builder page and components (palette, preview frame, properties forms per block type, design tab, page list). Depends on `add-shop-page-renderer`. Later personal accounts: employees edit drafts, only partner admins publish (noted for the accounts change).
