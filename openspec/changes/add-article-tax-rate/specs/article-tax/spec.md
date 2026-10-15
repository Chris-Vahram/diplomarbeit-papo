## ADDED Requirements

### Requirement: Gross price and tax rate per article
Each article SHALL store its unit price as a gross amount including USt and a tax rate `taxRatePercent`. Allowed tax rates SHALL be 20, 13, 10 and 0 percent; the default for new articles SHALL be 20 percent.

#### Scenario: New article gets default tax rate
- **WHEN** the company creates an article without choosing a tax rate
- **THEN** the system stores the article with `taxRatePercent = 20`

#### Scenario: Invalid tax rate rejected
- **WHEN** the company saves an article with a tax rate outside the allowed set
- **THEN** the system rejects the save with the German message "Bitte einen gültigen Steuersatz wählen."

### Requirement: Net and tax derivation
The system SHALL derive the net unit price as `gross / (1 + rate/100)` and the tax amount as `gross - net`, using `BigDecimal` with commercial rounding (HALF_UP) to two decimals. Line and order totals SHALL be computed from unrounded line values and rounded once per total.

#### Scenario: Net derivation at 20 percent
- **WHEN** an article has gross price 12.00 and tax rate 20
- **THEN** the derived net price is 10.00 and the tax amount is 2.00

#### Scenario: Zero rate
- **WHEN** an article has tax rate 0
- **THEN** net price equals gross price and the tax amount is 0.00

### Requirement: Gross and net display in the portal
Article prices in the company and partner portal SHALL be shown as gross with the label "inkl. USt" and the net value in smaller type below ("netto …").

#### Scenario: Partner catalog shows both values
- **WHEN** a partner opens the article catalog
- **THEN** each article shows the gross price and, below it, the net price

### Requirement: Existing prices are interpreted as gross
On migration, every existing article without a tax rate SHALL receive `taxRatePercent = 20`, and its existing price SHALL be kept unchanged as the gross price.

#### Scenario: Migrating demo data
- **WHEN** the application starts on a database whose articles have no tax rate
- **THEN** all those articles get tax rate 20 and their price values are unchanged
