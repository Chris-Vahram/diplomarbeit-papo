## MODIFIED Requirements

### Requirement: Product list search, filter, and pagination
The company product list SHALL support searching by name or BMD article number, filtering by availability, and pagination, to remain usable at catalog scale.

#### Scenario: Company searches products by name
- **WHEN** the company enters a name search term
- **THEN** the system shows only articles whose name matches the term

#### Scenario: Company pages through products
- **WHEN** the article list has more entries than fit on one page
- **THEN** the system shows a page of results and lets the company navigate to further pages

#### Scenario: Company searches by BMD article number
- **WHEN** the company enters a complete or partial BMD article number
- **THEN** matching articles are shown without losing significant leading zeros

## ADDED Requirements

### Requirement: Article has unique visible BMD number
Each article SHALL have a required visible BMD article number stored as text. Canonical equality SHALL preserve significant leading zeros and reject duplicate numbers across manual create and update.

#### Scenario: Article with leading zeros
- **WHEN** company saves BMD number `001234`
- **THEN** the stored and displayed number remains `001234`

#### Scenario: Duplicate manual number
- **WHEN** company creates or updates an article with an existing canonical BMD number
- **THEN** the request is rejected with a German duplicate message and no article changes

#### Scenario: Same name, different numbers
- **WHEN** two articles have the same name but different BMD numbers
- **THEN** both are permitted because the number is the business identifier
