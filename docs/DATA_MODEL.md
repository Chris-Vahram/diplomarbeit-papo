# Datenmodell — Partnerportal

ER-Diagramm und Entitätsbeschreibungen des fertigen Schemas (Phase 1–5). Rationale
hinter einzelnen Entscheidungen (z. B. kein `CommissionLevel`, Unit-Price-Snapshot)
steht in `openspec/changes/build-partnerportal-mvp/design.md` — hier nur das
Modell selbst.

## ER-Diagramm

```mermaid
erDiagram
    PARTNER ||--o{ PRODUCT_ENABLEMENT : "schaltet frei"
    PRODUCT ||--o{ PRODUCT_ENABLEMENT : "wird freigeschaltet"
    PRODUCT_IMAGE o|--o{ PRODUCT : "wird referenziert"
    ARTICLE_CATEGORY o|--o{ PRODUCT : "klassifiziert"
    PARTNER ||--o{ KUNDE : "legt an (ownerPartner)"
    PARTNER ||--o{ ORDER : "erstellt"
    KUNDE ||--o{ ORDER : "ist Kunde von"
    ORDER ||--|{ ORDER_ITEM : "enthält"
    PRODUCT ||--o{ ORDER_ITEM : "wird bestellt"
    ORDER ||--|| COMMISSION_RECORD : "erzeugt"
    ORDER ||--o{ ORDER_EMAIL : "löst aus"
    PARTNER ||--o{ COMMISSION_RECORD : "erhält"
    PARTNER ||--o| PARTNER_TRADE_PROOF : "reicht ein"
    PARTNER ||--o{ PARTNER_LOGIN : "hat Logins"
    PARTNER ||--o| PARTNER_SHOP : "betreibt"

    COMPANY_PROFILE {
        Long id PK
        String singletonKey UK "CURRENT; genau ein aktives Portalprofil"
        String businessName
        String taxCountry "ISO-3166-1 Alpha-2"
        BusinessTaxIdentifierType taxIdentifierType "VAT_ID oder NATIONAL_TAX_ID"
        String taxIdentifierValue "normalisiert, max. 64 Zeichen"
        String registerCountry "ISO-3166-1 Alpha-2"
        TradeRegisterIdentifierType registerType "GISA oder OTHER_TRADE_REGISTER"
        String registerValue "max. 64 Zeichen"
    }

    PARTNER {
        Long id PK
        String partnerNumber UK "kanonisch Großschreibung, z.B. PARTNER-DEMO; = Keycloak-Login"
        String name
        String address
        String email
        String phone
        String taxCountry "ISO-3166-1 Alpha-2"
        BusinessTaxIdentifierType taxIdentifierType "VAT_ID oder NATIONAL_TAX_ID"
        String taxIdentifierValue "normalisiert, max. 64 Zeichen"
        String internalNote "nullable, max. 4.000 Zeichen, nur Company-DTO"
        BigDecimal commissionPercent "5.2, Preset 5/10/15 + individuell"
        boolean active "DB-Flag, nicht mit Keycloak synced"
    }

    PARTNER_TRADE_PROOF {
        Long id PK
        Long partner_id FK UK
        String registerCountry "ISO-3166-1 Alpha-2"
        TradeRegisterIdentifierType registerType
        String registerValue "max. 64 Zeichen"
        String version "Versionskennung"
        bytea data "validierte PDF/JPEG/PNG-Daten"
        String contentType
        String filename "nur Anzeige"
        long sizeBytes "max. 10 MiB"
        Instant uploadedAt
        PartnerTradeProofStatus status "PENDING, ACCEPTED, REJECTED"
        Instant reviewedAt
        String rejectionReason "nullable"
    }

    PARTNER_SHOP {
        Long id PK
        Long partner_id FK UK
        String kennung UK "3-40 Zeichen a-z, 0-9, einzelne Bindestriche"
        Instant releasedAt "nullable = nie freigegeben"
        boolean offlineByCompany
        String offlineReason "nullable, max. 500 Zeichen"
        Instant createdAt
    }

    PRODUCT {
        Long id PK
        String name
        BigDecimal price "10.2"
        String description
        boolean available
        Long article_category_id FK "optional"
        String imageUrl
        Long image_id FK "optional; gespeichertes Galeriebild"
    }

    PRODUCT_IMAGE {
        Long id PK
        bytea data "validierte PNG/JPEG/WEBP-Daten"
        String contentType
        String filename "nur Anzeige, nicht als Speicherpfad"
        long sizeBytes
        Instant uploadedAt
    }

    ARTICLE_CATEGORY {
        Long id PK
        String name UK "1–255 Zeichen, normalisiert eindeutig"
        String normalized_name UK
        boolean active "deaktivierte Bestandszuordnung bleibt erlaubt"
    }

    PRODUCT_ENABLEMENT {
        Long id PK
        Long partner_id FK
        Long product_id FK "UNIQUE(partner_id, product_id)"
    }

    KUNDE {
        Long id PK
        String lastName "Pflichtfeld"
        String address "Pflichtfeld"
        String firstName
        String email
        String phone
        Long ownerPartner_id FK "Scoping: nur eigene Kunden sichtbar"
    }

    ORDER {
        Long id PK
        LocalDate orderDate
        Long partner_id FK "auto-zugewiesen bei Erstellung"
        Long kunde_id FK
        OrderStatus status "SUBMITTED, REVIEWED, FULFILLED"
        BigDecimal orderValue "10.2, aus OrderItems abgeleitet"
    }

    ORDER_ITEM {
        Long id PK
        Long order_id FK
        Long product_id FK
        int quantity
        BigDecimal unitPriceSnapshot "10.2, Preis zum Bestellzeitpunkt"
    }

    ORDER_EMAIL {
        Long id PK
        Long order_id FK "UNIQUE mit type + recipientRole"
        String type "ORDER_CONFIRMATION, ORDER_NOTICE_PARTNER, ORDER_NOTICE_COMPANY, SHIPPING_CONFIRMATION"
        String recipientRole "CUSTOMER, PARTNER, COMPANY"
        String recipientAddress "voll nur solange offen, danach maskiert/leer"
        String status "PENDING, SENDING, SENT, FAILED, SKIPPED"
        int attempts
    }

    COMMISSION_RECORD {
        Long id PK
        Long order_id FK "UNIQUE (1:1 mit Order)"
        Long partner_id FK
        BigDecimal orderValue "10.2, Berechnungsbasis-Snapshot"
        BigDecimal commissionPercentUsed "5.2, Prozentsatz-Snapshot"
        BigDecimal commissionValue "10.2, berechneter Betrag"
    }
```

