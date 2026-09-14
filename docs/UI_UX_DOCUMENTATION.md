# UI-/UX-Dokumentation des PAPO-MVP

Stand: 2026-09-14
Revisionsgrundlage: gemergter Stand `599146f`

Dieses Kapitel beschreibt die tatsächlich vorhandene Benutzeroberfläche. Die fachliche
Anforderungszuordnung steht in der [Traceability-Matrix](REQUIREMENTS_TRACEABILITY.md).
Die visuelle Endabnahme durch Tobias ist als eigener OpenSpec-Schritt ausgewiesen und wird hier
nicht vorweggenommen.

## 1. Nutzerbereiche

PAPO trennt die Oberfläche in einen Company- und einen Partnerbereich. Der Router ordnet jeder
Route eine erforderliche Rolle zu; die endgültige Berechtigung wird zusätzlich im Backend geprüft.

### Company

Die Company verwaltet im Dashboard, in der Artikelverwaltung, in der Kategorieverwaltung und in
der Partnerverwaltung die zentralen Daten. Dazu kommen Datenimport, Bestellübersicht,
Statusänderungen und Provisionsexport.

Wichtige Routen:

- `#/company/dashboard` — Kennzahlen und aktuelle Bestellungen
- `#/company/products` — Artikelverwaltung
- `#/company/categories` — Artikelkategorien
- `#/company/partners` — Partner und Freischaltungen
- `#/company/orders` — Bestellungen, Filter und Statusänderung
- `#/company/import` — Excel-/CSV-Import

### Partner

Der Partner sieht sein Dashboard, seine freigeschalteten Artikel, seine Bestellungen, sein
Profil und seine Endkunden. Eine Bestellung wird immer für einen ausgewählten Endkunden des
angemeldeten Partners erfasst.

Wichtige Routen:

- `#/partner/dashboard` — Partnerkennzahlen und Schnellaktionen
- `#/partner/products` — freigeschaltete Artikel
- `#/partner/orders` — eigene Bestellungen
- `#/partner/orders/new` — geführte neue Bestellung
- `#/partner/customers` — eigene Endkunden
- `#/partner/profile` — begrenzte eigene Stammdaten

Endkunden besitzen im MVP keinen eigenen Portalzugang.

## 2. Hauptflows

### Artikel und Kategorien verwalten

1. Company öffnet die Artikelverwaltung.
2. Sie sucht und filtert Artikel oder öffnet das Formular für einen neuen Artikel.
3. Name, Beschreibung, Preis, Verfügbarkeit, Kategorie und optional ein Bild werden gepflegt.
4. Kategorien können über die eigene Kategorienseite erstellt, umbenannt, aktiviert oder
   deaktiviert werden.
5. Änderungen werden über das bestehende REST-Backend gespeichert und per Toast bestätigt.

### Partner verwalten und Artikel freischalten

1. Company öffnet die Partnerverwaltung und legt einen Partner an oder bearbeitet ihn.
2. Provision und Aktivstatus werden dort gesetzt.
3. Über die Partneransicht werden Artikel einzeln freigeschaltet oder wieder entzogen.
4. Der Partner sieht anschließend ausschließlich die freigeschalteten Artikel.

### Endkunden verwalten

1. Partner öffnet „Meine Endkunden“.
2. Er legt einen Endkunden an oder verwendet einen bestehenden Datensatz.
3. Nachname und Adresse sind Pflichtfelder; weitere Kontaktangaben sind optional.
4. Die Liste bleibt auf den aktuell angemeldeten Partner begrenzt.

### Bestellung aufgeben

1. Partner startet „Neue Bestellung“.
2. Er wählt den Endkunden, sucht und filtert Artikel und legt mehrere Positionen mit Mengen in
   den Warenkorb.
3. Der Wizard zeigt Prüfung, Positionen, Summen und die Bestätigung innerhalb der Anwendung.
4. Nach dem Absenden erscheint eine Bestellzusammenfassung. Die Company kann den Status danach
   von `submitted` über `reviewed` auf `fulfilled` setzen.

## 3. Designsystem

Das Frontend verwendet eine gemeinsame CSS-Basis in `frontend/src/styles/base.css` und native
Web Components in `frontend/src/components/`.

