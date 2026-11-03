---
version: 1
slug: "frontend-landing-html"
primary_target: "frontend/landing.html"
related_targets: ["frontend/impressum.html","frontend/datenschutz.html","frontend/404.html","frontend/src/styles/public.css"]
---

# Surface: öffentlicher Einstieg (Landing, Rechtsseiten, 404)

Scope: `/` (landing.html), `/impressum`, `/datenschutz`, `404.html`, Stylesheet `public.css`. Mode: Persuade (Landing), Read (Rechtsseiten).
Audience: zuerst Unternehmen, die über PAPO entscheiden; dann neu eingeladene Partner; Endkunden nur Hinweis.
Job: verstehen, was PAPO ist, und zum Login finden. Einzige Aktion „Anmelden“ (`/portal/#/login`).
Proof: echte Screenshots aus Demo-Daten (Firmen-Dashboard, Bestellliste, Neue Bestellung, Partner-Dashboard, Demo-Shop). Keine Zahlen, Kunden, Bewertungen.
Constraints: PAPO-Tokens aus base.css, Plus Jakarta Sans selbst gehostet, nur Hellmodus, ohne JS lesbar, Sie-Anrede, Begriffe Artikel/Endkunde/Bestellung/Provision.
Unresolved: Platzhalter `[[…]]` in Rechtstexten und Kontakt.

## Direction contract

THESIS: Eine Bestellung, zwei Seiten. Die Seite zeigt dieselbe Arbeit gleichzeitig aus Sicht des Unternehmens und des Partners und verweigert die übliche Abfolge aus Held, Feature-Kachelreihe und Testimonials.

OWN-WORLD: Page Mist als Grund, weiße Flächen mit Border Sage, Petrol #456f6d als einziger öffentlicher Akzent; Grün und Blau tauchen nur als Rollenkennung in der Doppelansicht auf, als Bildlabel („Firmenbereich“ / „Partnerbereich“) im getönten Screenshot-Rahmen, nie als Seitenstreifen oder Eyebrow. Plus Jakarta Sans, große Überschriften mit negativer Laufweite, Zahlen tabellarisch.

STORY: Problem (Bestellungen per E-Mail und Telefon) → PAPO als eine Stelle → Kette Unternehmen → Partner → Endkunde → beide Seiten nebeneinander → Mein Webshop als zweiter Eingang in dieselbe Abwicklung → Hinweis für Endkunden → Anmelden.

FIRST VIEWPORT: Weißer Kopf mit Kante (Wortmarke, Anker, kompaktes „Anmelden“ in Petrol). Zweisätzige Headline über die volle Breite, Umbruch nur zwischen den Sätzen. Darunter links Lead und Textlink ↓, rechts der Bestellbeleg einer echten Demo-Bestellung, dessen Status beim Laden einmal Eingereicht → Geprüft → Abgeschlossen durchläuft (Signatur-Moment). Unterkante: Route Unternehmen → Partner → Endkunde als Knoten mit gepunktetem Pfad.

FORM: „Zwei Seiten einer Bestellung“, Position 2 der eigenen Rangliste, seed:9d678b4016cb21fa. Signatur: Bestellbeleg im Hero, Zettelstapel → eine Zeile vor dem Vergleich, Naht „Dieselbe Bestellung“ zwischen den Spalten; Doppelansicht, die am Desktop zwei Spalten mit synchroner Zeilenführung (Überschrift, Text, Screenshot, je drei Aufgaben Zeile für Zeile per verschachteltem Subgrid, Dashboard) bildet und am Handy untereinander steht; Bewegung nur als dezentes Einblenden der Screenshots, aus bei reduzierter Bewegung.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
