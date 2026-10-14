## MODIFIED Requirements

### Requirement: Company article navigation is grouped

The company area SHALL expose one primary navigation entry named „Artikelverwaltung“. Article-category management SHALL NOT appear as a separate primary navigation entry. Partner navigation SHALL use the possessive entries „Meine Endkunden“ and „Meine Artikel“.

#### Scenario: Company opens navigation
- **WHEN** a company user views the sidebar
- **THEN** one primary entry „Artikelverwaltung“ is shown
- **AND** no separate primary entry „Artikelkategorien“ is shown

#### Scenario: Partner opens navigation
- **WHEN** a partner views the sidebar
- **THEN** the entries „Meine Endkunden“ and „Meine Artikel“ are shown instead of „Endkunden“ and „Artikel“
- **AND** the partner article page title reads „Meine Artikel“
