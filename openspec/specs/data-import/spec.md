# data-import Specification

## Purpose
TBD - created by archiving change order-correctness-hardening. Update Purpose after archive.

## Requirements

### Requirement: XLSX header parsing does not shift columns on blank header cells
When parsing an uploaded XLSX file's header row, the system SHALL read every column position up to the sheet's last used column, including blank header cells, so a blank header cell does not shift the values of later columns into the wrong mapped field.

#### Scenario: Blank header cell mid-row
- **WHEN** an uploaded XLSX file has a blank header cell between two non-blank header cells
- **THEN** every column's data values are still associated with their correct original column position, not shifted left

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

### Requirement: Import partners and articles
The company SHALL be able to import partner and article records from an Excel/CSV file, mapping file columns to system fields.

#### Scenario: Successful import
- **WHEN** the company uploads a valid file and confirms the column mapping
- **THEN** the system creates the corresponding partner/article records and reports the number of accepted rows

#### Scenario: Column mapping
- **WHEN** the company uploads a file with columns not matching field names
- **THEN** the system lets the company map each file column to a system field before importing

### Requirement: Import validation and reporting
The import SHALL validate each row and produce a report of accepted and rejected rows, without silently creating inconsistent or partially corrupt data.

#### Scenario: File with invalid rows
- **WHEN** an uploaded file contains rows with missing required fields
- **THEN** the system rejects those rows, imports the valid ones, and shows a report listing accepted and rejected rows with reasons

#### Scenario: Duplicate detection
- **WHEN** an uploaded file contains records that duplicate existing identifiers
- **THEN** the system flags the duplicates in the report and does not create conflicting records

### Requirement: Partner import validates tax identity consistently
Partner Excel/CSV import SHALL map tax country, type and value and apply the same normalization and validation as company REST management.

#### Scenario: Valid imported identifier
- **WHEN** a partner row maps all tax fields to a supported valid identifier
- **THEN** the accepted Partner record stores the normalized identifier

#### Scenario: Missing or invalid imported identifier
- **WHEN** a partner row omits part of the identifier or fails type-specific syntax validation
- **THEN** that row is rejected with a German reason in the import report

### Requirement: Import file size remains bounded after proof upload expansion
Partner and article import files SHALL be limited to 2 MiB and rejected before their complete contents are read into application memory. Raising the global multipart ceiling for 10 MiB trade proofs SHALL NOT permit larger import files.

#### Scenario: Oversized partner or article import
- **WHEN** a company uploads an import file larger than 2 MiB
- **THEN** the request is rejected without parsing or persisting rows

#### Scenario: Import within limit
- **WHEN** a valid import file is at most 2 MiB
- **THEN** existing preview, mapping, validation and commit behavior remains available
