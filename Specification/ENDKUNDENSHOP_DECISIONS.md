# PAPO – Endkundenshop: vorläufige Entscheidungen

Stand: 15.10.2026. Vom Team entschieden (eingetragen durch Tobias) auf Basis der
Full-Vision-FSD, des Backlogs B-01/B-02/B-04/B-05, der Protokolle 17.06., 25.08.,
31.08. und 12.10.2026 sowie einer Codeanalyse. **Alle Punkte gelten vorläufig unter
Vorbehalt des nächsten Firmenmeetings.** Die offenen Fragen wurden per E-Mail über
Prof. Hammer an die Firma weitergeleitet. Antwortet die Firma abweichend, gilt ihre
Antwort und dieses Dokument wird angepasst.

Dieses Dokument ist keine Rechtsberatung. Rechtstexte liefert das Unternehmen.

## 1. Bereits festgelegte Grundlage

Aus Full-Vision-FSD (§2, §3, §6, §7), Entscheidung vom 25.09. und Protokoll 12.10.:

- Jeder Partner kann einen eigenen öffentlichen Shop erhalten (1 Partner : 1 Shop).
- Der Shop zeigt nur für den Partner freigeschaltete und verfügbare Artikel.
- Preise und Artikeltexte bleiben zentral beim Unternehmen gesperrt.
- Gastbestellung ohne Login; optionales Endkundenkonto als spätere Stufe.
- Bestellung ist kostenpflichtig, Zahlung und Abwicklung erfolgen später;
  **keine Online-Zahlung**.
- Shopbestellungen werden automatisch dem Partner des Shops zugeordnet und laufen
  durch dieselbe Statuspipeline (submitted -> reviewed -> fulfilled) inklusive
  `CommissionRecord` mit `Partner.commissionPercent`.
- Ein Endkunde aus dem Shop wird als `Kunde` des Partners angelegt.
- Shopgestaltung als Baukasten auf der Partnerseite, Menüpunkt „Mein Webshop“ in der
  Sidebar (Protokoll 12.10.; Referenz: Jimdo).

## 2. Verkauf, Preise, Bestellung

| Thema | Entscheidung |
|---|---|
| Rechtlicher Verkäufer | **Das Unternehmen.** Der Partner ist kaufmännischer Ansprechpartner und wird im Shop so angezeigt. Impressum, AGB, Datenschutz und Widerrufsbelehrung des Unternehmens gelten für alle Shops seiner Partner und hängen am `CompanyProfile`. Im Staging deutlich markierte Platzhalter. |
| Preisangabe | Das Unternehmen gibt **Bruttopreise** an. Zusätzlich erhält jeder Artikel einen **USt-Satz** (Standard 20 %; 10 % und 13 % möglich). |
| Provisionsbasis | Provision vom **Nettowert**, berechnet aus Brutto und USt-Satz; kaufmännische Rundung auf Cent je Bestellung. |
| Bestell-Snapshot | `OrderItem` speichert Einzelpreis und USt-Satz zum Bestellzeitpunkt. |
| Internes Partnerportal | Zeigt Brutto, Netto klein darunter. |
| Bestandsdaten | Heutige Demo-Preise werden als Brutto mit 20 % interpretiert. |
| Import | Beim Spalten-Mapping wählt das Unternehmen „netto“ oder „brutto“; PAPO rechnet bei Bedarf um. |
| Pflichtdaten Gastbestellung | Nachname, Adresse **und E-Mail** (intern bleibt E-Mail optional). |
| Kunde aus dem Shop | Jede Shopbestellung legt einen **neuen** `Kunde` mit Herkunft „Shop“ an. Kein automatischer Abgleich mit bestehenden Kunden, damit öffentliche Eingaben keine fremden Datensätze verändern. Zusammenführen durch den Partner ist eine spätere Option. |
| Bestellherkunft | `Order` erhält ein Herkunftsfeld (intern / Shop). |
| Bezahlung | Platzhaltertext, z. B. „Die Rechnung erhalten Sie mit der Lieferung.“ Zahlungsart klärt die Firma. |
| B-04 | **Keine technische Voraussetzung.** Die Gastbestellung darf ohne echte Mail live gehen (Bildschirmbestätigung, Bestellnummer, Druckbeleg). Bewusste Abweichung von Full-Vision-UC-A. Mails werden ergänzt, sobald B-04 umgesetzt ist. Die FAGG-Bestätigung auf dauerhaftem Datenträger ist spätestens mit der Lieferung (Papier) möglich; Firma bestätigt. |

