## ADDED Requirements

### Requirement: Searchable own customer selection
The first partner order step SHALL provide a labelled customer search, explicit selection and a “Neuen Kunden anlegen” button. Search SHALL match own customers by first name, last name or address without case sensitivity. Matching entries SHALL show name and address. No result SHALL be selected automatically.

#### Scenario: Search identifies a customer
- **WHEN** the partner searches for part of a name or address
- **THEN** only matching own customers are selectable, including matches beyond the first twenty entries

#### Scenario: Empty search and no match
- **WHEN** the search is empty
- **THEN** all own loaded customers are offered without automatic selection
- **AND** a search with no matches shows “Keine Endkunden gefunden” while the new-customer button remains available

#### Scenario: Filter hides selected entry
- **WHEN** a changed search excludes the current local selection
- **THEN** that selection is cleared and advancing requires a new explicit selection

#### Scenario: Keyboard selection
- **WHEN** the partner operates search, selection and actions by keyboard
- **THEN** every control has a visible label and the partner can complete the step without a pointer

### Requirement: Explicit new customer draft flow
The new-customer button SHALL open the existing new-customer step. New customer data SHALL remain a draft until the final order submission. Changing selection mode SHALL preserve the cart and a previously entered draft for explicit resumption, while preventing advancement with an invalid or unconfirmed customer.

#### Scenario: New customer starts without persistence
- **WHEN** the partner chooses “Neuen Kunden anlegen”
- **THEN** the new-customer step opens and neither a customer nor an order is saved yet

#### Scenario: Draft resumption and customer change
- **WHEN** the partner returns to selection after entering a new customer
- **THEN** an explicit draft-resumption action retains that draft and the cart remains unchanged
- **AND** choosing another existing customer replaces only the active customer selection

#### Scenario: Submission and restart
- **WHEN** the final order succeeds and the partner starts another order
- **THEN** search, customer selection, draft and cart start empty

#### Scenario: Unconfirmed customer cannot be skipped
- **WHEN** the first step contains an unconfirmed change or no valid customer
- **THEN** neither a next-step action nor a visited checkpoint permits submission for that unresolved customer
