# OpenSpec-Planungsreview — 11.09.2026

## Auftrag und geprüfte Grundlage

Freigegeben wurde die Umsetzung des Plans **ausschließlich in OpenSpec und Übergabedokumentation**. Kein Produktcode wurde in diesem Arbeitsblock geändert; vorhandene uncommittete Benutzeränderungen bleiben bestehen. Keine Commit-/Push-/Merge-/Deploymentaktion, kein VM-, SSH-, Secret-, Realm-, Backup- oder GitHub-Secrets-Zugriff.

Vollständig gelesen: AGENTS, Continuation Prompt, Next Session Plan, AI-Workflow; CLAUDE und aktive OpenSpec-Artefakte; Originalprotokolle [25.08.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll25_08_2026_Original.md), [31.08.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026_Original.md), [10.09.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll_10.9.md). Gegen FSD, Backlog, Roadmap, kanonische Specs und betroffene Code-/Testpfade abgeglichen. AGENTS, CLAUDE und Roadmap erhielten nur aktuelle Quellen-/Handoffhinweise; historische Inhalte blieben erhalten.

Start: main bei lokalem `fea0e5b`, identisch zum lokal gespeicherten origin/main; zahlreiche Änderungen. Deshalb kein Fetch/Pull, kein Stash. Planungsbranch: `docs/openspec-planning-2026-09-11`. Remote nicht neu geprüft.

## Neue Entscheidungen und Quellenabweichungen

- 10.09: eigene **Partnerfirma**, nicht betreibende Company, prominent anzeigen. Chris bestätigt bestehendes `Partner.name` als Quelle; keine Firmen-/Ansprechpartnermigration hierfür.
- „Huemer GmbH“ ist synthetische Testpartnerfirma. Kein reales Referenzunternehmen als Testname übernehmen.
- 25.08 fordert Kategorien verwalten/hinzufügen; 10.09 bestätigt Erweiterbarkeit. Chris bestätigt kontrollierte Company-Anlage/Umbenennung/Deaktivierung; Import unbekannter Kategorien wird abgelehnt. Die frühere pauschale Blockade durch BMD-Artikelnummer ist aufgehoben.
- 10.09 konkretisiert Kundensuche, Neuanlagebutton, Kundenüberschrift, Artikel links/Bestellung rechts und Prüfungstext. Bestehender Kern mit mehreren Artikeln bleibt.
- 25.08 nennt auch **Bearbeiten** eigener Endkunden. Chris bestätigt separate Planung unter Schutz historischer Kundendaten. Das ist nicht Bestandteil der unabhängigen Anlage.
- Chris entscheidet Loginbindung künftig über Keycloak-ID; vorhandene sichtbare Partnernummern behalten, neue automatisch vergeben. Ziel bestätigt, sichere Bestands-/Betriebszuordnung noch offen.
- Artikel-ID vom 31.08 wird durch 10.09 für die sichtbare Oberfläche aufgehoben; technische DB-ID bleibt. BMD bleibt führend. Direkte Schnittstelle und ausgenommene Felder bleiben ausgeschlossen.
- Original 25.08 nennt Bestell-E-Mail und Präsentationsformulierung ohne „simuliert“. Dies ist gewünschtes Ziel, kein Nachweis vorhandener SMTP-Funktion. Umsetzung bleibt wegen dokumentierter externer Entscheidungen offen.
- Präsentation mit Screenshots, Vorbereitung und nachvollziehbare Clockify-/KI-Nachweise bestätigt. Keine erfundenen Stunden, Screenshots oder Laufzeitergebnisse.

## Ergebnis pro aktivem Change

| Change | Umsetzungstasks | Ergebnis |
|---|---:|---|
| add-standalone-customer-creation | 0/6 | READY für Terra medium |
| fix-partner-create-commission-default | 0/2 | READY für Terra medium |
| define-mvp-traceability-format | 0/3 | READY für Terra medium |
| show-partner-company-name | 0/3 | READY für Terra medium |
| use-huemer-demo-partner | 0/2 | READY für Terra medium |
| search-order-customers | 0/3 | READY für Terra medium |
| clarify-order-customer-context | 0/3 | READY für Terra medium |
| manage-article-categories | 0/6 | READY für Terra medium |
| align-visible-article-terminology | 0/2 | READY für Terra medium; vorhandener Textdiff ist Vorarbeit, kein neuer Implementierungsnachweis |
| build-partnerportal-mvp | 50/53 | Offen: Tobias liefert 8.3/FD.4; gemeinsame 8.4-Ausfüllung/Abnahme fehlt |
| ci-cd-deployment-with-gh-actions | 30/41 | Offen: Tobias liefert datierte Betriebs-, Frontend-Reparatur- und Zugriffsgrenzennachweise |

