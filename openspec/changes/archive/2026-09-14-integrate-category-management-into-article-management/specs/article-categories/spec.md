## ADDED Requirements

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
