## ADDED Requirements

### Requirement: Reusable end-customer records
The system SHALL store end customers (Kunde) as reusable records. A partner SHALL be able to select an existing end customer or create a new one when placing an order.

#### Scenario: Create new customer during order
- **WHEN** a partner creates a new end customer with last name and address
- **THEN** the system stores the customer and makes it selectable for future orders

#### Scenario: Reuse existing customer
- **WHEN** a partner selects a previously created end customer for a new order
- **THEN** the system links the new order to that existing customer without re-entering data

### Requirement: Mandatory customer fields
End-customer records SHALL require at least a last name (Nachname) and an address. Other contact fields (first name, e-mail, phone) are optional.

#### Scenario: Missing mandatory field
- **WHEN** a partner tries to save an end customer without last name or address
- **THEN** the system rejects the save and shows an understandable validation message
