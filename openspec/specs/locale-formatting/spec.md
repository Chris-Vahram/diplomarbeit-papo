# locale-formatting Specification

## Purpose
TBD - created by archiving change add-german-locale-formatting. Update Purpose after archive.
## Requirements
### Requirement: German currency and number formatting
All user-visible money values SHALL be formatted in German (Austria) convention: comma as decimal separator, dot as thousands separator, two decimal places (e.g. `38.940,20` in tables with a `(€)` column header, `€ 391,80` standalone).

#### Scenario: Money in a table cell
- **WHEN** a list shows an order value, price, revenue, or commission under a `(€)` column
- **THEN** the value renders with decimal comma and thousands dot (e.g. `1.632,50`)

#### Scenario: Money in a metric card or text
- **WHEN** a dashboard metric card or the order confirmation/e-mail shows a money value
- **THEN** it renders as German-formatted Euro (e.g. `€ 3.265,00`)

### Requirement: German date formatting
All user-visible dates SHALL be formatted as `dd.MM.yyyy` (e.g. `05.07.2026`).

#### Scenario: Order date in a list
- **WHEN** an order list or dashboard shows an order date
- **THEN** it renders as `dd.MM.yyyy`, not ISO

