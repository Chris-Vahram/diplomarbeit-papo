## ADDED Requirements

### Requirement: Commission levels
The system SHALL define 3 commission levels, each with a percentage. Every partner SHALL have exactly one commission level, assigned and changeable manually by the company. Automatic level-up is out of scope (see phase2_backlog.md B-03).

#### Scenario: Partner has a level
- **WHEN** the company creates or edits a partner
- **THEN** the partner has exactly one of the 3 commission levels assigned

### Requirement: Per-order commission calculation
For each order the system SHALL calculate a commission as the assigned partner's level percentage of the total order value, and store it as its own commission record.

#### Scenario: Commission record created
- **WHEN** an order is created
- **THEN** the system creates one commission record linked to that order, containing the related order id, assigned partner, calculation basis (order value), percentage used, and calculated value

#### Scenario: Missing commission level
- **WHEN** an order is created for a partner without a valid commission level
- **THEN** the system handles the case with a clear message and does not create an inconsistent commission record

### Requirement: Commission visibility
The company SHALL view commission records for all orders; a partner SHALL view commission only for its own orders.

#### Scenario: Partner views own commission
- **WHEN** a partner opens its orders
- **THEN** the commission derived from each of its orders is visible
