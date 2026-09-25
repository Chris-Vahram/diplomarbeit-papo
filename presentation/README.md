# MVP-Präsentation

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

22 Folien, Terminologie „Artikel“, offline lauffähig. Inhalt nach Besprechungsprotokoll 14.9
überarbeitet: GitHub-Stars, digitale Souveränität, echte Browseraufnahmen,
Clockify-Übersicht und [Demo-Drehbuch](demo-drehbuch.md). Clockify-Istwert für Chris und
Tobias im Workspace „DA“, 01.03.–30.09.2026: 151:41:38.
