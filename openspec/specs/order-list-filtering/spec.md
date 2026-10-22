# order-list-filtering Specification

## Purpose
Client-side filters on the company and partner order lists (partner, end customer, time range, status) combined with AND, resettable in one click, with a German empty-result message.
## Requirements
### Requirement: Company order list filters by Partner
The company order list SHALL offer a Partner filter as a dropdown whose options are the distinct partners appearing in the loaded order list (labelled with the partner name, keyed by partner id so equal names stay distinct), sorted alphabetically for the German locale. Selecting a partner SHALL show only that partner's orders; the neutral option ("Alle") SHALL impose no restriction.

#### Scenario: Filter by one partner
- **WHEN** the company selects a partner in the Partner filter dropdown
- **THEN** the table shows only orders placed by (or manually created for) that partner, and the pager reflects the filtered row count

#### Scenario: Neutral selection shows all orders
- **WHEN** the Partner filter is set to the neutral "Alle" option
- **THEN** the Partner filter imposes no restriction and all orders (subject to other active filters) are shown

#### Scenario: Two partners with the same display name
- **WHEN** two different partners with the identical display name both appear in the order list
- **THEN** the dropdown lists them as separate entries and selecting one shows only that partner's orders, not the namesake's

### Requirement: Order lists filter by Endkunde
Both the company and the partner order list SHALL offer an Endkunde filter as a text input that matches the displayed Kunde name (first name + last name) case-insensitively as a substring. Matching SHALL treat umlauts and ß as themselves — no ASCII folding — so "Müller" matches "Müller" but "Muller" does not. An empty input SHALL impose no restriction.

#### Scenario: Substring match on the Kunde name
- **WHEN** the user types "mül" into the Endkunde filter
- **THEN** the table shows only orders whose Endkunde name contains "mül" case-insensitively (e.g. "Müller", "Mülleitner")

#### Scenario: Umlauts are not folded
- **WHEN** the user types "Muller" into the Endkunde filter and the list contains an Endkunde "Müller"
- **THEN** the order for "Müller" is not shown, because ASCII "u" does not match "ü"

#### Scenario: Clearing the input removes the restriction
- **WHEN** the user deletes all text from the Endkunde filter
- **THEN** the Endkunde filter imposes no restriction again

### Requirement: Order lists filter by time range
Both the company and the partner order list SHALL offer a date-range filter (Von/Bis via native date inputs) on the order date. Both bounds SHALL be inclusive; a bound left empty SHALL be open-ended.

#### Scenario: Only Von set
- **WHEN** the user sets the Von date and leaves Bis empty
- **THEN** the table shows only orders with an order date on or after the Von date

#### Scenario: Von and Bis set
- **WHEN** the user sets both Von and Bis
- **THEN** the table shows only orders whose order date lies within the range, boundary dates included

### Requirement: Order lists filter by status
Both the company and the partner order list SHALL offer a status filter as a dropdown with the German status labels ("Eingereicht", "Geprüft", "Abgeschlossen") plus a neutral "Alle" option that imposes no restriction.

#### Scenario: Filter by one status
- **WHEN** the user selects "Geprüft" in the status filter
- **THEN** the table shows only orders in status reviewed

### Requirement: Partner order list has no Partner filter
The partner order list SHALL NOT offer a Partner filter. A partner only ever sees their own orders, so a partner filter would be meaningless.

#### Scenario: Partner views their order list
- **WHEN** a partner opens "Meine Bestellungen"
- **THEN** the filter controls consist of Endkunde, date range, and status — no Partner filter is rendered

### Requirement: Filters combine with AND semantics
All active filters — and, where present, the free-text search — SHALL combine conjunctively: a row is shown only if it matches every active filter. Filters SHALL apply to the full loaded list before pagination; changing any filter SHALL reset the table to the first page.

#### Scenario: Partner and date range combined
- **WHEN** the company sets the Partner filter to a partner and a Von/Bis range
- **THEN** the table shows only that partner's orders inside the range

#### Scenario: Filter change resets pagination
- **WHEN** the user is on page 2 of the table and changes any filter value
- **THEN** the table returns to page 1 and the pager reflects the filtered row count

### Requirement: Filters can be reset in one click
Each order list SHALL offer a "Filter zurücksetzen" control that clears all filter values and the free-text search in one action and returns the table to the first page of the unfiltered list.

#### Scenario: Reset after combined filtering
- **WHEN** the user has set several filters and a search query and clicks "Filter zurücksetzen"
- **THEN** all filter controls return to their neutral/empty state and the table shows the complete list from page 1

### Requirement: Filtered-empty state shows a German message
WHEN active filters match no orders, the table SHALL show a German empty-state message ("Keine Treffer.") together with the still-usable filter controls, distinct from the no-data message shown when no orders exist at all ("Noch keine Bestellungen vorhanden.").

#### Scenario: No order matches the filters
- **WHEN** the user sets a filter combination that matches no loaded order
- **THEN** "Keine Treffer." is shown, the filter controls remain visible and editable, and resetting the filters restores the list

### Requirement: Filtering is client-side only
Order-list filtering SHALL operate entirely on the client over the already-loaded order list. The order-list endpoints SHALL NOT gain filter query parameters or new filter-specific endpoints for this capability.

#### Scenario: Applying a filter causes no request
- **WHEN** the user changes any filter on an order list
- **THEN** no additional HTTP request is made; the visible rows change based on the data already loaded
