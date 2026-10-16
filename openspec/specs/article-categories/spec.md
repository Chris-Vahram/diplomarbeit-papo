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

### Requirement: Category management is accessed through article management

The company article-management screen SHALL provide a clearly visible action to open the existing article-category management screen. The category screen SHALL provide a clear return path to article management.

#### Scenario: Company manages categories from article management
- **WHEN** a company user activates „Kategorien verwalten“ on the article-management screen
- **THEN** the existing company-only category management screen opens

#### Scenario: Company returns to article management
- **WHEN** a company user activates „Zur Artikelverwaltung“ on the category screen
- **THEN** the article-management screen opens with its normal list state

#### Scenario: Existing category behavior remains unchanged
- **WHEN** a company user lists, creates, renames, activates or deactivates a category
- **THEN** existing validation, assignment and authorization behavior remains unchanged

### Requirement: Category stored once per article
An article's category SHALL be stored only as a reference to the category catalogue. Every place that shows or exports an article's category SHALL read the current catalogue name, so renaming a category SHALL be visible on all assigned articles without changing the articles themselves. Requests and imports MAY still name a category; the name SHALL be resolved through the catalogue.

#### Scenario: Category renamed
- **WHEN** the company renames a category that is assigned to articles
- **THEN** all assigned articles show the new name in lists, filters and CSV export, and no article record is modified

#### Scenario: Import by category name
- **WHEN** an import row names an existing category
- **THEN** the article is linked to that catalogue entry

#### Scenario: Fresh demo database
- **WHEN** a dev or demo database is seeded from scratch
- **THEN** the seeded articles are linked to catalogue categories without relying on a startup migration
