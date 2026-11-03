## Context

`paportal.at/` liefert heute `index.html` aus (Caddy: `try_files {path} /index.html`), und `router.js` fällt ohne Hash auf `#/login` zurück. Das Portal ist eine hash-basierte SPA: Alle internen Navigationen, der 401-Redirect (`api.js`) und der Logout (`router.js`) arbeiten nur mit `location.hash`, Assets werden absolut (`/src/…`, `/vendor/…`) geladen. Der Shop hat mit `shop.html` bereits einen eigenen Einstieg, den Caddy und `tools/dev_frontend_server.py` für `/shop/*` ausliefern.

Rahmen aus der Explore-Session (03.11.2026, Tobias):
- Zielgruppe Unternehmen zuerst, dann Partner; Endkunden nur ein kurzer Hinweis.
- Einzige Aktion „Anmelden“; keine Registrierung (Portal-Realm hat `registrationAllowed: false`, Firmen werden manuell angelegt).
- Eigene statische Seite statt SPA-Route; Portal unter `/portal/`.
- Sitzung im selben Tab → direkt ins Portal (R2) plus ausblendbarer Lesezeichen-Hinweis (R4); kein `localStorage`-Wiederkehrer-Merker.
- Expliziter Logout → Landing; 401 → Login.
- Echte Screenshots, Webshop als Funktion, Diplomarbeitshinweis im Footer, Impressum/Datenschutz mit Platzhaltern, indexierbar, neutrale Markenfarbe, Gestaltung mit impeccable + apple-design, nur Hellmodus.
- Landing und Login sind für alle Mandanten gleich. Demo-Hinweis verschwindet von der Login-Seite; die Demo-Konten selbst bleiben (Checkliste „Vor echten Daten“).

## Goals / Non-Goals

**Goals:**
- Öffentlicher, ohne JavaScript lesbarer Einstieg unter `/`, der PAPO in wenigen Minuten verständlich macht.
- Drei klar getrennte Einstiege mit demselben Muster: `/` öffentlich, `/portal/` intern, `/shop/…` Shops.
- Tägliche Nutzer verlieren höchstens einen Klick (ohne Lesezeichen) oder keinen (mit Lesezeichen oder offener Sitzung).
- Lokales Routing verhält sich wie Produktion.

**Non-Goals:**
- Registrierung, Kontaktformular, Mandanten-Branding, Unterseiten, Dark Mode, Mehrsprachigkeit.
- Änderungen am Backend, an Keycloak, an Shops oder an den Demo-Konten.
- Rechtsberatung: Die Rechtstexte sind eine sorgfältige Vorlage mit Platzhaltern, keine geprüfte Rechtsauskunft.

## Decisions

### 1. Statische Seiten statt SPA-Route
`landing.html`, `impressum.html`, `datenschutz.html`, `404.html` sind eigenständige HTML-Dateien ohne Portal-Code. Sie sind ohne JS lesbar, schnell, indexierbar, und das Muster entspricht `shop.html`.
*Alternative:* `#/`-Route im Router. Abgelehnt, weil Inhalte dann nur per JS erscheinen und das komplette Portal-JS geladen würde.

### 2. Portal unter `/portal/`, kein Rewrite der App
Caddy liefert `/portal/` und `/portal/*` als `index.html` aus; `/portal` wird per 308 auf `/portal/` umgeleitet. Weil der Portal-Code nur Hashes und absolute Asset-Pfade nutzt, braucht er keine Pfadanpassung. Alte `/#/…`-Links werden bewusst nicht weitergeleitet (keine Lesezeichen im Umlauf).
*Alternative:* App bleibt auf `/`, Landing nur ohne Hash. Abgelehnt: Der Server sieht den Hash nie, also müsste die Landing per Skript zurückleiten, und die App hätte keinen eigenen Pfad.