Die sechs neuen Changes besitzen je proposal.md, design.md, mindestens eine Delta-Spec und tasks.md. Jede Task enthält Voraussetzung, Komponenten, Akzeptanz, Daten/Security, Tests, DoD und Stop. Checkboxen bezeichnen spätere Umsetzung, nicht erfolgreiche Planung. READY ist Planreife, keine Produkt- oder Betriebsabnahme.

Empfohlene Apply-Reihenfolge: Artikelsprache; 5-%-Vorauswahl; unabhängige Kundenanlage; Firmenanzeige und Huemer-Seed; Kundensuche; Kundenkontext; Kategorien. Traceability-Format unabhängig. Gemeinsame Dateiberührung erfordert Integration bestehender Änderungen, keinen pauschalen Reset.

## Präzise offene Entscheidungen — keine ausführbaren Terra-Changes

| Kandidat | Bestätigt / technische Richtung | Fehlende Entscheidung oder Evidenz; Stop |
|---|---|---|
| snapshot-order-customer-data | Unabhängige Kopie von Nach-/Vorname, Adresse, E-Mail/Telefon pro Bestellung; Kundenrelation/ID bleibt. OrderDto **und CSV** müssen Snapshot lesen; gemeinsamer Partner-/Company-Createpfad sowie Seed/Testfixtures befüllen. | Chris muss entscheiden: genügt für Altbestellungen der Kundenstand beim Umstellungszeitpunkt, oder gibt es eine maßgebliche historische Quelle? Ohne diese Entscheidung kein Backfillplan/READY. Keine ursprünglichen Werte erfinden. |
| edit-own-customers | Danach eigenes PUT /api/partner/customers/{id}, bestehende Pflichtfelder, serverseitiger Eigentümer, fremde/unbekannte ID 404. Keine Company-Kundenpflege/Löschung. | Snapshotstrategie und vollständiger Schutz aller alten Bestellungen zuerst. Atomare Änderungen/Snapshotkopie testen; alte Belege/CSV bleiben, neue Orders nutzen neue Stammdaten. Kein Live-Fallback. |
| Keycloak-ID-Bindung | Ein gemeinsamer Partnerzugang, künftig eindeutiges issuer/sub statt sichtbarer Nummer. Keine Tenant-/Organizationarbeit. | Verifiziertes Partner-ID↔issuer/sub-Mapping, Behandlung unzugeordneter/mehrdeutiger Konten; autorisierte Einrichtung/Neubindung/Entzug und sicherer Rückfall fehlen. Kein Username-Fallback, keine Partner-Selbstauskunft/Erstloginbindung. |
| Automatische Partnernummern | Bestandsnummern bleiben; neue P-Nummern oberhalb vorhandener numerischer P-Nummern, Lücken erlaubt, keine Wiederverwendung; parallele Anlage/Import zentral absichern. | Sichere Loginentkopplung sowie Importvertrag für bisherige externe Nummer/Quellenkennung und Dublettenschlüssel fehlen. Nicht als reine Profilanzeige planen: die Anzeige existiert bereits. |
| Ansprechpartner / weitere Stammdaten | Firmenanzeige nutzt vorerst vorhandenen Namen; Ansprechpartner bleibt separat. | Pflichtfelder, Company-/Partneränderungsrechte, Zuordnung bestehender Namen, Notizsichtbarkeit und unterstützte Steuerkennungsarten/Validierung fehlen. Je Anliegen später eigener kleiner Change, kein Sammelmodell. |
| Partnernachweis | Aktueller Unternehmensnachweis als eigenes sensibles Anliegen. | Nachweisarten, Dateigrenzen, Speicher, autorisierter Download, Austauschhistorie, Aufbewahrung/Löschung und Aktivierungsübergang fehlen. Keine Upload-/Storageplanung ohne Regeln. |
| BMD-Artikelnummer | Fachliche Artikelnummer; keine zweite sichtbare technische ID. | Verbindliches Export-/Spaltenformat, Pflicht/Änderbarkeit, Bestandszuordnung sowie Import-Aktualisierung/Dedupe fehlen. Kategorienverwaltung ist davon unabhängig. |
| Company-Logo/Begrüßung | Früherer Personalisierungswunsch bleibt eigenständig. | Pflegeort/-recht, Logoablauf und statische Konfiguration versus Company-Datenhaltung fehlen. Partnername löst diesen anderen Scope nicht. |
| Reale E-Mails | Drei Bestell-Empfänger als Ziel; Bestellung vor Versand sicher speichern. | Bestätigter SMTP-/Absenderstand, Empfängeradressen/-regeln, Datenschutz und Retry-/Betriebszuständigkeit fehlen. Keine Zugangsdaten anfordern/lesen. |
| In-App-Benachrichtigungen | Company-Neubestellungen teilweise bereits vorhanden. | Empfänger-/Gelesenmodell für Company, Statusereignisse und Abgrenzung von Versandfehlern fehlen. Keine pauschale E-Mail-Abhängigkeit für vorhandene Funktion erfinden. |
| MVP-Abschluss / Präsentation | Tobias: 8.3 und FD.4, Deck/Screenshots. Gemeinsam: 8.4. | Konkrete UI-Belege/Abnahme und gemeinsame technische Zuordnung fehlen. Format-Change schließt keine dieser Aufgaben. |
| CI/CD | Lokaler Merge fea0e5b enthält Frontendreparatur; kein aktueller Laufzeitbeleg. | Tobias: laufende Revision + HTTPS/OIDC/API/Login/Bestellung; Frontendpfad/Reparatur/Recovery; getrennt PR-5-Befehls-/Schlüsselgrenzen und negative Tests. Alte 404/502 nicht als aktuelle Diagnose behaupten. |

