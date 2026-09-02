## 1. Wizard shell and state (essential)

- [x] 1.1 Design the wizard state shape on `PartnerOrderNewPage`: `{ step, maxStepReached, kundeMode, kundeId, newKunde, cart }`.
- [x] 1.2 Add a `renderStep()` dispatcher that clears `#order-content` and renders the markup for the current `step` from state (replaces the current single `renderForm()`).
- [x] 1.3 Add `goToStep(index)` / `advanceTo(index)` helpers: `goToStep` allowed only for `index <= maxStepReached`; advancing past the current step raises `maxStepReached` and re-renders.
- [x] 1.4 Wire `load()` to initialize wizard state and call `renderStep()` for the initial "Endkunde wählen" step instead of the old `renderForm()`.

## 2. Progress bar / checkpoints (essential)

- [x] 2.1 Build a checkpoint-bar render function: 5 slots (Endkunde wählen, Neuer Endkunde, Produkte wählen, Übersicht, Erfolg), rendered above the step content on every step.
- [x] 2.2 Mark checkpoints as done / current / upcoming based on `step` and `maxStepReached`; grey/inactive-style the "Neuer Endkunde" checkpoint whenever `kundeMode !== "new"`.
- [x] 2.3 Make done checkpoints clickable, calling `goToStep(index)`; make current/upcoming checkpoints non-interactive.
- [x] 2.4 Add CSS for the checkpoint bar (horizontal, top of content area, responsive down to a reasonably narrow viewport).

## 3. Step: Endkunde wählen (essential)

- [x] 3.1 Render this step using the existing `kundePickerHtml`/`wireKundePicker` from `order-form.js`, reused as-is for the field markup and radio toggle behavior.
- [x] 3.2 On "Weiter": if `kundeMode === "existing"`, validate a Kunde is selected, store `kundeId` in wizard state, advance to "Produkte wählen".
- [x] 3.3 On "Weiter": if `kundeMode === "new"`, advance to the "Neuer Endkunde" step instead of validating/submitting here.
- [x] 3.4 When re-entering this step (back-navigation), pre-select the stored `kundeMode`/`kundeId`, or show "neu anlegen" pre-selected with the Endkunde's name if a `newKunde` draft exists in state.

## 4. Step: Neuer Endkunde (essential)

- [x] 4.1 Render the "new Kunde" fields (reuse the relevant portion of `kundePickerHtml`'s new-kunde block / `readAddress`/`addressFieldsHtml` from `address-fields.js`) as its own full step.
- [x] 4.2 On confirm: validate required fields (Nachname + address, per FSD), store the draft into `newKunde` in wizard state, advance directly to "Produkte wählen" (per clarified requirement — never back to "Endkunde wählen").
- [x] 4.3 When re-entering this step (back-navigation), pre-fill all fields from the stored `newKunde` draft.
- [x] 4.4 Update the "Endkunde wählen" checkpoint's completed-state label to show the new Endkunde's name when `kundeMode === "new"`.

## 5. Step: Produkte wählen (essential)

- [x] 5.1 Split `order-cart.js`'s combined grid+cart markup: extract a standalone catalog-grid step view (search, category filter, product cards, add-to-cart) without the side-by-side cart panel.
- [x] 5.2 Keep `filterCatalog`, `productCardHtml`, `catalogGridHtml`, `thumbHtml`, `handleThumbError`, `CATALOG_RENDER_CAP` logic unchanged; only change how/where the grid mounts.
- [x] 5.3 Wire add-to-cart to push into the wizard's `cart` state (reuse the existing cart array shape `{ product, quantity }`).
- [x] 5.4 Add a persistent "in Warenkorb: N Artikel" indicator on this step (so the partner has feedback without needing to already be on Übersicht).
- [x] 5.5 "Weiter" validates `cart.length > 0` before advancing to "Übersicht".

## 6. Step: Übersicht (essential)

- [x] 6.1 Render the cart lines + totals + commission preview for this step, reusing `cartLineHtml`, `cartTotalsHtml`, `cartTotalCents`, `clampQuantity` from `order-cart.js` unchanged.
- [x] 6.2 Wire quantity-change/remove interactions on this step's cart lines (same behavior as today, scoped to this step's DOM).
- [x] 6.3 Add "Bestellung aufgeben" as a plain button (drop the old `form="order-form"` cross-element trick — no longer needed once this step is self-contained).
- [x] 6.4 Implement `handleSubmit`: build the payload from wizard state (`kundeId` or `newKunde`, `cart`), call `POST /partner/orders`, show inline spinner/disabled state on the button while in flight, guard against duplicate submission.
- [x] 6.5 On success: store the returned order, advance to "Erfolg". On failure: show the error via `showToast` (existing pattern), re-enable the button, stay on "Übersicht".

## 7. Step: Erfolg (essential)

- [x] 7.1 Render `renderOrderConfirmationHtml(order, ...)` (unchanged from today) as this step's content.
- [x] 7.2 Replace the current inline `#new-order-btn` handler with one that fully resets wizard state (`step`, `maxStepReached`, `kundeMode`, `kundeId`, `newKunde`, `cart` all cleared) and calls `renderStep()` back at "Endkunde wählen".
- [x] 7.3 Ensure the checkpoint bar on this step renders with no clickable checkpoints (no back-navigation from Erfolg, per spec).

## 8. Completion feedback and motion (essential core, confetti optional polish)

- [x] 8.1 Add a `@media (prefers-reduced-motion: reduce)` block to `base.css` that disables/shortens the existing `--duration-*`/`--ease-*`-based transitions plus the new step-transition/checkmark/confetti animations. (essential — foundation for the rest of this section)
- [x] 8.2 Add a step-transition animation (e.g. slide/fade) for the content area on `renderStep()` calls. (essential)
- [x] 8.3 Add a brief checkmark/"done" animation on a checkpoint when its step is completed. (essential)
- [x] 8.4 Add a CSS/DOM confetti effect on the "Erfolg" step (small burst of styled `<span>` elements, animated and then removed), skipped entirely under reduced-motion. (optional polish — cut first if time is short)

## 9. Cleanup and manual verification

- [x] 9.1 Remove now-unused code paths in `partner-order-new-page.js`/`order-cart.js` left over from the flat single-page layout (old `renderForm()`, the `form="order-form"` submit-button wiring) once the wizard fully replaces them.
- [x] 9.2 Confirm `company-order-list-page.js` still renders and submits manual orders correctly (its `articleLinesHtml`/`wireArticleLines` path is untouched, but shared functions in `order-form.js` must not have regressed).
- [x] 9.3 Manual smoke test (no frontend test framework in this project): full happy path with an existing Endkunde; full happy path creating a new Endkunde; back-navigation via checkpoints at each step confirming no data loss; empty-cart block on "Produkte wählen"; failed submission (e.g. stop backend mid-flow) shows error and stays on "Übersicht"; `prefers-reduced-motion` emulated in devtools shows no animations.
- [x] 9.4 Run `partnerportal-quality` review skill against the diff before merge (role scoping, German texts, money handling, MVP scope discipline).
