## ADDED Requirements

### Requirement: Partner dashboard
The system SHALL provide each partner a compact dashboard as the landing screen after login, showing at least total commission, total revenue, order count, enabled-product count, and latest orders.

#### Scenario: Dashboard is landing screen
- **WHEN** a partner logs in
- **THEN** the dashboard is the first screen shown

#### Scenario: Dashboard metrics
- **WHEN** a partner views the dashboard
- **THEN** total commission, total revenue, order count, enabled-product count, and a list of latest orders are displayed for that partner only

#### Scenario: Scope isolation
- **WHEN** a partner views the dashboard
- **THEN** all figures reflect only that partner's own data