### 3. Routing-Tabelle (Caddy, Reihenfolge)
```
/auth/*                    → keycloak (unverändert)
/api/*                     → backend  (unverändert)
@shop /shop /shop/* /shop.html → shop.html, noindex (unverändert)
/portal                    → 308 /portal/
/portal/*                  → index.html (X-Robots-Tag noindex)
/                          → landing.html
/impressum, /datenschutz   → impressum.html, datenschutz.html
sonst                      → vorhandene Datei (try_files {path}) oder Fehler 404
handle_errors 404          → 404.html mit Status 404, noindex
```
Der bisherige SPA-Fallback „alles → index.html“ entfällt. Direkte Dateinamen (`/landing.html`, `/index.html`) bleiben als Dateien erreichbar; das ist harmlos.
`tools/dev_frontend_server.py` bekommt dieselbe Zuordnung als eine kleine Tabelle plus eigene 404-Antwort (Status 404, Body `404.html`).

### 4. Sitzungs-Weiterleitung (R2) als Inline-Skript im `<head>`
```html
<script>try{if(sessionStorage.getItem("papo.auth"))location.replace("/portal/")}catch(e){}</script>
```
Es steht vor allen Stylesheets, damit die Landing nicht kurz aufblitzt. Gültigkeit und Refresh prüft das Portal selbst (`ensureValidSession`); bei abgelaufener Sitzung landet man dort im Login. `sessionStorage` gilt pro Origin und Tab, deshalb ist die Prüfung von `/` aus möglich. `replace` statt `href`, damit „Zurück“ nicht in einer Schleife endet.

### 5. Logout vs. abgelaufene Sitzung
- `router.js`, Profilmenü „Abmelden“: `logout()` und dann `location.assign("/")` statt `location.hash = "#/login"`.
- `api.js` (`clearSessionAndRedirect`) und der Router-Guard bleiben bei `#/login`; der Hash gilt relativ zum Dokument `/portal/` und ergibt `/portal/#/login`.

### 6. Login-Seite
- Demo-Hinweis entfernen.
- Link „Was ist PAPO?“ → `/`.
- Lesezeichen-Hinweis mit Schließen-Button; Schlüssel `papo.bookmarkHintDismissed` in `localStorage`. Jeder Zugriff steht in `try/catch`; ohne Speicher erscheint der Hinweis einfach wieder. Der Merker enthält keine personenbezogenen Daten und wird in der Datenschutzerklärung genannt.

### 7. Styles und Marke
- Öffentliche Seiten laden `base.css` für Tokens und `@font-face` und ein neues `frontend/src/styles/public.css` für das Seitenlayout. Die neutrale Markenfarbe wird als eigener Akzent auf der Wurzel der öffentlichen Seiten gesetzt (gleicher Akzent-Mechanismus wie Firma/Partner), dokumentiert in `DESIGN.md` mit Kontrastnachweis.
- Falls `base.css` mit Portal-Layoutregeln stört (z. B. `body`-Styles für die App-Shell), wird ein Body-Klassen-Scope genutzt statt einer Kopie der Tokens.
- Wortmarke „PAPO“ als Text; Titel „PAPO – …“ auf allen Seiten, auch im Portal.
- Gestaltung mit den Skills impeccable (Gestaltung, Copy, Audit) und apple-design (Typografie, Ruhe, Bewegung nur dezent und mit `prefers-reduced-motion`). Lucide-Icons aus `frontend/vendor/` sind erlaubt; kein CDN.

### 8. Echte Screenshots
`tools/render_landing_screenshots.mjs` (Playwright, gleicher Aufbau wie `render_shop_template_thumbnails.mjs`) meldet sich lokal mit den Demo-Konten an, öffnet festgelegte Ansichten (Firmen-Dashboard, Bestellliste, Neue Bestellung, Partner-Dashboard, öffentlicher Demo-Shop) in fester Größe und speichert sie als WebP unter `frontend/assets/landing/`. WebP entsteht im Browser (`canvas.toDataURL("image/webp")`), damit kein zusätzliches Werkzeug nötig ist. Bilder bekommen `width`/`height`, `loading="lazy"` (außer dem ersten) und deutsche Alt-Texte. Vor dem Erzeugen wird geprüft, dass nur Demo-Daten sichtbar sind.

