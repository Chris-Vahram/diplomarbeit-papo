## ADDED Requirements

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
