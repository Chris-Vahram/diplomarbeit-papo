## MODIFIED Requirements

### Requirement: Enabled-products list search, filter, and pagination
A partner's enabled-products list SHALL support searching by name, filtering by category, and pagination, to remain usable when a partner has many enabled products.

#### Scenario: Partner searches enabled products by name
- **WHEN** a partner enters a search term in its enabled-products list
- **THEN** the system shows only enabled products whose name matches the search term

#### Scenario: Partner pages through enabled products
- **WHEN** a partner's enabled-products list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the partner navigate to further pages

#### Scenario: Partner filters enabled products by category
- **WHEN** a partner selects one or more categories in their enabled-products list filter
- **THEN** the system shows only enabled products in the selected categories
