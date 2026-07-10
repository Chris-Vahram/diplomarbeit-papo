# MVP-Präsentation

reveal.js-Deck für die Diplomarbeits-Präsentation (Thema: MVP, Use Cases, Tech-Stack).

## Öffnen

`presentation/mvp-deck/index.html` direkt im Browser öffnen — kein Server, kein Build-Schritt nötig.
Funktioniert vollständig offline (reveal.js ist unter `mvp-deck/reveal/` mitgeliefert, keine
externen Schriftarten oder CDN-Links, System-Font-Stack).

## Struktur

- `mvp-deck/index.html` — die 10 Folien (Titel, Problem, Akteure, Kernablauf, Funktionsumfang,
  Tech-Stack, Architektur, Bildschirmaufnahmen, Out-of-Scope, Ausblick)
- `mvp-deck/theme.css` — eigenes, handgeschriebenes Theme (keine Webfonts)
- `mvp-deck/reveal/` — reveal.js 5.2.1, lokal eingebunden (CSS + JS, kein CDN)
- `mvp-deck/assets/bestellablauf.svg` (+ `.png`, `.excalidraw`) — Architektur-/Ablaufdiagramm,
  erstellt mit dem `excalidraw-diagram`-Skill

## Noch offen

Die Folie „Bildschirmaufnahmen" enthält vier **Platzhalter** (Browser-Rahmen mit Wireframe-Boxen,
markiert mit einem „Platzhalter"-Tag): Login, Partner-Dashboard, Bestellformular, Bestellübersicht.
Echte Screenshots einfach als `<img>` in das jeweilige `.shot-body` einsetzen und die `.wf-*`-Divs
entfernen — Rahmen, Browser-Leiste und Caption bleiben unverändert.
