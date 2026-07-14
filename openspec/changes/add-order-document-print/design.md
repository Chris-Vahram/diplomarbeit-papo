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
The print button calls `window.print()`. A `@media print` section in `base.css` isolates the order document and hides everything else. This is the smallest possible mechanism and works in every browser's "Als PDF speichern".

**2. Print scoping via a cloned `#print-root`, not a `position:absolute` overlay (revised during implementation — see Risk below).**
The originally planned mechanism — wrap the document in `.print-document`, `@media print { body * { visibility: hidden } .print-document, .print-document * { visibility: visible } .print-document { position: absolute; ... } }` — was implemented, verified correct on screen and for short (single-page) documents, then found broken for genuinely long ones: real print-to-PDF testing (60-line order, not just computed-style assertions) showed content silently truncated to whatever page count the *invisible* underlying page happened to need, because `visibility: hidden` keeps hidden elements in normal flow and `position: absolute` removes `.print-document` from contributing to that flow's height — pagination was driven by the wrong element entirely. See Risks for the full failure mode.

Fixed mechanism: `.print-document` stays an unstyled wrapper (no position/visibility rules of its own). `router.js`'s print-button click handler captures `.print-document` (the button's `previousElementSibling` in both renderers) and, on the `beforeprint` event, clones its `innerHTML` into a fresh `#print-root` div appended directly to `<body>` (a sibling of `#app-shell`, cleaned up again on `afterprint`). `@media print` then does the simple, robust thing: `body > *:not(#print-root) { display: none }`, `#print-root { display: block }`. Because `#print-root` is a plain top-level flow element with nothing else contributing height, Chromium/Firefox paginate it correctly regardless of length. `body.modal-open`'s scroll-lock (`position: fixed; overflow: hidden`) is also reset under `@media print`, since the document is very often printed from inside a still-open modal (order details, manual/shipping confirmation, dashboard quick-order) and that lock alone reproduces the same one-page-cap symptom independently of the `.print-document` mechanism.

**3. Print-only letterhead.**
Add a letterhead fragment (PAPO wordmark, "Auftragsbeleg", order number in `--font-mono`, date) that is `display: none` on screen and shown only under `@media print` (via the shared `.print-only` utility class, also used for the confirmation view's print-only commission line — see Decision 4b), so the screen views are untouched (Non-Goal) while the printout looks like a document. No repeating footer was added — the spec only says a footer MAY repeat, and `thead { display: table-header-group }` (Decision 5) already gives every page an identifying header.

**4. Commission gating unchanged — except a gap found during implementation (4b).**
`renderOrderDetailsHtml` already shows commission on screen, gated on `commissionValue != null`; print reuses that unchanged, no new logic needed.

**4b. `renderOrderConfirmationHtml` had no commission line at all — not on screen, not printable.** The Context above assumed both renderers already show commission "where allowed"; checking the actual source showed the confirmation view never rendered `order.commissionValue`/`commissionPercentUsed` anywhere, on screen or off. The spec requires the printed confirmation to include commission when the viewer is entitled (always true for its two callers: a partner viewing their own order, or the company after a manual order), but the Non-Goal forbids changing what the confirmation shows *on screen*. Resolution: added a commission line inside `.print-document`, gated on `commissionValue != null` (same condition as `renderOrderDetailsHtml`, for consistency) and marked `.print-only` — invisible on screen, included in print. Satisfies both constraints exactly; no judgment call needed since only one reading fits both requirements at once.

**5. Pagination safety.**
`@media print` adds `table { break-inside: auto }` with `tr { break-inside: avoid }` so a row never splits, and `thead { display: table-header-group }` so the header repeats on each page. Margins via `@page { margin: 18mm }`.

**6. Escaping is already correct.**
Both renderers pass product names and Endkunde names through `escapeHtml`. Print reuses the same markup, so no new escaping path is introduced — noted so the reviewer confirms the print fragment doesn't interpolate any raw field.

## Risks / Trade-offs

- **[CONFIRMED, fixed] The `visibility:hidden` + `position:absolute` print-isolation idiom silently truncates long documents.** This is the mechanism Decision 2 originally specified (a well-known, normally-safe pattern). It failed here because `visibility: hidden` does not remove elements from normal flow — the whole (invisible) app page, including a still-"open" modal, keeps occupying real layout height — while `.print-document { position: absolute }` removes *it* from that same flow, contributing nothing to its own ancestor's height. Print pagination is computed from the ancestor chain's flow height, not from the absolutely-positioned overlay's actual content height, so anything beyond however many pages the *invisible* page happened to need was silently dropped — verified concretely with a 60-line order: computed styles all looked correct (`.print-document` measured its true ~3200px height via `getBoundingClientRect`), yet the generated PDF stopped after ~13 rows with no `Gesamtwert` line, and `document.documentElement.scrollHeight` under print-media emulation reported only ~900px (viewport-capped) because `body.modal-open`'s `position: fixed; overflow: hidden` scroll-lock was also still active and hadn't been reset for print. This is exactly the kind of bug that computed-style assertions and short test fixtures cannot catch — only a real print-to-PDF render of a genuinely long order surfaced it. Fixed via Decision 2's `#print-root` clone, which sidesteps the whole class of problem: real pagination testing (60 items, 4 pages, `pdftotext`/`pypdf` byte-verified — all 60 rows present in the correct 15/15/15/15 product distribution, `Gesamtwert` present, thead repeated on all 4 pages) after the fix. **Lesson for future print/PDF work in this codebase: verify with `page.pdf()` + real text extraction on a fixture long enough to force multiple pages, not just computed-style checks on a one-page fixture.**
- **Browser print rendering varies** (margins, color handling). Mitigation: keep the document black-on-white, avoid relying on background colors. Verified in Chrome (via `page.pdf()`, same Blink rendering path as "Als PDF speichern"); Firefox not available in the sandboxed dev environment this change was implemented in — flagged for a manual spot-check.
- **`.simulated-email` must be reliably hidden** — it's inside `renderOrderConfirmationHtml`, outside `.print-document`, so it's never cloned into `#print-root` and never appears in print. Verified explicitly.
- **No customer-facing variant** means if the company prints a Beleg to include in the Endkunde's package, the partner's commission would be on it. Called out as a Non-Goal/follow-up — if the client wants a package slip, that's a second document variant (hide commission, hide internal fields), a small future change.
