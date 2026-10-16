## Context

Visual authority is `Design/` (Tobias: `variables.css`, `tokens.json`, guidelines for colours, typography, spacing, components). The running app uses `frontend/src/styles/base.css` (~3,800 lines) and native Web Components. Impeccable found no `PRODUCT.md`/`DESIGN.md`; Chris chose "Verfeinerung + Akzente" (15.10.2026). Mode: **Operate** (task completion, scanability, consistency).

## Goals / Non-Goals

**Goals:** consistency with `Design/`, better mobile use of lists, uniform states, accessibility floor, two targeted accents (dashboards, wizard), no behaviour change.

**Non-Goals:** new colours/fonts/brand, shop surfaces, dark mode, CSS framework or build step, rewriting components that already follow the guidelines.

## Decisions

- **Refine, don't replace:** `Design/` stays authoritative; `DESIGN.md` documents it (impeccable `document`), it does not invent a new system.
- **Role accent:** `.area--company` / `.area--partner` set `--color-accent*` once; components only read the generic tokens (as `Design/guidelines/colors.md` demands). New token `--color-accent-strong` (step 700 in both areas, hover 800) for every fill behind white text, because the core steps fail AA (company 3.2:1, partner 4.1:1).
- **Buttons:** keep the class names used in pages (`button-primary`, `button-secondary`) and add `button-ghost`/`button-danger` instead of renaming to `.btn-*` — fewer touched files, same visual system. `button-primary--hero` stays for the one main action per page.
- **Responsive lists:** below 640 px `data-table` renders compact two-line cards: primary field + amount on top, date/secondary field + status below. Columns declare their mobile slot (`title`, `amount`, `meta`, `status`, or hidden); CSS grid places the existing cells, no second markup path. Fields whose meaning is not obvious keep a short label.
- **States:** one small helper (`lib/states.js`) returning loading/empty/error markup (icon, title, hint) with optional retry; the empty state may carry the page's main action (e.g. "Neue Bestellung"); pages call it instead of ad-hoc text.
- **Accents:** the dashboard lead metric (partner: Provision; company: chosen in 4.1) spans the full metric row on the accent tint with a larger value and trend as text; the other metrics sit below as compact cards. Wizard: done = filled strong accent with ✓, current = white circle with strong-accent border and soft ring, bold label and "Schritt N von M", upcoming = outlined number. Sidebar active item becomes a solid strong-accent fill with white text (matches `Design/`). No new animation beyond existing reduced-motion-safe transitions.
- **Variant decision (16.10.2026):** Chris chose on a static comparison page (impeccable, six questions, A = current): 1B strong accent buttons, 2C solid active nav, 3C lead metric row, 4B wizard ring + step text, 5C compact two-line cards, 6C states with main action. Tobias confirms during review of `DESIGN.md`.
- **Verification:** impeccable `audit` + `critique` before (baseline screenshots in `output/playwright/`) and after; `impeccable detect` on changed files; Playwright desktop 1280 / mobile 390 for every page; Tobias signs off visually.

## Risks / Trade-offs

- [Risk] Conflicts with Tobias' parallel shop work in `base.css`. Mitigation: small, scoped commits per area; shop CSS untouched.
- [Risk] Hidden behaviour change via markup edits. Mitigation: visual-only diffs, existing tests + Playwright flows (order, customer edit, logins) re-run.
- [Trade-off] Class names stay `button-*` instead of the guideline's `.btn-*`; documented in `DESIGN.md`.

## Migration Plan

Pure frontend; each area is its own commit and can be reverted alone.
