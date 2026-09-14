# Documentation TODO

Stand: 2026-09-13

Der technische MVP ist abgeschlossen. Offene Diplomarbeits- und UI/UX-Dokumentation wird unabhängig im OpenSpec-Change `complete-thesis-documentation` bearbeitet und blockiert den technischen MVP nicht.

Visueller Polish ist für den aktuellen Screenstand abgeschlossen. Neue oder wesentlich geänderte Screens erhalten Desktop-/Mobile-Polish und passende Belege in ihrem jeweiligen UI-Change.

## Tobias: UI/UX-Dokumentation

Tobias dokumentiert, wie das Frontend aufgebaut, gestaltet und bedienbar ist.

### Inhalte

- Screenshots der wichtigsten Screens:
  - Login
  - Company: Dashboard, Produkte, Partner, Bestellungen, Datenimport
  - Partner: Dashboard, Produkte, Meine Bestellungen, Neue Bestellung, Profil
- User-Flows:
  - Firma verwaltet Produkte und Partner
  - Firma schaltet Produkte fuer Partner frei
  - Partner sieht freigeschaltete Produkte
  - Partner erstellt eine Bestellung fuer einen Kunden
  - Firma bearbeitet den Bestellstatus
- Design-System:
  - Farben fuer Company- und Partnerbereich
  - Typografie
  - Abstaende und Layout
  - Buttons, Tabellen, Cards, Modals, Toasts, Status-Badges
- Responsive Verhalten:
  - Desktop: fixe Sidebar links
  - Mobile/Tablet: Menuebutton oeffnet Sidebar
- Usability-Entscheidungen:
  - getrennte Company- und Partnerbereiche
  - klare Navigation
  - deutsche UI-Texte
  - Such-/Pagination-Tabellen fuer viele Produkte/Partner
  - Produktbilder zur besseren Erkennbarkeit

## Chris: Technische Diplomarbeits-Dokumentation

Chris dokumentiert, was technisch umgesetzt wurde, warum es so umgesetzt wurde und wie es getestet wurde.

### Inhalte

- Systemarchitektur:
  - Frontend: HTML, CSS, native Web Components
  - Backend: Quarkus/Java
  - Datenbank: PostgreSQL
  - Authentifizierung: Keycloak
- Datenmodell:
  - auf `docs/DATA_MODEL.md` aufbauen
  - wichtige Entscheidungen erklaeren: Unit-Price-Snapshot, kein CommissionLevel, Partner/Kunde-Scoping
  - Requirements-Mapping:
  - zentrale Matrix in [`docs/REQUIREMENTS_TRACEABILITY.md`](REQUIREMENTS_TRACEABILITY.md) mit Quelle, Verhalten/Abgrenzung, Code, Test/Beleg, Implementierungs- und Prüfstatus sowie Zuständigkeit/Blocker
  - Chris pflegt die technische Zuordnung und ausführbaren Nachweise; Tobias ergänzt UI-/UX-Belege; die spätere fachliche Endabnahme erfolgt gemeinsam
- Sicherheits- und Rollenmodell:
  - Company vs Partner
  - serverseitige Rollenpruefung
  - Partner sieht nur eigene Daten
- Testkapitel:
  - Backend-Test-Suite: 77/77 Tests grün (0 Fehler, 0 Fehlschläge, 0 übersprungen)
  - Browserchecks/Demo-Flow
  - relevante Edge Cases: falsche Rolle, inaktiver Partner, nicht freigeschaltete Produkte, ungueltige Imports, XSS bei Bild-URLs
- Deferred Scope:
  - B-01 bis B-08 aus `Specification/phase2_backlog.md`
  - klar schreiben: nicht vergessen, sondern bewusst aus dem MVP geschnitten

### Offene Abnahme- und Dokumentationspunkte

- Die Traceability-Matrix ist das gemeinsame Arbeitsformat; offene Zuordnungen bleiben ausdrücklich offen.
- Die MVP-Punkte 8.3, 8.4 und FD.4 bleiben bis zu einer separaten fachlichen Prüfung offen.

### Beispiel fuer Requirements-Mapping

| Anforderung | Umsetzung | Code/Modul | Test/Nachweis | Status |
|---|---|---|---|---|
| Partnerverwaltung | CRUD fuer Partner inkl. Provision und Aktivstatus | `partner` Package | `PartnerResourceTest` | umgesetzt |
| Produktverwaltung | CRUD fuer Produkte inkl. Kategorie/Bild | `product` Package, Produktseiten | `ProductResourceTest`, Browsercheck | umgesetzt |
| Produktfreischaltung | Partner sieht/bestellt nur freigeschaltete Produkte | `enablement` Package | `EnablementResourceTest` | umgesetzt |
| Bestellung | Partner erstellt Bestellung fuer Kunde mit mehreren Artikeln | `order` Package | `OrderResourceTest` | umgesetzt |
| Provision | Provision wird pro Bestellung gespeichert | `commission` Package | Assertions in `OrderResourceTest` | umgesetzt |
| Import | Excel/CSV Import mit Mapping und Validierung | `importer` Package | Import-Tests | umgesetzt |

## Forschungsfrage

Die Forschungsfrage gehoert in die Diplomarbeits-Dokumentation. Sie ist nicht direkt ein Feature im Code, sondern der rote Faden der Arbeit:

- Welches Problem untersucht die Diplomarbeit?
- Was soll mit dem Prototyp gezeigt werden?
- Woran wird spaeter bewertet, ob die Arbeit ihr Ziel erreicht hat?

Wichtig: Das langfristige Ziel enthaelt weiterhin einen Endkundenshop. Der aktuelle MVP implementiert aber bewusst zuerst den internen Company-Partner-Prozess. Deshalb sollte die Forschungsfrage nicht so klingen, als waere der Endkundenshop bereits fertig umgesetzt. Sie kann aber ausdruecken, dass das Portal darauf ausbaubar ist.

### Empfohlene Formulierung

Status: von Chris am 2026-07-07 genehmigt und vom Betreuer bestätigt.

> Wie kann ein webbasiertes Partnerportal gestaltet werden, das Unternehmen, Vertriebspartner und Endkunden in einem partnerbasierten Vertriebsmodell schrittweise verbindet und dabei Partnerverwaltung, Produktfreigabe, Bestellabwicklung, Provisionszuordnung und Auswertungen in einer benutzerfreundlichen und erweiterbaren Plattform zusammenführt?

### Verstaendlich gesagt

Die Frage heisst nicht: "Wie bauen wir nur ein kleines internes Tool?"

Sie heisst eher:

> Wie muss ein Partnerportal aufgebaut sein, damit zuerst die interne Zusammenarbeit zwischen Firma und Partner funktioniert und spaeter ein Endkundenshop sinnvoll darauf aufbauen kann?

Damit passt sie zu beidem:

- zum aktuellen MVP: internes Portal fuer Company und Partner
- zum Endziel: spaeterer Endkundenshop als Erweiterung

## Clockify Kurztexte

### Finalisierung MVP

Finalisierung MVP: Produktbilder-Feature abgeschlossen, Browser-/Security-Checks durchgefuehrt, OpenSpec archiviert, Remote-Stand gemerged und finalen MVP-Stand gepusht.

### Groesserer Implementierungsblock

MVP-Implementierung Partnerportal: Partner-/Produktverwaltung, Rollen und Zugriffsschutz, Produktfreischaltung, Kunden- und Bestellprozess, Provisionen, Dashboards, Import/Export, Tests und Demo-Verifikation umgesetzt.
