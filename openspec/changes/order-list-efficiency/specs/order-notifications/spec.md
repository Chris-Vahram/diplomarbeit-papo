## ADDED Requirements

### Requirement: Unseen-order count is served without the full order list
The system SHALL expose a dedicated endpoint returning only the count of unseen orders, so the company-area unseen-order badge does not require downloading the full order list (with all items and Kunde data) on every navigation.

#### Scenario: Company fetches the unseen count
- **WHEN** the company requests the unseen-order count
- **THEN** the system returns only the count, not the underlying order records

#### Scenario: Count matches the badge
- **WHEN** the unseen-order count changes (e.g. after a new order arrives or the company opens an unseen order's details)
- **THEN** the dedicated count endpoint reflects the updated count
