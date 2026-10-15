## ADDED Requirements

### Requirement: Builder layout
"Mein Webshop" SHALL provide a builder with a block palette, a live preview rendered by the same components as the public shop, and a properties panel for the selected block. The preview SHALL be switchable between desktop and mobile width.

#### Scenario: Preview equals public rendering
- **WHEN** a partner publishes a page without further changes
- **THEN** the public page looks identical to the editor preview at the same width

### Requirement: Page management
The partner SHALL be able to add a free page (empty or from a template), rename it, set its menu name and URL segment, reorder pages in the navigation by drag & drop, and delete free pages. Mandatory pages SHALL always stay online. Limits from `shop-pages` SHALL be shown before they are hit ("4 von 6 Seiten online").

#### Scenario: Delete free page
- **WHEN** the partner deletes a free page and confirms
- **THEN** the page is removed from drafts and, if online, from the public shop

#### Scenario: URL segment suggestion
- **WHEN** the partner creates a page titled "Über uns"
- **THEN** the suggested URL segment is "ueber-uns"

### Requirement: Block editing
The partner SHALL be able to add blocks from the palette, edit their fields in the properties panel, duplicate, delete and reorder them. Reordering SHALL work by drag & drop and by "nach oben"/"nach unten" buttons. Only fields defined by the block schema SHALL be editable.

#### Scenario: Keyboard-free reorder
- **WHEN** the partner presses "nach oben" on the second block
- **THEN** the block becomes the first block in the draft

### Requirement: Design tab
A separate "Design" tab SHALL let the partner choose all theme colours with a free colour picker (including hex input) and a heading and body font from the curated list, preview the result, and publish it with "Design veröffentlichen", which updates all online pages at once.

#### Scenario: Unpublished design
- **WHEN** the partner changes the primary colour but does not publish the design
- **THEN** only the editor preview shows the new colour

### Requirement: Block colour override in the editor
In the properties panel the partner SHALL be able to override theme colours for a block and reset them with one click "Auf Design zurücksetzen".

#### Scenario: Reset
- **WHEN** the partner clicks "Auf Design zurücksetzen" on a block
- **THEN** all colour overrides of that block are removed

### Requirement: Publishing and restore
The partner SHALL publish pages individually. The partner SHALL be able to restore the previous published version of a page. Mandatory pages SHALL NOT be unpublished.

#### Scenario: Restore previous version
- **WHEN** the partner chooses "Vorherige Version wiederherstellen" on a published page
- **THEN** the previous published version becomes the published version and the draft

### Requirement: Autosave and undo
Draft changes SHALL be saved automatically within 2 seconds of the last edit, with a visible save state ("Gespeichert", "Wird gespeichert…", "Nicht gespeichert – erneut versuchen"). Undo and redo SHALL be available for the current editing session.

#### Scenario: Network failure
- **WHEN** an autosave fails
- **THEN** the editor shows "Nicht gespeichert – erneut versuchen" and keeps the local changes

### Requirement: Concurrent editing protection
Every draft save SHALL include the version the editor loaded. If the stored version is newer, the save SHALL be rejected and the editor SHALL offer to reload.

#### Scenario: Two tabs
- **WHEN** the same page is saved from two browser tabs
- **THEN** the second save is rejected with "Diese Seite wurde inzwischen geändert. Bitte neu laden."

### Requirement: SEO fields with explanation
Each designable page SHALL have optional SEO title (max. 60 characters) and description (max. 160 characters). The editor SHALL explain in plain German why these help customers find the shop and that indexing only applies to released live shops.

#### Scenario: Explanation visible
- **WHEN** the partner opens a page's settings
- **THEN** a short explanation of SEO title, description and noindex is shown next to the fields

### Requirement: Device support
The builder SHALL be usable on desktop and tablet widths (≥ 768 px). Below that width it SHALL show "Der Shop-Editor ist für Computer und Tablet gedacht." and offer the public link instead.

#### Scenario: Phone
- **WHEN** a partner opens "Mein Webshop" on a 390 px wide screen
- **THEN** the hint is shown and no editor is rendered