### 9. Rechtstexte
`impressum.html` und `datenschutz.html` sind statisch und enthalten Platzhalter `[[MEDIENINHABER]]`, `[[ANSCHRIFT]]`, `[[KONTAKT_EMAIL]]`, `[[VERANTWORTLICHER]]`, `[[HOSTING_ANBIETER]]`, `[[HOSTING_STANDORT]]`, `[[LOG_SPEICHERDAUER]]`. Dieselbe Kontakt-Platzhalter-E-Mail steht im Footer. `tools/check_public_placeholders.sh` listet verbleibende `[[…]]` in den öffentlichen Seiten auf und endet mit Fehlercode, wenn welche gefunden werden.

### 10. Indexierung
Landing und Rechtsseiten bekommen kein `noindex`, aber `<meta name="description">` und `<link rel="canonical">`. `index.html` bekommt `<meta name="robots" content="noindex">`, zusätzlich setzt Caddy `X-Robots-Tag: noindex` auf `/portal/*` und die 404. `robots.txt`: `Disallow: /portal/`, `Disallow: /api/`, `Disallow: /auth/`; Shops bleiben über ihr bestehendes `noindex` ausgeschlossen. Keine Sitemap in diesem Schritt.

## Risks / Trade-offs

- [Merge auf `main` deployt sofort, Platzhalter wären öffentlich sichtbar] → Platzhalter vor dem Merge ausfüllen oder bewusst entscheiden, mit sichtbaren Platzhaltern live zu gehen (offene Frage unten); `check_public_placeholders.sh` macht den Stand sichtbar.
- [404 statt SPA-Fallback kann vergessene Pfade brechen] → Alle aktuell referenzierten Pfade (Portal-Assets, `/shop*`, `/api`, `/auth`, Vendor, Assets) in der Abnahme per Request prüfen; die Shop-Seiten haben eigene Behandlung.
- [Screenshots veralten bei UI-Änderungen] → Ein Skript erzeugt sie neu; im Task „UI-Änderung“ als Prüfpunkt erwähnt.
- [Rechtstexte unvollständig oder fehlerhaft] → Platzhalter statt Erfindung; Betreuer oder Schule schauen vor echtem Betrieb drüber.
- [Tägliche Nutzer ohne Lesezeichen und ohne Sitzung haben einen Klick mehr] → Lesezeichen-Hinweis auf der Login-Seite; bewusst akzeptiert.
- [`base.css` bringt Portal-Regeln mit] → Scope über Body-Klasse; notfalls Tokens in eigene Datei auslagern (dann Portal mitprüfen).

## Migration Plan

1. Deploy wie gewohnt (Merge auf `main` → CI → `git archive -- frontend` → Symlink `current`); die neue `Caddyfile` wird mitgeliefert und der Caddy-Container neu geladen (`caddy reload`). Ob die Pipeline die Caddyfile bereits neu lädt, wird beim Umsetzen geprüft und sonst als Schritt in `docs/DEPLOYMENT.md` ergänzt.
2. Nach dem Deploy prüfen: `/` 200 (Landing), `/portal/` 200, `/portal` 308, `/impressum` und `/datenschutz` 200, `/gibt-es-nicht` 404, `/shop/<demo>` unverändert, Login und Logout im Browser.
3. Rollback: vorherigen Release-Symlink und vorherige Caddyfile wiederherstellen; Daten sind nicht betroffen.

## Open Questions

- Sollen Impressum und Datenschutz vor dem Merge ausgefüllt werden, oder darf die Seite mit sichtbaren Platzhaltern live gehen?
- Welche Kontakt-E-Mail, welcher Medieninhaber bzw. Verantwortliche und welcher Hosting-Anbieter werden eingetragen? (Bis dahin Platzhalter.)
