## MODIFIED Requirements

### Requirement: Order status workflow
The system SHALL support order statuses `submitted`, `reviewed`, and `fulfilled`. Only the company SHALL change status; partners SHALL view it. Status changes SHALL be forward-only along `submitted → reviewed → fulfilled`: skipping a step or reverting to an earlier status SHALL be rejected.

#### Scenario: Company advances status
- **WHEN** the company moves an order from submitted to reviewed to fulfilled
- **THEN** the system saves each status change

#### Scenario: Partner cannot change status
- **WHEN** a partner attempts to change an order status
- **THEN** the system rejects the action; the partner can only view status

#### Scenario: Skipping a status step rejected
- **WHEN** the company attempts to move an order from submitted directly to fulfilled
- **THEN** the system rejects the change with an understandable message and the order keeps its current status

#### Scenario: Reverting a status rejected
- **WHEN** the company attempts to move a fulfilled order back to reviewed or submitted
- **THEN** the system rejects the change with an understandable message and the order stays fulfilled
