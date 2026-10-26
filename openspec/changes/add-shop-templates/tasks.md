## 1. Design (Tobias, essential)

- [x] 1.1 Design 2–3 Startseite, Artikelübersicht and Artikeldetail templates and 2 free-page templates
- [x] 1.2 Create JSON files, theme suggestions and WebP thumbnails

## 2. Implementation (essential)

- [x] 2.1 Manifest loading and startup validation; exclude invalid templates with log
- [x] 2.2 Template list endpoint and picker UI with confirmation and "Farben und Schriften übernehmen"
- [ ] 2.3 Automated quality test running assistant rules over all templates (provisional `ShopTemplateQualityTest` with the spec's contrast and alt-text thresholds is in place; switch it to the assistant's rule units once `add-shop-design-assistant` exists)
- [x] 2.4 First-time setup: setup state, `POST /api/partner/shop/setup`, "Shop einrichten" dialog with style choice and "Überspringen"
- [x] 2.5 Demo shop per template: "Vorlage ansehen" in setup dialog and picker, renderer demo mode with page navigation, brand hint, "Diese Vorlage wählen"
- [x] 2.6 Theme `form` (klar/handwerk/kraeftig): validation, design-tab selection, renderer forms
- [x] 2.7 Bundled CC0 sample images with provenance, public image endpoint, copy into shop images on apply
- [ ] 2.8 Renderer redesign and recomposed templates with images (impeccable); thumbnails regenerated

## 3. Verification

- [x] 3.1 Tests green; `git diff --check`
- [x] 3.2 Browser check of every template desktop/mobile
