# Technische Dokumentation des PAPO-MVP

Stand: 2026-09-25
Revisionsgrundlage: lokaler Arbeitsstand nach `add-partner-trade-proof`

Dieses Kapitel beschreibt den tatsächlich umgesetzten technischen Stand des PAPO-MVP. Es ist
keine Planung für Phase 2. Für die vollständige Anforderungszuordnung siehe die
[Requirements-Traceability-Matrix](REQUIREMENTS_TRACEABILITY.md).

## 1. Ziel und Abgrenzung

PAPO ist ein B2B-Bestellportal für das partnerbasierte Modell **Unternehmen → Partner →
Endkunde**. Im MVP meldet sich der Partner an, verwaltet seine Endkunden und gibt Bestellungen
für diese Endkunden auf. Das Unternehmen verwaltet Partner, Artikel, Kategorien, Freigaben und
Bestellstatus.

Ein Endkunde hat im MVP keinen eigenen Portal-Login. Ein externer Shop, eine direkte BMD-
Schnittstelle, echtes E-Mail-Senden und eine automatische Provisionsauszahlung sind ausdrücklich
ausgeschlossen. Die vollständige Liste steht im [Phase-2-Backlog](../Specification/phase2_backlog.md).

## 2. Systemarchitektur

| Schicht | Umsetzung | Verantwortlichkeit |
|---|---|---|
| Browser | Statisches HTML, CSS und native Web Components in `frontend/src/` | Navigation, Formulare, Tabellen, lokale UI-Zustände |
| REST-Backend | Java 21 mit Quarkus REST, CDI und Hibernate ORM Panache in `backend/src/main/java/` | Validierung, Geschäftsregeln, Rollenprüfung und Datenzugriff |
| Authentifizierung | Keycloak über OIDC; Rollen `company` und `partner` | Login und Rollen-Claims; Fachregeln bleiben im Backend |
| Datenbank | PostgreSQL; Schema-Erzeugung über Hibernate | Persistenz von Unternehmen, Partnern, Trade-Proofs, Kunden, Artikeln, Bestellungen und Provisionen |
| Betrieb | Docker Compose, Caddy, CI/CD und eine VM | TLS/Routing, Backend-Container und statisches Frontend |

Das Frontend spricht ausschließlich über die vorhandene `apiFetch`-Schicht mit dem REST-
Backend. Der Router schützt Rollen auf UI-Ebene; jede geschützte REST-Methode prüft die Rolle
zusätzlich serverseitig. Partnerbezogene Abfragen lösen den Partner über `CurrentPartner` aus
dem Login auf. Kunden, Bestellungen und freigeschaltete Artikel werden dadurch partnerbezogen
begrenzt. Die Firma ist der Mandant: Firmenbenutzer werden über `CompanyLogin` und
`CurrentCompany` ihrer Firma zugeordnet; Partner, Artikel, Kategorien und Bilder tragen
`company_id` (NOT NULL), alle Firmenabfragen filtern danach (`add-company-tenancy`).

## 3. Fachliche Module und wichtige Pfade

| Bereich | Backend | Frontend |
|---|---|---|
| Partner | `partner/PartnerResource.java`, `PartnerService.java` | `company-partner-list-page.js`, `partner-profile-page.js` |
| Artikel und Bilder | `product/ProductResource.java`, `ProductService.java`, `ProductImageResource.java` | `company-product-list-page.js`, `partner-product-list-page.js` |
| Kategorien | `category/CategoryResource.java`, `CategoryService.java` | `company-category-list-page.js` |
| Freischaltungen | `enablement/EnablementResource.java`, `PartnerProductResource.java` | Company-Freigabe im Partnerbereich; Artikelliste des Partners |
| Import/Export | `importer/*Resource.java`, `ImportService.java`, `export/*` | `company-import-page.js` |
| Endkunden | `customer/KundeResource.java`, `KundeService.java` | `partner-customer-list-page.js` |
| Bestellungen | `order/OrderResource.java`, `CompanyOrderResource.java`, `OrderService.java` | `partner-order-new-page.js`, Bestelllisten |
| Dashboards | `dashboard/*Resource.java`, `DashboardService.java` | Company-/Partner-Dashboardseiten |
| Trade Proof | `partner/PartnerTradeProofResource.java`, `CompanyPartnerTradeProofResource.java` | `partner-profile-page.js`, `company-partner-list-page.js` |

## 4. Kernabläufe

### Partner- und Artikelverwaltung

