## Why

Partners start faster and get a professional result if they pick from prepared designs instead of an empty page. The decided scope is 2–3 templates per mandatory page type, designed by Tobias, in a separate change from the builder so that "builder works" and "builder looks good" stay separable (`Specification/ENDKUNDENSHOP_DECISIONS.md` §4). This also fulfils the original B-02 requirement of at least two layout variants.

## What Changes

- 2–3 templates each for Startseite, Artikelübersicht and Artikeldetail, plus 2–3 templates for common free pages (Über uns, Kontakt, Aktionen).
- Each template = block sequence + optional theme suggestion, stored as data (JSON); no code change needed to add templates.
- Template picker in the builder with thumbnail preview; applying a template to an existing page replaces its draft after confirmation.
- Theme suggestion is applied only if the partner chooses "Farben und Schriften übernehmen".
- Templates use only blocks and fonts from the existing catalogues and pass all assistant blocker and warning rules.

## Capabilities

### New Capabilities

- `shop-templates`: Template catalogue, data format, picker, apply rules, quality requirements.

### Modified Capabilities

None.

## Impact

JSON files under `backend/src/main/resources/shop-templates/`, thumbnail images, template list endpoint, picker UI in the builder. Depends on `add-shop-page-renderer` (format) and `add-shop-builder-editor` (picker).
