# shop-public-catalog Specification

## Purpose
Unauthenticated read access to a reachable partner shop: published theme, navigation and pages, the public article catalogue (enabled and available articles with gross price and tax rate) and image delivery limited to the shop's own catalogue and assets.

## Requirements

### Requirement: Public read of a reachable shop
For a reachable shop the public API SHALL return the published theme, the navigation, and published page content by URL segment. The seller name and categories SHALL come from the company of the shop's partner. For unreachable shops it SHALL return the uniform 404 defined in `shop-public-access`.

#### Scenario: Load start page
- **WHEN** a visitor opens `/shop/<kennung>`
- **THEN** the shop entry loads the published theme and Startseite and renders them

#### Scenario: Seller of the shop's company
- **WHEN** a visitor opens the shop of a company-B partner
- **THEN** the footer names company B as seller and no data of company A appears

### Requirement: Public article catalogue
The public catalogue SHALL contain only articles that are enabled for the shop's partner and available. Each entry SHALL contain name, description, category, image URL, gross price and tax rate; it SHALL NOT contain commission, net purchase data or enablement metadata.

#### Scenario: Non-enabled article hidden
- **WHEN** an article is not enabled for the partner
- **THEN** it is absent from the catalogue and its detail URL returns 404

#### Scenario: Price display
- **WHEN** a visitor views an article
- **THEN** the gross price is shown with "inkl. 20 % USt" (actual rate)

### Requirement: Public image delivery
The public API SHALL deliver images only if they belong to an article in the shop's public catalogue or to the shop's own uploaded assets, with the verified content type and caching headers. Any other image id SHALL return 404.

#### Scenario: Foreign image id
- **WHEN** a visitor requests an image id of an article not enabled for this partner
- **THEN** the response is 404