`Order` mappt in der DB auf Tabelle `orders` (`ORDER` ist ein reserviertes
SQL-Wort in PostgreSQL) — siehe `docs/CONVENTIONS.md`.

## Entitäten im Detail

### Partner
Ein Vertriebspartner (Firma). `commissionPercent` ist ein einfaches numerisches
Feld (kein `CommissionLevel`), von der Firma per Dropdown (5/10/15 % oder
individueller Wert) gesetzt. `active` ist ein reines DB-Flag — Keycloak weiß
nichts von Aktiv/Inaktiv; `CurrentPartner.resolve()` prüft es serverseitig bei
jedem partnerbezogenen Request. Der Login-Link läuft über `PartnerLogin`
(siehe unten): `resolve()` sucht die Bindung zum verifizierten OIDC-Subject
(`sub`, Keycloak-Benutzer-ID). `partnerNumber` wird bei jedem Schreibzugriff
(Anlage/Bearbeitung/Import) auf Großschreibung kanonisiert, damit der
case-sensitive Unique-Constraint je Firma (`company_id`, `partnerNumber`)
Groß-/Kleinschreibungs-Duplikate verhindert.
Ohne aktive Bindung ist kein Partner-Login möglich (Release B, kein Benutzername-Fallback mehr).
`internalNote` ist eine optionale Plain-Text-Notiz mit maximal 4.000 Zeichen,
die ausschließlich in Company-Partnerantworten enthalten ist. Partner-
Self-Service, Importe, Exporte, Bestellungen und Provisionsantworten erhalten
dieses Feld nicht. Die Firma verwaltet außerdem genau eine syntaktisch geprüfte
Steueridentifikation aus Land, Art (`VAT_ID` oder `NATIONAL_TAX_ID`) und Wert;
Details und Grenzen stehen in [`docs/TAX_IDENTIFIERS.md`](TAX_IDENTIFIERS.md).
Zusätzlich kann ein Partner genau einen aktuellen Trade-Proof mit geprüfter
Registerkennung und PDF/JPEG/PNG-Datei besitzen. Ein Ersatz überschreibt den
aktuellen Datensatz ohne Historie; der Status wird durch die Firma geprüft und
ändert `active` nicht automatisch. Binärdaten werden nicht in normalen
Partner-DTOs ausgeliefert.