## 3. Shop-Adresse, Freigabe, Konten

| Thema | Entscheidung |
|---|---|
| URL | **Pfad** `paportal.at/shop/<kennung>`. Subdomain später möglich (Weiterleitung). Das Protokoll 12.10. lässt beide Varianten offen; der Pfad funktioniert mit dem bestehenden Caddy ohne Wildcard-Zertifikat. |
| Kennung | Aus dem Partnernamen vorgeschlagen, vom **Unternehmen** bestätigt oder geändert, plattformweit eindeutig. Nicht die Partnernummer (kann sich durch BMD ändern). Nur das Unternehmen darf sie ändern. |
| Freigabe | Das Unternehmen gibt den Shop **einmal** frei (Standard: aus). Danach veröffentlicht der Partner Änderungen selbst. |
| Offline-Nehmen | Das Unternehmen kann den Shop jederzeit offline nehmen, mit Pflicht-Begründung an den Partner. Shop ist automatisch offline bei inaktivem Partner oder ohne freigeschaltete Artikel. |
| Endkundenkonten | **Eigener Keycloak-Realm** (z. B. `papo-shop`) in derselben Instanz; letzte Ausbaustufe. |
| Rechte bei persönlichen Konten | Partner-Mitarbeiter bearbeiten Entwürfe; nur Partner-Admins veröffentlichen. Jede Veröffentlichung kommt ins Audit-Log. |
| Eigene Domains | Vorerst ausgeschlossen. |

## 4. Baukasten („Mein Webshop“)

Ersetzt die frühere B-02-Fassung „Logo, Farben, Name, zwei Layouts“.

**Architektur:** Der Shop wird als Daten gespeichert, nicht als HTML:
`PartnerShop` -> `ShopPage` -> `ShopBlock(type, position, config)` plus Theme.
Block-Konfigurationen werden serverseitig je Typ gegen ein Schema geprüft.
Dieselben Web Components rendern Editor-Vorschau und öffentlichen Shop.

**Gestaltung**

- Farben ohne Einschränkung frei wählbar. Das Farbthema wird in einem eigenen Reiter
  definiert und standardmäßig angewendet; einzelne Blöcke können es überschreiben und
  mit einem Klick auf das Theme zurücksetzen.
- Schriften nur aus einer kuratierten Liste selbst gehosteter Schriften (ca. 10–15),
  aus rechtlichen Gründen (kein Nachladen von Google Fonts).
- Abschnitte stehen untereinander; Optionen innerhalb eines Abschnitts (Spalten,
  Ausrichtung, Abstände, Hintergrund). Keine freie Positionierung; jeder Shop bleibt
  responsiv.
- Kein freies HTML, CSS oder JavaScript; nur Felder je Block.
- Bilder nur PNG, JPG oder WebP über den bestehenden Bild-Validator; kein SVG.
  Speicherkontingent ca. 50 MB pro Shop, im Editor angezeigt. Beim Upload bestätigt
  der Partner, die Rechte am Bild zu haben.
- Umsortieren per Drag & Drop und per Hoch/Runter-Buttons.
- Fester Footer mit Rechtstexten und „Verkauf durch <Unternehmen>“. Weitere
  CI-Vorgaben des Unternehmens erst auf dessen Wunsch.

