## Why

Die Originalprotokolle vom 25.08. und 10.09.2026 verlangen auswählbare, filterbare und erweiterbare Artikelkategorien. Chris bestätigt zentrale Company-Verwaltung, Deaktivierung unter Erhalt bestehender Zuordnungen und Ablehnung unbekannter Importkategorien. Das heutige Freitextfeld bietet keine solche Verwaltung.

## What Changes

- Globale Kategorieentität mit stabiler ID, Name und Aktivstatus; Company kann anlegen, umbenennen und deaktivieren.
- Artikel wählen optional eine Kategorie; vorhandene Zuordnungen bleiben bei Deaktivierung erhalten.
- Additive, idempotente Übernahme vorhandener Freitexte ohne stilles Zusammenführen mehrdeutiger Namen.
- Import verwendet nur bekannte aktive Kategorien; unbekannte/inaktive Werte werden als Zeilenfehler gemeldet.
- Partner erhalten keine globale Kategorienverwaltung oder fremde Artikelzahlen.

## Capabilities

### New Capabilities

- `article-categories`: Kategorienverwaltung und sichere Bestandsübernahme.

### Modified Capabilities

- `product-management`: kontrollierte Kategoriezuordnung bei Artikelerstellung und Bearbeitung.
- `data-import`: validierte Kategorieauflösung beim Artikelimport.

## Impact

Neues Backend-Capability-Paket `category`, Product-Mapping/DTO/Service, Artikelimportdefinition, Company-Artikelseite und neue Kategorienseite samt Router. `GET/POST /api/company/categories`, `PUT /api/company/categories/{id}`; zusätzliche `categoryId` an Artikeln. Hibernate erzeugt additive Tabellen/Spalten; keine Flyway-/manuelle DDL-Umstellung. **BREAKING:** bisher beliebige Kategorie-Schreibwerte sind künftig auf den verwalteten Katalog beschränkt; alte String-Payloads bleiben für bekannte Namen lesbar/auflösbar.

## Non-goals

BMD-Artikelnummer oder Dedupeänderung, BMD-Schnittstelle, Kategorienlöschung, Mandanten, Fachbereiche, Produktgruppen, Pakete und Provisionsregeln.