### PartnerLogin (Tabelle `partner_login`)
Bindet ein verifiziertes OIDC-Subject (`oidcSubject`, eindeutig) an einen Partner;
mehrere Logins pro Partner sind möglich (heute geteilter Login, später persönliche
Konten). `createdBy`/`revokedBy` speichern das Subject des handelnden Firmenbenutzers.
Entfernen ist weich (`revokedAt`): ein entfernter Login löst nie mehr auf, und ein
Partner mit irgendeiner (auch entfernten) Bindung bekommt keinen Benutzername-Fallback.
Verwaltet nur von der Firma (`/api/company/partners/{id}/logins`, Dialog „Logins“).

### CompanyProfile (Tabelle `companyprofile`)
Die Firma ist der Mandant (`add-company-tenancy`). `Partner`, `Product`,
`ArticleCategory` und `ProductImage` tragen `company_id`; alle übrigen Daten hängen über
Partner, Bestellung oder Shop an genau einer Firma. Firmenbenutzer lesen und ändern nur
ihr eigenes Profil (Firmenname, Steueridentifikation, Registerkennung, „E-Mail für
Bestellhinweise“ `orderNoticeEmail`) über `/api/company/profile`. `singletonKey=CURRENT`
bleibt bis Release B nur als Kompatibilität für ein zurückgerolltes Image auf Firma 1.
Partnernummer und Kategoriename sind je Firma eindeutig. Demo-Werte sind synthetisch.

### CompanyLogin (Tabelle `company_login`)
Bindet ein verifiziertes OIDC-Subject (eindeutig) an genau eine Firma, gleiche Form wie
`PartnerLogin`. `CurrentCompany.resolve()` ermittelt daraus die Firma jedes
Firmen-Requests; Header, Host, Query und Body wählen nie die Firma. Ein Subject ist
entweder als Firma oder als Partner gebunden, nie beides (gemeinsame Sperre
`login_subject:<sub>`). Angelegt nur über `deploy/tenancy/provision-company.sql`.

### PartnerTradeProof
`PartnerTradeProof` ist eine optionale 1:1-Beziehung zu `Partner` für genau das
aktuelle Nachweisdokument. Partner dürfen ihren eigenen Nachweis hochladen oder
ersetzen und den eigenen Status sehen. Die Firma darf Metadaten und das private
Dokument abrufen sowie die aktuelle Version akzeptieren oder ablehnen. Uploads
sind auf 10 MiB und verifizierte PDF/JPEG/PNG-Inhalte begrenzt; Downloads setzen
`Cache-Control: private, no-store` und `X-Content-Type-Options: nosniff`.

### PartnerShop
Optionale 1:1-Beziehung zu `Partner` (Tabelle `partner_shop`) für den öffentlichen
Endkundenshop (B-01, `add-partner-shop-foundation`). Der Datensatz entsteht beim
ersten Aufruf durch Firma oder Partner. `kennung` ist plattformweit eindeutig,
wird aus dem Partnernamen vorgeschlagen (Umlaute transliteriert, bei Kollision
Zahlensuffix), nie aus der Partnernummer, und nur von der Firma geändert; der
Shop ist unter `/shop/<kennung>` erreichbar. Der wirksame Status wird nicht
gespeichert, sondern berechnet: `NOT_RELEASED` (kein `releasedAt`),
`OFFLINE_COMPANY` (`offlineByCompany`, Grund für den Partner sichtbar),
`OFFLINE_INACTIVE` (`Partner.active == false`), `OFFLINE_NO_ARTICLES` (keine
freigeschaltete, verfügbare Artikel), sonst `ONLINE`. Erneute Freigabe löscht
den Grund. Öffentlich ausgeliefert werden nur Kennung und Anzeigename.

### Product
Zentral von der Firma verwaltete Artikel. `available` steuert Bestellbarkeit
unabhängig von der partnerspezifischen Freischaltung (`ProductEnablement`) —
ein Produkt kann für einen Partner freigeschaltet, aber gerade nicht
verfügbar sein (z. B. ausverkauft); beides muss zutreffen, damit ein Partner
es bestellen kann. `articleCategory` ist eine optionale Zuordnung zu einer von
der Firma verwalteten Artikelkategorie und die **einzige** Quelle des Kategorienamens
(derive-product-category-from-catalog, 16.10.2026). Anfragen und Importe dürfen einen
Kategorienamen nennen; er wird über den Katalog aufgelöst. Die alte Textspalte `category`
ist nicht mehr gemappt (bestehende Datenbanken behalten sie ungenutzt, auf der VM leer).
`imageUrl` bleibt für externe Bild-URLs erhalten; bei einem
hochgeladenen oder aus der Galerie gewählten Bild verweist `image` optional auf
einen gespeicherten `ProductImage`-Datensatz.

