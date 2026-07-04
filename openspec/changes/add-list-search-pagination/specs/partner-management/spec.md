## ADDED Requirements

### Requirement: Partner list search, filter, and pagination
The company partner list SHALL support searching by name, and pagination, to remain usable at the client's partner scale (~200 partners). Searching SHALL treat German special characters (ä/ö/ü/ß) correctly (case-insensitive match includes umlauts).

#### Scenario: Company searches partners by name
- **WHEN** the company enters a search term in the partner list
- **THEN** the system shows only partners whose name matches the search term

#### Scenario: Search with umlauts
- **WHEN** the company searches for "müller" and a partner "Müller GmbH" exists
- **THEN** that partner is found (case-insensitive umlaut match)

#### Scenario: Company pages through partners
- **WHEN** the partner list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the company navigate to further pages
