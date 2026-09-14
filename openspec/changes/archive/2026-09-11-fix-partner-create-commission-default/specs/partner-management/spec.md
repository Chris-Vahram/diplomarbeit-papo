## ADDED Requirements

### Requirement: Manual partner creation starts with five percent commission
The company-facing new-partner form SHALL preselect 5 percent commission every time a new creation is started. The company SHALL retain the ability to select 5, 10, 15 or a valid custom percentage. Editing SHALL display the partner's persisted percentage. The change SHALL NOT introduce an API/import default or change existing partner values or historical commission records.

#### Scenario: New partner without changing the preset
- **WHEN** the company opens a new partner form and submits otherwise valid data without changing the commission choice
- **THEN** the submitted and stored commission percentage is 5

#### Scenario: Deliberate commission choice
- **WHEN** the company selects another preset or enters a valid custom percentage
- **THEN** that explicit value is submitted and stored

#### Scenario: Editing and returning to creation
- **WHEN** the company edits a partner with 10 percent or a custom percentage and subsequently starts a new creation
- **THEN** the edit form displays the persisted value, while the new form starts at 5 percent

#### Scenario: Cancelled form does not change defaults
- **WHEN** the company changes the creation preset, cancels and opens another new-partner form
- **THEN** no partner was created and the new form again starts at 5 percent
