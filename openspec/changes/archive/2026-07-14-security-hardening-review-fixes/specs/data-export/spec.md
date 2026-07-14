## ADDED Requirements

### Requirement: CSV export neutralizes formula injection
Exported CSV cell values that originate from user-controlled text (partner name, Kunde name, and similar free-text fields) SHALL be neutralized against spreadsheet formula injection: a value whose first character is `=`, `+`, `-`, or `@` is prefixed with a leading apostrophe so spreadsheet software treats it as text rather than a formula.

#### Scenario: Partner name begins with an equals sign
- **WHEN** an exported CSV row contains a partner or Kunde name beginning with `=`, `+`, `-`, or `@`
- **THEN** the written cell is prefixed so that German-locale Excel displays it as literal text and does not evaluate it as a formula

#### Scenario: Normal name is unchanged
- **WHEN** an exported CSV row contains an ordinary name (no leading formula character)
- **THEN** the cell value is written unchanged apart from existing quoting/encoding rules
