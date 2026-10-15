## ADDED Requirements

### Requirement: Legal system pages
Every shop SHALL provide the system pages Impressum, AGB, Datenschutz and Widerruf with the texts maintained by its company. Partners SHALL NOT edit them.

#### Scenario: Visitor opens imprint
- **WHEN** a visitor opens "Impressum" in a shop
- **THEN** the company's imprint text is shown in the shop's theme

### Requirement: Fixed legal footer
Every shop page SHALL show a footer that partners cannot remove, containing links to the four legal pages, the text "Verkauf durch <Firmenname>", the partner as contact person and a link "Cookie-Einstellungen".

#### Scenario: Footer present
- **WHEN** any shop page is rendered
- **THEN** the footer with all listed elements is visible

### Requirement: Checkout legal elements
The checkout SHALL require ticking "Ich akzeptiere die AGB" (with link) before submission, SHALL show a notice about the right of withdrawal with link to the Widerruf page and a link to the privacy information, and SHALL show all prices as gross incl. USt.

#### Scenario: AGB not accepted
- **WHEN** a visitor submits without ticking the AGB checkbox
- **THEN** the form shows "Bitte die AGB akzeptieren." and nothing is sent

### Requirement: Placeholder marking and release gate
Missing legal texts SHALL be replaced by placeholder texts that are visibly marked "Platzhalter – kein gültiger Rechtstext". In a deployment configured as production, the company SHALL NOT be able to release a shop while any legal text is missing or a placeholder; already released shops SHALL go offline in that case. Staging deployments SHALL allow release with placeholders.

#### Scenario: Production without texts
- **WHEN** the company tries to release a shop in production while the AGB text is missing
- **THEN** the release is refused with "Bitte zuerst alle Rechtstexte im Firmenprofil hinterlegen."
