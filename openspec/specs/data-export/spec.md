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

