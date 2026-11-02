# order-notifications Specification

## Purpose
Unseen-order count for the company navigation, served without loading the full order list.

## Requirements

### Requirement: Unseen-order count is served without the full order list
The system SHALL expose a dedicated endpoint returning only the count of unseen orders of the caller's company, so the company-area unseen-order badge does not require downloading the full order list (with all items and Kunde data) on every navigation.

#### Scenario: Company fetches the unseen count
- **WHEN** the company requests the unseen-order count
- **THEN** the system returns only the count, not the underlying order records

#### Scenario: Count matches the badge
- **WHEN** the unseen-order count changes (e.g. after a new order arrives or the company opens an unseen order's details)
- **THEN** the dedicated count endpoint reflects the updated count

#### Scenario: Other company's orders not counted
- **WHEN** a partner of company A submits an order
- **THEN** the unseen count of company B is unchanged
