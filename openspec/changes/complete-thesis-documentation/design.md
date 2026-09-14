## Context

`docs/REQUIREMENTS_TRACEABILITY.md` enthält bereits 25 FSD-Anforderungen und B-01 bis B-08, aber noch offene Code-/Testzuordnungen. `docs/DOCUMENTATION_TODO.md` beschreibt Inhalte, ist jedoch kein fertiges Diplomarbeitskapitel. Vorhandene Browserbilder und historische Prüfungen dürfen nur mit ihrer echten Revision und ihrem Datum verwendet werden.

## Goals / Non-Goals

**Goals:**
- Drei wartbare Quellen: UI/UX-Dokumentation, technische Dokumentation und Traceability-Matrix.
- Technische Zuordnungen aus Repository und Tests ableiten; Tobias nur für visuelle/fachliche Prüfung benötigen.
- Dokumentation als eigenständige Diplomarbeitsarbeit führen, nicht als technische MVP-Restfunktion.

**Non-Goals:**
- Keine Produktänderung, neue Prüfung oder automatische Generierung eines vollständigen Diplomarbeitstextes.
- Keine privaten Rohdaten, Secrets, erfundenen Stunden oder fingierten Nachweise.

## Decisions

- **Allgemeine Dateien unter `docs/`.** `UI_UX_DOCUMENTATION.md` und `TECHNICAL_DOCUMENTATION.md` bleiben außerhalb des OpenSpec-Archivs direkt nutzbar. Alternative: Text nur in OpenSpec speichern; verworfen, weil er dann kein gut auffindbarer Diplomarbeitsbaustein wäre.
- **Eine zentrale Matrix.** Keine zweite Mapping-Tabelle in jedem Kapitel. Kapitel verweisen auf `REQUIREMENTS_TRACEABILITY.md`.
- **Status evidenzbasiert.** `GEPRÜFT` nur mit ausführbarem Test oder Browserbeleg samt Datum und Revision/Diff-Grundlage; sonst `HISTORISCH BELEGT` oder `NICHT GEPRÜFT`.
- **Screenshots sparsam.** Nur wichtige Flows und responsive Unterschiede; vorhandene Bilder wiederverwenden. Neue Screens erhalten visuellen Polish und Beleg in ihrem eigenen Change.
- **Menschliche Abnahme getrennt.** Chris prüft Technik, Tobias UI/UX, beide finalen Diplomarbeitstext. Diese Abnahme blockiert nicht rückwirkend technische MVP-Funktion.

## Risks / Trade-offs

- [Historische Nachweise wirken aktueller als sie sind] — Datum und Revision je Matrixzeile angeben.
- [Dokumentation driftet vom Code] — Matrix über konkrete Pfade/Tests verankern und bei relevanten Changes aktualisieren.
- [Zu viele Screenshots blähen die Arbeit auf] — pro Hauptflow nur Desktop und Mobile, zusätzliche Bilder nur bei eigenständigem Erkenntniswert.
