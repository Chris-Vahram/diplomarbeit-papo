# Readiness-Review — 10.09.2026

## Fortschreibung 11.09.2026

Der unabhängige Security- und Funktionsreview vom 10./11.09. fand einen widersprüchlichen Request-Zähler: `apiFetch` wiederholt HTTP 401 nach Tokenrefresh einmal. Design, Delta-Spec und Task 1.2 zählen nun genau einen UI-Aufruf bei Doppelklick/Enter und erhalten den vorhandenen Auth-Retry ausdrücklich. Transportfehler lösen keinen zusätzlichen automatischen POST aus. Der neue Browserfall prüft beide Ebenen getrennt. Abschließende Artefaktprüfung und Validierung dieser Fortschreibung werden im Planungsreview dokumentiert; historische Ergebnisse unten sind keine neue Laufzeitprüfung.

**Ergebnis: READY für Terra medium.** Nur OpenSpec geplant; Produktimplementierung und Laufzeittests stehen aus.

- Fachlich: Besprechung 25.08 bestätigt unabhängige Endkundenanlage; aktuelle Seite besitzt nur Liste/Filter.
- Scope: bestehende API, Requestform und Partnerauflösung; kein neues Datenmodell, Dedupe, Realm oder Shared-Component-Refactor.
- Security: echter P1-POST plus DB-Eigentümerprüfung, fremde P2-Fixture im P1-GET ausgeschlossen, Rollen-/Inaktiv-/Pflichtfeld-/Escaping-Negativtests geplant.
- Funktion: native Dialogbedienung, Doppelsubmit-Sperre, Filter-/Pagination-Sichtbarkeit, stale-load-Schutz und getrennte Save/GET-Fehlerpfade spezifiziert.
- Bewusste Grenze: `apiFetch` liefert keinen strukturierten Status; fehlgeschlagener Save wird konservativ als unbestätigt behandelt und nie automatisch wiederholt.
- Stopbedingungen: API-/Schema-/Auth-Ausweitung, fehlgeschlagene Pflichtchecks oder neue fachliche Entscheidung.

Unabhängiger Securityreview und Funktionsreview erfolgten in dieser Sitzung; deren letzte zwei Fehlerpfad-Findings wurden in Design, Spec und Tasks übernommen. `openspec validate add-standalone-customer-creation --strict` erfolgreich. Keine Produkt-/Browser-/Backendtests ausgeführt.
