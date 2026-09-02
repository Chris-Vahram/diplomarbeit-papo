## Context

`partner-order-new-page.js` currently renders everything at once: `kundePickerHtml`/`wireKundePicker` from `order-form.js` (Endkunde radio + inline new-Endkunde form that pops in and pushes content down), followed by `orderCartHtml`/`wireOrderCart` from `order-cart.js` (search+filter+grid on the left 2/3, cart panel on the right 1/3). The cart's submit button lives *outside* the `<form>` element (`form="order-form"` attribute) purely so the cart panel can render independently of the Endkunde form.

The company's manual order-creation flow (`company-order-list-page.js`) uses a different, older code path in the same file — `articleLinesHtml`/`wireArticleLines`, a fieldset of combobox lines rather than a grid+cart. That flow is explicitly out of scope for this change (proposal.md, confirmed with user): company staff are internal power users and keep the dense form.

Kunde creation is not a separate API call: `newKunde` travels inside the `POST /partner/orders` payload and the backend creates the Kunde as part of order creation (`OrderService`, `OrderRequest`). This matters for the wizard: the "new Endkunde" data is a client-side draft the whole time, not a persisted record — which is exactly why it can be carried forward through wizard state without an extra network round-trip.

No `prefers-reduced-motion` handling exists anywhere in `frontend/src/styles/base.css` today.

## Goals / Non-Goals

**Goals:**
- Turn the flat order-creation page into a 5-checkpoint wizard (Endkunde wählen → Neuer Endkunde [conditional] → Produkte wählen → Übersicht → Erfolg) confined to the content area, sidebar/topbar chrome unchanged.
- Preserve all entered data (Endkunde selection/draft, cart) across forward/back navigation, including jump-to-checkpoint navigation.
- Reuse existing catalog/cart and Endkunde-picker/new-Endkunde building blocks from `order-cart.js`/`order-form.js` rather than rewriting their internals.
- Add lightweight completion feedback (per-step checkmark animation, confetti on success) with a `prefers-reduced-motion` fallback.
- Keep the `POST /partner/orders` contract, validation rules, and backend behavior completely unchanged.

**Non-Goals:**
- No changes to `company-order-list-page.js` or its `articleLinesHtml`/`wireArticleLines` code path.
- No literal game mechanics (points, badges, streaks, persisted progress) — confirmed with user as "delight only."
- No backend changes of any kind.
- No new external animation/confetti library — fixed stack has no npm/build step, so any animation is hand-rolled CSS/DOM.

## Decisions

### 1. State lives in one JS object, not the DOM
A `PartnerOrderNewPage` instance holds a single wizard state object: `{ step, kundeMode, kundeId, newKunde, cart }`. Each step is rendered fresh from this state on every transition (`this.content.innerHTML = renderStep(...)`), rather than keeping all 5 steps mounted-but-hidden in the DOM.

**Alternative considered**: keep all step panels in the DOM simultaneously (single `<form>`, toggle `hidden`), leaning on native HTML5 validation (`required`, `setCustomValidity`) across the whole form at once, matching the pattern `wireKundePicker` already uses today.
**Why rejected**: back-navigation/checkpoint-jumping and "don't lose work" are the two hardest requirements here; a single source-of-truth JS object makes both trivial (re-render from state) and keeps per-step validation scoped to that step's own data, matching the spec's "per-step validation gates progression" requirement. The cost is manually re-invoking `wireKundePicker`/`wireArticleLines`-style validation logic per step instead of getting it for free from one big form — acceptable, since each step already validates independently per the new spec.

### 2. Reuse `order-form.js`/`order-cart.js` functions as step bodies, not full-page includes
The wizard's "Endkunde wählen" + "Neuer Endkunde" steps call `kundePickerHtml`/`wireKundePicker` (unchanged), and "Produkte wählen"/"Übersicht" call `orderCartHtml`/`wireOrderCart`-derived rendering, but split across two steps instead of one grid+cart-side-by-side layout: "Produkte wählen" shows the catalog grid (no cart panel needed there since the cart is the *next* step's content), "Übersicht" shows the cart/summary + submit button.

**Alternative considered**: rewrite catalog/cart rendering from scratch for the wizard.
**Why rejected**: `order-cart.js` already has correct cents-based totals, quantity clamping, image-fallback handling, and category filtering (design.md risk notes in that file). Splitting its existing pieces across two step views is far cheaper and less error-prone than reimplementing money math and product search. The catalog-grid/cart-panel split in `orderCartHtml` needs light restructuring so each half can render as its own step, but the underlying `wireOrderCart`-style add/remove/quantity logic is preserved.

