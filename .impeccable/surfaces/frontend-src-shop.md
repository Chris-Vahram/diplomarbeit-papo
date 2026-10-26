---
version: 1
slug: "frontend-src-shop"
primary_target: "frontend/src/shop"
related_targets: ["backend/src/main/resources/shop-templates"]
---

# Surface: public partner shop (renderer + templates)

Mode: Persuade (end customers decide to order from a partner). Audience: end customers of a regional trade partner (tools, building material, paint, electrical, safety). Constraint: partner theme (colours, curated fonts) and the block catalogue drive everything; no free HTML.

## Direction contract

THESIS: A partner shop is a storefront of one trade business: one decisive photograph and the articles carry the page. Refuses the stacked full-width colour bands with centred text that every builder default ships.

OWN-WORLD: Three forms on one renderer, chosen in the theme. Klar: bright, precise Fachhandel; hairlines, 8px corners, split hero with portrait photo, borderless product tiles on a soft ground. Handwerk: warm material; serif display, passe-partout cards (16px), hero photo with an overlapping surface panel. Kräftig: modern brand; square corners, heavy display type, full-bleed dark photo hero with primary scrim, gridded product wall with 1px rules.

STORY: The visitor sees whose shop this is and what it sells within one viewport, browses articles with clear gross prices, opens an article, and orders.

FIRST VIEWPORT: Translucent sticky header (shop name left, nav right; Handwerk centred masthead). Hero below fills most of the viewport: photo plus display heading, one sentence, one button to the articles. Heading size per form: Klar clamp(2.5rem, 5.6vw, 4.75rem); Kräftig clamp(2.75rem, 7vw, 6rem); Handwerk clamp(2.25rem, 4.6vw, 3.75rem) because it sits in a 40rem surface panel and a serif at 5rem breaks every German noun there. Article tiles start at the fold on the Startseite; listing pages open with a compact head, never a full photo hero.

FORM: Pinned by the user (three characters "Klar", "Handwerk", "Kräftig"; "Fachhandel" and "moderne Marke" both covered); the concept roll was skipped because a user-pinned direction beats the roll. seed: none (roll skipped, direction pinned by the user). User confirmation 2026-10-26: "passt so, DESIGN.md nicht ändern" (roll not needed; shop direction stays documented here, not in DESIGN.md)

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Notes
- Sample photos: CC0 from Wikimedia Commons (Unsplash uploads), provenance in backend/src/main/resources/shop-templates/images/index.json; partners replace them.
- Signature interaction: product tiles respond on press (image settles, price stays put); header material blurs content scrolling under it.
- Every section ends in something the visitor can do with today's shop (browse, open an article); no copy promises online ordering until guest checkout ships.
