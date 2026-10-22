# partner-order-wizard Specification

## Purpose
Guided order creation in steps (end customer, articles, overview) with checkpoint progress, back-navigation without data loss, per-step validation and completion feedback.

## Requirements

### Requirement: Stepped order-creation flow
The partner order-creation page SHALL present the order-creation process as a sequence of full-content-area steps, showing exactly one step at a time, instead of a single scrolling page. The application sidebar and topbar SHALL remain visible; only the page content area becomes the wizard.

The step sequence SHALL be: Endkunde wählen → Neuer Endkunde (conditional) → Produkte wählen → Übersicht → Erfolg.

#### Scenario: Partner opens the new-order page
- **WHEN** a partner navigates to the new-order page
- **THEN** the system shows only the "Endkunde wählen" step, with the sidebar and topbar still visible

#### Scenario: Existing Endkunde selected skips the new-Endkunde step
- **WHEN** a partner selects an existing Endkunde on the "Endkunde wählen" step and advances
- **THEN** the system shows the "Produkte wählen" step next, without showing "Neuer Endkunde"

### Requirement: Progress bar with checkpoints
The wizard SHALL show a horizontal progress bar above the step content, with one checkpoint per step in the sequence. The "Neuer Endkunde" checkpoint SHALL be visually greyed/inactive whenever the partner has chosen an existing Endkunde instead of creating a new one.

#### Scenario: Checkpoint reflects current step
- **WHEN** the partner is on the "Produkte wählen" step
- **THEN** the progress bar highlights the "Produkte wählen" checkpoint as current and marks "Endkunde wählen" (and "Neuer Endkunde", if it was used) as completed

#### Scenario: Unused new-Endkunde checkpoint is greyed
- **WHEN** the partner selects an existing Endkunde (does not create a new one)
- **THEN** the "Neuer Endkunde" checkpoint is shown in a greyed/inactive state for the remainder of the flow

### Requirement: Checkpoint-based back-navigation without data loss
The partner SHALL be able to return to any already-visited step by clicking its checkpoint on the progress bar. Navigating backward or forward between steps SHALL NOT discard previously entered data (Endkunde selection or draft, cart contents).

#### Scenario: Jump back to an earlier step
- **WHEN** the partner is on the "Übersicht" step and clicks the "Produkte wählen" checkpoint
- **THEN** the system shows the "Produkte wählen" step with the cart contents exactly as the partner left them

#### Scenario: Forward-skip past an incomplete step is blocked
- **WHEN** the partner has not yet completed the current step's required data
- **THEN** the system does not allow navigating to a later, not-yet-visited step, whether via a "Weiter" action or a checkpoint click

### Requirement: New-Endkunde creation selects that Endkunde for the order
When the partner completes the "Neuer Endkunde" step, the system SHALL treat the just-entered Endkunde data as the selected Endkunde for the order and advance directly to "Produkte wählen" — not back to "Endkunde wählen".

#### Scenario: Completing the new-Endkunde form advances past selection
- **WHEN** the partner fills in a valid new Endkunde and confirms the "Neuer Endkunde" step
- **THEN** the system shows the "Produkte wählen" step next, and the new Endkunde's data is retained as the order's selected Endkunde

#### Scenario: Returning to Endkunde-selection shows the new Endkunde as selected
- **WHEN** the partner has completed "Neuer Endkunde" and later navigates back to the "Endkunde wählen" step
- **THEN** the "neu anlegen" choice and the previously entered Endkunde fields are shown exactly as the partner left them, not blank

### Requirement: Per-step validation gates progression
Each step SHALL validate its own required data before allowing the partner to advance to the next step. "Endkunde wählen" requires either a selected existing Endkunde or (if "neu anlegen" is chosen) completing "Neuer Endkunde" with its required fields. "Produkte wählen" requires at least one product in the cart.

#### Scenario: Cannot advance from Produkte wählen with an empty cart
- **WHEN** the partner is on "Produkte wählen" with no products added
- **THEN** the system does not allow advancing to "Übersicht"

#### Scenario: Cannot advance from Endkunde wählen without a valid selection
- **WHEN** the partner is on "Endkunde wählen" with neither an existing Endkunde selected nor a completed new-Endkunde entry
- **THEN** the system does not allow advancing to "Produkte wählen"

### Requirement: Order submission on the Übersicht step
The "Bestellung aufgeben" action SHALL remain on the "Übersicht" step. While the order-submission request is in flight, the system SHALL show an inline loading state on the submit control and prevent duplicate submission. On success, the system SHALL advance to the "Erfolg" step; on failure, the system SHALL remain on "Übersicht" with an error shown and the submit control usable again.

#### Scenario: Successful submission advances to Erfolg
- **WHEN** the partner clicks "Bestellung aufgeben" on the "Übersicht" step and the order is created successfully
- **THEN** the system shows an inline loading state during the request, then shows the "Erfolg" step

#### Scenario: Failed submission stays on Übersicht
- **WHEN** the partner clicks "Bestellung aufgeben" and the request fails
- **THEN** the system shows an error message, remains on "Übersicht", and re-enables "Bestellung aufgeben"

#### Scenario: Duplicate submission is prevented
- **WHEN** an order-submission request is already in flight
- **THEN** the system ignores additional "Bestellung aufgeben" activations until the request completes

### Requirement: Completion feedback with reduced-motion fallback
The wizard SHALL show a brief completion animation on the progress bar when a step is finished, and a celebratory animation on the "Erfolg" step after a successful order. When the browser reports a `prefers-reduced-motion: reduce` preference, these animations SHALL be skipped or reduced to a non-animated equivalent that still conveys completion/success.

#### Scenario: Step completion animation
- **WHEN** the partner successfully advances past a step
- **THEN** that step's checkpoint shows a brief completion animation (e.g. a checkmark)

#### Scenario: Success celebration
- **WHEN** the order is successfully placed and the "Erfolg" step is shown
- **THEN** the system plays a celebratory animation (e.g. confetti) alongside the order confirmation

#### Scenario: Reduced motion is respected
- **WHEN** the browser/OS reports `prefers-reduced-motion: reduce`
- **THEN** the system does not play the step-completion or success animations, showing their non-animated completion state instead

### Requirement: Restart after success
From the "Erfolg" step, the partner SHALL be able to start a new order, which resets the wizard to the "Endkunde wählen" step with empty state. The "Erfolg" step SHALL NOT offer backward navigation to earlier steps.

#### Scenario: Starting a new order after success
- **WHEN** the partner clicks "Neue Bestellung" on the "Erfolg" step
- **THEN** the system resets to the "Endkunde wählen" step with no Endkunde selected and an empty cart

#### Scenario: No back-navigation from Erfolg
- **WHEN** the partner is on the "Erfolg" step
- **THEN** the progress bar does not offer a way to navigate back to a previous step

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
