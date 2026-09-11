## Context

Der vorhandene uncommittete Frontend-Diff umfasst acht Dateien mit 24 Textsubstitutionen. Gemeinsame Helfer nutzen vielfach bereits „Artikel“, Backendfehler enthalten teilweise noch „Produkt“. Dies ist vorhandener Benutzerstand, keine Umsetzung dieser Sitzung.

## Goals / Non-Goals

Sichtbare Artikelsprache in aktiven UI-/Fehlerpfaden. Keine technische Umbenennung oder Übersetzung historischer Dokumente.

## Decisions

- Bestehenden Diff vor Apply prüfen und erhalten. `rg` nach sichtbaren Varianten Produkt/Produkte/Produktkategorie/Artikel-ID in Frontend und Backendmeldungen; Treffer in Symbolen, URLs, Datenwerten, Beispieldateien, Kommentaren oder historischen Artefakten nicht ersetzen.
- Navigation/Listentitel, Buttons, Dialoge, Toasts, Bestellschritte, Filter und neue Validierungsfehler verwenden Artikel/Artikelkategorie. Backendfehler nur Textliteral korrigieren; spezifische Fehlermeldungstests passend aktualisieren, Assertions nicht abschwächen.
- Technische `Product`, `productId`, `/products`, Importkeys und existierende Headeraliases bleiben unverändert. Keine sichtbare technische Artikel-ID hinzufügen; IDs für Beziehungen und Requests erhalten.
- Forschungsfrage und Originalprotokolle wortgetreu erhalten. Ein bewusst wörtliches Zitat darf „Produkt“ enthalten. Keine Massenersetzung im Repository.
- Reihenfolge: diesen kleinen Textchange vor neuen Wizard-/Kategorie-UI-Changes anwenden, damit sie auf einer konsistenten Textbasis aufsetzen; deren eigene neuen Texte bleiben jeweils in ihrem Change.

## Risks / Trade-offs

Ein technischer Suchtreffer ist kein sichtbarer Text. Bestehende Imports müssen weiterhin ältere Header akzeptieren. Browserbelege aus früheren Sitzungen gelten nicht als aktuelle Regression.

## Migration Plan

Keine Datenmigration; nur Strings und passende Tests. API-/DB-Namen dürfen sich im Diff nicht ändern.
