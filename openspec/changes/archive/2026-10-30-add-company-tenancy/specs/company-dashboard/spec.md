## MODIFIED Requirements

### Requirement: Company-wide dashboard totals
The company SHALL see aggregate totals across all of its own partners: total revenue, total commission, total fulfilled-order count, and total partner count. Totals SHALL count only fulfilled orders, consistent with the partner dashboard convention, and SHALL never include another company's data.

#### Scenario: Company views dashboard totals
- **WHEN** the company opens the company dashboard
- **THEN** the system shows total revenue, total commission, and total fulfilled-order count summed across all of its own partners' fulfilled orders

#### Scenario: Non-fulfilled orders excluded from totals
- **WHEN** a partner has orders in `submitted` or `reviewed` status
- **THEN** those orders are not counted in the company dashboard totals

#### Scenario: Other company excluded
- **WHEN** another company has fulfilled orders and partners
- **THEN** they do not change this company's totals or partner count

### Requirement: Per-partner settlement breakdown
The company SHALL see, per own partner, the fulfilled-order count, revenue, and total commission for that partner.

#### Scenario: Company views settlement breakdown
- **WHEN** the company opens the settlement view
- **THEN** the system shows one row per own partner with that partner's fulfilled-order count, revenue, and total commission, and no row for another company's partner

#### Scenario: Partner with no fulfilled orders
- **WHEN** a partner has no fulfilled orders
- **THEN** the settlement view shows that partner with zero order count, zero revenue, and zero commission
