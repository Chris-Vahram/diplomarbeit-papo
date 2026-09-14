## Context

MVP-8.4 ist eine gemeinsame Dokumentationsaufgabe. Das Tabellenmuster in `docs/DOCUMENTATION_TODO.md` und `docs/partner_portal_audit_suite/Audit/01_PROJECT_OVERVIEW_REPORT.md` existieren bereits; der Auditbericht beschreibt historische Revisionen. Das bestätigte Ergebnis dieses kleinen Changes ist ein Format, keine erneut behauptete vollständige MVP-Abnahme.

## Goals / Non-Goals

**Ziel:** einheitliche, überprüfbare Zuordnung für spätere gemeinsame Bearbeitung.

**Nicht enthalten:** vollständiges Testaudit, neue Browserbelege, Diplomarbeitstext, Nachbauen fehlender Features, UI/UX-Dokumentation 8.3, visueller Polish FD.4, Schließen oder Archivieren des MVP-Changes.

## Decisions

- Ziel ist `docs/REQUIREMENTS_TRACEABILITY.md`, verlinkt aus `docs/DOCUMENTATION_TODO.md`. Markdown genügt; keine Generatoren, Tabellenbibliothek oder zweite historische Auditkopie.
- Eine ausfüllbare Matrix mit genau 33 eindeutigen Quellenzeilen, sortiert FR-01 bis FR-25, dann B-01 bis B-08. Pro Zeile ID, kurzer Quellentitel und relativer Link zum jeweiligen FSD-/Backlog-Abschnitt. Neuere Besprechungsdeltas werden später in derselben Quellenzelle ergänzt, ohne FR-Nummern umzudeuten.
- Spalten exakt: `ID | Quelle | Verhalten/Abgrenzung | Code | Test/Beleg | Implementierungsstatus | Prüfstatus/Datum/Revision | Zuständigkeit/Blocker`.
- Für das Format Quellentitel und Quellenlinks aus FSD/Backlog übernehmen. Noch nicht recherchierte Code-/Testzellen tragen `OFFEN – Zuordnung ausständig`; Implementierungsstatus `NICHT BEWERTET`; Prüfstatus `NICHT GEPRÜFT`. Dies beschreibt fehlende Zuordnung, nicht fehlende Produktimplementierung. B-Zeilen übernehmen den belegten CUT-/STUB-Stand samt Quelle, nicht einen erfundenen Liefertermin.
- Die Legende erlaubt später `UMGESETZT`, `TEILWEISE`, `NICHT UMGESETZT`, `NICHT BEWERTET` sowie belegte Backlogstatus für Implementierung; `NICHT GEPRÜFT`, `HISTORISCH BELEGT`, `GEPRÜFT` für Prüfung. Ein `GEPRÜFT`-Eintrag benötigt ausführbaren Testnamen oder konkreten Browserbeleg, Datum, Ergebnis und Revision; bei schmutzigem Arbeitsbaum zusätzlich die benannte Diff-Grundlage. Ein historischer Testlauf ist nie eine neue Regression.
- Zuständigkeit: Chris technische Code-/Testzuordnung, Tobias UI-Belege, gemeinsame Endabnahme separat. Offene externe Daten oder Prüfungen bleiben mit Verantwortlichem und genau fehlender Entscheidung sichtbar. Keine pauschale Erledigt-Markierung von 8.4 aufgrund dieses Formats.
- Ein ausgefülltes Beispielformat darf als ausdrücklich fiktive Beispieldaten außerhalb der 33-Zeilen-Matrix erläutert werden; keine echte Evidenz erfinden. Die vorhandene Auditmatrix dient nur als Navigationshilfe für spätere Zuordnung.
- Nur Repository-Quellen und sanitierte Nachweise referenzieren. Keine Rohchats, privaten Archive, Zugangsdaten oder personenbezogenen Firmendaten einbetten.

## Risks / Trade-offs

- `NICHT BEWERTET` könnte als fehlende Funktion verstanden werden: Legende trennt fehlende Dokumentationszuordnung von Implementierungsstand.
- Veraltete Angaben könnten als aktuelle Abnahme erscheinen: eigene Status-/Datum-/Revisionsspalte und historischer Nachweistyp sind verpflichtend.
- Format fertig, MVP-Dokumentation unvollständig: 8.4 bleibt unverändert offen. Fehlende Tobias-Belege blockieren spätere gemeinsame Abnahme, nicht die Erstellung des Formats.

## Migration Plan

Keine Datenmigration oder Laufzeitänderung. Bestehende Auditberichte unverändert erhalten. Kein externes System und keine neue Abhängigkeit.

## Readiness

Format, Umfang und Zuständigkeit sind durch Chris bestätigt. Keine weitere Entscheidung für diesen reinen Format-Change nötig; unabhängige Artefaktprüfung wird in `review.md` dokumentiert.
