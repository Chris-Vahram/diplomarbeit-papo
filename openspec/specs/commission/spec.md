# commission Specification

## Purpose
Per-partner commission percentage set by the company and one commission record per order calculated from the order value, visible to the company for all partners and to each partner for its own orders.
## Requirements
### Requirement: Partner commission percentage
Each partner SHALL have a commission percentage stored as `Partner.commissionPercent`. The company sets it via a dropdown with presets 5% / 10% / 15% plus an optional custom value, and can change it at any time. There is no separate commission-level entity. Automatic level-up is out of scope (see phase2_backlog.md B-03).

#### Scenario: Partner has a percentage
- **WHEN** the company creates or edits a partner
- **THEN** the partner has a `commissionPercent` value (a preset 5/10/15 or a custom value)

### Requirement: Per-order commission calculation
For each order the system SHALL calculate a commission as the assigned partner's `commissionPercent` of the **net** order value (sum of net line values, excluding USt), and store it as its own commission record. Commission records created before this rule SHALL NOT be recalculated.

#### Scenario: Commission record created
- **WHEN** an order is created
- **THEN** the system creates one commission record linked to that order, containing the related order id, assigned partner, calculation basis (net order value), percentage used, and calculated value

#### Scenario: Commission excludes VAT
- **WHEN** a partner with 10 percent commission places an order with gross value 120.00 at 20 percent USt
- **THEN** the commission basis is 100.00 and the commission value is 10.00

#### Scenario: Missing commission percentage
- **WHEN** an order is created for a partner without a valid `commissionPercent`
- **THEN** the system handles the case with a clear message and does not create an inconsistent commission record

#### Scenario: Historic records unchanged
- **WHEN** the application starts after this change with existing commission records
- **THEN** their stored basis and values remain unchanged

### Requirement: Commission visibility
The company SHALL view commission records for all orders of its own partners; a partner SHALL view commission only for its own orders. Commission records of another company SHALL NOT be visible.

#### Scenario: Partner views own commission
- **WHEN** a partner opens its orders
- **THEN** the commission derived from each of its orders is visible

#### Scenario: Company sees only own commission
- **WHEN** a company user views orders, dashboard or commission export
- **THEN** only commission records of the own company's partners are included
