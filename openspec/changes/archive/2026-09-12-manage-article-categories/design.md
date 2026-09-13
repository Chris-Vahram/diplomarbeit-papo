## Context

`Product.category` ist optionaler Freitext. Import erstellt Artikel und validiert alle Zeilen vor Persistierung in einer Transaktion. Partnerlisten erhalten nur freigeschaltete Artikel. Hibernate erzeugt Schema; dev/test-Seeding allein deckt NORMAL nicht ab.

## Goals / Non-Goals

Eine globale verwaltete Kategorie pro Artikel, weiterhin optional. Keine Kategorieprovision, Tenantbasis, BMD-ID, neue Dedupeidentität oder externe Integration.

## Decisions

### Daten und Namensregeln

- Neue `Category` mit technischer Long-ID, `name` (1–255 Zeichen nach `strip()`), `normalizedName` (`name.toLowerCase(Locale.ROOT)`, DB-unique, nicht nullable) und `active`. Deutsche Bean-Validation; DB-Konflikt als 409 „Artikelkategorie bereits vorhanden.“ abbilden, keine Constraintdetails.
- Product erhält optionalen FK `articleCategory`. Altes Stringfeld `category` bleibt für additive Übernahme erhalten und wird bei späteren Schreibvorgängen/Umbenennung als kompatibler Anzeigename synchronisiert. Es ist keine zweite Autorität. Lesen liefert Namen aus FK, null ohne Zuordnung. Keine stille Live-Fallbacklogik.
- Keine Zusammenführung allein wegen ähnlicher Schreibweise. Bestandswerte mit derselben Normalisierung, aber unterschiedlichen getrimmten Schreibweisen führen zu einem klaren Übernahmekonflikt. Identische getrimmte Werte werden gemeinsam einer Kategorie zugeordnet. Null/Blank bedeutet keine Kategorie.

### Endpunkte und Artikelvertrag

- `GET /api/company/categories` liefert alle Kategorien als `{id,name,active}`, nach Name/ID sortiert; keine Artikelzahlen. `POST` nimmt `{name}`, legt aktiv an und liefert 200. `PUT /{id}` nimmt `{name,active}`, liefert 200; unbekannte ID 404. Alle Methoden explizit `@RolesAllowed("company")`. Kein DELETE und kein Partner-Katalogendpunkt.
- Artikelantwort ergänzt `categoryId`; `category` bleibt der Anzeigename. Schreibpayload darf neue `categoryId` oder alten `category`-String enthalten. Beide nicht-null zusammen: 400, auch bei vermeintlicher Übereinstimmung. Beide null/fehlend/Blank: Zuordnung entfernen (bestehende PUT-Vollersetzung). Neue UI sendet nur ID, nicht Anzeigenamen.
- Unbekannte ID/Name oder neue Zuordnung einer inaktiven Kategorie: 400, keine Artikeländerung. Bei Bearbeiten darf dieselbe inaktive ID unverändert bleiben. Aktivstatus verändert weder Bestellbarkeit bestehender Artikel noch Freischaltungen. Name allein bleibt bei alten Clients nur für bekannte Kategorien kompatibel; keine automatische Anlage.
- Namenänderung behält ID und aktualisiert alle zugehörigen Legacy-Strings in derselben Transaktion. Reaktivieren über PUT ist zulässig.

### Bestandsübernahme und Transaktionen

- Additive Hibernate-Tabellen/FK; kein Entfernen bestehender Spalten und keine sofortige NOT-NULL-Pflicht am Product-FK.
- Ein idempotenter ORM-Startabgleich läuft nach Schema und dev/test-Seed, auch in NORMAL, vor Bereitschaft der Anwendung. Observer-Reihenfolge ausdrücklich festlegen: bestehender Seed zuerst, Kategorieabgleich danach. Beide werden nur im jeweiligen bisherigen Modus ausgeführt; NORMAL führt nur den Abgleich aus.
- Alle betroffenen Artikel innerhalb einer Transaktion sperren. Erst Daten/Normalisierung/Kollisionen vollständig prüfen, dann Kategorien anlegen und fehlende FKs füllen. Vorhandene FKs niemals still ersetzen; widersprüchliche FK-/Legacywerte stoppen mit deutscher aggregierter Fehlermeldung ohne Rohdaten. Bei Fehler ganze Übernahme zurückrollen und Start abbrechen. Wiederholter Start verändert bereits zugeordnete Daten nicht.
- Design gilt für bestehende einzelne Appinstanz. Kein paralleler alter/neuer Anwendungsschreiber während Übernahme oder Rückbau; ein Mehrinstanz-Rollout braucht vorab neuen Plan, kein stiller Clusteranspruch.
- Für alle Kategorie-/Artikelschreibpfade gilt ausschließlich: Kategorie-IDs aufsteigend sperren, dann Artikel-IDs aufsteigend. Artikelupdate liest alte/gewünschte Kategorie vor, sperrt beide, sperrt und aktualisiert dann den Artikel aus der DB; hat sich die ursprüngliche Zuordnung inzwischen verändert, gesamte Transaktion mit 409 „Artikel wurde zwischenzeitlich geändert. Bitte erneut laden.“ abbrechen. Keine weitere Kategorie unter gehaltenem Artikellock nachsperren und kein automatischer Schreibretry. Rename sperrt zuerst die Kategorie und ermittelt/sperrt danach die aktuell zugeordneten Artikel für den Legacy-Sync. Artikelneuanlage sperrt gewünschte Kategorie vor Persist. Dadurch kann während Rename keine neue Zuordnung eintreten.
- Import löst alle nichtleeren Kategorien im bestehenden Commitprozess vor Persistierung auf und sperrt sie nach ID sortiert bis Transaktionsende. Unbekannt/inaktiv als abgelehnte Zeile; andere gültige Zeilen bleiben nach bestehendem Importvertrag importierbar. Keine Kategorieanlage im Import. Nach Preview erfolgte Deaktivierung/Umbenennung wird im Commit sichtbar und abgelehnt. Bestehendes namensbasiertes Artikel-Dedupe unverändert.
- Kein neuer Hintergrundjob oder dauerhafter Migrationservice. Ein Startabgleich für diese additive Übernahme genügt; spätere Entfernung der Legacyspalte ist nicht Teil dieses Changes.

