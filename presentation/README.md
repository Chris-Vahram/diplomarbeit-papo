# MVP-Präsentation

reveal.js-Deck für die Diplomarbeits-Präsentation (Thema: MVP, Use Cases, Tech-Stack).

## Öffnen

`presentation/mvp-deck/index.html` direkt im Browser öffnen — kein Server, kein Build-Schritt nötig.
Funktioniert vollständig offline (reveal.js ist unter `mvp-deck/reveal/` mitgeliefert, keine
externen Schriftarten oder CDN-Links, System-Font-Stack).

## Struktur

- `mvp-deck/index.html` — 15 Folien (Titel, Akteure, USP, Ausgangslage, Kernablauf,
  Funktionsumfang, Technologie, Architektur, Bildschirmaufnahmen, Live-Demo, Out-of-Scope,
  Feedback, Hosting, Ausblick)
- `mvp-deck/theme.css` — eigenes, handgeschriebenes Theme (keine Webfonts)
- `mvp-deck/reveal/` — reveal.js 5.2.1, lokal eingebunden (CSS + JS, kein CDN)
- `mvp-deck/assets/bestellablauf.svg` (+ `.png`, `.excalidraw`) — Architektur-/Ablaufdiagramm,
  erstellt mit dem `excalidraw-diagram`-Skill

## Status

Die Präsentation verwendet die fachliche Terminologie „Artikel“ und wird nach dem
Besprechungsprotokoll 14.9 weiterbearbeitet. Nächster Block: Sprachkorrekturen, digitale
Souveränität, GitHub-Stars, zusätzliche Screenshots, Clockify-Gesamtübersicht und Demo-Drehbuch.
Die aktuelle Quelle umfasst 15 Folien; produktive Deployment-/Live-Nachweise sind inzwischen
über CI/CD und Live-Smoke belegt.
