## Why

Wer `paportal.at` aufruft, landet heute direkt auf dem Login-Formular des Portals (`router.js` fällt ohne Hash auf `#/login` zurück). Erstbesucher – vor allem Unternehmen, die über den Einsatz von PAPO entscheiden, aber auch neu eingeladene Partner – erfahren nirgends, was PAPO ist und was es leistet. Seit PAPO mandantenfähig ist und öffentliche Partner-Shops hat, braucht das Produkt einen öffentlichen Einstieg, der es vorstellt und dann zum bestehenden Login führt.

## What Changes

- Neue statische Landing-Page `frontend/landing.html` unter `/`: stellt PAPO vor, primär für Unternehmen (Problem → Lösung, Kette Unternehmen → Partner → Endkunde, Funktionen für Unternehmen), danach für Partner (Bestellablauf, Provision) und „Mein Webshop“. Echte Screenshots aus den Demo-Daten, kurzer Hinweis für Endkunden („Shop-Link erhalten Sie von Ihrem Partner“), Footer mit Hinweis auf die HTL-Diplomarbeit, Kontakt, Impressum und Datenschutz. Einzige Aktion: „Anmelden“. Keine Registrierung, keine Demo-Zugangsdaten.
- Die Landing ist ohne JavaScript vollständig lesbar und indexierbar; einziges Skript: Wer im selben Tab eine Portal-Sitzung hat, wird direkt ins Portal weitergeleitet.
- **BREAKING** (URL): Das Portal (`index.html`) zieht von `/` nach `/portal/` (`/portal` leitet auf `/portal/` um). Alte `/#/…`-Links werden nicht weitergeleitet (es gibt noch keine Lesezeichen); Portal-Code selbst bleibt hash-basiert und pfadunabhängig.
- Neue statische Seiten `/impressum` und `/datenschutz` mit Texten für Landing und Portal; fehlende Fakten (Medieninhaber, Verantwortlicher, Anschrift, Kontakt-E-Mail, Hosting-Anbieter, Log-Speicherdauer) als deutlich markierte Platzhalter, die vor dem Live-Gang ausgefüllt sein müssen.
- Neue statische Not-Found-Seite (HTTP 404) für unbekannte Pfade außerhalb von Portal und Shop, mit Link zur Landing.
- Login-Seite: Demo-Login-Hinweis entfällt; neu ein ausblendbarer Hinweis, `paportal.at/portal` als Lesezeichen zu speichern, und ein Link „Was ist PAPO?“ zur Landing.
- Expliziter Logout führt zur Landing (`/`); abgelaufene Sitzungen (401) führen weiterhin zum Login (`/portal/#/login`).
- Caddy und `tools/dev_frontend_server.py` erhalten dieselbe Routing-Tabelle (`/`, `/portal/`, `/impressum`, `/datenschutz`, Shop unverändert, sonst 404); Portal, Shop und 404 bleiben `noindex`, Landing und Rechtsseiten sind indexierbar (`robots.txt`).
- Neutrale Markenfarbe für öffentliche Seiten in `DESIGN.md`; einheitlicher Titel „PAPO“ statt „Partnerportal“; Wortmarke PAPO (kein Logo-Asset). Gestaltung mit den Skills impeccable und apple-design; nur Hellmodus wie das Portal.
- Doku-Sync: `docs/SETUP.md`, `docs/DEPLOYMENT.md`, `PRODUCT.md`, `DESIGN.md`, `docs/REQUIREMENTS_TRACEABILITY.md`.

Nicht enthalten: Registrierung oder „Zugang anfragen“, Deaktivierung der Demo-Konten (bleibt auf der Checkliste „Vor echten Daten“), mandantenspezifische Landing- oder Login-Seiten, Unterseiten, Dark Mode, Änderungen an den Shops.

## Capabilities

### New Capabilities
- `public-landing-page`: öffentlicher Einstieg unter `/` (Inhalt, Zielgruppen, Screenshots, Sitzungs-Weiterleitung, Footer), Impressum, Datenschutz, Not-Found-Seite und Indexierungsregeln.

### Modified Capabilities
- `frontend-shell`: Portal unter `/portal/`, Logout-Ziel Landing, Login-Seite ohne Demo-Hinweis, mit ausblendbarem Lesezeichen-Hinweis und Link zur Landing, Seitentitel „PAPO“.
- `deployment-pipeline`: Single-Origin-Routing liefert `/` als Landing, das Portal unter `/portal/`, Rechtsseiten und eine 404-Seite statt SPA-Fallback für alle Pfade.

## Impact

- Frontend: neu `frontend/landing.html`, `frontend/impressum.html`, `frontend/datenschutz.html`, `frontend/404.html`, `frontend/robots.txt`, Stylesheet für öffentliche Seiten, `frontend/assets/landing/*.webp`; geändert `frontend/index.html` (Titel, noindex), `frontend/src/pages/login-page.js`, `frontend/src/lib/router.js` (Logout-Ziel, Titel).
- Infrastruktur: `deploy/caddy/Caddyfile`, `tools/dev_frontend_server.py`; neues Skript `tools/render_landing_screenshots.mjs` (Playwright, analog zu `render_shop_template_thumbnails.mjs`). Keycloak unverändert (`redirectUris` `https://<domain>/*` deckt `/portal/` ab; Login per Direct Grant).
- Backend, Datenbank, API: keine Änderungen. Bestell-Mails enthalten keine Portal-Links.
- Betrieb: Vor dem Live-Gang müssen die Platzhalter in Impressum, Datenschutz und Kontakt ausgefüllt sein; ein kurzer Blick von Betreuer oder Schule auf die Rechtstexte ist empfohlen.
