## Context

Two shared renderers already produce the order document markup: `renderOrderConfirmationHtml` in `frontend/src/lib/order-form.js` (shown after placing an order — includes a `.simulated-email` block) and `renderOrderDetailsHtml` in `frontend/src/lib/order-details.js` (the order-details overlay for both roles). Both output an items table (Artikel/Menge/Preis/Summe), Gesamtwert, and — where allowed — a commission line; `renderOrderDetailsHtml` already gates commission on `order.commissionValue != null`. There is no print CSS in the project today. Direction B ("Der Auftragsbeleg") already frames the order as a document with a monospace order-number motif. The whole stack is offline-capable and dependency-light by rule.

## Goals / Non-Goals

**Goals:**
- One-click "Beleg drucken" from the confirmation and the details overlay that yields a clean paper/PDF document via the browser's native print.
- Print output is the order document only — no app chrome, no simulated-e-mail block.
- Correct commission gating, umlauts, and HTML-escaping carried over from the on-screen renderers.
- Zero backend change, zero new dependency, fully offline.

**Non-Goals:**
- No PDF-generation library (jsPDF/html2pdf/pdfmake) and no server-side PDF. Native browser print-to-PDF is sufficient and offline; a library would add weight and an XSS surface for no gain.
- No batch printing of multiple orders, no print of list views.
- No separate customer-facing document variant (a real Endkunde Beleg would hide commission entirely) — flagged as a possible follow-up, not built now.
- No change to what the order-details / confirmation views show on screen.

## Decisions

**1. `window.print()` + a single `@media print` block.**
The print button calls `window.print()`. A `@media print` section in `base.css` sets `display: none` on the app shell (header, sidebar/nav, buttons, `.simulated-email`) and on everything except the order-document section, which is promoted to full width. This is the smallest possible mechanism and works in every browser's "Als PDF speichern".

**2. Print scoping via a wrapper class, not per-element rules.**
Wrap the printable document in a `.print-document` container in both renderers, and in `@media print` hide `body *` then reveal `.print-document, .print-document *` (the standard print-isolation idiom). This keeps the rule set tiny and robust as the surrounding pages evolve, rather than enumerating every chrome element to hide.

**3. Print-only letterhead/footer.**
Add a letterhead fragment (PAPO wordmark, "Auftragsbeleg", order number in `--font-mono`, date) that is `display: none` on screen and shown only under `@media print`, so the screen views are untouched (Non-Goal) while the printout looks like a document. Use `position: fixed` bottom for a small footer (order number + date) if it repeats cleanly; otherwise a static footer — footer repetition is a MAY in the spec, not required.

**4. Commission gating unchanged.**
The print reuses the exact commission-gating each renderer already applies: `renderOrderDetailsHtml` shows it only when `commissionValue != null`; the partner confirmation shows the partner's own commission. No new visibility logic — the printed document inherits whatever the role is already allowed to see, satisfying the spec's "MUST NOT appear when it would not be shown on screen".

**5. Pagination safety.**
`@media print` adds `table { break-inside: auto }` with `tr { break-inside: avoid }` so a row never splits, and `thead { display: table-header-group }` so the header repeats on each page. Margins via `@page { margin: 18mm }`.

**6. Escaping is already correct.**
Both renderers pass product names and Endkunde names through `escapeHtml`. Print reuses the same markup, so no new escaping path is introduced — noted so the reviewer confirms the print fragment doesn't interpolate any raw field.

## Risks / Trade-offs

- **Browser print rendering varies** (margins, color handling). Mitigation: keep the document black-on-white, avoid relying on background colors (`print-color-adjust: exact` only if the calm-green accent must survive; otherwise let it drop for legibility). Verify in at least Chrome + Firefox print preview.
- **`.simulated-email` must be reliably hidden** — it's inside `renderOrderConfirmationHtml`. Because it sits outside `.print-document` (the wrapper goes around the document proper, not the e-mail block), the `body *` hide covers it. Verify explicitly.
- **`position: fixed` footers can overlap content** across pages in some engines. Mitigation: prefer a static footer or reserve bottom margin via `@page`; footer is optional per spec.
- **No customer-facing variant** means if the company prints a Beleg to include in the Endkunde's package, the partner's commission would be on it. Called out as a Non-Goal/follow-up — if the client wants a package slip, that's a second document variant (hide commission, hide internal fields), a small future change.
