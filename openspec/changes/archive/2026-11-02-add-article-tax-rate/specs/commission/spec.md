## MODIFIED Requirements

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
