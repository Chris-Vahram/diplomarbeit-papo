## ADDED Requirements

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

#### Scenario: Existing authentication retry
- **WHEN** the first HTTP creation attempt receives 401 and the existing token refresh succeeds
- **THEN** the shared API helper may retry that request once, while repeated UI activation remains blocked
- **AND** the page handles the final success or failure without adding another retry

#### Scenario: Validation failure
- **WHEN** the API rejects the form
- **THEN** values remain available for correction and controls become usable again

#### Scenario: Cancellation and reopening
- **WHEN** the partner cancels before submitting and later reopens the dialog
- **THEN** no customer was created, the new form is empty, and keyboard focus stays within the open dialog

### Requirement: Save outcome and list refresh have distinct feedback
A confirmed save SHALL remain reported as successful even when the following list refresh fails. The overview SHALL provide an explicit GET-only retry that preserves visibility of the created customer. Until that refresh succeeds the overview SHALL prevent starting a second creation. An unknown transport outcome SHALL be reported as uncertain, retain entered values and offer a list check without automatically repeating creation. Older responses SHALL NOT overwrite a newer list or update a page that has been left.

#### Scenario: Save succeeds but refresh fails
- **WHEN** creation succeeds and the following list request fails
- **THEN** the form closes, “Endkunde angelegt. Liste konnte nicht aktualisiert werden.” is displayed with “Liste erneut laden”, and retry fetches the list without creating another customer
- **AND** the successfully refreshed list still shows the originally created customer on page 1

#### Scenario: Transport result is unknown
- **WHEN** the creation response is lost and persistence cannot be confirmed
- **THEN** the form retains its values, reports “Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.” and offers a list refresh without automatically repeating the POST

#### Scenario: The client cannot classify a failed save
- **WHEN** the creation call fails without reliable outcome information
- **THEN** the page conservatively reports the save as unconfirmed in addition to the error feedback, without inferring rejection from message text

#### Scenario: Checking the list after an unconfirmed save
- **WHEN** the partner chooses “Liste prüfen” after a failed creation call
- **THEN** the dialog closes without clearing entered values and the list becomes accessible through a GET-only refresh
- **AND** “Eingabe fortsetzen” reopens retained values, while starting a fresh “Neuer Endkunde” form clears them

#### Scenario: An earlier load finishes late
- **WHEN** an earlier list request returns after the successful creation refresh or after leaving the page
- **THEN** it does not replace the newer list or affect the newly opened page
