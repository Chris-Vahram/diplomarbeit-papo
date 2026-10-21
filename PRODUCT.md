# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Zwei gleichrangige Hauptnutzergruppen, dazu eine dritte Gruppe im Shop:

- **Unternehmen (Rolle `company`):** Sachbearbeitung im Backoffice, überwiegend am Desktop. Wickelt eingehende Bestellungen ab (Status `submitted -> reviewed -> fulfilled`), pflegt Partner (inkl. Provisionssatz, aktiv/inaktiv), Artikel und Kategorien, importiert Excel/CSV, schaltet Artikel je Partner frei, verknüpft Logins, sieht alle Bestellungen und Provisionen.
- **Partner (Rolle `partner`):** Vertriebsfirmen des Unternehmens (Referenzkunde: ca. 100 Partner). Bestellen im Namen ihrer Endkunden: Endkunden auswählen oder anlegen, mehrere Artikel mit Mengen bestellen, eigene Bestellungen und Provision verfolgen. Nutzung am Desktop und unterwegs auf Handy/Tablet. Dashboard ist der Einstieg. Im aktuellen Stand ein gemeinsamer Login je Partnerfirma; persönliche Konten sind beschlossener Ausbau.
- **Endkunden:** nutzen das interne Portal nicht. Im Endkundenshop (Phase 2) sind sie öffentliche Besucher eines Partner-Shops und bestellen als Gast.

## Product Purpose

PAPO ist eine webbasierte, erweiterbare B2B-Partnerbestellplattform für partnergetriebene Vertriebsmodelle (Kette Unternehmen -> Partner -> Endkunde). Die erste Stufe ersetzt Bestellungen per E-Mail und Telefon durch einen internen Bestellprozess mit automatischer Partnerzuordnung und Provision je Bestellung. Erfolg heißt: Partner bestellen schnell und fehlerfrei für ihre Endkunden, das Unternehmen wickelt alles an einer Stelle ab, Provisionen sind nachvollziehbar.

Phase 2 ergänzt „Mein Webshop“: Jeder Partner gestaltet per Baukasten einen eigenen öffentlichen Shop (`paportal.at/shop/<kennung>`), über den Endkunden direkt bestellen; diese Bestellungen laufen durch dieselbe Pipeline.

PAPO ist der Produktname (HTL-Diplomarbeit, Chris + Tobias), nicht der Kunde. Der Referenzkunde bleibt ungenannt.

## Positioning

Unternehmensunabhängiges Produkt für genau diese Dreiecksbeziehung: Der Partner bestellt stellvertretend für seinen Endkunden, die Bestellung ist automatisch dem Partner zugeordnet und erzeugt einen eigenen Provisionsdatensatz mit Preis-Snapshot. Partner sehen nur freigeschaltete Artikel und nur ihre eigenen Endkunden. Der Partner-Shop nutzt dieselben Artikel, Preise und Abwicklung des Unternehmens statt eines separaten Webshops; Verkäufer im rechtlichen Sinn bleibt das Unternehmen.

## Operating Context

- Ablauf Partner: Login (Keycloak) -> Dashboard -> „Neue Bestellung“ (Endkunde wählen/anlegen, Artikel + Mengen) -> Bildschirmbestätigung + Bestell-E-Mail (B-04, Versand auf der VM aus bis SMTP-Daten vorliegen).
- Ablauf Unternehmen: Dashboard mit Zeitraum (Woche/Monat/Jahr, Standard Monat) -> Bestellliste -> Statuswechsel; Verwaltung Partner/Artikel/Kategorien/Import/Firmenprofil; Mail-Statusansicht.
- Shop: Baukasten im Partnerbereich (Seiten, Blöcke, Theme), öffentliche Shopseiten, Gastbestellung ohne Online-Zahlung.
- BMD ist das führende Artikel-Stammsystem; PAPO arbeitet derzeit mit Importdaten. Datenimport bleibt Dummy, bis Firmendaten vorliegen.
- Live-Demo auf `paportal.at` (nur Demo-Daten). Abnahme durch Betreuer Prof. Hammer und den Referenzkunden über Besprechungen.

## Capabilities and Constraints

