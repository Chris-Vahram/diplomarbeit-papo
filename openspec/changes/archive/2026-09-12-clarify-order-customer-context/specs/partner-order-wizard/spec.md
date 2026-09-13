## ADDED Requirements

### Requirement: Customer context remains visible in ordering
After a valid customer selection the wizard SHALL show “Neue Bestellung für <name>” and label the article-step order panel “Bestellung für <name>”. The name SHALL contain the optional first name followed by the last name, rendered as safe text. Without a confirmed valid customer the main heading SHALL remain “Neue Bestellung”.

#### Scenario: Existing or draft customer selected
- **WHEN** the partner confirms an existing customer or completes a valid new-customer draft
- **THEN** both applicable headings identify that customer

#### Scenario: Customer changes or wizard restarts
- **WHEN** the customer changes
- **THEN** headings update to the active customer without retaining the previous name
- **AND** starting a fresh order restores the neutral heading

#### Scenario: Special characters and missing first name
- **WHEN** the customer has no first name or contains HTML-like text or umlauts
- **THEN** the heading displays the available name as literal text with no empty-name artifact

### Requirement: Catalog precedes order panel
The article step SHALL show the catalog on the left and order panel on the right on desktop, and the catalog before the order panel on mobile. Keyboard and reading order SHALL agree with the visual order. Totals and continuation controls SHALL remain reachable.

#### Scenario: Responsive ordering panels
- **WHEN** the partner uses the article step at desktop and mobile widths
- **THEN** catalog and order panel follow the specified order without overlap or inaccessible totals

### Requirement: Explicit order review instruction
The review step SHALL display “Bitte Bestellung überprüfen und kostenpflichtig aufgeben”. The established final submission action, loading state and order contract SHALL remain unchanged; no payment processing is introduced.

#### Scenario: Review before submission
- **WHEN** the partner reaches the review step
- **THEN** the exact review instruction is displayed before the existing final submission action
