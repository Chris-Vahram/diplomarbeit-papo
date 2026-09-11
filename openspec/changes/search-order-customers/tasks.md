# Tasks

Reviewpräzisierung zu 1.2 (11.09.2026): gemeinsames Kunden-Weitergate ausdrücklich in `goToStep` und Button-/Checkpointzustände einbauen. `maxStepReached` allein ist kein Validitätsnachweis. Nach Such-/Auswahländerung oder Änderung/Leeren eines Neuentwurf-Pflichtfeldes darf auch die bereits besuchte Übersicht nicht aufgerufen werden; erst erneute gültige Bestätigung entsperrt sie. Diese beiden direkten Checkpointfälle gehören zwingend zum Browsercheck.

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Auswahl (erforderlich)

- [ ] 1.1 **Ziel/Komponenten:** Ersten Schritt in partner-order-new-page.js mit Suchfeld, Select und Neuanlagebutton ersetzen; Company-Picker erhalten. **Voraussetzung:** Reviewte Spec; Artikeltexte aus bestehendem Diff erhalten. **Akzeptanz:** Suche über Namen/Adresse, alle Treffer, keine automatische Auswahl; leere/fehlende Treffer klar. **Daten/Security:** Nur eigene geladene Kunden; Ausgabe escaped, keine zusätzlichen Requests. **Tests:** Browser: mehr als 20 Kunden, gleiche Namen/andere Adresse, Umlaute, leere Suche, kein Treffer, Tastatur. **DoD:** Such- und Auswahlfälle 1440/390 Pixel bestanden. **Stop:** Backend-/globale Suche oder gemeinsamer Company-Umbau erforderlich.
- [ ] 1.2 **Ziel/Komponenten:** Auswahl-/Draft-/Checkpointzustand und Neuanlagebutton im bestehenden Wizard verbinden. **Voraussetzung:** 1.1. **Akzeptanz:** Kein Weiter mit unbestätigtem Wechsel, Draft ausdrücklich fortsetzbar, Warenkorb erhalten, Neustart leer. **Daten/Security:** Kein Kunden-POST vor Order-POST; Partnerzuordnung unverändert. **Tests:** Browser: bestehend→neu→bestehend, Draftfortsetzung, Filter verbirgt Auswahl, besuchte Checkpoints, Abbruch; Netzwerkaufrufe prüfen. **DoD:** Kein vorzeitiger Datensatz und kein falscher Kunde in abschließendem Payload. **Stop:** Verlust von Draft/Warenkorb, ungültiger Vorwärtssprung oder neue Persistenzentscheidung.

## 2. Regression (erforderlich)

- [ ] 2.1 **Ziel/Komponenten:** Partner- und Company-Bestellungen sowie Übersichtskunden-Wiederverwendung prüfen. **Voraussetzung:** 1.1–1.2; Übersichtsanlage nur mitprüfen, falls bereits umgesetzt. **Akzeptanz:** Mehrere Artikel, bestehender/neuer Kunde, Erfolg/Fehler unverändert. **Daten/Security:** Synthetische lokale Kunden; keine neue Auth-/Geldlogik. **Tests:** Browser 1440/390; cd backend && ./mvnw package; openspec validate search-order-customers --strict; git diff --check. **DoD:** Exakte Tests und partnerportal-quality plus unabhängiger Funktions-/Securityreview ohne Blocker. **Stop:** Pflichtcheck scheitert oder Shared-Flowregression.
