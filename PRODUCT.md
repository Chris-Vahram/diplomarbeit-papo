# Product

<!-- impeccable:product-schema 1 -->

<!-- Zusammengeführt am 22.10.2026 aus Chris' Fassung (init-Interview 16.10.) und Tobias'
     Fassung (Branch docs/impeccable-product-context, 21.10.). Wo beide abweichen, gelten
     Chris' bestätigte Interview-Antworten (Nutzung, WCAG-Stufe, Ton). -->

## Platform

web

## Users

Zwei gleichrangige Hauptnutzergruppen, dazu eine dritte Gruppe im Shop:

- **Unternehmen (Rolle `company`):** Sachbearbeitung im Backoffice, am Desktop. Wickelt eingehende Bestellungen ab (Status `submitted -> reviewed -> fulfilled`), pflegt Partner (inkl. Provisionssatz, aktiv/inaktiv), Artikel und Kategorien, importiert Excel/CSV, schaltet Artikel je Partner frei, verknüpft Logins, sieht alle Bestellungen und Provisionen.
- **Partner (Rolle `partner`):** Vertriebsfirmen des Unternehmens (Referenzkunde: ca. 100 Partner). Bestellen im Namen ihrer Endkunden: Endkunden auswählen oder anlegen, mehrere Artikel mit Mengen bestellen, eigene Bestellungen und Provision verfolgen. Überwiegend am Desktop; gelegentlich am Handy, dort muss alles ohne seitliches Scrollen funktionieren. Das Dashboard ist der Einstieg.
- **Endkunden:** nutzen das interne Portal nicht. Im Endkundenshop (Phase 2) sind sie öffentliche Besucher eines Partner-Shops und bestellen als Gast.

## Product Purpose

PAPO ist eine webbasierte, unternehmensunabhängige und erweiterbare B2B-Partnerbestellplattform für partnergetriebene Vertriebsmodelle (Kette Unternehmen -> Partner -> Endkunde). Die erste Stufe ersetzt Bestellungen per E-Mail und Telefon durch einen internen Bestellprozess mit automatischer Partnerzuordnung und Provision je Bestellung.

Erfolg heißt: Ein Partner legt in wenigen Schritten eine korrekte Bestellung mit mehreren Artikeln für einen Endkunden an, die Bestellung ist automatisch ihm zugeordnet, und das Unternehmen wickelt alles an einer Stelle ab, ohne abzutippen. Provisionen sind nachvollziehbar.

Phase 2 ergänzt „Mein Webshop“: Jeder Partner gestaltet per Baukasten einen eigenen öffentlichen Shop (`paportal.at/shop/<kennung>`), über den Endkunden direkt bestellen; diese Bestellungen laufen durch dieselbe Pipeline.

PAPO ist der Produktname (HTL-Diplomarbeit, Chris + Tobias), nicht der Kunde. Der Referenzkunde bleibt ungenannt.

## Positioning

Gebaut für genau diese Dreiecksbeziehung statt als allgemeiner Webshop: Der Partner bestellt stellvertretend für seinen Endkunden, die Bestellung gehört automatisch zum Partner und erzeugt einen eigenen Provisionsdatensatz mit Preis-Snapshot. Partner sehen nur freigeschaltete Artikel und nur ihre eigenen Endkunden, Bestellungen und Provisionen. Der Partner-Shop nutzt dieselben Artikel, Preise und dieselbe Abwicklung des Unternehmens statt eines separaten Webshops; Verkäufer im rechtlichen Sinn bleibt das Unternehmen. Artikelstammdaten kommen aus dem führenden System des Unternehmens (BMD) per Import; PAPO baut keinen konkurrierenden Stammdatenbestand auf.

## Operating Context

- Ablauf Partner: Login (Keycloak) -> Dashboard -> „Neue Bestellung“ (geführter Ablauf: Endkunde wählen/anlegen, Artikel + Mengen, Übersicht) -> Bildschirmbestätigung + Bestell-E-Mail (B-04; Versand auf der VM aus, bis SMTP-Daten vorliegen).
- Ablauf Unternehmen: Dashboard mit Zeitraum (Woche/Monat/Jahr, Standard Monat) -> Bestellliste mit Filtern -> Statuswechsel; Verwaltung von Partnern, Artikeln, Kategorien, Import, Firmenprofil; Mail-Statusansicht.
- Shop: Baukasten im Partnerbereich (Seiten, Abschnitte, Design, Bilder), öffentliche Shopseiten, Gastbestellung ohne Online-Zahlung.
- Datenmengen beim Referenzkunden: rund 200 Partner, rund 800 Artikel.
- BMD ist das führende Artikel-Stammsystem; PAPO arbeitet derzeit mit Importdaten. Der Datenimport bleibt Dummy, bis Firmendaten vorliegen.
- Live-Demo auf `paportal.at` (nur Demo-Daten, kein echter Kundenbetrieb). Abnahme durch Betreuer Prof. Hammer und den Referenzkunden in Besprechungen; Tobias verantwortet das visuelle Design.

## Capabilities and Constraints

