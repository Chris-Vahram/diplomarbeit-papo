## MODIFIED Requirements

### Requirement: Commission visibility
The company SHALL view commission records for all orders of its own partners; a partner SHALL view commission only for its own orders. Commission records of another company SHALL NOT be visible.

#### Scenario: Partner views own commission
- **WHEN** a partner opens its orders
- **THEN** the commission derived from each of its orders is visible

#### Scenario: Company sees only own commission
- **WHEN** a company user views orders, dashboard or commission export
- **THEN** only commission records of the own company's partners are included