Company-Endpunkte liegen unter `/api/company/...` und sind mit der Rolle `company` geschützt.
Partner werden mit Kontakt-, Aktiv- und Provisionsdaten verwaltet. Artikel werden mit Name,
Preis, Beschreibung, Verfügbarkeit, Kategorie und optionalem Bild geführt. Kategorien sind eine
eigene Company-Verwaltung und werden in der Artikelverwaltung verwendet.

Partner können genau einen aktuellen Trade-Proof mit Registerkennung hochladen oder ersetzen.
Das Backend prüft Größe und Dateiinhalt für PDF/JPEG/PNG, hält die Datei privat und versieht
den Nachweis mit `PENDING`, `ACCEPTED` oder `REJECTED`. Die Firmenprüfung arbeitet auf einer
Versionskennung; ein veralteter Review-Versuch wird abgelehnt. Der Proof-Status ändert den
Partner-Aktivstatus nicht automatisch.

### Freischaltung

Das Unternehmen verwaltet die Zuordnung unter `/api/company/partners/{partnerId}/products`.
Die Partner-Artikelliste unter `/api/partner/products` liefert nur für den angemeldeten Partner
freigeschaltete Artikel. Die serverseitige Einschränkung ist maßgeblich; die UI-Auswahl ist nur
Bedienhilfe.

### Bestellung für einen Endkunden

Der Partner lädt seine Kunden über `/api/partner/customers` und legt neue Kunden mit den
erforderlichen Daten, insbesondere Nachname und Adresse, an. Im Bestellwizard wählt er einen
Kunden, mehrere freigeschaltete Artikel und Mengen. Das Backend prüft Partnerzugehörigkeit,
Artikel-Freischaltung, Verfügbarkeit und Eingaben erneut.

Eine Bestellung wird dem angemeldeten Partner zugeordnet und speichert Positionen mit einem
Unit-Price-Snapshot. Der Status läuft im MVP von `SUBMITTED` über `REVIEWED` zu `FULFILLED` und
wird nur im Company-Bereich geändert. Bestätigungen sind innerhalb der Anwendung simuliert;
ein SMTP-Versand findet nicht statt.

## 5. Datenmodell und technische Entscheidungen

- `Partner` gehört zum Unternehmen und enthält unter anderem Aktivstatus und
  `commissionPercent`; fachlich ist die Zuordnung zum Portalunternehmen implizit, eine
  Company-Fremdschlüsselbeziehung ist derzeit nicht modelliert. Ein aktueller
  `PartnerTradeProof` ist optional 1:1 zugeordnet.
- `CompanyProfile` enthält neben der Steueridentifikation eine syntaktisch geprüfte
  Registerkennung (`GISA` für Österreich, sonst `OTHER_TRADE_REGISTER`).
- `Kunde` gehört genau zu einem Partner. Kunden- und Bestellabfragen verwenden die Zuordnung
  des authentifizierten Partners, nicht eine frei übergebene Partner-ID.
- `Product` speichert Artikelstammdaten; `ProductEnablement` bildet die Partner-Artikel-
  Freischaltung ab. Kategorien werden über die Kategoriebeziehung referenziert.
- `Order` wird explizit auf die PostgreSQL-Tabelle `orders` gemappt. `OrderItem` speichert die
  bestellte Menge und den Preis-Snapshot, damit spätere Artikelpreisänderungen alte Bestellungen
  nicht verändern.
- Provisionen werden pro Bestellung als `CommissionRecord` aus dem Partner-Prozentsatz und dem
  Bestellwert berechnet. Es gibt keine separate automatische Level-Entität.
- Geldbeträge und Prozentsätze verwenden im Backend `BigDecimal`/PostgreSQL `numeric`. Die
  Frontend-Summen arbeiten mit Ganzzahl-Cents.
- Das Schema folgt Hibernate ORM; eine manuelle Flyway-DDL ist nicht Teil des Projekts.

Weitere Feld- und Beziehungsdetails stehen in [DATA_MODEL.md](DATA_MODEL.md).

## 6. Rollen und Sicherheit

| Rolle | Erlaubter Bereich |
|---|---|
| `company` | Verwaltung des aktuellen Portalunternehmens, Artikel/Kategorien, Partner, Freischaltungen, Import/Export, alle Bestellungen und Statusänderungen |
| `partner` | eigenes Profil, eigene Endkunden, freigeschaltete Artikel, eigene Bestellungen und eigenes Dashboard |
| Endkunde | kein eigener Portalzugang im MVP |