### UI

- Companynavigation erhält „Artikelkategorien“ und eigene Liste/Formular für Name/Aktivstatus. Bestehende Tabellen/Formular-/Dialogmuster verwenden; Submit-Pending verhindern, Fehler erhalten Eingaben.
- Artikelform verwendet Select aktiver Kategorien plus leere Option „Keine Artikelkategorie“. Beim Edit eine aktuell zugewiesene inaktive Kategorie zusätzlich mit Kennzeichnung anzeigen. Artikellisten/Filter zeigen neue Namen.
- Partnerfilter weiterhin ausschließlich aus eigenen freigeschalteten Artikeln ableiten. Keine globale Kategorienliste an Partner laden. Kategorieinformationen immer escaped.
- Bei Erfolg und fehlgeschlagenem Reload gespeicherten Erfolg separat melden und GET-only Retry anbieten; keinen POST wiederholen.

## Risks / Trade-offs

Bestandskollisionen sind eine konkrete Stopbedingung, keine automatische Bereinigung. Lokale synthetische Migrationstests belegen den Algorithmus, nicht den Produktivdatenstand. Es wird keine Produktivübernahme oder Betriebsfreigabe in dieser Planung behauptet.

## Migration Plan

### Verbindliche Reviewergänzungen — 11.09.2026

- Auch manuelle Artikelanlage/-update refreshen jede Kategorie nach Sperrerwerb aus der Datenbank und prüfen Aktivstatus sowie bei Legacy-Stringeingabe den aktuellen normalisierten Namen erneut. Ein vorher geladener ORM-Zustand genügt nicht. Test: Rename/Deaktivierung zwischen Vorauflösung und Sperrerwerb darf keine veraltete Zuordnung legitimieren.
- Alle Kategorie-Fetch-Joins sind wegen optionaler Zuordnung ausdrücklich `LEFT JOIN FETCH`; Artikel ohne Kategorie dürfen in keiner Liste verschwinden.

- `normalizedName` erhält Spaltenlänge 510; `name` bleibt auf 255 begrenzt. `Locale.ROOT` kann beispielsweise `İ` auf zwei Zeichen erweitern. Unicode-Längentest ist Pflicht.
- Beim Entfernen der optionalen Kategorie FK **und** Legacystring atomar auf null setzen. Die Übernahme synchronisiert den getrimmten Anzeigenamen im selben Commit; andernfalls würde ein späterer Start entfernte Zuordnungen wiederherstellen. Bereits zugeordnete konsistente Werte bleiben unverändert.
- Import benötigt vor endgültiger Zeilenvalidierung eine produktbezogene Vorbereitungsphase: referenzierte Kategorie-IDs sammeln, nach ID sortiert sperren und aus der DB refreshen. Danach aktuelle Namen und Aktivstatus erneut prüfen. Erst jetzt Zeilenreport finalisieren und gültige Artikel persistieren. Bei zwischenzeitlichem Rename kein Fallback auf die vorab gefundene ID. Partnerimport bleibt unverändert; keine Kategorie-Sperren im generischen Partnerpfad.
- Product-FK als LAZY abbilden; die Artikel-/Freischaltungslisten laden Kategorie per Fetch-Join ohne N+1. `ProductService.list`, `EnablementService.listForPartner` und `listEnabledProducts` sowie `EnablementDto.from` berücksichtigen. Auch Freischaltungsantworten beziehen den Namen aus dem FK; bestehende Partnerfilter bleiben im Query.
- Zusätzliche Pflichtfälle: Übernahme → Kategorie entfernen → zweiter Start; Rename → zweiter Start; langer Unicode-Name; mehrere Kategorien in umgekehrter Importreihenfolge parallel; Artikelwechsel während Rename; keine Teiländerung bei Fehler. Keiner dieser Tests benötigt reale Daten oder Infrastrukturzugriffe.

Umsetzung: additive Felder, synthetischer Bestandsabgleich, normale sowie wiederholte Starts und Rollback der Transaktion testen. Bei später autorisiertem Betrieb nur einzelne gestoppte Vorgängerversion ablösen; keine Infrastrukturtask hier. Rückbau nur nach Kompatibilitätsprüfung der synchronisierten Legacywerte, keine Datenlöschung und kein Downgrade unter laufenden Schreibern.
