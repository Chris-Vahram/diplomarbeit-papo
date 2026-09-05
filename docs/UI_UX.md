# UI/UX — Partnerportal

## Aufbau

Das Frontend ist eine statische JavaScript-Anwendung mit nativen Web Components im
Light DOM. Der Router liegt in `frontend/src/lib/router.js`; jede Seite lädt ihre
Daten selbst über `frontend/src/lib/api.js`. `frontend/src/styles/base.css` enthält
das gemeinsame Design und die responsiven Breakpoints.

Company und Partner sind getrennte Bereiche. Der Router schützt Routen zusätzlich
zur serverseitigen Rollenprüfung; die Navigation zeigt nur zur Rolle passende Ziele.
Auf schmalen Viewports öffnet der Menüknopf die seitliche Navigation, die nach einer
Navigation, Backdrop-Klick oder Escape wieder schließt.

## Routen und Kernabläufe

| Bereich | Route | Seite |
|---|---|---|
| Anmeldung | `#/login` | `login-page.js` |
| Firma | `#/company/dashboard` | `company-dashboard-page.js` |
| Firma | `#/company/products` | `company-product-list-page.js` |
| Firma | `#/company/partners` | `company-partner-list-page.js` |
| Firma | `#/company/orders` | `company-order-list-page.js` |
| Firma | `#/company/import` | `company-import-page.js` |
| Partner | `#/partner/dashboard` | `partner-dashboard-page.js` |
| Partner | `#/partner/products` | `partner-product-list-page.js` |
| Partner | `#/partner/orders` | `partner-order-list-page.js` |
| Partner | `#/partner/orders/new` | `partner-order-new-page.js` |
| Partner | `#/partner/customers` | `partner-customer-list-page.js` |
| Partner | `#/partner/profile` | `partner-profile-page.js` |

Firmen verwalten Artikel und Partner, schalten Artikel frei, prüfen Bestellungen und
importieren Daten. Partner sehen nur freigeschaltete Artikel, verwalten eigene
Endkunden und erstellen Bestellungen. Der Bestellablauf führt durch Endkunde,
Artikel/Warenkorb, Übersicht und Bestätigung. Die Bestätigung enthält einen
druckbaren Auftragsbeleg; die E-Mail-Darstellung ist bewusst simuliert.

## Gemeinsame Komponenten

| Komponente | Zweck |
|---|---|
| `data-table` | Tabellen, Suche, Filter, Pagination und klickbare Zeilen |
| `form-field` | Beschriftete, erforderliche Formularfelder mit Fehlermeldung |
| `modal-dialog` | Dialog mit Escape/Backdrop-Schließen, Scroll-Lock und Fokus auf „Schließen“ |
| `toast-notice` | Kurzlebige deutsche Erfolgs-, Hinweis- und Fehlermeldungen |
| `status-badge` | Status für Bestellungen und Partner |
| `metric-card` | Kennzahlen auf Dashboards |
| `image-chooser` | Artikelbild per Upload, Galerie oder externer URL |

Registrierung zentral: `frontend/src/components/index.js`. Inhalte aus Formularen,
Imports und APIs werden vor HTML-Ausgabe über `escapeHtml` behandelt.

## Responsivität und Zugänglichkeit

- Desktop: permanente Seitenleiste, Tabellen und Filter in voller Breite.
- Mobil: Menüknopf statt permanenter Navigation; Navigation schließt nach Auswahl;
  Bestellschritte, Karten und Formulare bleiben einspaltig bedienbar.
- Native Buttons, Eingaben, Selects und Labels liefern Tastaturbedienung und
  sichtbaren Browserfokus. Dialoge schließen mit Escape und setzen beim Öffnen den
  Fokus auf ihre Schließen-Schaltfläche.
- Bilder nutzen neutrale Platzhalter oder leere Alternativtexte, wenn sie rein
  dekorativ sind. Texte, Validierung und Statusmeldungen sind auf Deutsch und
  verwenden fachlich „Artikel“.

## Verifikation

Am 04.09.2026 wurden lokaler Login, Company-Artikelansicht mit Bildauswahl,
Bestellfilter sowie Partner-Navigation, Artikelkatalog, Endkunden-Auswahl,
Warenkorb, Bestellübersicht und Bestätigung im Browser auf Desktop und 390×844 px
geprüft. Die Druckschaltfläche öffnet eine einseitige, lesbare Systemdruckvorschau
des Auftragsbelegs mit synthetischen Daten.
