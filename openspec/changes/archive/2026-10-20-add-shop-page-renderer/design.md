## Context

Decisions in `Specification/ENDKUNDENSHOP_DECISIONS.md` §4: shop as data, one renderer for preview and public shop, free colours with theme + overrides, curated self-hosted fonts, stacked sections without free positioning, page kinds and limits, per-page publishing, theme publication updates all pages. Fixed stack: native Web Components, Quarkus, PostgreSQL. Existing self-hosted fonts: Plus Jakarta Sans, Fira Code (OFL) — pattern from `self-host-fonts`.

## Goals / Non-Goals

**Goals:** stable data model the editor can write to; secure server-side schema validation; one renderer; working default storefront.

**Non-Goals:** editing endpoints/UI (`add-shop-builder-editor`), more templates (`add-shop-templates`), cart/checkout (`add-shop-guest-checkout`), legal page content (`add-shop-legal-consent`), inquiry block (`add-shop-inquiries`).

## Decisions

1. **Block config as PostgreSQL `jsonb` column, validated by a Java schema registry (`BlockType` enum + validator per type)** rather than one table per block type. Adding a block type = one enum entry + validator + custom element. Alternative JSON Schema library — rejected to avoid a new dependency; validators are small.
2. **Draft/published as two JSON snapshots per page (`draftContent`, `publishedContent`, `previousPublishedContent`)** holding the ordered block list. Simpler than versioned block rows; restore = swap snapshots. `ShopBlock` therefore is a JSON structure, not a table; block ids are UUIDs inside the snapshot for editor addressing.
3. **Theme likewise as `draftTheme`/`publishedTheme` JSON on `ShopTheme`.** Colour values validated as `#RRGGBB`; fonts validated against `ShopFonts` registry (id → files, licence).
4. **Renderer as custom elements in `frontend/src/shop/blocks/`**, each taking a config object property and theme CSS custom properties (`--shop-primary` …). Overrides set custom properties on the block host. No shadow DOM per block, so theme CSS cascades; escaping via the existing `html.js` helper.
5. **Templates as JSON resources (`backend/src/main/resources/shop-templates/default/*.json`)** loaded on shop creation. `add-shop-templates` only adds files.
6. **Curated font list of about 10–15 OFL families (latin + latin-ext woff2)**, served from `/vendor/fonts`. Final selection with Tobias.
7. **Catalogue query reuses `EnablementService` rules** (enabled ∧ available ∧ partner active) to avoid divergence from the portal.

## Risks / Trade-offs

- [JSON snapshots grow] → bounded by 20 pages and block-count limit (max. 40 blocks per page).
- [XSS via config] → plain-text storage, schema whitelist, escaping in every block; tests with payloads.
- [Image URLs leaking non-public images] → public image endpoint checks catalogue/asset membership.

## Migration Plan

New tables only. Existing shops from the foundation change get mandatory pages via an idempotent startup service. Rollback: previous image ignores tables.

## Interim States

Deliberate gaps at implementation time; each one is closed by a follow-up change, not by this one.

- **Tax rate (shop-public-catalog "Price display").** `Product` has no tax-rate field before `add-article-tax-rate`. The public catalogue therefore returns `Product.price` as gross price with a fixed `taxRatePercent` of 20 (`PublicArticleDto.DEFAULT_TAX_RATE_PERCENT`), the default that change uses. Closed by `add-article-tax-rate` task 4.5.
- **Shop-owned images (shop-public-catalog "Public image delivery").** Shop asset uploads (`ShopAsset`) arrive with `add-shop-builder-editor`. Until then the public image endpoint and block image references accept only images of the shop's catalogue articles (`ShopCatalogService.publicImage`). Same-origin frontend asset paths (`/assets/...`) of articles are passed through; external URLs and `/api/` paths are dropped. Closed by `add-shop-builder-editor` task 1.5.

## Open Questions

- Final curated font list (Tobias).
