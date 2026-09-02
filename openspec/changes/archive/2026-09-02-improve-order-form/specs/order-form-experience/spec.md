## ADDED Requirements

### Requirement: Searchable product picker

The order form SHALL let the user find an article by typing instead of scrolling a plain select: each article line offers a search field that filters the partner's enabled products by name and category as the user types, showing name, category, unit price, and availability for each suggestion. All product-derived text shown in a suggestion (name and category) MUST be HTML-escaped. Matching MUST treat German special characters as ordinary characters (searching "Müll" matches "Müller-Ventil"). Unavailable products appear disabled, cannot be selected, and are skipped during keyboard navigation. Once a product is selected, editing the visible search text again MUST clear the selection, so a line can never display one product while submitting another. The picker MUST be fully keyboard-operable (arrow keys to move, Enter to choose, Escape to close).

#### Scenario: Type-to-filter finds an article

- **WHEN** the partner types "vent" into an article line's search field
- **THEN** only enabled products whose name or category contains "vent" (case-insensitive) are suggested, each showing its unit price, and choosing one fills the line

#### Scenario: Umlaut search term

- **WHEN** the partner types "Müll" into the search field
- **THEN** products containing "Müll" in name or category are suggested with the umlaut rendered correctly

#### Scenario: Unavailable product cannot be picked

- **WHEN** the suggestions include a product marked unavailable
- **THEN** it is shown visibly disabled with the label "(nicht verfügbar)" and selecting it is not possible

#### Scenario: No match

- **WHEN** the typed term matches no enabled product
- **THEN** the picker shows the German empty message "Keine Artikel gefunden" and the line keeps no product selected

#### Scenario: Edited search text clears a prior selection

- **WHEN** a product is already selected on a line and the user edits the search text
- **THEN** the hidden product selection is cleared, so the line cannot be submitted with a product that no longer matches the shown text

#### Scenario: Escaped category

- **WHEN** a product's category contains HTML-special characters
- **THEN** the suggestion renders them as literal text, not markup

### Requirement: Live line and order totals

The order form SHALL show, without any submit, the unit price of each chosen article, the line total (quantity × unit price), and the running order total across all lines. All sums MUST be computed in integer cents and rendered in de-AT Euro format. Totals MUST update immediately when a line is added, removed, or its product/quantity changes.

#### Scenario: Quantity change updates totals

- **WHEN** the partner changes a line's quantity from 1 to 3
- **THEN** the line total and the order total update immediately to the tripled value with no rounding drift

#### Scenario: Removing a line updates the total

- **WHEN** the partner removes an article line
- **THEN** the order total immediately drops by that line's total

### Requirement: Commission preview

The order form SHALL show a live commission preview line: the creating partner's commission percentage applied to the current order total, labelled with the percentage (e.g. "Ihre Provision (10 %)"). In the company's manual-order flow the line uses the selected partner's percentage. The preview is clearly marked as an estimate (Vorschau); the authoritative commission record is created server-side on submission. The preview MUST reproduce the backend's commission arithmetic (integer-cent basis, HALF_UP rounding) so it equals the stored commission to the cent.

#### Scenario: Partner sees commission while composing

- **WHEN** a partner with `commissionPercent` 10 has lines totalling 200,00 €
- **THEN** the form shows "Ihre Provision (10 %): 20,00 €" and updates it live as lines change

#### Scenario: Company manual order shows the partner's percentage

- **WHEN** the company composes a manual order and selects a partner with `commissionPercent` 15
- **THEN** the commission preview uses 15 % of the current total

### Requirement: Live order summary

The order form SHALL render a live summary panel showing the composed order as it is built: article rows (name, quantity, unit price, line total), the order total, and the commission preview line. It updates as the order is composed, and all product-derived text in it MUST be HTML-escaped. The summary is an estimate; the post-submit confirmation is authoritative and MAY differ only if central prices changed during composition (see design.md). (The full document/letterhead "Belegvorschau" styling is a deferred follow-up, not part of this change.)

#### Scenario: Summary tracks the composed order

- **WHEN** the partner adds, changes, or removes article lines
- **THEN** the summary rows, order total, and commission preview update immediately to match the current lines

#### Scenario: Escaped product text in the summary

- **WHEN** a product name or category contains HTML-special characters
- **THEN** the summary renders them as literal text, not markup
