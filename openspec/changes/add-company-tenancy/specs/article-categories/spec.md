## MODIFIED Requirements

### Requirement: Company manages article categories
The company SHALL be able to list, create, rename, activate, and deactivate its own article categories. Category names SHALL be trimmed and unique case-insensitively within the company. Existing article assignments SHALL remain valid when a category is deactivated; new assignments SHALL require an active category of the same company.

#### Scenario: Company updates a category
- **WHEN** a company submits a valid category name and active state
- **THEN** the category is saved and its current state is returned

#### Scenario: Duplicate category name
- **WHEN** a company creates or renames a category to a case-insensitive duplicate within the same company
- **THEN** the API rejects the request without changing the catalog

#### Scenario: Same name in another company
- **WHEN** company B creates a category "Werkzeug" while company A already has one
- **THEN** the category is stored for company B

#### Scenario: Foreign category
- **WHEN** a company user lists categories or renames or deactivates a category of another company
- **THEN** it is absent from the list and the request returns 404 without change

#### Scenario: Deactivated category remains on existing articles
- **WHEN** a category with existing article assignments is deactivated
- **THEN** those assignments remain visible and new article assignments to that category are rejected

#### Scenario: Category routes are company-only
- **WHEN** an unauthenticated user or a partner calls the category management endpoint
- **THEN** the request is rejected according to the existing authentication and role policy
