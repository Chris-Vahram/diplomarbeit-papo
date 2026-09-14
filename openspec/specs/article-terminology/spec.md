# article-terminology Specification

## Purpose

Keep visible article terminology consistent while preserving existing technical API, import, and identifier compatibility.

## Requirements

### Requirement: Consistent visible article terms
Active portal navigation, forms, order steps, lists, filters and user-facing validation feedback SHALL use “Artikel” and “Artikelkategorie” for the corresponding business concepts. Technical entity names, API paths, payload keys and existing import aliases SHALL remain compatible. Original quotations and the confirmed research question SHALL remain unchanged.

#### Scenario: User reads an article workflow
- **WHEN** the company or partner opens an article-related view or receives an article validation error
- **THEN** its interface text consistently uses the business article terminology

#### Scenario: Existing technical integration
- **WHEN** a client calls the existing product API or imports a supported historical header
- **THEN** technical names and mappings remain accepted as before

### Requirement: Technical article identifiers remain internal
The portal SHALL NOT add a second visible business “Artikel-ID” alongside future article numbers. Existing technical identifiers needed for relationships and requests SHALL remain intact. This terminology change SHALL NOT implement a BMD article number.

#### Scenario: Article form
- **WHEN** the company creates or edits an article
- **THEN** no additional technical article-ID input is introduced and existing technical references continue to work
