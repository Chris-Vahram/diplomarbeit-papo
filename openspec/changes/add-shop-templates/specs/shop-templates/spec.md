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
