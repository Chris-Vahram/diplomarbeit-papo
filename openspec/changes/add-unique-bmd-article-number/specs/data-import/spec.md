## ADDED Requirements

### Requirement: Article import identifies rows by BMD number
Article Excel/CSV import SHALL require a mapped BMD article-number column and use its canonical textual value for duplicate detection instead of article name. The number SHALL remain create-only until an approved BMD sample defines update and deactivation semantics.

#### Scenario: Duplicate inside file
- **WHEN** two rows contain the same canonical BMD number
- **THEN** conflicting rows are reported as duplicates and no conflicting article is created

#### Scenario: Duplicate against database
- **WHEN** an imported BMD number already exists
- **THEN** the row is rejected as existing and the stored article is not updated

#### Scenario: Leading-zero spreadsheet value
- **WHEN** CSV or formatted XLSX provides `001234`
- **THEN** preview, commit and stored article preserve `001234`

#### Scenario: Name reused with new number
- **WHEN** a row has an existing article name but a new unique BMD number
- **THEN** name alone does not trigger duplicate rejection

#### Scenario: Concurrent insert
- **WHEN** another request inserts the number between preview and commit
- **THEN** commit reports a German conflict and does not return a misleading accepted result or partial transaction

### Requirement: No PAPO-to-BMD master export
PAPO SHALL treat BMD as the article master and SHALL NOT provide an article-master export intended to overwrite BMD in this change.

#### Scenario: Company uses export functions
- **WHEN** company opens existing order or commission exports
- **THEN** no new reverse-synchronization or BMD article-master export is offered

## MODIFIED Requirements

### Requirement: Article import resolves controlled categories
Article import SHALL resolve a nonblank category name against the known active category catalog during commit. Unknown and inactive names SHALL be rejected per row with German feedback and SHALL NOT create categories. Blank optional categories SHALL remain permitted. Article identity and duplicate detection SHALL use the required BMD article number.

#### Scenario: Known and unknown category rows
- **WHEN** an import contains valid rows with known categories and rows with unknown or inactive categories
- **THEN** valid rows are imported under the existing report contract and invalid category rows are rejected without creating categories

#### Scenario: Category changes after preview
- **WHEN** the selected category is renamed or deactivated after preview and before commit
- **THEN** commit validates current catalog state and reports a row error for an old or inactive name

#### Scenario: Deactivation races with commit
- **WHEN** category deactivation and import commit execute concurrently
- **THEN** accepted assignments are protected through persistence and no new assignment commits using an already inactive category