**Seiten**

| Art | Seiten | Gestaltbar | Zählt zum Limit |
|---|---|---|---|
| System | Warenkorb, Checkout, Impressum, AGB, Datenschutz, Widerruf | nein | nein |
| Pflicht | Startseite, Artikelübersicht, Artikeldetail | ja (Vorlage) | ja |
| Frei | leer oder aus Vorlage, z. B. Über uns, Kontakt | ja | ja |

- Maximal **6 gestaltbare Seiten gleichzeitig online** (Pflicht + frei), maximal
  **20 Seiten insgesamt** inklusive Entwürfen.
- Pflichtseiten können nicht gelöscht oder offline genommen werden.
- Artikeldetail ist **eine** Vorlage für alle Artikel.
- 2–3 Vorlagen je Pflichtseitentyp; Vorlagen sind Daten (Blockfolge + Theme) und
  in einem eigenen Change ergänzbar.
- Navigation wird automatisch aus den Online-Seiten erzeugt; Reihenfolge per Drag &
  Drop, Menüname editierbar.
- Seiten-URL aus dem Titel vorgeschlagen, editierbar, eindeutig im Shop
  (z. B. `/shop/<kennung>/ueber-uns`).

**Veröffentlichen**

- Seiten werden einzeln veröffentlicht. Die Pflichtseiten müssen online sein.
- Theme-Änderungen sind ein eigener Entwurf mit Vorschau; „Theme veröffentlichen“
  aktualisiert alle Online-Seiten auf einmal. Block-Überschreibungen bleiben erhalten.
- Entwürfe werden automatisch gespeichert; Rückgängig/Wiederholen im Editor; die
  letzte veröffentlichte Version jeder Seite ist wiederherstellbar (eine Stufe).

**Editor**

- Für Desktop und Tablet; am Smartphone ein Hinweis. Vorschau umschaltbar
  Desktop/Mobil.

**Gestaltungs-Assistent (regelbasiert, keine KI)**

- Einzelne, erweiterbare Prüfregeln, z. B. Kontrast, fehlender Alt-Text, leere Seite,
  Bild zu klein oder zu groß, Startseite ohne Artikel, fehlendes Logo.
- Drei Stufen: **Tipp**; **Warnung** (Veröffentlichen mit Bestätigung möglich);
  **Blocker** (nur funktional oder rechtlich, z. B. fehlende Pflichtseite, leeres
  Pflichtfeld, Kontrast unter 3:1).
- Fehlender Alt-Text: Warnung mit Nachdruck, kein Blocker.
- Der Assistent erklärt in einfacher Sprache, warum ein Punkt wichtig ist.

## 5. Betrieb, Statistik, Rechtliches

- Staging-Shops und Entwürfe stehen immer auf `noindex`. Live-Seiten erhalten
  einfache SEO-Felder (Seitentitel, Beschreibung). Der Editor erklärt dem Partner,
  warum das wichtig ist.
- **Statistik:** eigene Auswertungsseite für den Partner, selbst gebaut, ohne
  externe Dienste. Standardmäßig ohne Cookie, aggregiert, ohne gespeicherte IP:
  Seitenaufrufe, Artikelaufrufe, In-den-Warenkorb, Bestellungen, Conversion-Rate,
  Herkunftsdomain, Gerätetyp. Eindeutige und wiederkehrende Besucher nur mit
  Einwilligung.
- **Consent-Banner:** echter Mechanismus mit gleichwertigem „Akzeptieren“ und
  „Ablehnen“, jederzeit widerrufbar.
- Pflicht-Rechtliches vor echter Veröffentlichung: Systemseiten mit Rechtstexten,
  Button „zahlungspflichtig bestellen“, Bruttopreise mit USt-Hinweis,
  Datenschutz-Einwilligung im Anfrageformular, Hinweis zur Warenkorb-Speicherung.

## 6. Anfragen (B-05)

