# shop-pages Specification

## Purpose
Pages of a partner shop (B-01/B-02 builder): page kinds (system, mandatory, free), page limits, typed blocks with server-validated configuration, the first block catalogue, draft and published versions with one-step restore, and the generated navigation with page URLs.

## Requirements
### Requirement: Page kinds
Each shop SHALL have three page kinds. System pages (Warenkorb, Kasse, Impressum, AGB, Datenschutz, Widerruf) SHALL exist for every shop, SHALL NOT be designable and SHALL NOT count toward page limits. Mandatory pages (Startseite, Artikelübersicht, Artikeldetail) SHALL exist for every shop, SHALL be designable and SHALL NOT be deletable or unpublishable. Free pages SHALL be optional and designable.

#### Scenario: New shop gets mandatory pages
- **WHEN** a shop record is created
- **THEN** the three mandatory pages exist, are filled from the default template and are published

#### Scenario: Mandatory page cannot be deleted
- **WHEN** a request tries to delete or unpublish the Startseite
- **THEN** the system rejects it with "Pflichtseiten können nicht entfernt werden."

### Requirement: Page limits
A shop SHALL have at most 6 designable pages online at the same time (mandatory plus free) and at most 20 pages in total including drafts.

#### Scenario: Seventh online page
- **WHEN** a request would publish a seventh designable page
- **THEN** the system rejects it with "Es können höchstens 6 Seiten gleichzeitig online sein."

#### Scenario: Twenty-first page
- **WHEN** a request would create a 21st page
- **THEN** the system rejects it

### Requirement: Single article-detail template
The Artikeldetail page SHALL be one template rendered for every article of the shop; article name, description, price and image SHALL come from central article data and SHALL NOT be editable per shop.

#### Scenario: Two articles share layout
- **WHEN** a visitor opens two different articles
- **THEN** both are rendered with the same published Artikeldetail page and show their own central data

### Requirement: Typed blocks with validated configuration
A designable page SHALL consist of an ordered list of blocks. Each block SHALL have a type from the block catalogue and a configuration that the server validates against that type's schema (allowed fields, max text lengths, allowed image references, allowed article/category references). Free HTML, CSS or script SHALL NOT be accepted; text fields SHALL be stored as plain text and escaped on rendering.

#### Scenario: Unknown field rejected
- **WHEN** a block configuration contains a field not in its type schema
- **THEN** the system rejects the save naming the field

#### Scenario: Script in text is inert
- **WHEN** a text field contains `<script>alert(1)</script>`
- **THEN** the rendered page shows the characters as text and no script runs

### Requirement: Block catalogue
The first block catalogue SHALL contain: Hero/Banner, Überschrift + Text, Bild, Bild + Text, Artikelraster (all articles or one category), Hervorgehobene Artikel (manually chosen enabled articles), Artikeldetail (only on the Artikeldetail page and required there), Ansprechpartner (partner-published contact text), Abstand/Trennlinie. Each block SHALL offer layout options within the block (columns where applicable, alignment, spacing, background) and SHALL NOT support free positioning.

#### Scenario: Article reference becomes unavailable
- **WHEN** an article chosen in "Hervorgehobene Artikel" is no longer enabled or available
- **THEN** the public page omits it without error

### Requirement: Draft and published versions
Each page SHALL keep a draft and a published version. Public visitors SHALL only see published versions. The previous published version of each page SHALL be retained for one-step restore.

#### Scenario: Draft not public
- **WHEN** a page has unpublished draft changes
- **THEN** visitors still see the last published version

### Requirement: Navigation and page URLs
The shop navigation SHALL be generated from online designable pages in their configured order using their menu names. Each page SHALL have a URL segment unique within the shop (lowercase, digits, hyphens); the Startseite SHALL be the shop root.

#### Scenario: Navigation lists online pages only
- **WHEN** a free page is a draft
- **THEN** it does not appear in the public navigation

