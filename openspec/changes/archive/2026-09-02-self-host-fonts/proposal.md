## Why

`frontend/src/styles/base.css` imports Plus Jakarta Sans and Fira Code from the Google Fonts CDN (`@import url('https://fonts.googleapis.com/...')`). That breaks the offline constraint for the thesis-defense demo and any air-gapped run, and it is the recorded precondition for the Direction-B redesign work. It is unrelated to any single feature, so it is its own change (split out of `improve-order-form` on review).

## What Changes

- Vendor the required font weights as local woff2 files and serve them via `@font-face`; remove the Google Fonts `@import`.
- No visual change: the existing `--font-*` tokens keep pointing at the same families.

## Capabilities

### New Capabilities
- `offline-fonts`: the frontend loads all fonts from self-hosted files, with no request to an external font host.

### Modified Capabilities
<!-- None. -->

## Impact

- **Frontend only:** `frontend/src/styles/base.css` (@font-face block, remove @import), new `frontend/vendor/fonts/*.woff2`.
- No backend change, no new tooling. Adds ~150–300 KB of binary font assets to the repo (acceptable for the offline requirement).
