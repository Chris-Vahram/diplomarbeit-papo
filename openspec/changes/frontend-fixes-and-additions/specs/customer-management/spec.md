## MODIFIED Requirements

### Requirement: Mandatory customer fields
End-customer records SHALL require at least a last name (Nachname) and a structured address (street, house number, postal code, city, country). Other contact fields (first name, e-mail, phone) are optional.

#### Scenario: Missing mandatory field
- **WHEN** a partner tries to save an end customer without last name or without one of the mandatory address parts (street, house number, postal code, city, country)
- **THEN** the system rejects the save and shows an understandable validation message

#### Scenario: Structured address stored and displayed
- **WHEN** a partner or the company views an end customer's address (in forms, order confirmations, or order details)
- **THEN** the system shows the address composed from its structured parts, not a single free-text field

## ADDED Requirements

### Requirement: "Endkunde" terminology
The system SHALL refer to end-customer records as "Endkunde" (not "Kunde") in all user-facing text, in both the partner area and the company area, including labels, table columns, form fields, and confirmation text.

#### Scenario: Partner-area terminology
- **WHEN** a partner views end-customer selection, order creation, or order details
- **THEN** all labels referring to the end customer say "Endkunde", not "Kunde"

#### Scenario: Company-area terminology
- **WHEN** the company views the order list, order details, or manual order creation
- **THEN** all labels referring to the end customer say "Endkunde", not "Kunde"
