# Tasks

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Kundenkontext (erforderlich)

- [ ] 1.1 **Ziel/Komponenten:** Überschriften und Übersichtsbeschreibung in partner-order-new-page.js an bestätigten Kunden binden. **Voraussetzung:** Bestehende selectedKunde-Auflösung bzw. bereits angewandten Suchchange beachten. **Akzeptanz:** Exakte Texte; gültige bestehende/neue Kunden, Wechsel und Neustart korrekt. **Daten/Security:** Nur escaped/Textcontent; Bestellpayload und Geldwerte unverändert. **Tests:** Browser: fehlender Vorname, langer/HTML-Name, bestehender/neuer Kunde, Wechsel, Erfolg/Neustart. **DoD:** Alle Text- und Resetfälle bestanden. **Stop:** Neue Validierungs-/Persistenz-/Zahlungslogik erforderlich.
- [ ] 1.2 **Ziel/Komponenten:** DOM-/Gridreihenfolge Katalog vor Bestellpanel mit bestehender CSS gestalten. **Voraussetzung:** 1.1. **Akzeptanz:** Desktop links Katalog/rechts Bestellung; mobil Katalog vor Bestellung, Summen und Weiter erreichbar. **Daten/Security:** Keine Produkt-/Preislogik ändern; Tastatur- und Lesereihenfolge stimmen. **Tests:** Browser 1440/390 sowie geringe Fensterhöhe, viele Artikel und mehrzeilige Namen. **DoD:** Keine Überlagerung, kein verlorener Warenkorb; zugängliche Aktionen. **Stop:** Erforderlicher visueller Komplettumbau statt begrenzter Layoutkorrektur.

## 2. Regression (erforderlich)

- [ ] 2.1 **Ziel/Komponenten:** Bestellablauf und Diff prüfen. **Voraussetzung:** 1.1–1.2. **Akzeptanz:** Mehrere Artikel/Mengen, Kunde und Gesamtsumme bleiben vor/nach Navigation gleich. **Daten/Security:** Synthetische Daten, keine Zahlung/E-Mail. **Tests:** Browser 1440/390; cd backend && ./mvnw package; openspec validate clarify-order-customer-context --strict; git diff --check. **DoD:** partnerportal-quality und unabhängiger Funktions-/Securityreview sowie tatsächliche Belege. **Stop:** Fehlgeschlagener Check oder Bestellvertrag geändert.