- Pflichtfelder: Name, E-Mail **oder** Telefon, Nachricht (max. 2000 Zeichen),
  Datenschutz-Einwilligung; optional Artikelbezug.
- Spamschutz: Honeypot-Feld, Rate-Limit pro IP, Mindest-Ausfüllzeit;
  **kein externes CAPTCHA**.
- Zuordnung zum Partner über die Shop-Route. Posteingang beim Partner mit Zähler;
  Mail ergänzt, sobald B-04 umgesetzt ist.
- Nur der Partner sieht Anfragen; er kann sie als erledigt markieren und löschen.
  Keine erfundene Löschfrist.

## 7. Geplanter Schnitt in OpenSpec-Changes

Jeder Punkt ist ein eigener Change; keine Sammel-Implementierung.

| # | Change | Inhalt | Abhängigkeit |
|---|---|---|---|
| 0 | `add-article-tax-rate` | Bruttopreis + USt-Satz je Artikel, Netto-Provisionsbasis, Import netto/brutto | – |
| 1 | `add-partner-shop-foundation` | `PartnerShop`, Kennung, Freigabe/Offline-Nehmen, öffentliche Route, `noindex`, Systemseiten-Gerüst, getrennter Shop-Einstieg im Frontend | – |
| 2 | `add-shop-page-renderer` | Seiten/Blöcke/Theme als Daten, gemeinsamer Renderer, Pflichtseiten mit je einer Standardvorlage | 1 |
| 3 | `add-shop-builder-editor` | Menüpunkt „Mein Webshop“, Editor, Theme-Reiter, Entwurf/Veröffentlichen, Wiederherstellen, Bilder + Kontingent | 2 |
| 4 | `add-shop-design-assistant` | Regelbasierte Prüfungen in drei Stufen mit Erklärungen | 3 |
| 5 | `add-shop-templates` | 2–3 Vorlagen je Pflichtseitentyp (Tobias) | 2 |
| 6 | `add-shop-guest-checkout` | Warenkorb, Checkout, Bestellherkunft, Kunde aus Shop, Bruttopreise | 0, 2 |
| 7 | `add-shop-legal-consent` | Rechtstexte, Consent-Banner, Pflichthinweise | 1; vor echter Veröffentlichung |
| 8 | `add-shop-statistics` | Statistikseite für den Partner | 7 |
| 9 | `add-shop-inquiries` | B-05 | 2 |
| 10 | `add-shop-customer-accounts` | Eigener Realm, Registrierung, Verifizierung, Reset | 6, B-04 |

Die Mandantenfähigkeit blockiert den Shop nicht: Der Shop hängt am Partner und
erbt dessen spätere `company_id`. Changes 1–3 sind der Kern; Tobias implementiert
den Baukasten.

Sicherheitsgrenzen für alle Shop-Changes: eigener öffentlicher API-Namensraum
(z. B. `/api/shop/<kennung>/…`), keine interne Firmen-/Partner-API erreichbar,
Preise und Partnerzuordnung serverseitig ermittelt, Rate-Limit und Idempotenz bei
öffentlichen Schreibzugriffen, öffentliche Bildauslieferung nur für freigeschaltete
Artikel und Shop-Bilder. Negative Tests für fremde Partner, manipulierte
Preise/Mengen und doppelte Übermittlung.

## 8. Noch offen

- Rückmeldung der Firma zu allen Punkten (insbesondere Verkäufer, Rechtstexte,
  Zahlungsart, Brutto/USt).
- Konkrete Blocktypen und ihre Felder (im Change `add-shop-page-renderer`).
- Konkrete Vorlagen (Tobias, im Change `add-shop-templates`).
- ~~Synchronisation der Full-Vision-FSD~~: erledigt mit PR #39 (B-06 wieder offen, Gast +
  optionales Konto, Baukasten statt Zwei-Layout); B-04-Zeile in UC-A am 15.10. angepasst.
