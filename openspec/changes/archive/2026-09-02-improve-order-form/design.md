## Context

The shared order-composition code lives in `frontend/src/lib/order-form.js`, consumed by `partner-order-new-page.js` (partner flow) and `company-order-list-page.js` (company manual flow). Article lines are `<select name="productId">` + `<input name="quantity">`; payloads are read via `FormData` (`readItemsPayload` uses `data.getAll("productId")`/`getAll("quantity")`). No price is visible before submit. All required data already reaches the browser: the enabled-products list (`ProductDto`: id, name, price, description, available, category, imageUrl) and the partner profile (`PartnerDto.commissionPercent` on `GET /api/partner/profile`); the company partner list carries `commissionPercent` too. The post-submit confirmation is rendered by `renderOrderConfirmationHtml` (items table, Gesamtwert, simulated e-mail). `base.css:8` imports Plus Jakarta Sans + Fira Code from the Google Fonts CDN; `--font-mono` already exists as a token. Client meeting 10.07.2026 demands a clearer product-adding experience (Suchleiste, Filter); the 2026-07-09 design review fixed direction B "Der Auftragsbeleg" with Q1 (live money preview) as the highest-leverage change and self-hosted fonts as a precondition for design work.

## Goals / Non-Goals

**Goals:**
- Type-to-search product picking that stays usable at ~800 articles, keyboard-first, umlaut-correct, escaped, with no stale hidden selection.
- Live money: unit price + line total per line, running order total, commission preview matching the backend to the cent — the "why not just e-mail" answer, visible while composing.
- A live order-summary panel that tracks the composed order.
- No backend change; the submit payload contract is untouched.

**Non-Goals:**
- No product-picker rework elsewhere (enablement matrix, product management pages keep their UI).
- No image thumbnails in the picker suggestions (data exists; deliberate restraint — flag as possible later polish).
- No backend search endpoint — the full enabled list is already loaded client-side; at ≤800 items, in-memory filtering is instant.
- No change to validation rules, double-submit guard, or confirmation/e-mail content.
- **Font self-hosting split out** into the `self-host-fonts` change (review: unrelated to the picker; it's the offline precondition for all design work). This change depends on it landing first.
- **The full document/letterhead "Belegvorschau" styling is deferred** to a later change (review: bigger than the client's "Suchleiste, Filter" ask). This change ships search + live totals + commission; the summary is a plain panel, not the letterhead document.

## Decisions

**1. Combobox, not a filtered `<select>`.**
Each article line gets a search `<input>` + suggestion list (positioned dropdown), replacing the `<select>`. A hidden `<input name="productId">` keeps the exact `FormData` contract so `readItemsPayload` and both page flows work unchanged. Filtering = case-insensitive `String.includes` on name and category over the already-loaded enabled products — plain JS string comparison is inherently umlaut-correct; no ASCII folding. Both name and category in a suggestion are rendered via `escapeHtml` (review: the category tag was previously unescaped). **Stale-selection guard:** the hidden `productId` is set only on an explicit choice and cleared whenever the search text is edited afterwards, so a line can never show one product and submit another; disabled (unavailable) items are not selectable and are skipped in keyboard navigation. Rationale: a native `<select>` cannot search, and `<datalist>` has broken keyboard/styling behavior across browsers; a light-DOM combobox with `role="listbox"`/`aria-activedescendant` is small, testable, and serves the open a11y findings.

**1b. Company flow must carry price/category.**
`company-order-list-page.js` currently maps enabled products down to `{id, name, available}` (drops price/category), so the picker + totals would break there. First step: preserve `price` and `category` in that mapping. Data already exists on `ProductDto`.

**2. Category doubles as the "filter".**
The meeting asked for "Suchleiste, Filter". Search covers name + category in one field; suggestions render the category as a small tag. A separate category dropdown is deliberately omitted (one control, non-technical users) — noted as an easy follow-up if the client insists on an explicit filter after seeing it.

**3. Money in integer cents, with backend-matching commission rounding.**
Line totals and order total compute in integer cents (project rule; float `reduce` is banned). The `sumCents` pattern exists in `dashboard-data.js` as a module-private function — extract/mirror a small shared helper rather than duplicating. **Commission preview must reproduce the backend exactly** (review): the backend uses `BigDecimal` with `HALF_UP` (`CommissionService`). A naive `totalCents * percent / 100` JS float can differ by a cent. Compute the preview as integer cents with explicit half-up rounding (e.g. `Math.round(totalCents * percentInBasisPoints / 10000)` with a half-up tie rule) so the preview equals the stored `CommissionRecord`. Labelled "Ihre Provision (X %)" with a "(Vorschau)" hint; the record stays server-created, this is display only.

**4. Live summary panel (not the letterhead document — deferred).**
Reuse the confirmation's items-table structure (Artikel / Menge / Preis / Summe) + Gesamtwert + commission line, rendered live in a simple panel on the calm-green foundation. Empty state: "Noch keine Artikel". Extract the items-table fragment into a shared helper both the confirmation and the summary use, so they can't drift. The document/letterhead styling (PAPO letterhead, Fira-Code order-number motif, "becomes the confirmation" effect from Direction B) is a **separate deferred change** — out of scope here.

**5. Preview is an estimate; the confirmation is authoritative.**
Review finding: the browser filters on prices fetched at page load, while the backend snapshots the *current* DB price at submit (`OrderService`). If the company changes a price mid-composition, the summary and the confirmation legitimately differ. This is the same accepted staleness as `commissionPercent` below — the spec states the summary is an estimate and the confirmation authoritative, rather than promising byte-equality (no client-side quote/version mechanism, which would be out of proportion for a ~14-orders/day prototype).

**6. Commission preview in the company flow uses the selected partner's percent.**
The company already sees all commission records; showing the percent it set itself leaks nothing. When no partner is selected yet, the line is hidden.

**7. Live region for the total.**
The order-total element gets `aria-live="polite"` so screen readers hear updates — cheapest slice of the open a11y findings, in scope because the element is being built anyway.

## Risks / Trade-offs

- **Combobox complexity in vanilla JS** (focus management, outside-click close, keyboard loop). Mitigation: one shared function in `order-form.js` used by both flows; keep behavior minimal (filter, choose, close) — no async, no virtual scrolling (≤800 items render fine; cap visible suggestions at ~20 with a "weitere Treffer eingrenzen…" hint).
- **FormData contract depends on DOM order** of hidden `productId` inputs vs quantity inputs — preserved by keeping both inside the same `.order-line` row exactly as today; verify `readItemsPayload` pairing with a multi-line order.
- **Summary duplicating confirmation markup** could drift from `renderOrderConfirmationHtml`. Mitigation: extract the items-table fragment into a shared helper both renderers use.
- **Preview vs confirmation drift** on a mid-composition price/commission change — handled by Decision 5 (preview = estimate, confirmation authoritative). Verify the confirmation, not the preview, is what's stored.
- **commissionPercent / price staleness**: profile and product prices are fetched once per page load; a mid-session central change lags until reload. Accepted — the server-side snapshot is authoritative.
