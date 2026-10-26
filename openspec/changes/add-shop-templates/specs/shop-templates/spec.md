## ADDED Requirements

### Requirement: Template catalogue
The system SHALL provide at least 2 and at most 3 templates per mandatory page type (Startseite, Artikelübersicht, Artikeldetail) and at least 2 templates for free pages. Each template SHALL have a German name, a short description and a thumbnail.

#### Scenario: Picker lists templates
- **WHEN** the partner opens the template picker for the Startseite
- **THEN** 2 or 3 Startseite templates with name, description and thumbnail are shown

### Requirement: Templates as data
A template SHALL be a data file containing a block sequence and an optional theme suggestion in the same format as page drafts. Adding a template SHALL NOT require code changes. Invalid template files SHALL be rejected at startup with a log error and excluded from the catalogue.

#### Scenario: Broken template file
- **WHEN** a template file references an unknown block type
- **THEN** the application starts, logs the error and does not offer that template

### Requirement: Applying a template
Applying a template to a page SHALL replace the page draft after confirmation ("Der aktuelle Entwurf wird ersetzt."). The published version SHALL stay unchanged until the partner publishes. The theme suggestion SHALL only be applied to the design draft if the partner selects "Farben und Schriften übernehmen".

#### Scenario: Keep own colours
- **WHEN** the partner applies a template without selecting "Farben und Schriften übernehmen"
- **THEN** only the page draft changes and the design draft stays as it was

### Requirement: Template quality
Every template SHALL pass all Blocker and Warnung rules of the design assistant with its own theme suggestion and with the default theme.

#### Scenario: Automated check
- **WHEN** the template test suite runs
- **THEN** each template yields no Blocker or Warnung findings

### Requirement: First-time shop setup
When a partner opens the builder for a shop that has not been set up yet, the builder SHALL first offer a style choice ("Shop einrichten") built from the Startseite templates. Choosing a style SHALL set Startseite, Artikelübersicht and Artikeldetail from that style's templates and the design from its theme suggestion, as both draft and published version, keeping the replaced version for restore. "Überspringen" SHALL keep the default content. Either answer SHALL mark the shop as set up; closing the dialog without an answer SHALL show it again next time. A shop counts as not set up while it has no recorded setup and neither a saved page, a saved design nor a free page, so shops edited before this feature never see the dialog.

#### Scenario: Partner picks a style
- **WHEN** a partner opens the builder of a new shop and chooses „Handwerk“
- **THEN** the three mandatory pages and the design show the „Handwerk“ templates and the dialog does not appear again

#### Scenario: Shop edited before
- **WHEN** a partner whose shop already has a saved page opens the builder
- **THEN** no setup dialog is shown

#### Scenario: Setup twice
- **WHEN** a setup request arrives for a shop that is already set up
- **THEN** the server refuses it and nothing changes
