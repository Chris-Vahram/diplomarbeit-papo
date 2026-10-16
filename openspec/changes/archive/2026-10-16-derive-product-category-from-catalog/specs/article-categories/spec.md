## ADDED Requirements

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
