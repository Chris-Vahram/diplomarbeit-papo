## 1. Context and baseline — essential

- [x] 1.1 Run impeccable `init` (PRODUCT.md) and `document` (DESIGN.md from `Design/` and `base.css`); Tobias reviews both.
- [x] 1.2 Baseline: impeccable `audit` + `critique` on all portal pages, Playwright screenshots desktop 1280 / mobile 390 to `output/playwright/refine-*-before.png`.

## 2. Foundation — essential

- [x] 2.1 Role accent: replace hard-wired role palettes in `base.css` with `--color-accent*`; verify both areas.
- [x] 2.2 Button system: add `button-ghost`/`button-danger`, unify sizes/focus; map row actions; verify all pages.
- [x] 2.3 Accessibility floor: focus, contrast, touch targets, reduced motion; verify with keyboard and detector.

## 3. Components — essential

- [x] 3.1 `data-table` stacked cards below 640 px with `data-label`; verify search/filter/pagination/actions on orders, customers, partners, articles.
- [x] 3.2 Shared states helper and adoption on list/detail pages; verify loading, empty, error + retry.
- [x] 3.3 Metric card neutral icon without trend.

## 4. Accents

- [x] 4.1 Dashboard lead metric (company and partner).
- [x] 4.2 Wizard step states with non-colour indicators.

## 5. Completion — essential

- [x] 5.1 `impeccable detect` on changed files, Playwright desktop/mobile for every page and the main flows (order, customer edit, logins, mail status), `node --check`, `git diff --check`, `openspec validate refine-portal-frontend --strict`; Tobias visual sign-off.
- [ ] 5.2 Tobias visual sign-off on the implemented state (screenshots `output/playwright/refine-*-after.png`); then archive.
