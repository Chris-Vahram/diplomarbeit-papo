## ADDED Requirements

### Requirement: Company maintains shop legal texts
Company users SHALL maintain Impressum, AGB, Datenschutzerklärung and Widerrufsbelehrung (incl. Muster-Widerrufsformular) in the company profile as plain text with paragraphs, headings, lists and links only (max. 30,000 characters each). The profile SHALL show which texts are missing.

#### Scenario: Save AGB
- **WHEN** a company user saves an AGB text
- **THEN** the text is stored and shown on the AGB page of all its partners' shops

#### Scenario: Unsafe markup
- **WHEN** a legal text contains HTML tags
- **THEN** they are shown as text and not interpreted
