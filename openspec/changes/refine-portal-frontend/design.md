## Context

Visual authority is `Design/` (Tobias: `variables.css`, `tokens.json`, guidelines for colours, typography, spacing, components). The running app uses `frontend/src/styles/base.css` (~3,800 lines) and native Web Components. Impeccable found no `PRODUCT.md`/`DESIGN.md`; Chris chose "Verfeinerung + Akzente" (15.10.2026). Mode: **Operate** (task completion, scanability, consistency).

## Goals / Non-Goals

**Goals:** consistency with `Design/`, better mobile use of lists, uniform states, accessibility floor, two targeted accents (dashboards, wizard), no behaviour change.

**Non-Goals:** new colours/fonts/brand, shop surfaces, dark mode, CSS framework or build step, rewriting components that already follow the guidelines.

## Decisions

- **Refine, don't replace:** `Design/` stays authoritative; `DESIGN.md` documents it (impeccable `document`), it does not invent a new system.
- **Role accent:** `.area--company` / `.area--partner` set `--color-accent*` once; components only read the generic tokens (as `Design/guidelines/colors.md` demands).
- **Buttons:** keep the class names used in pages (`button-primary`, `button-secondary`) and add `button-ghost`/`button-danger` instead of renaming to `.btn-*` — fewer touched files, same visual system. `button-primary--hero` stays for the one main action per page.
- **Responsive lists:** `data-table` renders stacked cards below 640 px via CSS (`data-label` per cell), no second markup path.
- **States:** one small helper (`lib/states.js`) returning loading/empty/error markup with optional retry; pages call it instead of ad-hoc text.
- **Accents:** dashboard lead metric gets more weight (size/colour), wizard steps show done/current/next clearly. No new animation beyond existing reduced-motion-safe transitions.
- **Verification:** impeccable `audit` + `critique` before (baseline screenshots in `output/playwright/`) and after; `impeccable detect` on changed files; Playwright desktop 1280 / mobile 390 for every page; Tobias signs off visually.

## Risks / Trade-offs

- [Risk] Conflicts with Tobias' parallel shop work in `base.css`. Mitigation: small, scoped commits per area; shop CSS untouched.
- [Risk] Hidden behaviour change via markup edits. Mitigation: visual-only diffs, existing tests + Playwright flows (order, customer edit, logins) re-run.
- [Trade-off] Class names stay `button-*` instead of the guideline's `.btn-*`; documented in `DESIGN.md`.

## Migration Plan

Pure frontend; each area is its own commit and can be reverted alone.
