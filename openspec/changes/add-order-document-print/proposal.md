## Why

Orders in PAPO exist only on screen. A partner who wants a paper copy for their own records, or the company handling Konfektionierung who wants a pick-slip to put in the box, has nothing to print — printing a portal page today dumps the whole app shell (sidebar, nav, buttons) onto the page. Direction B fixed the visual identity of the order as a document ("Der Auftragsbeleg"); this change makes that document actually printable and saveable as PDF, using nothing but the browser's built-in print-to-PDF (no library, no backend, offline-safe).

## What Changes

- The order confirmation view (partner after placing an order; company after a manual order) and the order-details overlay (both roles) get a "Beleg drucken" action that opens the browser print dialog, from which the user can print or "Als PDF speichern".
- A print stylesheet (`@media print`) isolates the order document: the app shell (header, sidebar, navigation, action buttons, the simulated-e-mail block) is hidden, and only the Auftragsbeleg prints — PAPO letterhead, order number in the monospace motif, Endkunde, article table, Gesamtwert, and (for the partner's own / company view) the commission line.
- Page setup: sensible margins, the item table does not break awkwardly across pages, and the document carries a print-only header/footer (PAPO, "Auftragsbeleg", order number, date) that is not shown on screen.

## Capabilities

### New Capabilities
- `order-document-print`: What a printable/PDF order document must contain and exclude, and from which views it can be produced.

### Modified Capabilities
<!-- None. No order data, submission, or backend behavior changes. -->

## Impact

- **Frontend only:** `frontend/src/styles/base.css` (a `@media print` block + a print-only letterhead fragment), `frontend/src/lib/order-form.js` (`renderOrderConfirmationHtml` — add the print button + print-only header), `frontend/src/lib/order-details.js` (`renderOrderDetailsHtml` — same), and the pages that mount them.
- **No backend change, no new dependency, no external service.** Print-to-PDF is the browser's native capability, so it works fully offline.
- Depends on nothing, but pairs naturally with `improve-order-form` (shared Beleg styling) — either can ship first.
