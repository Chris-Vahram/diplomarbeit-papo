## ADDED Requirements

### Requirement: Company-wide dashboard totals
The company SHALL see aggregate totals across all partners: total revenue, total commission, total fulfilled-order count, and total partner count. Totals SHALL count only fulfilled orders, consistent with the partner dashboard convention.

#### Scenario: Company views dashboard totals
- **WHEN** the company opens the company dashboard
- **THEN** the system shows total revenue, total commission, and total fulfilled-order count summed across all partners' fulfilled orders

#### Scenario: Non-fulfilled orders excluded from totals
- **WHEN** a partner has orders in `submitted` or `reviewed` status
- **THEN** those orders are not counted in the company dashboard totals

### Requirement: Per-partner settlement breakdown
The company SHALL see, per partner, the fulfilled-order count, revenue, and total commission for that partner.

#### Scenario: Company views settlement breakdown
- **WHEN** the company opens the settlement view
- **THEN** the system shows one row per partner with that partner's fulfilled-order count, revenue, and total commission

#### Scenario: Partner with no fulfilled orders
- **WHEN** a partner has no fulfilled orders
- **THEN** the settlement view shows that partner with zero order count, zero revenue, and zero commission
