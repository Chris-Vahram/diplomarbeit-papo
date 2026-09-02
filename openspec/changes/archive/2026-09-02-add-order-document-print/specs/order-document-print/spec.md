## ADDED Requirements

### Requirement: Printable order document

The system SHALL let a user produce a printable order document from the order confirmation view and from the order-details overlay, via a "Beleg drucken" action that opens the browser's native print dialog (from which the user can print on paper or save as PDF). No external library or backend call is used.

#### Scenario: Print from the confirmation

- **WHEN** a partner has placed an order and sees the confirmation, and clicks "Beleg drucken"
- **THEN** the browser print dialog opens showing only the order document, and the user can save it as PDF or print it

#### Scenario: Print from order details

- **WHEN** a user opens an existing order's details overlay and clicks "Beleg drucken"
- **THEN** the browser print dialog opens showing that order's document

### Requirement: Print output excludes app chrome

The printed/PDF output SHALL contain only the order document. The application shell — header, sidebar, navigation, action buttons — and the on-screen simulated-e-mail block MUST NOT appear in the printout.

#### Scenario: Only the Beleg prints

- **WHEN** the print dialog previews the page
- **THEN** no sidebar, navigation, buttons, or simulated-e-mail section is visible, only the Auftragsbeleg

### Requirement: Print document content and letterhead

The printed order document SHALL show a PAPO letterhead, the heading "Auftragsbeleg", the order number in the monospace style, the order date, the Endkunde (name + address), the article table (article, quantity, unit price, line total), and the Gesamtwert. When the viewing role is entitled to see commission (the partner's own order, or the company view), the commission line SHALL be included; it MUST NOT appear when it would not be shown on screen. All texts are German (de-AT) and umlauts render correctly. User-supplied text (product names, Endkunde name) MUST be HTML-escaped.

#### Scenario: Document carries letterhead and order data

- **WHEN** an order document is printed
- **THEN** it shows the PAPO letterhead, "Auftragsbeleg", the order number in monospace, the date, the Endkunde, the article table, and the Gesamtwert

#### Scenario: Umlaut and escaping in print

- **WHEN** the order's Endkunde or a product name contains ä/ö/ü/ß or HTML-special characters
- **THEN** the printed document renders the umlauts correctly and the special characters as literal text, not markup

### Requirement: Readable page layout

The printed document SHALL use sensible page margins and MUST NOT split the article table so a row is cut across a page boundary; a print-only header/footer identifying the document (PAPO, order number, date) MAY repeat but the on-screen view stays unchanged.

#### Scenario: Long order paginates cleanly

- **WHEN** an order with enough articles to exceed one page is printed
- **THEN** article rows are not cut in half across the page break and each page is legible
