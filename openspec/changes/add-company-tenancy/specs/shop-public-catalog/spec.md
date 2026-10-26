## MODIFIED Requirements

### Requirement: Public read of a reachable shop
For a reachable shop the public API SHALL return the published theme, the navigation, and published page content by URL segment. The seller name and categories SHALL come from the company of the shop's partner. For unreachable shops it SHALL return the uniform 404 defined in `shop-public-access`.

#### Scenario: Load start page
- **WHEN** a visitor opens `/shop/<kennung>`
- **THEN** the shop entry loads the published theme and Startseite and renders them

#### Scenario: Seller of the shop's company
- **WHEN** a visitor opens the shop of a company-B partner
- **THEN** the footer names company B as seller and no data of company A appears
