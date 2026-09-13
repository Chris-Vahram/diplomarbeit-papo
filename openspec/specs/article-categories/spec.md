# article-categories Specification

## Purpose
Controlled category catalog for company-managed articles.

## Requirements
### Requirement: Company manages article categories
The company SHALL be able to list, create, rename, activate, and deactivate article categories. Category names SHALL be trimmed and unique case-insensitively. Existing article assignments SHALL remain valid when a category is deactivated; new assignments SHALL require an active category.

#### Scenario: Company updates a category
- **WHEN** a company submits a valid category name and active state
- **THEN** the category is saved and its current state is returned

#### Scenario: Duplicate category name
- **WHEN** a company creates or renames a category to a case-insensitive duplicate
- **THEN** the API rejects the request without changing the catalog

#### Scenario: Deactivated category remains on existing articles
- **WHEN** a category with existing article assignments is deactivated
- **THEN** those assignments remain visible and new article assignments to that category are rejected

#### Scenario: Category routes are company-only
- **WHEN** an unauthenticated user or a partner calls the category management endpoint
- **THEN** the request is rejected according to the existing authentication and role policy
