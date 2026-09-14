## Context

Company-Navigation enthält derzeit „Artikel“ und „Artikelkategorien“ nebeneinander. Artikel- und Kategorienseiten sowie `/company/categories` funktionieren bereits. Änderung betrifft nur Informationsarchitektur.

## Goals / Non-Goals

**Goals:**
- Ein eindeutiger Hauptpunkt „Artikelverwaltung“.
- Kategorien mit höchstens einem zusätzlichen Klick erreichbar.
- Bestehende Seiten, API und Rollenregeln wiederverwenden.

**Non-Goals:**
- Kein gemeinsames Großformular, Tabsystem, Modal-CRUD oder neue Komponente.
- Keine Änderungen an Kategoriendaten, Artikelzuordnung oder Partnernavigation.

## Decisions

- **Bestehende Kategorienseite als Unterseite behalten.** Artikelverwaltung erhält „Kategorien verwalten“; Kategorienseite erhält Rücklink. Alternative: CRUD in Artikelseite einbetten; verworfen wegen dichter Oberfläche und doppelter Zustands-/Fehlerlogik.
- **Route behalten, Navigation entfernen.** Direkte Links und Reload bleiben kompatibel; nur Sidebar wird einfacher.
- **Bezeichnungen nach Rolle.** Company sieht „Artikelverwaltung“, Partner weiterhin „Artikel“.
- **Native Navigation.** Bestehende Hash-Routen/Links nutzen; kein neuer Zustand.

## Risks / Trade-offs

- [Kategorien schwerer auffindbar] — sichtbare sekundäre Aktion im Seitenkopf, nicht in Untermenü verstecken.
- [Benutzer verliert Kontext] — Kategorienseite zeigt Rücklink „Zur Artikelverwaltung“.
- [Mobile Aktionen werden eng] — vorhandenes responsives Seitenkopf-/Buttonmuster verwenden und bei 390 px prüfen.
