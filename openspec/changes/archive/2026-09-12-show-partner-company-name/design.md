## Context

Router und Dashboard zeigen feste Texte. Das eigene Profil liefert bereits `name`; es ist auch durch den Partner bearbeitbar. Siehe Proposal und Originalprotokoll 10.09.

## Goals / Non-Goals

Nur sichtbare Firmenzugehörigkeit im Partnerbereich. Keine neue Identitätsquelle oder Änderung des Authvertrags.

## Decisions

- Router lädt nach der Rollenprüfung das eigene Profil für die linke Beschriftung; Dashboard lädt sein Profil selbst. Zwei kleine lesende Aufrufe sind dem neuen globalen Store vorzuziehen.
- Die Hauptüberschrift lautet „Dashboard der Firma <name>“, die linke Beschriftung „Partnerportal der Firma <name>“. Ausschließlich `textContent`; kein HTML aus Profilwerten. Profilfeld bleibt technisch `name`.
- Bei jedem Render zunächst neutral „Partnerportal“ bzw. „Dashboard“. Fehler: neutrale Überschrift und sichtbare deutsche Fehlermeldung mit lokalem GET-only „Erneut laden“. Kein alter oder fest codierter Firmenname als Ersatz. Kein Profilinhalt im Browserstorage.
- Bestehende Router-`renderGeneration` für verspätete Antworten nutzen; Logouthandler neutralisiert Beschriftung synchron. Zusätzlich vor Request gültige Session über bestehenden Helper sicherstellen und deren Access-Token nur im lokalen Closure vergleichen. Antwort nur bei gleicher Rendergeneration, weiterhin Partnerrolle und gleichem Token anwenden. Tokenwechsel einschließlich Refresh verwirft konservativ die Antwort; sichtbarer GET-only Retry lädt unter aktueller Session. Tokens niemals ausgeben oder zusätzlich speichern. Dashboard prüft außerdem `isConnected`. Keine Erweiterung von `auth.js`.
- Profiländerungen werden spätestens beim nächsten Navigations-/Dashboardaufruf sichtbar. Firmenname ist ein bearbeitbarer Anzeigename, keine verifizierte rechtliche Firmenidentität.
- Lange Namen umbrechen; vorhandene Gestaltung erhalten. Browser-Tabtitel unverändert.

## Risks / Trade-offs

Konservativer Tokenvergleich kann bei parallelem Refresh einen erneuten Profilabruf verlangen. Das ist ausdrücklich zulässig; keine stale Identität anzeigen. Ein Requestfehler darf weder Dashboarddaten löschen noch die übrige Navigation blockieren.

## Migration Plan

Keine Migration. Rückbau betrifft nur Beschriftungen/GET-Aufrufe. Umsetzung benötigt keine Infrastrukturaktion.
