Alle Aufgaben sind **Pflicht**, außer sie sind mit *(optional)* markiert.

## 1. Routing und Einstiege

- [x] 1.1 `deploy/caddy/Caddyfile` nach Routing-Tabelle (design.md, Entscheidung 3) umbauen: `/` → `landing.html`, `/portal` → 308 `/portal/`, `/portal/*` → `index.html` mit `X-Robots-Tag noindex`, `/impressum` und `/datenschutz` → statische Seiten, sonst `try_files {path}` + `handle_errors` 404 → `404.html`; Shop, `/api`, `/auth` unverändert.
- [x] 1.2 `tools/dev_frontend_server.py` auf dieselbe Zuordnung und eine echte 404-Antwort (Status 404, Body `404.html`) bringen; Startmeldung um `/portal/` ergänzen.
- [x] 1.3 Prüfen, ob die Deploy-Pipeline eine geänderte Caddyfile neu lädt; falls nicht, Reload-Schritt in `docs/DEPLOYMENT.md` dokumentieren.
- [x] 1.4 Lokal per `curl -I` prüfen: `/` 200, `/portal` 308, `/portal/` 200, `/impressum` 200, `/datenschutz` 200, `/robots.txt` 200, `/gibt-es-nicht` 404, `/shop/<demo>` unverändert, Portal-Assets (`/src/…`, `/vendor/…`, `/assets/…`) 200.

## 2. Portal-Anpassungen

- [x] 2.1 `frontend/index.html`: Titel „PAPO“, `<meta name="robots" content="noindex">`; `router.js` setzt `document.title` als „PAPO – <Seite>“.
- [x] 2.2 Logout im Profilmenü (`router.js`) führt nach `logout()` zu `/`; 401-Pfad in `api.js` und Router-Guard bleiben bei `#/login` (prüfen, dass das `/portal/#/login` ergibt).
- [x] 2.3 `login-page.js`: Demo-Hinweis entfernen, Link „Was ist PAPO?“ → `/`, ausblendbarer Lesezeichen-Hinweis (`papo.bookmarkHintDismissed`, `localStorage` in `try/catch`).

## 3. Gestaltung und Marke (impeccable + apple-design)

- [x] 3.1 Skills impeccable und apple-design laden; Richtung für öffentliche Seiten festlegen (Unternehmen zuerst, ruhig, sachlich, „Sie“, kein Marketington) gemäß `PRODUCT.md`/`DESIGN.md`.
- [x] 3.2 Neutrale Markenfarbe für öffentliche Seiten festlegen und in `DESIGN.md` mit Kontrastwerten (Text ≥ 4.5:1, UI ≥ 3:1) dokumentieren.
- [x] 3.3 `frontend/src/styles/public.css` anlegen (lädt nach `base.css`, Body-Klassen-Scope), gemeinsamer Kopf- und Fußbereich für Landing, Rechtsseiten und 404.

## 4. Screenshots

- [x] 4.1 `tools/render_landing_screenshots.mjs` (Playwright, Aufbau wie `render_shop_template_thumbnails.mjs`): Demo-Login, festgelegte Ansichten (Firmen-Dashboard, Bestellliste, Neue Bestellung, Partner-Dashboard, öffentlicher Demo-Shop), WebP per Canvas nach `frontend/assets/landing/`.
- [x] 4.2 Screenshots erzeugen, auf reine Demo-Daten prüfen, Aufruf in `docs/SETUP.md` dokumentieren.

## 5. Landing-Page

- [x] 5.1 `frontend/landing.html`: Kopf mit Wortmarke und „Anmelden“ (`/portal/#/login`), Inline-Weiterleitung bei vorhandener Sitzung (design.md, Entscheidung 4) vor allen Stylesheets.
- [x] 5.2 Inhalt: Hero (Unternehmen), Problem → Lösung, Kette Unternehmen → Partner → Endkunde, Abschnitt Unternehmen, Abschnitt Partner, Abschnitt „Mein Webshop“, Hinweis für Endkunden; Anker-Navigation; keine erfundenen Zahlen, Kunden oder Bewertungen; Begriffe Artikel/Endkunde/Bestellung/Provision.
- [x] 5.3 Footer: Hinweis HTL-Diplomarbeit, Kontakt `[[KONTAKT_EMAIL]]`, Links Impressum und Datenschutz.
- [x] 5.4 SEO-Grundlagen: `<meta name="description">`, `<link rel="canonical">`, `frontend/robots.txt` (Disallow `/portal/`, `/api/`, `/auth/`).

## 6. Rechtsseiten und 404

- [x] 6.1 `frontend/impressum.html` (§ 5 ECG, § 25 MedienG) mit Platzhaltern `[[MEDIENINHABER]]`, `[[ANSCHRIFT]]`, `[[KONTAKT_EMAIL]]`.
- [x] 6.2 `frontend/datenschutz.html` (Art. 13 DSGVO): Server-Logs, selbst gehostete Schriften, `sessionStorage` (Login), `localStorage` (Lesezeichen-Hinweis), Keycloak-Anmeldung, Betroffenenrechte, Beschwerde bei der Datenschutzbehörde; Platzhalter `[[VERANTWORTLICHER]]`, `[[HOSTING_ANBIETER]]`, `[[HOSTING_STANDORT]]`, `[[LOG_SPEICHERDAUER]]`.
- [x] 6.3 `frontend/404.html` mit `noindex`, kurzer deutscher Meldung und Link zur Landing.
- [x] 6.4 `tools/check_public_placeholders.sh`: listet verbleibende `[[…]]` in den öffentlichen Seiten, Exit-Code ≠ 0 bei Fund.

## 7. Prüfung

- [x] 7.1 Playwright Desktop (1440 px) und Mobil (360 px): Landing ohne seitliches Scrollen, „Anmelden“ ≥ 44 px, sichtbarer Fokus, Landing ohne JS lesbar, reduzierte Bewegung respektiert.
- [x] 7.2 Abläufe im Browser: `/` ohne Sitzung → Landing; Login → Dashboard; `/` im selben Tab → Portal; Abmelden → Landing; abgelaufene Sitzung → `/portal/#/login`; Lesezeichen-Hinweis ausblenden und nach Reload weiter ausgeblendet.
- [x] 7.3 impeccable-Audit der öffentlichen Seiten (Kontrast, Hierarchie, Copy) und Funde beheben.
- [x] 7.4 `git diff --check`, JavaScript-Syntaxprüfung der geänderten Dateien, `openspec validate add-public-landing-page --strict`.

## 8. Doku-Sync

- [x] 8.1 `docs/SETUP.md` und `docs/DEPLOYMENT.md`: neue URLs (`/`, `/portal/`), lokale Startmeldung, Nachprüfung nach dem Deploy.
- [x] 8.2 `PRODUCT.md` (öffentlicher Einstieg, Login ohne Demo-Hinweis), `DESIGN.md` (öffentliche Seiten, Markenfarbe), `docs/REQUIREMENTS_TRACEABILITY.md`.

## 9. Freigabe

- [ ] 9.1 Platzhalter in Impressum, Datenschutz und Footer ausfüllen (Daten von Tobias/Chris) oder bewusst entscheiden, mit Platzhaltern live zu gehen; `check_public_placeholders.sh` ausführen und Ergebnis festhalten.
- [ ] 9.2 *(optional)* Rechtstexte von Betreuer oder Schule gegenlesen lassen.
- [ ] 9.3 Nach dem Deploy die Prüfliste aus design.md (Migration Plan, Schritt 2) auf der VM abarbeiten.
