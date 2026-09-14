## ADDED Requirements

### Requirement: Company manages partner tax identity
Company users SHALL manage the partner company's required tax identifier. Partner self-service SHALL neither return an editable central tax field nor accept changes to it.

#### Scenario: Company updates partner tax identifier
- **WHEN** a company user submits a complete valid identifier
- **THEN** it is stored for that partner

#### Scenario: Partner attempts self-edit
- **WHEN** a partner adds tax fields to its profile update payload
- **THEN** the stored tax identifier remains unchanged and no internal field is exposed
