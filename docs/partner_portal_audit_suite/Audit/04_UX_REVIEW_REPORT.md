# 04 — UX-, Desktop-, Mobile-, Accessibility- & Print-Review

- **Audit-Datum:** 2026-07-17
- **Geprüfter HEAD:** `9a9f5cd` (main; nur `docs/partner_portal_audit_suite/` untracked)
- **Phase:** 4 von 5 (read-only; kein Produktivcode geändert)
- **Methode:** Live-Durchlauf im echten Dev-Stack (Compose-Keycloak/Postgres, `quarkus:dev`, Frontend :3000) mit Playwright in **1920×1080, 1440×900, 390×844**; beide Demo-Rollen; Print via `beforeprint`-Klon + Print-Media-Emulation; Befunde gegen Code (`base.css`, `data-table.js`, `router.js`, Pages) verifiziert.
- **Evidenz:** 29 Screenshots in `Audit/evidence/04-ux/` (Dateinamen unten je Finding referenziert).

---

## 1. UX-Verdikt

**Das Portal ist für den MVP-Anspruch („einfache Benutzerführung, gute Mobile-Version", Meeting 10.07.) überwiegend gut gebaut — aber zwei Defekte gehören vor den nächsten Kundentermin gefixt:** (1) Ein **harter Reload/Deep-Link auf jeder Listen-Seite rendert eine leere Tabelle** (UX-001, reproduzierbar, Root Cause identifiziert — F5 ist genau das, was ein nicht-technikaffiner Partner tut, wenn „etwas komisch aussieht"); (2) im **Warenkorb überlappen Produktname und Stückpreis** auf jeder Viewport-Größe, auch Desktop 1440 (UX-002) — ausgerechnet auf dem Demo-Kernscreen.

Sonst trägt der Kernfluss: Katalog+Suche+Kategoriefilter, Live-Summen mit Provisions-Vorschau („(Vorschau)"-Hinweis), Mengen-Clamping (0→1, 99999→10000), verständliche deutsche Fehlertoasts (Status-Stepper-Fehler ist vorbildlich formuliert), klare Bestellbestätigung mit simulierter E-Mail, sauberer Auftragsbeleg-Druck, Umlaute/ß fehlerfrei auf allen geprüften Screens (inkl. absichtlich langem Testnamen „Größenwahn-Übermüller …"). Kein horizontales Seiten-Scrolling auf 390px. Desktop-Inhalt sauber auf 1280px gedeckelt (1920 geprüft). Modals mit `role="dialog"`, Fokus-Verschiebung und Esc-Close.

