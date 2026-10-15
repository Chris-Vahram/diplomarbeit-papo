## Why

The decided shop builder ("Mein Webshop", `Specification/ENDKUNDENSHOP_DECISIONS.md` §4) stores a shop as data — pages made of typed blocks plus a theme — and renders it with the same Web Components in the editor preview and the public shop. This change delivers that data model, the shared renderer and the mandatory pages with one default template each, so every released shop shows a working storefront before the editor exists.

## What Changes

- Data model: `ShopTheme` (1 per shop), `ShopPage` (per shop), `ShopBlock` (ordered per page, typed, JSON config validated server-side per type schema). Each page and the theme hold a draft and a published version.
- Page kinds: system (cart, checkout, Impressum, AGB, Datenschutz, Widerruf — fixed, not designable), mandatory (Startseite, Artikelübersicht, Artikeldetail — designable, cannot be deleted or unpublished), free (added later via editor).
- Limits enforced server-side: max. 6 designable pages online, max. 20 pages incl. drafts.
- First block catalogue: Hero/Banner, Überschrift + Text, Bild, Bild + Text, Artikelraster, Hervorgehobene Artikel, Artikeldetail (only on the Artikeldetail page), Ansprechpartner, Abstand/Trennlinie.
- Theme: free colours (primary, accent, background, text, surface), font pair from a curated list of self-hosted OFL fonts; blocks may override colours.
- Shared renderer (`<shop-page-view>` + one custom element per block type) used by the public shop and later the editor preview; navigation generated from online pages.
- Public read API for published pages, theme and the shop's catalogue (enabled + available articles only, gross price incl. USt) and public image delivery for those articles and shop assets.
- On shop creation: mandatory pages created and published from one default template; system pages reserved.
- Not included: editing UI, templates beyond the default, cart/checkout behaviour, legal texts, inquiry form.

## Capabilities

### New Capabilities

- `shop-pages`: Page kinds, limits, blocks, block schemas, draft/published versions, navigation, page URLs.
- `shop-theme`: Theme colours, curated fonts, block overrides, published theme application.
- `shop-public-catalog`: Public read of published pages, theme, articles and images for a reachable shop.

### Modified Capabilities

None (public image delivery is specified within `shop-public-catalog`; the protected `product-images` endpoints stay unchanged).

## Impact

Backend: new entities and validators in the `shop` package, public resources under `/api/shop/{kennung}/…`, catalogue query reusing enablement/availability rules. Frontend: `src/shop/` renderer components and block elements, self-hosted font files (OFL) under `frontend/vendor/fonts`. Depends on `add-partner-shop-foundation`; prices depend on `add-article-tax-rate`.