- Stack fest: HTML, CSS, native Web Components ohne Framework und ohne Build-Schritt; Quarkus-Backend; PostgreSQL; Keycloak (Realm `partnerportal`, Rollen `company`/`partner`). Keine Alternativen vorschlagen.
- Terminologie in UI: **Artikel** (nicht Produkt), **Endkunde**, „Meine Endkunden“, „Meine Artikel“, „Mein Webshop“. UI-Texte und Fehlermeldungen auf Deutsch, einfache Sprache.
- Geldwerte als `BigDecimal`; Provision = Prozent vom Bestellwert (Shop: vom Nettowert), je Bestellung als `CommissionRecord`.
- Rollentrennung serverseitig: Partner sehen nur eigene Bestellungen, Endkunden und Provision; nur freigeschaltete Artikel.
- Shop-Baukasten: Abschnitte untereinander, keine freie Positionierung, kein freies HTML/CSS/JS, kuratierte selbst gehostete Schriften (kein Google-Fonts-Nachladen), Bilder nur PNG/JPG/WebP.
- Shop-Dateien (`frontend/shop.html`, `frontend/src/shop/`, `frontend/src/shop-editor/`, Seiten `partner-shop-*`) gehören zu Tobias' Changes; Portal-Arbeit fasst sie nicht an.
- Offen: Subdomain vs. Pfad für Shops (vorläufig Pfad), Mandantenfähigkeit/persönliche Konten (beschlossen, nicht spezifiziert), BMD-Schnittstelle, Provisionsabrechnung, Firmenregeln für Fachbereiche/Produktgruppen.

## Brand Commitments

- Name **PAPO**, Domain `paportal.at`.
- Tobias' Design-System in `Design/` (Tokens, `variables.css`, Guidelines) ist verbindlich: Prinzipien minimal, rollenunterscheidbar, datenorientiert, barrierefrei. Firmenbereich grün, Partnerbereich blau über einen gemeinsamen Akzent-Mechanismus.
- Stil: sachlich, präzise, kein übertriebener Marketington.

## Evidence on Hand

- Spezifikation: `Specification/functional_specification_document_partnerportal_final.md`, `Specification/phase2_backlog.md`, `Specification/ENDKUNDENSHOP_DECISIONS.md`, Besprechungsprotokolle in `Documentation/Besprechungsprotokolle/`.
- Design-System: `Design/` (README, `tokens.json`, `variables.css`, `guidelines/`).
- Assets: Artikel-Platzhaltergrafiken `frontend/assets/products/*.svg`, Shop-Schriften `frontend/assets/shop-samples/`, Lucide-Icons `frontend/vendor/`.
- Kein Logo-Asset, keine echten Firmendaten, keine Kundenstimmen, keine Kennzahlen oder Referenzen. Nichts davon erfinden; Rechtstexte liefert das Unternehmen.

## Product Principles

1. **Bestellen geht vor.** Der Partner-Bestellprozess ist der Kern; „Neue Bestellung“ ist immer die wichtigste Aktion im Partnerbereich.
2. **Jeder weiß, wo er ist.** Firmen- und Partnerbereich sind sofort unterscheidbar; jede Rolle sieht nur, was ihr zusteht.
3. **Daten vor Dekoration.** Tabellen, Formulare und Status tragen die Oberfläche; Elemente ohne Bedeutung fallen weg.
4. **Erweiterbar statt fertig.** Aktuelle Stufe ist der erste Schritt eines Produkts; nichts verbaut Mandanten, persönliche Konten oder den Shop, aber nichts davon wird stillschweigend vorgezogen.
5. **Nur Echtes zeigen.** Keine erfundenen Zahlen, Kunden oder Rechtsaussagen; Platzhalter sind als solche erkennbar.

## Accessibility & Inclusion

WCAG 2.1 AA: Textkontrast ≥ 4.5:1 (UI ≥ 3:1), sichtbarer Fokus, Touch-Ziele ≥ 44 px, Status nicht nur über Farbe, `prefers-reduced-motion` respektieren. Gilt für Portal und öffentliche Shopseiten. Eine bekannte Kontrast-Ausnahme ist in `Design/guidelines/colors.md` dokumentiert.
