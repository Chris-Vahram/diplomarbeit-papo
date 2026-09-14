## ADDED Requirements

### Requirement: Company partner list shows proof state
Company partner management SHALL show whether the current proof is missing, pending, accepted or rejected without embedding document bytes in ordinary partner responses.

#### Scenario: Company opens partner management
- **WHEN** partners have different proof states
- **THEN** each partner row or detail shows its current state and company can open the protected review flow
