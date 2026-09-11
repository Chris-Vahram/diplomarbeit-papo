# customer-management Specification

## Purpose
TBD - created by archiving change add-dormant-kunde-visibility. Update Purpose after archive.
## Requirements
### Requirement: Last order date per end customer
The partner's end-customer list (`GET /api/partner/customers`) SHALL include, per Endkunde, the date of that Endkunde's most recent order as a nullable `lastOrderDate`. An Endkunde with no orders SHALL have `lastOrderDate = null`. The value SHALL be produced by a single bounded, partner-scoped query with no per-Endkunde lookups (no N+1), and the endpoint SHALL continue to resolve the calling partner server-side (`CurrentPartner.resolve()`), never from a client-supplied partner id. (The specific query shape is a design/implementation detail — see design.md.)

#### Scenario: Endkunde with orders
- **WHEN** a partner requests their end-customer list and an Endkunde has one or more orders
- **THEN** that Endkunde's entry carries the date of the most recent of those orders as `lastOrderDate`

#### Scenario: Endkunde without any order
- **WHEN** a partner requests their end-customer list and an Endkunde has never been ordered for
- **THEN** that Endkunde is still included in the response with `lastOrderDate = null`

#### Scenario: Scoping unchanged
- **WHEN** a partner requests their end-customer list
- **THEN** only Endkunden created by that partner are returned, and the last-order dates reflect only that partner's data

### Requirement: Partner end-customer overview page
The portal SHALL provide a partner-only page "Meine Endkunden" (route `#/partner/customers`, sidebar entry "Endkunden") listing the partner's own Endkunden with name, address, and a "Letzte Bestellung" column. The date SHALL be shown in German format (`dd.MM.yyyy`); an Endkunde without orders SHALL show "Noch keine Bestellung" instead of a date. The list SHALL default to oldest-first ordering (Endkunden without any order first, then oldest to newest last order), so dormant Endkunden surface at the top. Endkunden-name text SHALL be HTML-escaped on output.

#### Scenario: Page shows last order dates
- **WHEN** a partner opens "Meine Endkunden"
- **THEN** each own Endkunde appears with its last order date formatted as `dd.MM.yyyy`, and Endkunden without orders show "Noch keine Bestellung"

#### Scenario: Dormant Endkunden surface first
- **WHEN** the partner opens "Meine Endkunden"
- **THEN** Endkunden without any order appear first, followed by the remaining Endkunden from oldest to newest last order

#### Scenario: Umlaut names render correctly
- **WHEN** an Endkunde is named with German special characters (e.g. "Größbacher", "Müller-Lüdenscheidt")
- **THEN** the name renders with correct umlauts/ß in the table, in search results, and in the filtered dormant view, with no broken characters and no HTML injection

#### Scenario: Deep-link protection
- **WHEN** an unauthenticated user or a company user navigates to `#/partner/customers` directly
- **THEN** the router redirects them away, consistent with the existing role-based route protection

### Requirement: Dormant end-customer filter and marker
The "Meine Endkunden" page SHALL let the partner filter to Endkunden without a recent order: an Endkunde counts as dormant when its last order is strictly older than a fixed client-side threshold (default 60 days) or when it has no orders at all. Dormancy SHALL be computed on whole calendar days (date-only), not elapsed milliseconds, so boundary dates are not misclassified by timezone/DST. Dormant rows SHALL additionally carry a subtle visual marker (badge "Inaktiv" and row highlight) that uses the same threshold and computation as the filter.

#### Scenario: Filter shows dormant Endkunden
- **WHEN** the partner activates the dormant filter ("Länger keine Bestellung")
- **THEN** only Endkunden whose last order is older than the threshold, plus Endkunden with no orders at all, remain visible

#### Scenario: Dormant marker matches the filter
- **WHEN** an Endkunde is dormant per the threshold
- **THEN** its row shows the "Inaktiv" badge and the dormant row highlight, and it appears in the dormant filter result; a non-dormant Endkunde shows neither

### Requirement: Partner can create an end customer from the overview
The partner-only page "Meine Endkunden" SHALL provide an action to create an end customer independently of an order. The form SHALL require a last name and complete address and SHALL accept optional first name, e-mail address, and telephone number. A submitted form SHALL create the end customer for the authenticated partner only and SHALL show the new entry in the overview without requiring a page reload.

#### Scenario: Partner creates an end customer from the overview
- **WHEN** an authenticated partner submits valid end-customer data from "Meine Endkunden"
- **THEN** the system creates the end customer for that partner and shows it in the overview with "Noch keine Bestellung"

