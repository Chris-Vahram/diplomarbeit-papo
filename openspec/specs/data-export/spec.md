# data-export Specification

## Purpose
TBD - created by archiving change add-german-locale-formatting. Update Purpose after archive.
## Requirements
### Requirement: German number and date format in CSV exports
Exported CSV files SHALL write money values with a decimal comma (no thousands grouping) and dates as `dd.MM.yyyy`, so German-locale spreadsheet software parses them as numbers and dates rather than text.

#### Scenario: Money value in export
- **WHEN** an order or commission CSV row contains a money value
- **THEN** it is written with a decimal comma (e.g. `12,30`) and German-locale Excel can sum the column

#### Scenario: Date in export
- **WHEN** an order CSV row contains the order date
- **THEN** it is written as `dd.MM.yyyy` (e.g. `05.07.2026`)

### Requirement: Export order list as CSV
The company SHALL be able to export the full order list as a downloadable CSV file, including per-order commission columns.

#### Scenario: Company exports orders
- **WHEN** the company triggers the order CSV export
- **THEN** the system returns a CSV file containing all orders with date, partner, Kunde, order value, commission percentage, commission value, and status

### Requirement: Export commission list as CSV
The company SHALL be able to export the commission-record list as a downloadable, commission-focused CSV file containing only orders that have a commission record.

#### Scenario: Company exports commission records
- **WHEN** the company triggers the commission CSV export
- **THEN** the system returns a CSV file with one row per commission record: partner, order reference, calculation basis (order value), commission percentage used, and commission value

#### Scenario: Orders without commission record excluded
- **WHEN** an order exists without a linked commission record
- **THEN** the commission export does not contain a row for it

### Requirement: CSV encoding
Exported CSV files SHALL correctly render German special characters (ä, ö, ü, ß) when opened in common spreadsheet software (Excel, German locale).

#### Scenario: Export contains umlauts
- **WHEN** an exported row contains a partner or Kunde name with ä/ö/ü/ß
- **THEN** the CSV file displays those characters correctly when opened in a spreadsheet application

### Requirement: Authenticated export download
Export downloads SHALL be authenticated with the user's session token and restricted to the company role.

#### Scenario: Partner cannot export
- **WHEN** a partner-role user requests an export endpoint
- **THEN** the system rejects the request

