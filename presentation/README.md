# MVP-Präsentation

## Abschlussnachtrag — 14.09.2026

Das Deck umfasst jetzt 22 Folien und wurde nach dem Besprechungsprotokoll 14.9 überarbeitet:
verständliche Akteurs- und Ablauftexte, GitHub-Stars, drei sachliche Folien zu digitaler
Souveränität, zwei zusätzliche echte Browseraufnahmen, eine Clockify-Übersicht für Chris und
Tobias sowie das [Demo-Drehbuch](demo-drehbuch.md). Die Clockify-Folie weist klar aus, dass
„ca. 400 h“ ein dokumentierter Zielwert und keine ausgelesene Ist-Summe ist.

reveal.js-Deck für die Diplomarbeits-Präsentation (Thema: MVP, Use Cases, Tech-Stack).

## Öffnen

`presentation/mvp-deck/index.html` direkt im Browser öffnen — kein Server, kein Build-Schritt nötig.
Funktioniert vollständig offline (reveal.js ist unter `mvp-deck/reveal/` mitgeliefert, keine
externen Schriftarten oder CDN-Links, System-Font-Stack).

## Struktur

- `mvp-deck/index.html` — 22 Folien (Titel, Akteure, USP, Ausgangslage, Ablauf,
  Funktionsumfang, Technologie, GitHub-Stars, digitale Souveränität, Architektur,
  Bildschirmaufnahmen, Live-Demo, Clockify, Out-of-Scope, Feedback, Hosting, Ausblick)
- `mvp-deck/theme.css` — eigenes, handgeschriebenes Theme (keine Webfonts)
- `mvp-deck/reveal/` — reveal.js 5.2.1, lokal eingebunden (CSS + JS, kein CDN)
- `mvp-deck/assets/bestellablauf.svg` (+ `.png`, `.excalidraw`) — Architektur-/Ablaufdiagramm,
  erstellt mit dem `excalidraw-diagram`-Skill

## Status

Die Präsentation verwendet die fachliche Terminologie „Artikel“ und ist für die Abnahme nach
dem Besprechungsprotokoll 14.9 vorbereitet. Produktive Deployment-/Live-Nachweise sind über
CI/CD und Live-Smoke belegt; die Clockify-Istsumme muss noch direkt aus Workspace „DA“ übernommen
werden, sobald der angemeldete Browserzugriff verfügbar ist.
