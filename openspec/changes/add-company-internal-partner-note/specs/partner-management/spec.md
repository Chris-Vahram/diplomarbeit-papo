## ADDED Requirements

### Requirement: Company-only internal partner note
Each partner SHALL have one optional plain-text internal note of at most 4,000 characters. Only company-role APIs SHALL return or modify it; partner profile, import, export, order and commission responses SHALL omit it entirely.

#### Scenario: Company saves long operational note
- **WHEN** a company user saves a note containing at most 4,000 characters, line breaks and German characters
- **THEN** the exact plain text is stored and shown in company partner management

#### Scenario: Note exceeds limit
- **WHEN** a company user submits 4,001 characters
- **THEN** the request is rejected with a German validation message and the previous note remains

#### Scenario: Partner reads or modifies profile
- **WHEN** a partner retrieves or updates its own profile, including a manipulated note property
- **THEN** no note property is returned and the stored internal note remains unchanged

#### Scenario: Company clears note
- **WHEN** a company user saves an empty note
- **THEN** the internal note is removed
