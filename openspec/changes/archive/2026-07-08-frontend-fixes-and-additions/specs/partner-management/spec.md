## MODIFIED Requirements

### Requirement: Partner list search, filter, and pagination
The company partner list SHALL support searching by name, filtering by active/inactive status, and pagination, to remain usable at the client's partner scale (~200 partners). Searching SHALL treat German special characters (ä/ö/ü/ß) correctly (case-insensitive match includes umlauts).

#### Scenario: Company searches partners by name
- **WHEN** the company enters a search term in the partner list
- **THEN** the system shows only partners whose name matches the search term

#### Scenario: Search with umlauts
- **WHEN** the company searches for "müller" and a partner "Müller GmbH" exists
- **THEN** that partner is found (case-insensitive umlaut match)

#### Scenario: Company pages through partners
- **WHEN** the partner list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the company navigate to further pages

#### Scenario: Company filters partners by active/inactive status
- **WHEN** the company selects active or inactive in the partner list filter
- **THEN** the system shows only partners matching that status

## ADDED Requirements

### Requirement: Structured partner address
A partner's address SHALL be stored as structured fields (street, house number, postal code, city, country) rather than a single free-text field.

#### Scenario: Company edits partner address
- **WHEN** the company edits a partner's address
- **THEN** the system presents and stores separate street, house number, postal code, city, and country fields

#### Scenario: Partner edits own address
- **WHEN** a partner edits their own contact address via self-edit
- **THEN** the system presents and stores the same structured address fields

### Requirement: "Partnerverwaltung" navigation label
The company-area navigation item for partner management SHALL be labeled "Partnerverwaltung".

#### Scenario: Company views navigation
- **WHEN** the company views the sidebar navigation
- **THEN** the item linking to the partner list reads "Partnerverwaltung", not "Partner"
