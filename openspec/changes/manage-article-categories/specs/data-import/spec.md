## ADDED Requirements

### Requirement: Article import resolves controlled categories
Article import SHALL resolve a nonblank category name against the known active category catalog during commit. Unknown and inactive names SHALL be rejected per row with German feedback and SHALL NOT create categories. Blank optional categories SHALL remain permitted. Existing article identity and duplicate detection SHALL remain unchanged.

#### Scenario: Known and unknown category rows
- **WHEN** an import contains valid rows with known categories and rows with unknown or inactive categories
- **THEN** valid rows are imported under the existing report contract and invalid category rows are rejected without creating categories

#### Scenario: Category changes after preview
- **WHEN** the selected category is renamed or deactivated after preview and before commit
- **THEN** commit validates current catalog state and reports a row error for an old or inactive name

#### Scenario: Deactivation races with commit
- **WHEN** category deactivation and import commit execute concurrently
- **THEN** accepted assignments are protected through persistence and no new assignment commits using an already inactive category
