## ADDED Requirements

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
