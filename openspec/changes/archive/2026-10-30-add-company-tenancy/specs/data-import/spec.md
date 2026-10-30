## MODIFIED Requirements

### Requirement: Import partners and articles
The company SHALL be able to import partner and article records from an Excel/CSV file, mapping file columns to system fields. Imported records SHALL belong to the caller's company; category lookup and duplicate detection SHALL consider only that company's records.

#### Scenario: Successful import
- **WHEN** the company uploads a valid file and confirms the column mapping
- **THEN** the system creates the corresponding partner/article records for the caller's company and reports the number of accepted rows

#### Scenario: Column mapping
- **WHEN** the company uploads a file with columns not matching field names
- **THEN** the system lets the company map each file column to a system field before importing

#### Scenario: Identifiers of another company
- **WHEN** company B imports a partner number, article name or category name that exists only in company A
- **THEN** the row is not reported as a duplicate and company A's records stay unchanged
