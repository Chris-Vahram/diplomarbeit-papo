## Context

`openCreate()` setzt nach `form.reset()` den Provisions-Preset ausdrücklich auf `"10"`. `openEdit()` übernimmt dagegen den gespeicherten Wert. Backend-`PartnerRequest` verlangt eine explizite Provision; `PartnerImportDefinition` verlangt die Importspalte ebenfalls. Der bestätigte Formularfehler ist unabhängig vom blockierten Partneridentitätsmodell.

## Goals / Non-Goals

**Ziel:** den bestätigten 5-%-Startwert bei manueller Anlage zuverlässig anbieten.

**Nicht enthalten:** API-/Importdefaults, Änderungen vorhandener Partner oder Provisions-Snapshots, automatische Nummern, Login-, Steuer-, Kategorie- oder Nachweislogik.

## Decisions

- Nur `this.presetSelect.value = "10"` in `openCreate()` durch `"5"` ersetzen. Keine neue Konstante/Abstraktion für eine einzige Verwendung; keine Neuordnung des Formulars.
- Die vorhandenen Presets 5/10/15 und individuelle Eingabe bleiben. `toggleCustomField()` bleibt nach Setzen des Presets, damit frühere Individualeingaben beim nächsten Öffnen keine sichtbare Wirkung haben.
- Keine API-Defaultlösung: sie würde die bestätigte Formularabweichung nicht beheben und den Importvertrag unnötig erweitern.
- Browserprüfung statt neuer Testbibliothek: frisches Öffnen, Abbrechen, Bearbeiten eines 10-%- und eines individuellen Werts, anschließende Neuanlage und gespeicherter POST-Payload reichen für die geänderte Vorauswahl. Keine neue Geldberechnung.

## Risks / Trade-offs

- Ein vorheriger Bearbeitungswert könnte wiederverwendet werden: Öffnungssequenz im Browser prüfen.
- Die Datei enthält bereits uncommittete Artikelterminologie-Änderungen des Benutzers. Nur die Vorauswahl ändern; fremde Änderungen erhalten.

## Migration Plan

Keine Migration oder Änderung bestehender Daten. Ein späterer Rückbau betrifft nur die Formularvorauswahl; bereits bewusst gespeicherte Prozentsätze bleiben gültig. Kein Deployment in dieser Planungssitzung.

## Readiness

Keine offene fachliche oder externe Entscheidung. Die getrennte Planung und Beschränkung auf manuelle Anlage wurden von Chris bestätigt. Ergebnis der unabhängigen Artefaktprüfung steht nach Abschluss in `review.md`.
