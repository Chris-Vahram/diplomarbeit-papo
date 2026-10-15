## 1. Context and baseline — essential

- [ ] 1.1 Run impeccable `init` (PRODUCT.md) and `document` (DESIGN.md from `Design/` and `base.css`); Tobias reviews both.
- [ ] 1.2 Baseline: impeccable `audit` + `critique` on all portal pages, Playwright screenshots desktop 1280 / mobile 390 to `output/playwright/refine-*-before.png`.

## 2. Foundation — essential

- [ ] 2.1 Role accent: replace hard-wired role palettes in `base.css` with `--color-accent*`; verify both areas.
- [ ] 2.2 Button system: add `button-ghost`/`button-danger`, unify sizes/focus; map row actions; verify all pages.
- [ ] 2.3 Accessibility floor: focus, contrast, touch targets, reduced motion; verify with keyboard and detector.

## 3. Components — essential

- [ ] 3.1 `data-table` stacked cards below 640 px with `data-label`; verify search/filter/pagination/actions on orders, customers, partners, articles.
- [ ] 3.2 Shared states helper and adoption on list/detail pages; verify loading, empty, error + retry.
- [ ] 3.3 Metric card neutral icon without trend.

## 4. Accents

- [ ] 4.1 Dashboard lead metric (company and partner).
- [ ] 4.2 Wizard step states with non-colour indicators.

## 5. Completion — essential

- [ ] 5.1 `impeccable detect` on changed files, Playwright desktop/mobile for every page and the main flows (order, customer edit, logins, mail status), `node --check`, `git diff --check`, `openspec validate refine-portal-frontend --strict`; Tobias visual sign-off.
