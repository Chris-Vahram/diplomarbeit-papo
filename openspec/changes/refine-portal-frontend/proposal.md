## Why

The portal grew feature by feature (dashboards, wizard, customer list, mails, logins). Tobias' design system (`Design/`) defines one role-accent mechanism and one button system, but the code mixes `.button-primary`, row-action buttons and hard-wired role palettes (`.area--partner … var(--color-accent-600)`). Metric cards show empty icon circles without a trend, wide tables scroll sideways on phones, and loading/empty/error states look different on every page. Tobias agreed (15.10.2026) to a refinement with targeted accents, planned with the impeccable design workflow.

## What Changes

- **Refinement, not a new look:** Tobias' tokens, colours (green company / blue partner), fonts and components stay. No new visual world.
- One role accent: every role-aware element reads `--color-accent*`; hard-wired role palettes removed.
- One button system (primary, secondary, ghost, danger, plus one hero size for the main page action), consistent sizes and focus states.
- Metric cards without empty decoration: neutral icon when there is no trend.
- Main lists (orders, end customers, partners, articles) become stacked cards below 640 px instead of sideways scrolling.
- One shared pattern for loading, empty and error states with German text and a retry action.
- Accessibility floor: visible focus, text contrast ≥ 4.5:1, touch targets ≥ 44 px, `prefers-reduced-motion`.
- Accents: clearer hierarchy on both dashboards (one lead metric), clearer progress in the order wizard.
- Durable design context for future work: `PRODUCT.md` and `DESIGN.md` derived from `Design/` and the code.
- Not included: shop pages/builder (Tobias' shop changes), new features, copy changes beyond state texts, framework or build step.

## Capabilities

### New Capabilities

- `portal-visual-design`: role accent, button system, responsive lists, shared states, accessibility floor and dashboard/wizard accents for the internal portal.

### Modified Capabilities

None. Behaviour and data stay unchanged.

## Impact

Frontend only: `frontend/src/styles/base.css`, `frontend/src/components/*` (metric-card, data-table), pages that use buttons/states, new `PRODUCT.md`/`DESIGN.md`. No backend, API or schema change. Implementation by Chris with impeccable, visual acceptance by Tobias. Must not touch shop files from Tobias' changes.
