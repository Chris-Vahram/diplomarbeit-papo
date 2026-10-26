## ADDED Requirements

### Requirement: Rule-based checks with three severities
The builder SHALL evaluate draft pages and the draft theme against a set of deterministic rules. Each finding SHALL have one severity: Tipp, Warnung or Blocker. No external service SHALL be called for checks.

#### Scenario: Live feedback
- **WHEN** the partner changes a block so that a rule is violated
- **THEN** the assistant shows the finding within one second without saving

### Requirement: Plain-language explanations
Every finding SHALL contain a short German explanation of the problem, why it matters for customers, and how to fix it, plus an action that selects the affected block.

#### Scenario: Jump to problem
- **WHEN** the partner clicks "Zum Problem springen"
- **THEN** the affected block is selected and scrolled into view

### Requirement: Contrast rule
Text and background colour pairs (theme and overrides) SHALL be checked using the WCAG contrast ratio. A ratio below 4.5:1 for body text SHALL be a Warnung; a ratio below 3:1 SHALL be a Blocker.

#### Scenario: Light grey on white
- **WHEN** a block uses text #BBBBBB on background #FFFFFF (ratio ≈ 1.9:1)
- **THEN** the assistant reports a Blocker "Text ist kaum lesbar" with the suggestion of a darker text colour

#### Scenario: Borderline contrast
- **WHEN** a pair has ratio 3.5:1
- **THEN** the assistant reports a Warnung

### Requirement: Alt-text rule
An image without alternative text SHALL be reported as an emphasised Warnung explaining that people using screen readers and search engines cannot understand the image.

#### Scenario: Image without description
- **WHEN** an image block has no alt text
- **THEN** a highlighted Warnung "Bildbeschreibung fehlt" is shown and the publish confirmation lists it first

### Requirement: Further guidance rules
The first rule set SHALL additionally contain: empty page (Warnung), missing logo (Tipp), Startseite without an article block (Tipp), image smaller than its display width (Tipp), image larger than 2 MB (Warnung), missing SEO title on an online page (Tipp), empty required block field (Blocker).

#### Scenario: Missing logo
- **WHEN** the first Hero block of the Startseite has no logo (optional image field "Logo")
- **THEN** a Tipp "Ein Logo macht Ihren Shop wiedererkennbar" is shown

### Requirement: Publish gating
Publishing a page or the design SHALL be refused while it has Blocker findings; the server SHALL re-evaluate blocker rules. With Warnung findings, publishing SHALL require an explicit confirmation listing them. Tipps SHALL NOT affect publishing.

#### Scenario: Blocker prevents publish
- **WHEN** the partner publishes a page with a contrast blocker
- **THEN** the server rejects the publish and the editor lists the blocker

#### Scenario: Warning confirmation
- **WHEN** the partner publishes a page with one Warnung
- **THEN** a dialog lists the warning and publishing proceeds only after "Trotzdem veröffentlichen"

### Requirement: Extensible rule set
Rules SHALL be independent units with id, severity, scope (page, block, theme), check and explanation, so new rules can be added without changing existing ones.

#### Scenario: Adding a rule
- **WHEN** a developer adds a new rule unit
- **THEN** it appears in the assistant without changes to other rules