- Umgesetzt: Partner- und Artikelverwaltung, Kategorien, Excel/CSV-Import mit Spalten-Mapping, Artikelfreischaltung je Partner, Endkunden (anlegen/bearbeiten, je Partner getrennt), Bestellungen mit mehreren Artikeln und Preis-Snapshot, Statusablauf, Provision je Bestellung, Dashboards mit Zeitraum und Trends, Bestell-E-Mails (vorbereitet), Partner-Logins über Keycloak-Benutzer-ID (mehrere je Partner möglich), Firmenprofil, Gewerbenachweis, Shop-Renderer und Shop-Baukasten.
- Stack fest: HTML, CSS, native Web Components (Light DOM) ohne Framework, Build-Schritt, npm oder CDN; Quarkus-Backend; PostgreSQL; Keycloak (Realm `partnerportal`, Rollen `company`/`partner`). Keine Alternativen vorschlagen.
- Terminologie in der UI: **Artikel** (nicht Produkt), **Endkunde**, „Meine Endkunden“, „Meine Artikel“, „Mein Webshop“, „Bestellung“, „Provision“. Alle UI-Texte und Fehlermeldungen auf Deutsch, einfache Sprache, korrekte Umlaute ä/ö/ü/ß.
- Geldwerte als `BigDecimal`; Provision = Prozent vom Bestellwert (Shop: vom Nettowert, vorläufige Entscheidung), je Bestellung als `CommissionRecord`.
- Rollentrennung serverseitig: Partner sehen nur eigene Bestellungen, Endkunden und Provision und nur freigeschaltete Artikel; inaktive Partner werden serverseitig blockiert.
- Shop-Baukasten: Abschnitte untereinander, keine freie Positionierung, kein freies HTML/CSS/JS, kuratierte selbst gehostete Schriften (kein Nachladen von Google Fonts), Bilder nur PNG/JPG/WebP.
- Zuständigkeit: Shop-Dateien (`frontend/shop.html`, `frontend/src/shop/`, `frontend/src/shop-editor/`, Seiten `partner-shop-*`) gehören zu Tobias' Shop-Changes. Portalweite Designänderungen dürfen sie mitziehen (z. B. Rollen-Tokens), werden aber mit Tobias abgestimmt.
- Offen: Subdomain oder Pfad für Shops (vorläufig Pfad), Mandantenfähigkeit und persönliche Konten mit Rollen/Audit (beschlossen, noch nicht spezifiziert), BMD-Schnittstelle, Provisionsabrechnung, Firmenregeln für Fachbereiche und Produktgruppen.

## Brand Commitments

- Name **PAPO**, Domain `paportal.at`.
- Ton: freundlich und ermutigend, aber sachlich. Partner sollen sich unterstützt fühlen (ihre Bestellungen, ihr Umsatz und ihre Provision sind sichtbar und schnell erreichbar). Kein Marketington, keine Übertreibung. Anrede „Sie“.
- Tobias' Design-System in `Design/` (Tokens, `variables.css`, Guidelines) ist die Grundlage: minimal, rollenunterscheidbar, datenorientiert, barrierefrei. Firmenbereich grün, Partnerbereich blau über einen gemeinsamen Akzent-Mechanismus. Die umgesetzte Ausprägung und die bestätigten Abweichungen stehen in `DESIGN.md`; visuelle Änderungen laufen über Tobias.

## Evidence on Hand

- Spezifikation: `Specification/functional_specification_document_partnerportal_final.md`, `Specification/phase2_backlog.md`, `Specification/ENDKUNDENSHOP_DECISIONS.md`, Besprechungsprotokolle in `Documentation/Besprechungsprotokolle/` (Feedback 12.10.2026: Bestellprozess „bereits sehr gut umgesetzt“).
- Design: `Design/` (README, `tokens.json`, `variables.css`, `guidelines/`), `DESIGN.md`, Variantenentscheidung `openspec/changes/archive/2026-10-22-refine-portal-frontend/variants/papo-varianten.html`.
- Assets: Artikel-Platzhaltergrafiken `frontend/assets/products/*.svg`, Shop-Beispielbilder `frontend/assets/shop-samples/`, Schriften `frontend/vendor/fonts/`, Lucide-Icons `frontend/vendor/`.
- Nur Demo-Daten (`DemoDataSeeder`, Demo-Logins `company-demo` / `partner-demo`). Kein Logo-Asset, keine echten Firmendaten, keine Kundenstimmen, keine Kennzahlen oder Referenzen. Nichts davon erfinden; Rechtstexte liefert das Unternehmen.

## Product Principles

1. **Bestellen geht vor.** Der Partner-Bestellprozess ist der Kern; „Neue Bestellung“ ist immer die wichtigste und schnellste Aktion im Partnerbereich.
2. **Jeder weiß, wo er ist.** Firmen- und Partnerbereich sind sofort unterscheidbar; jede Rolle sieht nur, was ihr zusteht, und die Oberfläche deutet keine fremden Daten an.
3. **Daten vor Dekoration.** Tabellen, Formulare und Status tragen die Oberfläche; Geldbeträge sind immer eindeutig; Elemente ohne Bedeutung fallen weg.
4. **Erweiterbar statt fertig.** Die aktuelle Stufe ist der erste Schritt eines Produkts; neue Stufen (Shop, Mandanten) bauen auf denselben Entitäten und demselben Design-System auf. Nichts davon wird stillschweigend vorgezogen.
5. **Nur Echtes zeigen.** Keine erfundenen Zahlen, Kunden, Geschäftsregeln oder Rechtsaussagen; offene Firmenentscheidungen bleiben sichtbar offen, Platzhalter sind als solche erkennbar.

## Accessibility & Inclusion

WCAG 2.2 AA für Portal und öffentliche Shopseiten: Textkontrast ≥ 4.5:1 (UI-Elemente ≥ 3:1), sichtbarer Tastaturfokus, Touch-Ziele ≥ 44 px auf dem Handy, Status nie nur über Farbe, `prefers-reduced-motion` respektieren. Die frühere Kontrastlücke der Primärbuttons ist durch `--color-accent-strong` geschlossen (siehe `DESIGN.md`, „The Strong Accent Rule“).