Jede geschützte Resource verwendet `@RolesAllowed("company")` oder
`@RolesAllowed("partner")`. Partner-Services lösen den aktuellen Partner aus dem OIDC-Kontext
auf. Direkte URLs und manipulierte IDs ersetzen diese Prüfung nicht. Inaktive Partner werden
serverseitig vor neuen Schreibvorgängen blockiert. Untrusted HTML wird vor der Einfügung im
Frontend mit `escapeHtml` behandelt; Validierungs- und Fehlermeldungen bleiben deutsch und
enthalten keine Stacktraces oder Secrets.

## 7. Import und Datenherkunft

Das führende Stammdatensystem bleibt BMD. PAPO erhält im aktuellen Stand Artikel- und
Partnerdaten über Excel/CSV-Importe mit Vorschau, Spaltenmapping, Validierung, Duplikatprüfung
und Ergebnisbericht. Importdateien bleiben auf 2 MiB begrenzt und werden vor vollständigem
Einlesen geprüft. Die fachliche Zielentscheidung ist eine eindeutige sichtbare BMD-Artikelnummer;
das aktuelle `Product`-Modell und der Import enthalten dieses Feld noch nicht und verwenden
weiterhin den Artikelnamen für die Dublettenprüfung. Die Umsetzung wartet auf anonymisierte
BMD-Regeln und Beispieldaten. Eine direkte BMD-API ist nicht umgesetzt.

## 8. Tests und Nachweise

Die Backend-Tests liegen unter `backend/src/test/java/` und verwenden JUnit 5, Quarkus Test und
REST Assured, wo ein HTTP-Verhalten geprüft wird. Die fachlichen Testklassen sind nach den
Modulen getrennt, etwa `PartnerResourceTest`, `ProductResourceTest`, `EnablementResourceTest`,
`KundeResourceTest`, `OrderResourceTest`, `DashboardResourceTest`, die Importtests und die
Kategorietests.

Der am 16.09.2026 archivierte Trade-Proof-Change dokumentiert 125/125 erfolgreiche
Backend-Tests sowie Package- und Clean-Package-Prüfung, JavaScript-/Browserprüfungen und
Reviews. Die Präsentation wurde zusätzlich mit Playwright in Desktop- und Mobilgröße geprüft.
Das sind Nachweise dieses damaligen Stands, keine erneute Prüfung am 25.09.2026.

## 9. Betrieb und offene Betriebsgrenzen

CI/CD baut das Backend-Image unveränderlich pro Commit-SHA und stellt das statische Frontend
versioniert bereit. PostgreSQL, Keycloak und Caddy werden bei einer normalen Anwendungsversion
nicht neu erzeugt. Die aktuelle lokale Gateway- und VM-Dokumentation ist im Change
[`harden-deployment-gateway`](../openspec/changes/harden-deployment-gateway/) geführt.

Die externe SSH-Negativmatrix für leere/beliebige Befehle, PTY, Weiterleitungen, SCP/SFTP und
ungültige Archive bleibt dort offen, weil der autorisierte private Deploy-Key nicht lokal
vorliegt. Das ist eine Betriebsnachweis-Lücke, keine behauptete Produktfunktion.

## 10. Bewusst nicht im MVP

| Backlog | Abgrenzung |
|---|---|
| B-01 | Öffentlicher Endkundenshop: geschnitten |
| B-02 | Partnerindividuelles Shop-Design: geschnitten |
| B-03 | Automatischer Provisions-Levelaufstieg: Stub/manuell |
| B-04 | Echtes E-Mail-Senden: Stub/intern simuliert |
| B-05 | Automatische Shopanfragen-Weiterleitung: geschnitten |
| B-06 | Direkte BMD-Schnittstelle: dauerhaft geschnitten; Excel/CSV bleibt |
| B-07 | Provisionsabrechnung und Auszahlung: geschnitten |
| B-08 | Vollständige Vorperioden-Trends im Dashboard: Stub/ausstehender Ausbau |

Die Abgrenzung folgt dem [Phase-2-Backlog](../Specification/phase2_backlog.md); geschnittene
oder simulierte Punkte werden in der Matrix nicht als vollständig umgesetzt dargestellt.

## Quellen

- [Funktionale Spezifikation](../Specification/functional_specification_document_partnerportal_final.md)
- [Datenmodell](DATA_MODEL.md)
- [Konventionen](CONVENTIONS.md)
- [Deployment-Dokumentation](DEPLOYMENT.md)
- [Setup](SETUP.md)
- [Requirements-Traceability-Matrix](REQUIREMENTS_TRACEABILITY.md)
