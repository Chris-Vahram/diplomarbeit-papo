## MODIFIED Requirements

### Requirement: Import partners and articles
The company SHALL be able to import partner and article records from an Excel/CSV file, mapping file columns to system fields. For article imports the company SHALL declare whether the mapped price column contains net or gross prices, and MAY map an optional tax-rate column; rows without a tax rate use 20 percent.

#### Scenario: Successful import
- **WHEN** the company uploads a valid file and confirms the column mapping
- **THEN** the system creates the corresponding partner/article records and reports the number of accepted rows

#### Scenario: Column mapping
- **WHEN** the company uploads a file with columns not matching field names
- **THEN** the system lets the company map each file column to a system field before importing

#### Scenario: Net price column converted
- **WHEN** the company declares the price column as net and a row has price 10.00 and tax rate 20
- **THEN** the article is stored with gross price 12.00 and tax rate 20

#### Scenario: Invalid tax rate in file
- **WHEN** a row contains a tax rate outside the allowed set
- **THEN** the row is rejected and the report names the invalid tax rate
