## Why

Optional enhancement 9.4 from the MVP build's task list, selected by Chris. At the reference client's scale (~800 articles, ~200 partners) the flat, unpaginated lists become unusable once real data is imported — finding one product among 800 rows needs search and paging.

## What Changes

- `data-table` component gains optional client-side search + pagination (`.searchKeys`, `.pageSize`).
- Wired into the three large lists: company product list, company partner list, partner's enabled-products list.
- Client-side only — the existing list endpoints keep returning full lists (~800 rows is a few hundred KB; no backend pagination API needed).
- Search must handle Austrian data: case-insensitive matching including ä/ö/ü/ß.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `product-management`: company product list gains search/pagination.
- `partner-management`: company partner list gains search/pagination.
- `product-enablement`: partner's enabled-products list gains search/pagination.

## Impact

- Frontend only: `frontend/src/components/data-table.js` (additive properties), `company-product-list-page.js`, `company-partner-list-page.js`, `partner-product-list-page.js`.
- No backend changes, no schema changes, no new endpoints.
- **Depends on `fix-audit-hardening`**: same `data-table` render path (escape-by-default lands there first; search then naturally matches raw data values, never rendered HTML).
