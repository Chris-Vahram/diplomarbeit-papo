## Why

Originalprotokolle vom 31.08. und 10.09.2026 legen „Artikel“ und „Artikelkategorie“ als sichtbare Begriffe fest. Acht Frontenddateien enthalten bereits uncommittete Umbenennungen; dieser Change übernimmt deren fachlichen Scope und vervollständigt die Prüfung, ohne fremde Arbeit zu ersetzen.

## What Changes

- Benutzerseitige Navigation, Listen, Bestelltexte und Rückmeldungen verwenden konsistent „Artikel“.
- Kategoriebezeichnungen heißen „Artikelkategorie“.
- Bestehende fachlich passende Änderungen bewahren; verbleibende sichtbare Texte einschließlich Backendfehlermeldungen gezielt abgleichen.
- Technische IDs bleiben intern; keine zweite sichtbare Artikel-ID hinzufügen.

## Capabilities

### New Capabilities

- `article-terminology`: konsistente sichtbare Artikelsprache.

### Modified Capabilities

Keine.

## Impact

Bestehender Frontend-Diff sowie betroffene sichtbare Stringliterale in gemeinsamen Frontendhelfern und Backendvalidierung/Fehlern. Keine Umbenennung von Java-/API-/Tabellennamen; keine Verhaltens- oder Importmappingänderung. Bereits vorhandene Produktänderungen sind keine neue Leistung dieser Planung.

## Non-goals

Forschungsfrage umformulieren, globale Dokumentationsübersetzung, BMD-Artikelnummer implementieren, IDs löschen oder bestehende Importaliasnamen verändern.
