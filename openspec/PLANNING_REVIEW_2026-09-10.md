# OpenSpec-Planungsreview — 10.09.2026

> Fortschreibung 11.09.2026: Dieser Report bleibt historische Evidenz. Maßgeblich für aktuelle READY-/Blockerentscheidungen ist [PLANNING_REVIEW_2026-09-11.md](PLANNING_REVIEW_2026-09-11.md), einschließlich sechs neuer Changes und korrigierter Kategorien-/Kunden-/Loginentscheidungen. Die folgenden alten Blocker sind nicht ungeprüft zu übernehmen.

## Ergebnis

| Change | Status | Nächste Aktion |
|---|---|---|
| `add-standalone-customer-creation` | READY für Terra medium | Tasks implementieren und alle vorgesehenen Backend-/Browserchecks ausführen |
| `fix-partner-create-commission-default` | READY für Terra medium | Kleine UI-Korrektur mit Browserregression |
| `define-mvp-traceability-format` | READY für Terra medium | Nur Format/Quellenindex erstellen; MVP-8.4 offen lassen |
| `build-partnerportal-mvp` | BLOCKIERT für Abschluss | 8.3/FD.4 Tobias; 8.4 gemeinsame Ausfüllung/Abnahme |
| `ci-cd-deployment-with-gh-actions` | BLOCKIERT | Tobias-Diagnose und datierte Betriebs-/Securitynachweise; siehe Change-`review.md` |

## Nicht als aktive Changes geplant

- Partneridentität/Stammdaten: Nummern-/Loginmigration, Bestandszuordnung, Pflicht-/Änderungsregeln, Notizsichtbarkeit und Steuerkennung offen. 5-%-Vorauswahl ist bewusst separat.
- Partnernachweis: Nachweisarten, Speicher, Downloadrechte, Dateigrenzen, Austausch, Aufbewahrung/Löschung und Aktivierungsübergang offen.
- Artikel-Minimalmodell: vereinfachte Firmenmail oder verbindliche Beispieldaten sowie Import-/Bestandsregeln fehlen.
- Kategorien: Migration des Freitextfelds und BMD-Zusammenspiel offen.
- Unternehmenspersonalisierung: statische Konfiguration oder Company-Modell sowie Pflege-/Logoablauf offen.
- Reale E-Mails: SMTP/Absender, Empfänger, Retry-/Betriebsverantwortung offen.
- In-App-Benachrichtigungen: Empfängermodell, Gelesenstatus und E-Mail-Abgrenzung offen.
- Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Feature bleiben ausgeschlossen.

## Prüfungen

Am 10.09.2026 erfolgreich: `openspec validate --strict` einzeln für alle fünf obigen Changes; `git diff --check`. Die frühere MVP-Warnung wurde durch Umnummerierung 9.1–9.5 auf 10.1–10.5 beseitigt, ohne Checkboxstatus zu ändern. Keine Produkt-, Backend-, Browser-, Infrastruktur- oder externe Prüfung ausgeführt. Kein Commit, Push, Merge oder Deployment.
