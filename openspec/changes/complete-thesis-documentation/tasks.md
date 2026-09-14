## 1. Technische Dokumentation (Chris, wesentlich)

- [ ] 1.1 `docs/TECHNICAL_DOCUMENTATION.md` aus aktuellen Quellen erstellen: Architektur, Datenmodell, Rollen/Sicherheit, Kernentscheidungen, Tests und B-01–B-08-Abgrenzung; alle Links und Aussagen gegen Repository/Specs prüfen.
- [ ] 1.2 FR-01 bis FR-25 sowie B-01 bis B-08 in `docs/REQUIREMENTS_TRACEABILITY.md` mit konkreten Codepfaden, Tests, Status, Datum und Revision/Diff-Grundlage befüllen; offene oder nur historische Nachweise ausdrücklich so markieren und Quellen-/Linkcheck ausführen.

## 2. UI/UX-Dokumentation (Tobias-Prüfung, wesentlich)

- [ ] 2.1 `docs/UI_UX_DOCUMENTATION.md` mit Hauptscreens, Company-/Partner-Flows, Designsystem, Komponenten, Accessibility-Grundlagen und Responsive-Verhalten erstellen; vorhandene Screenshots referenzieren und fehlende Desktop-/Mobile-Belege benennen.
- [ ] 2.2 Tobias prüft Text und Screenshots; nur bestätigte Belege übernehmen und alle referenzierten Bildpfade auflösen.

## 3. Gemeinsame Abnahme (Chris + Tobias, wesentlich)

- [ ] 3.1 Dokumentation und Matrix gemeinsam gegen FSD, Besprechungsprotokolle und Phase-2-Backlog prüfen; keine CUT-/STUB-Funktion als vollständig umgesetzt darstellen.
- [ ] 3.2 Markdown-Links, Matrixstruktur und `git diff --check` prüfen, `openspec validate complete-thesis-documentation --strict` ausführen und tatsächliche Ergebnisse dokumentieren.