Nicht einplanen: Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Feature.

## Unabhängige Reviews und eingearbeitete Korrekturen

- Securityreview durch `security_review`, Funktions-/Test-/Sinnhaftigkeitsreview durch `functional_review`: vollständige sechs neue Artefaktsätze, Endkundenkorrektur und bestehende kleine Changes geprüft.
- Endkundenanlage: UI-Submit von HTTP-Versuchen trennen; bestehender einmaliger 401-Refresh-Retry erhalten, kein zusätzlicher Transportretry.
- Kundensuche: bestehendes maxStepReached genügt nicht. Gemeinsames Kunden-Weitergate inklusive direkter bereits besuchter Checkpoints und geänderter Draft-Pflichtfelder festgelegt.
- Kategorien: einheitlich Kategorien-IDs vor Artikel-IDs, Refresh/Recheck und 409 bei Zuordnungsdrift; Rename entdeckt Artikel erst nach Kategorielock. Import sammelt/sperrt/refresht vor endgültiger Zeilenvalidierung.
- Unabhängiger Datenbankreview durch `category_plan_review`: der zuvor wegen Nutzungslimit abgebrochene Teilreview wurde in dieser Umsetzung fortgesetzt und abgeschlossen. Zusätzlich Legacy-null beim Entfernen, Unicode-Normalisierungslänge 510, LEFT JOIN FETCH und EnablementDto-Pfad aufgenommen.
- Projektcheck `partnerportal-quality`: ausschließlich Planungs-/Dokumentationsänderungen; deutsche Texte, Rollen, Partnerisolation, feste Stack-/Geld-/Schema-/Scopegrenzen geprüft. Keine fremden Produktänderungen als eigene bewertet.
- Abschließende Datenbank-, Security- und Funktionsreviews: alle Findings eingearbeitet; neun kleine Changes READY. MVP-/CI-CD-Blocker und offene Folgeentscheidungen bleiben.

## Tatsächliche Prüfungen und Grenzen

Abschließender Artefaktlauf am 11.09.2026: `openspec validate --changes --strict --json --no-interactive` — 11/11 gültig, keine Findings. `git diff --check` — erfolgreich. Der SHA-256 des bereits vorhandenen Produkt-/Präsentationsdiffs blieb vor und nach der Planungsarbeit identisch: `a5b58fafb581d0b8edfba9b7d5acfaec52179bdfeb399a66094ae272c4697f5b`.

Keine Backend-/Browser-/Produkt-/Datenbanktests ausgeführt: diese Sitzung setzt ausschließlich Planung um. Vorhandene Produkt-/Präsentationsdiffs werden vor/nach verglichen.

`python3 tools/ai_evidence_log.py sync` nicht ausgeführt: das Skript liest standardmäßig externe Sessionarchive und schreibt ins private Roharchiv (tools/ai_evidence_log.py:21–23,194–217). Diese Zugriffe sind im engen Auftrag nicht freigegeben. Auch keine fingierte Enrichment-Zeile; dieser Repo-Report hält Zweck, Ergebnis und echte Prüfungen fest.
