# Tasks

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Anzeige (erforderlich)

- [ ] 1.1 **Ziel/Komponenten:** Router und Partnerdashboard gemäß Design an eigenes Profil anbinden; neutrale Überschriften und GET-only Retry ergänzen. **Voraussetzung:** Reviewte Specs; bestehende Benutzeränderungen erhalten. **Akzeptanz:** Eigener Name erscheint an beiden Stellen; Dashboardkennzahlen bleiben bei Profilfehler nutzbar. **Daten/Security:** Nur bestehende partner-scoped API; textContent, kein Cache/Identitätsnachweis. **Tests:** Browser: Erfolg, Profilfehler/Retry, geänderter Name, Umlaute und HTML-Zeichen. **DoD:** Beide Anzeigen nachweisbar korrekt; keine Auth-/APIänderung. **Stop:** Neue Identitäts-/Companyfelder oder Shared-Auth-Umbau erforderlich.
- [ ] 1.2 **Ziel/Komponenten:** Router-Rendergeneration, synchronen Logoutreset und lokale Session-/Disconnected-Guards ergänzen; seitenspezifischen Textumbruch prüfen. **Voraussetzung:** 1.1. **Akzeptanz:** Alte Antworten nach Navigation, Logout und neuer Session erscheinen niemals; Tokenrotation erlaubt ausdrücklich neutralen GET-Retry. **Daten/Security:** Tokens nur im vorhandenen Sessionhelper/kurzem Closure vergleichen, niemals protokollieren. **Tests:** Browser: langsamer GET bei P1→Logout→P2/Company, erneuter Login gleichen Namens und paralleler Tokenrefresh; 1440/390 Pixel. **DoD:** Alle Race-/Layoutfälle nachvollziehbar geprüft. **Stop:** Fremder/veralteter Name sichtbar oder Authänderung nötig.

## 2. Abschlussprüfung (erforderlich)

- [ ] 2.1 **Ziel/Komponenten:** Gesamtdiff und Portalnavigation prüfen. **Voraussetzung:** 1.1–1.2. **Akzeptanz:** Überschrift korrekt; Rollenrouting und Profilspeichern unverändert. **Daten/Security:** Nur synthetische lokale Profile. **Tests:** Browser 1440/390; cd backend && ./mvnw package; openspec validate show-partner-company-name --strict; git diff --check. **DoD:** Exakte Belege, partnerportal-quality und unabhängiger Funktions-/Securityreview ohne Blocker. **Stop:** Fehlende lokale Prüfung oder offenes Finding; kein Deployment.