#### Scenario: Required end-customer data is missing
- **WHEN** a partner submits the creation form without a last name or a complete address
- **THEN** the form prevents submission and keeps entered valid values visible for correction

#### Scenario: Partner isolation on standalone creation
- **WHEN** a partner creates an end customer from the overview
- **THEN** the end customer is associated with the authenticated partner resolved server-side and is not visible to another partner

#### Scenario: Optional contacts and later reuse
- **WHEN** a partner saves a customer with valid required fields and no optional contact values
- **THEN** creation succeeds without creating an order, and that customer can be selected for a later order

#### Scenario: Complete address validation
- **WHEN** last name, street, house number, postal code, city or country is missing or blank in a direct creation request
- **THEN** the API rejects it with status 400 and no new customer is persisted

#### Scenario: Wrong role or inactive partner
- **WHEN** an unauthenticated user or company user requests the customer list or creation endpoint
- **THEN** access is denied with 401 or 403 respectively
- **AND** an authenticated inactive partner's creation request is denied with 403 without saving a customer

### Requirement: Created customer is immediately visible
After confirmed creation the overview SHALL clear search and activity filters and show page 1 with the created customer first among customers without orders. Other customers SHALL retain their relative oldest-first ordering. Customer text SHALL remain escaped. Repeated names or addresses SHALL remain permissible, without automatic merging or duplicate rejection.

#### Scenario: Search and pagination would hide the customer
- **WHEN** creation succeeds while search or an activity filter is active and more than 20 customers have no orders
- **THEN** the new customer appears on page 1 with “Noch keine Bestellung” after filters are cleared

#### Scenario: Untrusted text remains text
- **WHEN** a customer name or address contains umlauts or HTML-like text
- **THEN** the overview and subsequent order selection display text without executing markup

### Requirement: Safe customer form submission and cancellation
The creation dialog SHALL be keyboard accessible, have an accessible name, place initial focus in the last-name field and return focus to its opener when closed. Before submission cancellation SHALL create nothing. While a creation request is pending the form SHALL prevent further submissions and dismissal. Rejected submissions SHALL preserve entered values and provide German feedback.

#### Scenario: Repeated submission while pending
- **WHEN** the partner repeats the submit action by click or Enter while the request is pending
- **THEN** repeated UI activation starts only one apiFetch creation call and close/cancel/Escape cannot dismiss the pending form
- **AND** the existing single retry after HTTP 401 and token refresh remains unchanged; transport failures do not trigger a new automatic POST

#### Scenario: Existing authentication retry remains bounded
- **WHEN** the first creation request returns HTTP 401 and token refresh succeeds
- **THEN** the existing authenticated retry may issue exactly one replacement POST, and a further failure is reported without another automatic creation attempt

#### Scenario: Cancellation before submission
- **WHEN** the partner closes or cancels the dialog before submitting
- **THEN** no customer is created and the next opening starts with a clean form

#### Scenario: Validation failure
- **WHEN** the API rejects the form
- **THEN** values remain available for correction and controls become usable again

### Requirement: Save outcome and list refresh have distinct feedback
A confirmed save SHALL remain reported as successful even when the following list refresh fails. The overview SHALL provide an explicit GET-only retry that preserves visibility of the created customer. Until that refresh succeeds the overview SHALL prevent starting a second creation. An unknown transport outcome SHALL be reported as uncertain, retain entered values and offer a list check without automatically repeating creation. Older responses SHALL NOT overwrite a newer list or update a page that has been left.

#### Scenario: Save succeeds but refresh fails
- **WHEN** creation succeeds and the following list request fails
- **THEN** the form closes, “Endkunde angelegt. Liste konnte nicht aktualisiert werden.” is displayed with “Liste erneut laden”, and retry fetches the list without creating another customer
- **AND** the successfully refreshed list still shows the originally created customer on page 1

#### Scenario: Retry checks the list only
- **WHEN** the partner selects “Liste erneut laden” after an uncertain save or failed refresh
- **THEN** the retry performs a GET-only list refresh and never repeats the creation POST

#### Scenario: Transport result is unknown
- **WHEN** the creation response is lost and persistence cannot be confirmed
- **THEN** the form retains its values, reports “Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.” and offers a list refresh without automatically repeating creation

#### Scenario: Older refresh cannot overwrite newer state
- **WHEN** an earlier list response arrives after a newer list or after the partner leaves the page
- **THEN** the stale response is ignored and cannot overwrite the current list or another page
