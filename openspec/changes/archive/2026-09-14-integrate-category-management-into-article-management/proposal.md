## Why

Artikelkategorien sind Hilfsstammdaten der Artikelverwaltung und verdienen keinen gleichwertigen Hauptnavigationspunkt. Eine gebündelte, klar benannte Artikelverwaltung verkürzt die Navigation und hält die Bedienung einfach.

## What Changes

- Company-Navigation und Seitenüberschrift von „Artikel“ auf „Artikelverwaltung“ umbenennen.
- Separaten Sidebarpunkt „Artikelkategorien“ entfernen.
- In der Artikelverwaltung eine sekundäre Aktion „Kategorien verwalten“ ergänzen, die die bestehende Kategorienseite öffnet.
- Auf der Kategorienseite einen klaren Rückweg zur Artikelverwaltung ergänzen.
- Bestehende Kategorie-Route, CRUD-Logik, API, Rollenregeln und Artikelzuordnung unverändert wiederverwenden.
- Partnernavigation bleibt „Artikel“, weil Partner Artikel konsumieren und nicht verwalten.

## Capabilities

### New Capabilities

- Keine.

### Modified Capabilities

- `frontend-shell`: Company-Navigation bündelt Kategorien unter „Artikelverwaltung“.
- `article-categories`: Kategorienverwaltung bleibt erreichbar, aber als Unterfunktion der Artikelverwaltung.

## Impact

- Betroffen: Company-Router/Navigation, Artikel- und Kategorienseite sowie minimale vorhandene CSS-Regeln.
- Keine Backend-, API-, Datenbank- oder Berechtigungsänderung.
- Keine Tabs, Modals oder neue Komponenten; vorhandene Seiten und Route werden wiederverwendet.