### ArticleCategory
Eine von der Firma verwaltete Artikelkategorie. `name` wird nach Trim und
Unicode-validierter Längenprüfung gespeichert; `normalized_name` ist
case-insensitive eindeutig. `active=false` verhindert neue Artikelzuordnungen
und Importzeilen, lässt bestehende Artikelzuordnungen aber unverändert. Es
gibt keinen DELETE-Endpunkt. Der Seeder verknüpft Kategorien direkt; die frühere
Start-Migration wurde nach dem VM-Check vom 16.10.2026 entfernt.

### ProductImage
Ein serverseitig gespeichertes Artikelbild. Die binären Daten liegen direkt in
PostgreSQL als `bytea`; `contentType` wird aus dem Dateiinhalt validiert und
nicht aus dem Dateinamen übernommen. Der ursprüngliche UTF-8-Dateiname dient
nur zur Anzeige. Ein Bild kann von mehreren Artikeln aus der Galerie
wiederverwendet werden.

### ProductEnablement
Reine Zuordnungstabelle Partner × Product. Die Existenz einer Zeile bedeutet
„freigeschaltet"; Deaktivieren löscht die Zeile ersatzlos (kein Soft-Delete).

### Kunde
Der Endkunde, für den ein Partner bestellt. Wiederverwendbar über mehrere
Bestellungen hinweg. Pflicht: Nachname + Adresse; alles andere optional.
`ownerPartner` scoped den Datensatz auf den anlegenden Partner — ein Partner
sieht nie die Kunden eines anderen Partners; die Firma sieht Kundendaten nur
indirekt über die Bestelldetails.

### Order (Tabelle `orders`)
Eine Bestellung, automatisch dem erstellenden Partner zugewiesen (nie ein vom
Client mitgeschickter Wert). `orderValue` wird bei Erstellung aus den
`OrderItem`s berechnet und danach nicht mehr neu berechnet — ändert sich
später der Produktpreis, bleibt der historische Bestellwert unverändert.
Status-Workflow `SUBMITTED → REVIEWED → FULFILLED`, nur von der Firma
änderbar.

### OrderItem
Eine Bestellzeile (Artikel + Menge). `unitPriceSnapshot` speichert den
Produktpreis zum Bestellzeitpunkt — schützt historische Bestellungen und
Provisionen vor späteren zentralen Preisänderungen.

### CommissionRecord
Wird einmalig bei Bestellerstellung erzeugt (1:1 mit `Order`). Sowohl die
Berechnungsbasis (`orderValue`) als auch der verwendete Prozentsatz
(`commissionPercentUsed`) werden auf dem Record selbst gespeichert
(Snapshot) — eine spätere Änderung von `Partner.commissionPercent` verändert
keine bereits existierenden Provisionsdatensätze rückwirkend.

### OrderEmail (Tabelle `order_email`)
Outbox für Bestell-E-Mails (B-04, `send-order-emails`): wird in derselben Transaktion wie
die Bestellung bzw. der Wechsel auf `FULFILLED` angelegt und von einem Scheduler einzeln
versendet (mit Wiederholungen). Pro Bestellung, Typ und Empfängerrolle höchstens ein
Eintrag. Die volle Adresse steht nur, solange die Mail offen oder fehlgeschlagen ist; nach
dem Versand bleibt nur die maskierte Adresse (`a***@example.at`), übersprungene Einträge
speichern keine Adresse. Fehlgeschlagene Einträge verlieren die Adresse 90 Tage nach dem Anlegen (Entscheidung 15.10.2026, kein gesetzlicher Wert).

## Geld-Konvention
Alle Geldbeträge (`price`, `unitPriceSnapshot`, `orderValue`,
`commissionPercent`, `commissionPercentUsed`, `commissionValue`) sind
`java.math.BigDecimal` / SQL `numeric` — nie `double`/`float` (siehe
`docs/CONVENTIONS.md`).
