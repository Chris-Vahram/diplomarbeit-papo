> Depends on `self-host-fonts` (offline fonts) landing first — the Fira-Code monospace amounts assume local fonts. Font vendoring moved to that change (review split-out).

## 1. Shared money helper

- [x] 1.1 Extract a shared integer-cent sum helper (reuse/move the `sumCents` pattern from `frontend/src/lib/dashboard-data.js`) into a shared lib module; keep dashboard behavior identical.

## 2. Product picker (combobox)

- [x] 2.1 First fix the company manual flow's product mapping: `company-order-list-page.js` currently maps enabled products down to `{id, name, available}` (drops price/category). Carry `price` and `category` through so the picker + totals work in the company flow too.
- [x] 2.2 Build the combobox in `frontend/src/lib/order-form.js`: search input + suggestion list per article line, hidden `<input name="productId">` preserving the FormData contract; case-insensitive name+category filtering over the loaded enabled products.
- [x] 2.3 Suggestion rendering: **name AND category tag both passed through `escapeHtml`**, unit price (de-AT), unavailable products disabled with "(nicht verfügbar)"; cap visible suggestions (~20) with a German narrow-down hint; empty state "Keine Artikel gefunden".
- [x] 2.4 Stale-selection safety: once a product is chosen, editing the visible search text again clears the hidden `productId` (forces re-select) so the line can never display one product while submitting another. Disabled (unavailable) items are skipped during keyboard navigation.
- [x] 2.5 Keyboard + a11y: arrow keys, Enter to choose, Escape to close, outside-click closes; `role="listbox"`/`aria-activedescendant`; visible focus styles.
- [x] 2.6 Wire picker into both flows (`partner-order-new-page.js`, company manual flow in `company-order-list-page.js`) replacing the `<select>`; confirm multi-line orders still pair productId/quantity correctly in `readItemsPayload`.

## 3. Live totals + commission preview

- [x] 3.1 Show unit price and live line total (Menge × Einzelpreis) on each article line; recompute on product choice, quantity input, line add/remove.
- [x] 3.2 Running order total in integer cents, de-AT rendering, `aria-live="polite"` on the total element.
- [x] 3.3 Commission preview line "Ihre Provision (X %): …" from `GET /api/partner/profile` (partner flow) / selected partner's `commissionPercent` (company flow, hidden until a partner is chosen); marked clearly as a Vorschau (estimate).
- [x] 3.4 Commission-parity: compute the preview commission by mirroring the backend exactly — integer cents × percent with HALF_UP rounding (see `CommissionService`), not a naive JS float multiply, so the preview matches the stored CommissionRecord to the cent.
- [x] 3.5 Full parity for the dashboard quick-order modal (`partner-dashboard-page.js`, not in the original proposal's Impact list — added after review): fetch `GET /api/partner/profile` alongside products/customers, wire the same live summary + commission preview as the full order-new page. Stacked layout (not the 2-column `.order-form-layout`) — same shared 42.5rem modal panel constraint as the company manual-order flow.

## 4. Live summary (search + totals scope)

- [x] 4.1 Extract the items-table fragment from `renderOrderConfirmationHtml` into a shared helper used by both the confirmation and the live summary.
- [x] 4.2 Render a live summary panel: article rows, Gesamtwert, commission preview line; empty state "Noch keine Artikel". Styled on the calm-green foundation, responsive to mobile. (The full document/letterhead "Belegvorschau" treatment is deferred to a separate later change — see design.md.)

## 5. Verify (dev stack + Playwright)

- [x] 5.1 Partner flow: search "Müll"-style umlaut term matches, unavailable product not selectable, keyboard-only line completion works.
- [x] 5.2 Compose a 3-line order: line totals, Gesamtwert, and commission preview correct in integer cents; the preview commission equals the stored CommissionRecord (test a custom % that exercises HALF_UP rounding, e.g. 7 % on an odd total).
- [x] 5.3 Stale-selection: choose a product, edit the search text, confirm the line no longer carries the old productId (cannot submit a mismatched line).
- [x] 5.4 Company manual flow: picker shows price/category, commission line follows the selected partner, hidden before selection.
- [x] 5.5 Product name AND category with HTML-special characters render as literal text in picker and summary.
- [x] 5.6 Existing order submission tests / backend suite stay green (no payload change).
- [x] 5.7 Dashboard quick-order modal: commission preview line and summary panel appear and track edits like the full order-new page; modal stays usable (stacked layout, no width regression).
