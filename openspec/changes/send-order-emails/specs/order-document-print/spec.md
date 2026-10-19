## MODIFIED Requirements

### Requirement: Print output excludes app chrome

The printed/PDF output SHALL contain only the order document. The application shell — header, sidebar, navigation, action buttons — and the on-screen e-mail notice MUST NOT appear in the printout.

#### Scenario: Only the Beleg prints

- **WHEN** the print dialog previews the page
- **THEN** no sidebar, navigation, buttons, or e-mail notice is visible, only the Auftragsbeleg
