# commission Specification

## Purpose
TBD - created by archiving change build-partnerportal-mvp. Update Purpose after archive.

## Requirements

### Requirement: Partner commission percentage
Each partner SHALL have a commission percentage stored as `Partner.commissionPercent`. The company sets it via a dropdown with presets 5% / 10% / 15% plus an optional custom value, and can change it at any time. There is no separate commission-level entity. Automatic level-up is out of scope (see phase2_backlog.md B-03).

#### Scenario: Partner has a percentage
- **WHEN** the company creates or edits a partner
- **THEN** the partner has a `commissionPercent` value (a preset 5/10/15 or a custom value)

### Requirement: Per-order commission calculation
For each order the system SHALL calculate a commission as the assigned partner's `commissionPercent` of the total order value, and store it as its own commission record.

#### Scenario: Commission record created
- **WHEN** an order is created
- **THEN** the system creates one commission record linked to that order, containing the related order id, assigned partner, calculation basis (order value), percentage used, and calculated value

#### Scenario: Missing commission percentage
- **WHEN** an order is created for a partner without a valid `commissionPercent`
- **THEN** the system handles the case with a clear message and does not create an inconsistent commission record

### Requirement: Commission visibility
The company SHALL view commission records for all orders; a partner SHALL view commission only for its own orders.

#### Scenario: Partner views own commission
- **WHEN** a partner opens its orders
- **THEN** the commission derived from each of its orders is visible