| Element | Verwendung |
|---|---|
| Company-Akzent | Blau für Company-Navigation und Company-Inhalte |
| Partner-Akzent | Grün für Partner-Navigation und Partner-Inhalte |
| Typografie | System-Font-Stack ohne externe Webfonts |
| Layout | Sidebar, Inhaltsbereich, Cards und responsive Tabellen |
| Formulare | `form-field`, modale Formulare und klare Pflichtfeld-Hinweise |
| Tabellen | `data-table` mit Suche, Filterung und Pagination bei größeren Datenmengen |
| Rückmeldung | `toast-notice` für Erfolg, Fehler und Validierung |
| Status | `status-badge` für Bestell- und Aktivstatus |
| Bilder | `image-chooser` und Artikelbild-Vorschau |

Die Präsentation verwendet für ihre Beispielbilder ebenfalls echte Browseraufnahmen und nicht
nachgezeichnete Mockups.

## 4. Responsive Verhalten

Auf Desktopgrößen steht die feste Navigation links und der Inhaltsbereich nutzt die verfügbare
Breite für Tabellen und Cards. Auf Mobile-/Tabletgrößen wird die Navigation über den
Menübutton geöffnet. Tabellen, Formulare und Bestellschritte werden untereinander angeordnet;
die zentralen Aktionen bleiben erreichbar.

Vorhandene Belege:

- [Partner-Dashboard, Desktop](partner_portal_audit_suite/Audit/evidence/04-ux/partner-dashboard-1440.png)
- [Neue Bestellung, Desktop](partner_portal_audit_suite/Audit/evidence/04-ux/order-new-1440.png)
- [Neue Bestellung, Mobile](partner_portal_audit_suite/Audit/evidence/04-ux/m-order-new-390.png)
- [Endkunden, Mobile](partner_portal_audit_suite/Audit/evidence/04-ux/m-customers-390.png)
- [Bestelldetails, Mobile](partner_portal_audit_suite/Audit/evidence/04-ux/m-order-details-390.png)
- [Artikelverwaltung, Desktop](partner_portal_audit_suite/Audit/evidence/04-ux/company-products-1440.png)
- [Partnerverwaltung, Desktop](partner_portal_audit_suite/Audit/evidence/04-ux/company-partners-1440.png)

Zusätzliche aktuelle Browseraufnahmen für die Präsentation:

- [Partner-Firmenname, Desktop](../output/playwright/show-partner-company-name-desktop.png)
- [Artikelkategorien, Desktop](../output/playwright/article-categories-desktop.png)
- [Kategorienverwaltung, Desktop](../output/playwright/integrate-category-management-desktop.png)
- [Kategorienverwaltung, Mobile](../output/playwright/integrate-category-management-mobile.png)

## 5. Accessibility-Grundlagen

- Formulare verwenden beschriftete Eingabefelder und kennzeichnen Pflichtangaben.
- Interaktive Elemente sind als Buttons oder Links umgesetzt und damit per Tastatur erreichbar.
- Modale Dialoge besitzen sichtbare Aktionen zum Speichern und Abbrechen; `Esc` wird für das
  Schließen unterstützt, wo der Dialog es vorsieht.
- Status und Rückmeldungen werden zusätzlich als Text dargestellt und nicht nur über Farbe
  vermittelt.
- Die Anwendung verwendet deutsche, verständliche Bezeichnungen und Fehlermeldungen.
- Nutzereingaben und Importwerte werden vor HTML-Einfügung escaped.

## 6. UX-Entscheidungen

- Die getrennten Bereiche machen sichtbar, ob gerade Company- oder Partnerdaten bearbeitet
  werden.
- Der Bestellwizard reduziert den komplexen Bestellvorgang auf nachvollziehbare Schritte.
- Artikelsuche, Kategorienfilter und Pagination unterstützen viele Artikel.
- Endkunden werden als wiederverwendbare Datensätze geführt, damit der Partner nicht bei jeder
  Bestellung dieselben Daten neu eingeben muss.
- Die Oberfläche bleibt bewusst bei einem gemeinsamen organisatorischen Zugang je Partnerfirma;
  persönliche Mitarbeiterkonten und ein öffentlicher Endkundenshop sind spätere Ausbaustufen.

## 7. Offene Beleggrenze

Die oben verlinkten Bilder lösen die im Repository vorhandenen Screenshotpfade auf. Die
abschließende Prüfung von Text, Bildauswahl und fachlicher Darstellung durch Tobias ist noch
offen. Fehlende oder nur historische Belege werden in der Matrix als solche gekennzeichnet und
nicht als aktuelle technische Abnahme ausgegeben.

## Quellen

- [Frontend-Konventionen](CONVENTIONS.md)
- [Traceability-Matrix](REQUIREMENTS_TRACEABILITY.md)
- [Funktionale Spezifikation](../Specification/functional_specification_document_partnerportal_final.md)
- [Besprechungsprotokoll 14.9](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll_14.9.md)
