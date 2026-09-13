## Context

`selectedKunde()` löst bestehenden Kunden oder neuen Entwurf bereits auf. Katalog und Warenkorb sind innerhalb `.produkte-step-layout` getrennt. Preis-/Provisionsberechnung bleibt in bestehenden Helfern.

## Goals / Non-Goals

Nur Kundenkontext, Text und Anordnung. Keine Neudefinition von Zahlungspflicht oder Zahlungssystem.

## Decisions

- Bei jedem `renderStep()` Hauptüberschrift aus gültigem bestätigtem Kunden aktualisieren: „Neue Bestellung für <Vorname Nachname>“. Optionalen Vornamen auslassen; ohne gültigen Kunden „Neue Bestellung“. Nach Erfolg bleibt der bestätigte Kundenname bis zum Neustart; Neustart setzt neutral zurück.
- Die Warenkorbüberschrift im Artikelschritt lautet „Bestellung für <Name>“. Text sicher mit `textContent` oder `escapeHtml` ausgeben, keine Rohinterpolation. Kundenwechsel und bearbeiteter neuer Entwurf aktualisieren beide Überschriften.
- DOM-Reihenfolge Katalog vor Warenkorb; Desktopgrid links flexibler Katalog, rechts vorhandene kompakte Bestellübersicht. Bei existierendem Mobilebreakpoint einspaltig in derselben Reihenfolge. Bestehende unabhängige Scrollbereiche und Erreichbarkeit der Summen/Weiter-Aktion erhalten; kein reines visuelles CSS-Reordering gegen die Tastaturreihenfolge.
- Übersichtsbeschreibung genau „Bitte Bestellung überprüfen und kostenpflichtig aufgeben“. Bestehender Button „Bestellung aufgeben“ und dessen Pending-/Fehler-/Erfolgsverhalten bleiben.
- Keine Geldberechnung, API oder Formularvalidierung ändern. Technische `produkte`-Schrittnamen bleiben.

## Risks / Trade-offs

Die Bestellübersicht steht mobil unter dem Katalog; vorhandene begrenzte Scrollhöhe des Katalogs erhalten, damit sie erreichbar bleibt. Langtexte dürfen weder Summen noch Aktionen überdecken.

## Migration Plan

Keine Migration. Bei Anwendung nach Suchchange dessen Auswahlvalidität verwenden; sonst bestehende `selectedKunde()`-Validität. Keine automatische Kundenpersistierung.