### 3. Submit button loses its `form="order-form"` cross-element trick
Because the wizard renders one step at a time, "Übersicht" is a single self-contained view: the submit button can live inside a normal `<form>` (or just as a plain button wired to `handleSubmit`) with no need for the current `form="order-form"` attribute workaround that exists only because the cart panel used to sit outside the `<form>` in the flat layout.

### 4. New-Endkunde completion advances directly to Produkte, and is remembered as "selected"
Per the clarified requirement, finishing "Neuer Endkunde" sets `kundeMode: "new"` with the filled `newKunde` object in wizard state and moves `step` straight to `"produkte"` — it never routes back through "Endkunde wählen". The "Endkunde wählen" checkpoint's completed-state label (and the step's own re-render if the partner navigates back to it) reads from `newKunde.lastName`/`firstName` in state so it reflects "Neuer Endkunde: <Name>" rather than reopening a blank picker.

### 5. Checkpoint model: fixed slots, greyed when unused, click-to-jump only for visited steps
The progress bar always renders 5 checkpoint slots. "Neuer Endkunde" is rendered in a distinct greyed/inactive visual state whenever `kundeMode !== "new"`. A checkpoint is clickable only if its step index is ≤ the highest step index reached so far in this session (tracked as `maxStepReached` alongside `step` in state) — this is what blocks forward-skipping past an incomplete step while still allowing free backward movement, matching the spec's two navigation requirements.

### 6. Motion: CSS-only, gated by a shared `prefers-reduced-motion` block
Step-completion checkmarks and the confetti effect are implemented as CSS animations/transitions plus a small amount of DOM (e.g. generating a handful of absolutely-positioned confetti `<span>` elements on success, styled and animated via CSS, removed after the animation ends). A single `@media (prefers-reduced-motion: reduce)` block in `base.css` disables/shortcuts all of it — new addition to that file (none exists today), applied broadly enough to also cover the existing `--duration-*`/`--ease-*` transitions already used elsewhere on the page (step-swap transition itself included), not just the new confetti/checkmark bits.

**Alternative considered**: a JS-driven canvas confetti effect for more visual richness.
**Why rejected**: no external library allowed (fixed stack), and a canvas implementation is meaningfully more code/complexity than the payoff justifies for what's meant to be a small, tasteful moment — a CSS/DOM approach is simpler to keep in sync with `prefers-reduced-motion` and easier to review/maintain given the team size (Chris + Tobias).

## Risks / Trade-offs

- **[Risk]** Splitting `order-cart.js`'s combined grid+cart view into two separate step views (Produkte wählen vs. Übersicht) requires restructuring `orderCartHtml`/`wireOrderCart`'s internals (currently one function produces both halves together) → **Mitigation**: keep the money/quantity/image-fallback logic (`cartTotalCents`, `clampQuantity`, `handleThumbError`, etc.) untouched; only split the *markup assembly and mount points* so each step calls the piece it needs.
- **[Risk]** Manual per-step validation (decision #1) duplicates some logic `wireKundePicker`/`wireArticleLines` got "for free" from native form validation across one big `<form>` → **Mitigation**: keep validation predicates small and colocated with each step's render function; this is what the spec's "per-step validation gates progression" requirement calls for regardless of implementation approach.
- **[Risk]** Hand-rolled confetti/checkmark animations are easy to over-build or under-test relative to their value → **Mitigation**: explicitly scoped as "delight only" (non-goal: no game mechanics), kept CSS-first and small; `tasks.md` should treat this as one bounded task, not an open-ended polish pass.
- **[Trade-off]** Re-rendering each step's full markup on every transition (decision #1) is simpler and safer for data-loss prevention than an incremental-DOM-update approach, at the cost of some potential flicker/state-reset for sub-widgets (e.g. product search field focus) across a transition — acceptable given the page's traffic pattern (one partner, one order at a time, not a hot path needing fine-grained DOM diffing).

## Migration Plan

Frontend-only, single-page change with no persisted data or backend contract involved — no migration/rollback beyond normal deploy of the updated static frontend assets. No feature flag needed: the old flat layout is fully replaced in `partner-order-new-page.js`, not run alongside it (the company flow, which stays on the old pattern, is a separate file/code path already).

## Open Questions

None outstanding — scope, step model, navigation, gamification level, and company-flow exclusion were all confirmed with the user during exploration prior to this proposal.
