## 1. Dashboard trend arrows

- [x] 1.1 Add a `trailingWindowTrend(orders, days = 30)` helper to `frontend/src/lib/dashboard-data.js` reusing `previousPeriod`, `filterByPeriod`, `fulfilledTotals`, `trendInfo`.
- [x] 1.2 In `partner-dashboard-page.js#render()`, when `this.period` is `null`, use this helper for `revenueTrend`/`commissionTrend`/`countTrend` instead of hardcoding `null`; keep `revenue`/`commission`/`count` sourced from `this.base` unchanged.
- [x] 1.3 Manually verify in the running app: dashboard with no period picked shows arrows on Provision/Umsatz/Abgeschlossene Bestellungen; explicit-period behavior unchanged; a partner with zero fulfilled orders in the last 60 days shows no arrow (not a broken one).

## 2. Endkunde picker rebuild

- [x] 2.1 In `partner-order-new-page.js#kundeStepHtml()`, replace the `<select name="kundeId">` with a labelled, scrollable list container (`<ul role="listbox">` of `<button type="button" role="option">` rows), keeping the existing search `<input>` above it.
- [x] 2.2 In `wireKundeStep()`, replace `renderOptions()`'s `<select>`-option building with rendering the same filtered `matches` array as list rows (name + address, unchanged text); wire click (and Enter/Space via native button activation) to set `this.state.kundeId`, update the row's selected visual state, and call `refreshCheckpointBar()`/`refreshOrderHeading()` exactly as the old `select` change handler did.
- [x] 2.3 Preserve "Filter hides selected entry": when the search input changes and the current `kundeId` drops out of `matches`, clear it the same way the old code cleared `select.value`.
- [x] 2.4 Preserve the empty state ("Keine Endkunden gefunden") and keep the "Neuen Kunden anlegen" button reachable regardless of search state.
- [x] 2.5 Update the form submit handler (previously reading `select.value`) to read the list's current selection state instead.
- [x] 2.6 Manually verify: typing filters the list live with no extra step; clicking a row selects it; keyboard-only (Tab + Enter) can complete the step; changing the search after selecting clears an out-of-filter selection.

## 3. Catalog/cart column ratio

- [x] 3.1 In `frontend/src/styles/base.css`, change `.produkte-step-layout`'s `grid-template-columns` from `21rem minmax(0, 1fr)` to give the catalog panel the flexible/majority column and the cart-preview panel a smaller fixed column (e.g. `minmax(0, 1fr) 20rem`), without touching the row-height / `align-items: stretch` mechanism described in the surrounding comments.
- [x] 3.2 Check the mobile/narrow-viewport override for `.produkte-step-layout` (around `base.css:3550`) still stacks correctly after the ratio change.
- [x] 3.3 Manually verify at desktop width: catalog visibly takes up more space than the cart preview, catalog/cart height still stay in sync, no overflow into the buttons below.

## 4. Wizard button labels + profile label

- [x] 4.1 In `partner-order-new-page.js`, rename button text: `kundeStepHtml()`'s submit button "Nächster Schritt" → "Weiter"; `neuerKundeStepHtml()`'s prev/submit buttons → "Zurück" / "Weiter"; `produkteStepHtml()`'s prev/next buttons → "Zurück" / "Weiter"; `uebersichtStepHtml()`'s prev button → "Zurück" (its submit action stays "Bestellung aufgeben").
- [x] 4.2 In `frontend/src/pages/partner-profile-page.js`, change the `<dt>Partnernummer</dt>` label text to `<dt>Benutzername</dt>` (id `profile-number` and its bound value stay unchanged).
- [x] 4.3 Manually verify all four wizard steps and the profile page read as expected in German.

## 5. Wrap-up

- [x] 5.1 Run existing frontend checks/tests if any exist for the touched pages. (No frontend test framework in repo — ran `node --check` on all 4 touched files, all clean.)
- [x] 5.2 Run through the full "Neue Bestellung" flow once end-to-end (existing Endkunde, new Endkunde) to confirm none of the four fixes broke wizard navigation or submission.
