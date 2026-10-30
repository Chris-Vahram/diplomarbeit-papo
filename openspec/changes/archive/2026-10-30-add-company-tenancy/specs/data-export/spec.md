## MODIFIED Requirements

### Requirement: Export order list as CSV
The company SHALL be able to export the full order list of its own company as a downloadable CSV file, including per-order commission columns.

#### Scenario: Company exports orders
- **WHEN** the company triggers the order CSV export
- **THEN** the system returns a CSV file containing all orders of the own company with date, partner, Kunde, order value, commission percentage, commission value, and status

#### Scenario: Other company's orders absent
- **WHEN** company B exports orders while company A has orders
- **THEN** the file contains no row of company A

### Requirement: Export commission list as CSV
The company SHALL be able to export the commission-record list of its own company as a downloadable, commission-focused CSV file containing only orders that have a commission record.

#### Scenario: Company exports commission records
- **WHEN** the company triggers the commission CSV export
- **THEN** the system returns a CSV file with one row per commission record of the own company: partner, order reference, calculation basis (order value), commission percentage used, and commission value

#### Scenario: Orders without commission record excluded
- **WHEN** an order exists without a linked commission record
- **THEN** the commission export does not contain a row for it

#### Scenario: Other company's commission absent
- **WHEN** company B exports commission records while company A has records
- **THEN** the file contains no row of company A