Schwächste Bereiche: Tastatur-Zugänglichkeit der klickbaren Zeilen (Details/Beleg per Tastatur unerreichbar), Button-Kontrast der Primärfarben (AA-Fail), Import-Feedback (grüner Erfolgs-Toast bei 0 importierten Zeilen), diverse kleine Konsistenz-/Formatierungsthemen (gemischte €-Formate, „9.8%" mit Punkt).

Klassifikations-Legende: **[B]** blocks task completion · **[C]** creates confusion · **[F]** avoidable friction · **[V]** visual inconsistency · **[A]** accessibility · **[P]** optional polish.

---

## 2. Desktop-Findings (1920×1080 + 1440×900)

### UX-001 [B] — Harter Reload/Deep-Link auf Listen-Seiten rendert leere Tabelle (High, Konfidenz hoch)

- **Viewport:** alle; **Seiten:** jede Seite mit `<data-table>` (Meine Endkunden, Meine Bestellungen, Bestellungen/Firma, Partnerverwaltung, Produkte beide Rollen).
- **Evidenz:** `partner-customers-dormant-1440.png` (leere Seite nach Reload); 3× reproduziert: Direct-Load `#/partner/customers` → 0 `<td>`/0 `<th>` bei 6 `<tr>`; Direct-Load `#/partner/orders` → 0 Zellen bei 15 Zeilen; **SPA-Navigation danach → 18 Zellen, 3 Header, alles korrekt.**
- **Root Cause (code-verifiziert):** `frontend/src/lib/router.js:269-281` lädt `components/index.js` und alle Page-Module als **ungeordnete parallele dynamic imports**; keine Page importiert `data-table.js` selbst. Gewinnt das Page-Modul das Rennen (auf dieser Maschine beim Kalt-Load deterministisch), weist `partner-customer-list-page.js:33-51` `.columns`/`.filters`/`.searchKeys` einem **noch nicht upgegradeten** `<data-table>` zu → klassisches Custom-Element-Property-Shadowing (eigene Instanz-Properties verdecken die späteren Klassen-Setter dauerhaft). Nur `.rows` kommt nach dem async-Fetch **nach** dem Upgrade an — exakt das beobachtete DOM (nackte `<tr>` inkl. `rowClass`, keine Zellen).
- **User-Impact:** F5, Lesezeichen oder geteilter Link → scheinbar „kaputte"/leere App ohne Fehlermeldung. Für die Zielgruppe (Partner refresht bei Irritation) ein Task-Blocker; im Kundentermin ein Live-Demo-Risiko.
- **Fix-Richtung (klein):** Entweder (a) jede Page importiert ihre Komponenten statisch (`import "../components/data-table.js"` — 1 Zeile je Page), oder (b) Router `await import("../components/index.js")` **vor** dem ersten `render()`, oder (c) Upgrade-sicheres Property-Muster im `connectedCallback` von `data-table` (own-property re-adoption). (b) ist der kleinste zentrale Fix.
- **Owner:** Chris. **Wann:** **vor dem nächsten Termin.**

### UX-002 [C→B] — Warenkorb-Zeile: Produktname (0px breit) überlappt Stückpreis (High, Konfidenz hoch)

- **Viewport:** **alle** — auch Desktop 1440 (Standard) und 1920; **Seite:** `/partner/orders/new`.
- **Evidenz:** `order-cart-filled-1440.png`, `m-order-new-390.png`; gemessen: `.cart-line__name` bekommt **width: 0** (`getBoundingClientRect`), Text malt über `.cart-line__unit-price`. Fixbreiten-Summe (Thumb 40 + Preis 50 + Menge 56 + Summe 67 + Entfernen 75 + Gaps) sprengt das 22rem-Cart-Panel; `flex-wrap` existiert nur ≤47.9375em (`base.css:2952`), und auch dort bleibt die Name/Preis-Kollision bestehen.
- **User-Impact:** Unlesbare Warenkorbzeilen im Kernscreen des Bestellflusses; wirkt kaputt, genau dort, wo der Kunde „Spaß beim Bestellen" bestellt hat.
- **Fix-Richtung:** `.cart-line { flex-wrap: wrap; }` generell + `min-width` (z. B. 8rem) auf `.cart-line__name`, oder Preis unter den Namen stapeln (zweizeilige Cart-Line). Rein `base.css`.
- **Owner:** Chris (Fix) / Tobias (Layout-Feinschliff FD.4). **Wann:** **vor dem nächsten Termin.**

### UX-003 [C] — Endkunden-Dropdown wählt stillschweigend den ersten Kunden vor (Medium, Konfidenz hoch)

- **Viewport:** alle; **Seite:** `/partner/orders/new` (und Company-Manuell-Flow, gleiche `kundePickerHtml`).
- **Evidenz:** `order-new-1440.png`; Select hat **keine leere „Bitte wählen…"-Option**, `value` steht sofort auf dem ersten Eintrag (hier zufällig der zuletzt angelegte Endkunde).
- **User-Impact:** Ein Partner, der das Dropdown übersieht, bucht die Bestellung auf den **falschen Endkunden** — still, ohne Validierungsstopp. Risiko-Aktion ohne bewusste Entscheidung.
- **Fix-Richtung:** Erste Option `<option value="" disabled selected>Bitte wählen…</option>` + `required`; Fehlermeldung „Bitte Endkunden wählen." beim Submit.
- **Owner:** Chris (`lib/order-form.js`). **Wann:** vor dem nächsten Termin (billig, hohes Fehlbuchungs-Risiko).

### UX-004 [C] — „Meine Bestellungen" sortiert älteste zuerst; neue Bestellung landet ganz unten (Medium, Konfidenz hoch)

- **Evidenz:** `partner-orders-1440.png` (09.02.2026 oben, die frisch aufgegebene Nr. 22 vom 17.07. als letzte Zeile); Backend `OrderService.java:45` `order by o.id` **aufsteigend**, Partner-Page sortiert clientseitig nicht nach. (Company-Seite sortiert bereits `b.id - a.id`, `company-order-list-page.js:110`.)
- **User-Impact:** „Wo ist meine eben aufgegebene Bestellung?" — der häufigste Kontrollblick läuft ins Leere; Verwirrung statt Bestätigung.
- **Fix-Richtung:** `order by o.id desc` für die Partner-Liste oder Client-Sort wie auf der Company-Seite. Eine Zeile.
- **Owner:** Chris. **Wann:** vor dem nächsten Termin.

### UX-005 [C] — Import: grüner Erfolgs-Toast bei „0 von 3 Zeilen importiert", Root Cause (nicht zugeordnete Pflichtspalte) wird nicht benannt (Medium, Konfidenz hoch)

- **Evidenz:** `company-import-error-1440.png`: Pflichtfeld „Provision (%) *" stand auf „– nicht zuordnen –", „Import bestätigen" war trotzdem klickbar; Ergebnis: **success-grüner** Toast „0 von 3 Zeilen importiert." + Fehlerliste, in der jede Zeile einzeln „Provision fehlt." meldet. Kosmetik daneben: „Name fehlt., Provision fehlt." (Doppelpunktion).
- **User-Impact:** Der Anwender muss aus n identischen Zeilenfehlern selbst auf „Spalte nicht zugeordnet" rückschließen; der grüne Toast signalisiert das Gegenteil des Ergebnisses. Verwirrende Validierung im Feature, das demnächst mit echten Kundendaten (FR-04) ernst wird.
- **Fix-Richtung:** (a) „Import bestätigen" deaktivieren, solange Pflicht-Zuordnungen fehlen (inkl. Hinweistext), (b) Toast-Ton an Ergebnis koppeln (0 importiert → error/warning), (c) Fehlertexte ohne Doppelpunkt joinen.
- **Owner:** Chris. **Wann:** vor Feature-Freeze (spätestens mit den echten Import-Daten).

### UX-006 [F] — Profil-/Import-Formulare ohne max-width: überbreite Felder, Zuordnungszeilen mit ~800px Leerraum (Low, Konfidenz hoch)

- **Evidenz:** `partner-profile-1440.png` (Straße/E-Mail/Telefon volle 1136px; auf 1920 noch breiter), `company-import-preview-1440.png` (Label links, Dropdown rechtsbündig — Auge muss die Zeile queren).
- **Fix-Richtung:** `max-width` (~40rem) auf Profilformular; Import-Mapping als zweispaltiges Grid mit anliegendem Select.
- **Owner:** Tobias (FD.4-Polish), Chris falls schneller. **Wann:** vor Abgabe.

### Weitere Desktop-Notizen (Low/Info)

- **UX-007 [C, Low]:** Abgelaufene Session → stiller Redirect auf Login ohne „Sie wurden abgemeldet."-Hinweis (beim Review-Start live beobachtet: Refresh-400 → Login). Ein Satz im Login-Screen nach Session-Logout würde Irritation nehmen. Owner Chris, vor Abgabe.
- **UX-008 [P/Deployment]:** Demo-Zugangsdaten hart codiert auf der Login-Seite (`login-page.js:16`). Vor dem echten Hetzner-Deployment entfernen/auf dev-Umgebung gaten — sonst stehen gültige Logins öffentlich auf der Anmeldemaske. Owner Chris (bzw. mit Tobias' ci-cd-Umgebungsschaltung koordinieren), **vor Deployment**.
- **Favicon fehlt** (`favicon.ico` 404 in jeder Session, kein `<link rel="icon">` in `index.html`) — Polish, vor Abgabe.
- **Company-Bestellliste:** Sortierung id-desc zeigt mit den Seed-Daten partner-gruppierte Blöcke (Seed vergibt IDs partnerweise). Mit Echtdaten chronologisch — keine Aktion, nur wissen. Alle 20 Zeilen tragen aktuell NEU-Badge + Gelb-Tint gleichzeitig (redundantes Doppelsignal; bedeutungslos, solange alles „neu" ist) — beobachten, kein Fix nötig.
- **„Sehr geehrte/r Fischer GmbH"** in der simulierten E-Mail — bei Firmen-Endkunden unpassend; „Sehr geehrte Damen und Herren" als Fallback ohne Vorname wäre sauberer. Polish.

---

## 3. Mobile-Findings (390×844)

Positiv: **kein horizontales Seiten-Scrolling** auf allen geprüften Screens (`scrollWidth` ≤ Viewport); Off-Canvas-Drawer mit Backdrop funktioniert; Formulare kollabieren einspaltig; Katalog 2-spaltig; Bestell-Modal passt (367×606 in 390×844).

### M-001 [F] — Tabellenspalten enden unsichtbar hinter dem internen Scroll (Medium, Konfidenz hoch)

- **Evidenz:** `m-dashboard-390.png` (Status-Badges „EIN…"/„GEP…" angeschnitten), `m-customers-390.png` („Inaktiv"-Badge + Datum angeschnitten), `m-order-details-390.png` (SUMME-Spalte nur per dünnem Scrollbalken erreichbar).
- **User-Impact:** `.data-table__scroll` funktioniert technisch, aber es gibt **keine Affordanz** (kein Verlaufsschatten/Indikator), dass rechts mehr liegt — Status/Beträge wirken abgeschnitten. Prompt-Checkpunkt „amounts being cut off" trifft für die Zeilensumme im Details-Modal zu.
- **Fix-Richtung:** Scroll-Hint (Fade-Gradient am rechten Rand des `.data-table__scroll`) und/oder mobile Spalten-Priorisierung (Status vor Provision auf schmalen Screens). 
- **Owner:** Tobias (FD.4) mit Chris. **Wann:** vor Abgabe.

### M-002 [F] — Warenkorb liegt unter dem gesamten Katalog; skaliert nicht auf reale Sortimente (Medium, Konfidenz hoch — vorausschauend)

- **Evidenz:** `m-order-new-390.png` — einspaltig: Endkunde → alle Produktkarten → erst dann Cart/Summe/Submit. Mit 3 Demo-Produkten ok; der Referenzkunde hat **~800 Artikel** — freigeschaltete Kataloge von 50+ Artikeln schieben den Submit-Button tausende Pixel nach unten.
- **Fix-Richtung:** Mobile Sticky-Mini-Cart (Zeile „3 Artikel · € 282,00 · [Zum Warenkorb]" am unteren Rand) oder Anker-Button. Erst relevant, wenn echte Kataloggrößen kommen — aber **vor** dem ersten Partner-Rollout.
- **Owner:** Chris + Tobias gemeinsam (Interaktion + Design). **Wann:** vor Abgabe/Rollout, nicht vorm Termin.

### M-003 [P] — Kleinigkeiten

- Top-Endkunden-Balken degenerieren zu ~24px-Stummeln (Grid reserviert fix 9.25rem fürs Label, `base.css:568`) — Chart trägt auf Mobile keine Information mehr. Polish (Tobias).
- Touch-Targets unter 44px: „Entfernen" im Cart (~25px hoch), Modal-Close „×", Mengen-Input 32px hoch. Für „Partner oft nicht technikaffin" (Meeting) lieber großzügiger. Polish → FD.4.

---

## 4. Accessibility-Findings

Positiv: `role="dialog"` + `aria-modal="true"` + Fokus ins Modal + **Esc schließt**; sichtbarer Fokusring (`--shadow-focus-ring`) auf Buttons/Nav; Selects der Tabellenfilter haben `aria-label`; `escapeHtml` durchgängig (kein a11y-Thema, aber verwandt).

### A11Y-001 [A] — Bestell-Details (und damit „Beleg drucken") per Tastatur unerreichbar (Medium, Konfidenz hoch)

- **Evidenz:** klickbare Zeilen (`data-table__row--clickable`) haben **kein `tabindex`, keine Rolle, keinen Enter-Handler** (`data-table.js:148-151`, DOM-verifiziert `tabindex:null`); in Partner-Zeilen existiert kein anderes interaktives Element.
- **Fix-Richtung:** `tabindex="0"` + `role="button"`/`aria-label` auf klickbare `<tr>` + Enter/Space im Click-Handler; alternativ sichtbarer „Details"-Button je Zeile.
- **Owner:** Chris. **Wann:** vor Abgabe.

### A11Y-002 [A] — Primärbutton-Kontrast verfehlt WCAG AA (Medium, Konfidenz hoch)

- Weiß auf `--color-primary-500` #689c6d = **3,2:1**, weiß auf Partner-`--color-accent-600` #5e838d = **4,1:1** — beide unter 4.5:1 bei 14px/600. Betrifft alle `.button-primary`/`.btn-primary`/Login-Button.
- **Fix-Richtung:** Primär-Aktionen auf die 600er/700er-Stufe heben (primary-600 #578462 ≈ 4,0:1, primary-700 #456851 ≈ 6,3:1) — **Design-Token-Entscheidung, nicht CSS-Hack**.
- **Owner:** **Tobias** (Design-System v1.1), Umsetzung Chris. **Wann:** vor Abgabe.

### A11Y-003 [A] — Filter-Inputs ohne zugängliche Namen (Low, Konfidenz hoch)

- Text-Filter nur mit Placeholder, **Datums-Inputs ganz ohne Label/aria-label** (`data-table.js:203,211-213`; DOM-verifiziert `aria:null, labelled:false`). Fix: `aria-label` analog zum Select-Zweig. Owner Chris, vor Abgabe.

### A11Y-004 [A/V] — Bildergalerie/Image-Chooser (Low)

- Galerie-Buttons nur mit `title`, ohne `aria-label`; **kein sichtbarer Selected-State**; aktiver Tab weiß auf primary-500 (Kontrast s. A11Y-002; wirkt zudem „ausgegraut", `company-product-gallery-1440.png`). Owner Chris/Tobias, vor Abgabe.

---

## 5. Print-Findings

**Keine neuen Defekte.** Geprüft an frischer Bestellung (Nr. 22) über den echten Mechanismus (Klick „Beleg drucken" → `#print-root`-Klon → Print-Media): Letterhead (PAPO/Auftragsbeleg/Bestellnummer/Datum) ✓, Endkunde mit Umlauten/ß ✓, Artikeltabelle ✓, **Gesamtwert und Provisionszeile direkt beieinander** ✓ (Provision korrekt print-only), keine interaktiven Elemente im Klon ✓, keine Duplikate ✓ (`print-confirmation.png`). `@page`-Margin, `tr { break-inside: avoid }`, `thead: table-header-group` vorhanden (`base.css:3038-3052`); Mehrseiten-Verhalten wurde bereits per `page.pdf()`-Byte-Prüfung in der Implementierungssession verifiziert — hier nicht erneut erzwungen.

Offen bleibt (bekannt, Task 4.5): **Firefox-Print-Preview-Spotcheck** auf Chris' Maschine. Langer Kundenname im Beleg: mit dem Testnamen „Größenwahn-Übermüller …" umbricht die Kopfzeile sauber (im Details-Beleg geprüft).

---

## 6. Design-System-Inkonsistenzen

| ID | Fund | Beleg | Richtung |
|---|---|---|---|
| DS-001 [V] | Partner-Dashboard-Metric-Cards zeigen **leere Farbkreise** (Default-Badge ohne Glyphe, `metric-card.js:67`), Company-Cards zeigen ↗ — wirkt wie kaputtes Icon; das dekorative Dauer-↗ auf der Company-Seite suggeriert zudem „alles steigt" | `partner-dashboard-1440.png` vs. `company-dashboard-1440.png` | neutrales Icon je Kennzahl (percent/chart/check als Badge statt Watermark) oder Kreis weglassen; Tobias entscheidet |
| DS-002 [V] | Gemischte Zahlenformate: Y-Achse „**1,0 T€** / **€ 515,15** / **0€**" in einer Achse; Provisionsquote „**9.8%**" mit **Punkt** statt Komma (de-AT) | `company-dashboard-1440.png`; `company-dashboard-page.js` (Achsen-Labels, `rate.toFixed(1)`) | eine Formatfunktion (`Intl.NumberFormat("de-AT")`) für alle Achsen/Quoten |
| DS-003 [V] | `.nav-badge` referenziert nicht existentes Token `--color-neutral-0` (Fallback #fff greift), `.badge-new` hartes `#fff` | `base.css:376,1394` | Token `--color-white` verwenden |
| DS-004 [V] | „Verfügbar: Ja/Nein" als Text in der Produktliste, während sonst überall Status-**Badges**; „Beleg drucken" als ungestylter Browser-Button neben gestylten Buttons; Preis-Input zeigt „89,9" statt „89,90" | `company-products-1440.png`, `m-order-details-390.png`, `company-product-modal-1440.png` | Badge-Klasse wiederverwenden; `.button-secondary` auf Print-Button; Preis beim Befüllen auf 2 Nachkommastellen formatieren |
| DS-005 [Info] | Inline-Styles in JS sind ausschließlich datengetrieben (Chart-Höhen, `--pct`) — **kein** Verstoß; `Design/variables.css` lädt Google-Fonts per CDN-`@import`, `base.css` self-hostet korrekt (nur die Design-Quelle ist stale, kein Runtime-Problem) | grep + `variables.css:6` | Tobias: CDN-Import aus der Design-Datei streichen, sonst kopiert ihn jemand zurück |

Token-Konformität insgesamt: gut. Farben/Spacing in `base.css` laufen konsequent über die v1.1-Tokens; Rollen-Kontexte (grün/teal) greifen inkl. `accent-color` für native Controls.

---

## 7. Wertvollste UX-Verbesserungen (Reihenfolge = Empfehlung)

1. **UX-001 Reload-Bug fixen** — einziger echter Task-Blocker; zentraler 1-Zeilen-Fix im Router möglich; nimmt zugleich das größte Live-Demo-Risiko vom Termin.
2. **UX-002 Cart-Line-Layout** — Kernscreen des vom Kunden gelobten Bestellflusses; reine CSS-Änderung.
3. **UX-003 „Bitte wählen…" im Endkunden-Dropdown** — verhindert stille Fehlbuchungen auf falsche Endkunden; Minutenaufwand.
4. **UX-004 Partner-Bestellliste neueste zuerst** — eine Zeile, direkte Verwirrungsquelle nach jedem Bestellabschluss.
5. **UX-005 Import-Feedback** (Gate + Toast-Ton) — wird in dem Moment kritisch, in dem die echten Kundendateien eintreffen.
6. **A11Y-001 Zeilen tastaturbedienbar** + **A11Y-002 Button-Kontrast** — die zwei substanziellen A11y-Lücken; Kontrast gehört als Token-Entscheidung zu Tobias.
7. **M-001/M-002 Mobile-Tabellen-Affordanz + Sticky-Cart** — zahlt direkt auf die Kundenpriorität „gute mobile Version" ein; M-002 vor dem ersten echten Partner-Rollout.

Bewusst **nicht** empfohlen: Umbau der Katalog-/Cart-Architektur, Backend-Pagination, Virtualisierung — bei ~10–15 Bestellungen/Tag und MVP-Scope wäre das Overengineering (deckt sich mit Scope-Review §4).

---

## 8. Evidenz & Limitationen

- **Durchlauf:** beide Rollen (`company-demo`/`partner-demo`, Credentials nur lokal, nicht im Report); Partner: Login → Dashboard (+Zeitraum-Picker inkl. B-08-Trendlabels) → Endkunden (inkl. eigens per API angelegtem, nie-bestellendem Endkunden mit langem Umlaut-Namen → Dormant-Zeile/Badge/Filter live geprüft) → Katalog/Cart (Mengen-Grenzen 0/99999, Provisions-Vorschau) → Bestellung Nr. 22 real aufgegeben → Bestätigung → Beleg-Druckpfad → Bestellliste/Filter (Umlaut-Suche „müller" ✓) → Details-Modal → Produkte → Profil. Firma: Dashboard → Bestellungen (Statuswechsel vorwärts ✓, Rückwärts-Fehlertoast ✓) → Produkte inkl. Galerie-Modal → Partnerverwaltung inkl. Provision „Individuell" → Import mit realer CSV (Umlaute, Duplikat, fehlende Pflichtwerte).
- **Viewports:** 1440×900 als Primärlage vollständig; 390×844 für Dashboard, Katalog/Cart, Bestellliste, Details-Modal, Endkunden, Drawer; 1920×1080 als Stretch-Spotcheck (max-width verifiziert). Desktop bekam wie gefordert das größere Gewicht.
- **Playwright-Gotchas beachtet:** `about:blank`-Bounce vor Same-URL-Reloads (genau dieser Bounce hat UX-001 aufgedeckt — der Bug ist **kein** bfcache-Artefakt: DOM-Zustand und Import-Graph belegen die Race).
- **Limitationen:** Kein echter `window.print()`-Dialog/PDF-Bytes in dieser Session (Print via Media-Emulation + `#print-root`-Klon; Mehrseiten-PDF war in der Implementierungssession bereits byte-geprüft, Firefox-Check offen = Task 4.5). Session-Refresh nach 15-min-Token-Ablauf nicht abgewartet (nur der beobachtete Abgelaufen-Redirect beim Start). Screenreader nicht real getestet (nur ARIA-/Fokus-/Kontrast-Prüfung per DOM/Rechnung). Kataloggrößen >8 Produkte nicht simuliert (M-002 ist Extrapolation auf die reale ~800-Artikel-Welt). Enablement-Pflege-Screen (Partner×Produkte-Matrix) nicht tief geprüft (nicht in der Pflichtliste). Kein Produktivcode geändert; angelegte Testdaten (Endkunde „Größenwahn-Übermüller…", Bestellung Nr. 22, 1 Statuswechsel) verbleiben in der Wegwerf-Dev-DB (drop-and-create beim nächsten Start).
