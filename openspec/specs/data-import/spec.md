# data-import Specification

## Purpose
TBD - created by archiving change order-correctness-hardening. Update Purpose after archive.
## Requirements
### Requirement: XLSX header parsing does not shift columns on blank header cells
When parsing an uploaded XLSX file's header row, the system SHALL read every column position up to the sheet's last used column, including blank header cells, so a blank header cell does not shift the values of later columns into the wrong mapped field.

#### Scenario: Blank header cell mid-row
- **WHEN** an uploaded XLSX file has a blank header cell between two non-blank header cells
- **THEN** every column's data values are still associated with their correct original column position, not shifted left
