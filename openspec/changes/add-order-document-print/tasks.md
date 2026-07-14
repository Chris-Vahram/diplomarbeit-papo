## 1. Printable document markup

- [x] 1.1 In `renderOrderConfirmationHtml` (`order-form.js`) and `renderOrderDetailsHtml` (`order-details.js`), wrap the order document (letterhead + Endkunde + item table + Gesamtwert + commission line) in a `.print-document` container; keep the `.simulated-email` block OUTSIDE that wrapper.
- [x] 1.2 Add a print-only letterhead fragment inside `.print-document` (PAPO wordmark, "Auftragsbeleg", order number in `--font-mono`, order date), styled `display:none` on screen.

## 2. Print action

- [x] 2.1 Add a "Beleg drucken" button to the confirmation view and the order-details overlay that calls `window.print()`; the button itself is hidden in print output.

## 3. Print stylesheet

- [x] 3.1 Add a `@media print` block in `base.css`: hide `body *`, reveal `.print-document, .print-document *`, promote it to full width; reveal the print-only letterhead.
- [x] 3.2 Pagination safety: `@page { margin: 18mm }`, `tr { break-inside: avoid }`, repeat `thead` per page; keep the document black-on-white and legible.

## 4. Verify (dev stack + browser print preview)

- [x] 4.1 Partner places an order → "Beleg drucken" → print preview shows only the Auftragsbeleg (no sidebar, nav, buttons, or simulated-e-mail block), with letterhead, order number in monospace, Endkunde, item table, Gesamtwert, and the partner's commission line.
- [x] 4.2 Company opens an order's details → "Beleg drucken" → document prints with commission shown per the existing gating.
- [x] 4.3 Endkunde name and a product name with ä/ö/ü/ß and HTML-special characters print correctly and escaped.
- [x] 4.4 An order long enough to span two pages paginates without cutting a table row; table header repeats.
- [ ] 4.5 Check in Chrome and Firefox print preview; confirm "Als PDF speichern" yields the same clean document.
