---
name: PAPO Partnerportal
description: Gemeinsame Arbeitsfläche für Unternehmen und Partner, eine Theke mit zwei Seiten.
colors:
  firmengruen: "#689c6d"
  firmengruen-hover: "#578462"
  firmengruen-active: "#456851"
  firmengruen-light: "#f0f7f1"
  firmengruen-medium: "#d8eedb"
  partnerblau: "#5e838d"
  partnerblau-hover: "#4a6870"
  partnerblau-active: "#374e54"
  partnerblau-light: "#eff5f6"
  partnerblau-medium: "#d4e8eb"
  stuetzteal: "#abc7c4"
  seite: "#f6f9f7"
  flaeche: "#ffffff"
  flaeche-gedaempft: "#edf2ee"
  rahmen: "#d6e1d8"
  rahmen-stark: "#b5c8b9"
  text-deaktiviert: "#8ca594"
  text-sekundaer: "#52675a"
  text-label: "#3d4f43"
  text: "#0b110c"
  status-eingegangen-bg: "#fef3c7"
  status-eingegangen-text: "#92400e"
  status-geprueft-bg: "#d4e8eb"
  status-geprueft-text: "#374e54"
  status-erledigt-bg: "#d0fae5"
  status-erledigt-text: "#166534"
  status-aktiv-bg: "#ecfdf4"
  status-aktiv-text: "#15803d"
  erfolg: "#15803d"
  fehler: "#dc2626"
  fehler-dunkel: "#b91c1c"
  fehler-light: "#fef2f2"
  hinweis-rot: "#ef4444"
typography:
  headline:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1.875rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.025em"
  metric:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.025em"
    fontFeature: "tnum"
  title:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.3
  body:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "0.05em"
  wordmark:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 800
    lineHeight: 1
    letterSpacing: "-0.025em"
  mono:
    fontFamily: "Fira Code, JetBrains Mono, ui-monospace, monospace"
    fontSize: "0.875rem"
    fontWeight: 400
rounded:
  sm: "0.25rem"
  md: "0.375rem"
  lg: "0.5rem"
  xl: "0.75rem"
  2xl: "1rem"
  full: "9999px"
spacing:
  "1": "0.25rem"
  "2": "0.5rem"
  "3": "0.75rem"
  "4": "1rem"
  "5": "1.25rem"
  "6": "1.5rem"
  "8": "2rem"
  "10": "2.5rem"
  "12": "3rem"
  "16": "4rem"
components:
  button-primary:
    backgroundColor: "{colors.firmengruen}"
    textColor: "{colors.flaeche}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
    typography: "{typography.body}"
  button-primary-hover:
    backgroundColor: "{colors.firmengruen-hover}"
  button-primary-partner:
    backgroundColor: "{colors.partnerblau}"
    textColor: "{colors.flaeche}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
  button-primary-partner-hover:
    backgroundColor: "{colors.partnerblau-hover}"
  button-primary-hero:
    padding: "0.75rem 1.5rem"
    rounded: "{rounded.md}"
  button-secondary:
    backgroundColor: "{colors.flaeche}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
  button-secondary-hover:
    backgroundColor: "{colors.flaeche-gedaempft}"
  input:
    backgroundColor: "{colors.flaeche}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "0.5rem 0.75rem"
    height: "2.5rem"
  card:
    backgroundColor: "{colors.flaeche}"
    rounded: "{rounded.lg}"
    padding: "1.5rem"
  metric-card:
    backgroundColor: "{colors.flaeche}"
    rounded: "{rounded.lg}"
    padding: "1.5rem"
    typography: "{typography.metric}"
  status-badge-eingegangen:
    backgroundColor: "{colors.status-eingegangen-bg}"
    textColor: "{colors.status-eingegangen-text}"
    rounded: "{rounded.full}"
    padding: "0.125rem 0.75rem"
    typography: "{typography.label}"
  status-badge-geprueft:
    backgroundColor: "{colors.status-geprueft-bg}"
    textColor: "{colors.status-geprueft-text}"
    rounded: "{rounded.full}"
    padding: "0.125rem 0.75rem"
  status-badge-erledigt:
    backgroundColor: "{colors.status-erledigt-bg}"
    textColor: "{colors.status-erledigt-text}"
    rounded: "{rounded.full}"
    padding: "0.125rem 0.75rem"
  nav-item:
    textColor: "{colors.text-label}"
    rounded: "{rounded.md}"
    height: "2.75rem"
    padding: "0 0.75rem"
  nav-item-active:
    backgroundColor: "{colors.firmengruen-light}"
    textColor: "{colors.firmengruen-hover}"
  modal-panel:
    backgroundColor: "{colors.flaeche}"
    rounded: "{rounded.xl}"
    width: "42.5rem"
---

# Design System: PAPO Partnerportal

Quelle der Wahrheit für Tokens bleibt Tobias' System in `Design/` (`variables.css`, `tokens.json`, `guidelines/`). Die Werte oben sind aus `frontend/src/styles/base.css` übernommen, das die Tokens 1:1 in `rem` führt. Diese Datei beschreibt das interne Portal (Firmen- und Partnerbereich, Login, Baukasten-Rahmen). Öffentliche Shopseiten nutzen das freie Theme des jeweiligen Partners und stehen nicht unter diesen Farbregeln.

## Overview

**Creative North Star: "Die Zwei-Schlüssel-Theke"**

Eine gemeinsame Theke, zwei Seiten. Unternehmen und Partner arbeiten an derselben Fläche, mit denselben Tabellen, Formularen und Status, aber jede Seite hat ihren eigenen Schlüssel: Firmengrün für das Unternehmen, Partnerblau für den Partner. Die Rollenfarbe ist kein Schmuck, sondern das Schild über der Theke. Wer auf die Seite schaut, weiß sofort, auf welcher Seite er steht.

Der Rest der Theke ist bewusst ruhig. Eine hell grünstichige Seite (`seite`), weiße Arbeitsflächen mit Haarlinien-Rahmen, eine einzige Schriftfamilie und Daten in Tabellenform. Dichte geht vor Inszenierung: Bestellungen, Artikel, Endkunden und Provisionen müssen schnell lesbar sein. Die Oberfläche soll dabei freundlich und greifbar wirken: Knöpfe sind klar als Knöpfe erkennbar, Karten heben sich leicht ab, Rückmeldungen (Hover, Fokus, Toast) kommen sofort.

**Key Characteristics:**
- Ein Rollenakzent je Bereich, gesteuert über `.context-company` / `.context-partner` am Shell-Root.
- Grünstichige Neutraltöne statt Grau; sie sind die eigentliche Handschrift des Systems.
- Eine Schrift (Plus Jakarta Sans), Hierarchie über Größe und Gewicht.
- Flach mit Haarlinien, leichte Schatten auf Karten, starker Schatten nur bei Modals.
- Status immer als Text-Badge, nie nur als Farbe.

## Colors

Zwei Rollenfarben auf grünstichigem Neutral, dazu feste Statusfarben, die in beiden Bereichen gleich bleiben.

### Primary
- **Firmengrün** (`firmengruen`): Rollenakzent im Firmenbereich. Primärknöpfe, aktiver Navigationspunkt (`firmengruen-light` als Fläche, `firmengruen-hover` als Text), Fokusring, Avatar, Kennzahlen. Hover eine Stufe dunkler (`firmengruen-hover`).
- **Partnerblau** (`partnerblau`): derselbe Akzent im Partnerbereich. Gleiche Rollen wie Firmengrün, nie gemischt auf einem Bildschirm.

### Secondary
- **Stützteal** (`stuetzteal`): nur für unterstützende Flächen. Optisch nah am Partnerblau; nicht verwenden, wenn die Partnerrolle gemeint ist.

### Neutral
- **Seite** (`seite`): Hintergrund hinter allen Flächen.
- **Fläche** (`flaeche`): Karten, Tabellen, Sidebar, Topbar, Modals.
- **Fläche gedämpft** (`flaeche-gedaempft`): Hover auf Zeilen und Navigationspunkten, Karten-Fußzeilen, deaktivierte Felder.
- **Rahmen** (`rahmen`) / **Rahmen stark** (`rahmen-stark`): Haarlinien um Flächen bzw. um Eingabefelder und Sekundärknöpfe.
- **Text** (`text`), **Text sekundär** (`text-sekundaer`), **Text Label** (`text-label`), **Text deaktiviert** (`text-deaktiviert`): Fließtext, Begleittext und Spaltenköpfe, Feld-Labels und Navigation, Platzhalter.

### Status
- **Eingegangen** (Bernstein), **Geprüft** (Blaugrau), **Erledigt** (Grün), **Aktiv/Inaktiv**: feste Badge-Paare aus heller Fläche und dunklem Text, in beiden Rollen identisch.
- **Fehler** (`fehler`, `fehler-dunkel`): Feldfehler, Gefahr-Aktionen, Fehler-Toast. **Hinweis-Rot** (`hinweis-rot`): Zähler-Pille für ungelesene Bestellungen und Pflichtfeld-Stern.

### Named Rules
**The Ein-Schlüssel Rule.** Rollenfarbe kommt ausschließlich aus `--color-accent*`. Kein Bauteil setzt `--color-primary-*` oder `--color-accent-600` direkt; die Kontextklasse am Shell-Root entscheidet.

**The Keine-Mischung Rule.** Firmengrün und Partnerblau erscheinen nie gemeinsam auf einem Bildschirm. Sie sind Rollensignale, keine Dekoration.

**The Bekannte-Lücke Rule.** Weiß auf Firmengrün (3,2:1) und auf Partnerblau (4,1:1) erreicht AA für Text nicht; erst die Hover-Stufen bestehen. Die Lösung (dunklere Ruhestufe oder dunkler Text) ist offen und wird nicht stillschweigend eingebaut; siehe `Design/guidelines/colors.md`.

## Typography

**Display Font:** Plus Jakarta Sans (mit ui-sans-serif, system-ui)
**Body Font:** Plus Jakarta Sans
**Label/Mono Font:** Fira Code für Nummern und Codes, wo eine Festbreite hilft

**Character:** Eine geometrisch-freundliche Grotesk für alles. Sie wirkt offen und nahbar, bleibt in kleinen Größen in Tabellen gut lesbar. Beide Schriften liegen selbst gehostet unter `frontend/vendor/fonts/`.

### Hierarchy
- **Wortmarke** (800, 1.75rem, 1): nur „PAPO“ oben in der Sidebar.
- **Headline** (700, 1.875rem, 1.2, −0.025em): Seitentitel `h1`; unter 768 px 1.5rem.
- **Metric** (700, 1.5rem, tabellarische Ziffern): Kennzahlen in Metrik-Karten, in Rollenfarbe.
- **Title** (600, 1.125rem, 1.3): Karten- und Abschnittstitel, Modal-Kopf.
- **Body** (400, 0.875rem, 1.5): Tabellen, Formulare, Fließtext. Der Arbeitstext des Portals ist 14 px, nicht 16 px.
- **Label** (600, 0.75rem, 0.05em, Großbuchstaben): Tabellenköpfe, Status-Badges, Rollenzeile unter der Wortmarke.

### Named Rules
**The Eine-Familie Rule.** Keine zweite Anzeigeschrift im Portal. Hierarchie entsteht aus Größe und Gewicht.

**The Ziffern-stehen-still Rule.** Geldbeträge und Kennzahlen nutzen `font-variant-numeric: tabular-nums`, damit Spalten und Vergleiche ruhig bleiben.

## Layout

App-Shell aus fester Sidebar (15rem) und schlanker Topbar (4rem) mit Profilmenü rechts. Inhalt in `.app-root`, maximal 80rem breit, Innenabstand 2rem. Abstände folgen der 4-px-Skala (`0.25rem`-Basis); zwischen Abschnitten typisch 1.5–2.5rem, innerhalb von Karten 1.5rem.

Kennzahlen stehen in `.metric-row` als Raster; Formulare nutzen ein zweispaltiges `.entity-form--grid`.

Responsiv:
- **≤ 991 px:** Sidebar wird zur Off-Canvas-Schublade mit Hintergrundabdunklung, Menüknopf in der Topbar.
- **≤ 767 px:** Formulare und Kennzahlen einspaltig, Seitentitel kleiner. Breite Tabellen scrollen derzeit seitlich; gestapelte Karten unter 640 px sind geplant (`refine-portal-frontend`).
- **Druck:** eigener Druckstil für den Auftragsbeleg.

## Elevation & Depth

Flach mit Haarlinien als Grundzustand, Tiefe in drei Stufen. Karten und Metrik-Karten tragen einen leichten, grünstichigen Schatten (`shadow-sm`), der sie spürbar von der Seite abhebt. Toasts schweben mit `shadow-md`. Nur Modals bekommen echten Abstand (`shadow-xl`) über einer 50-%-Abdunklung. Tabellen, Sidebar und Topbar bleiben schattenlos und grenzen sich nur über Rahmen ab.

### Shadow Vocabulary
- **Karte** (`box-shadow: 0 0.0625rem 0.1875rem 0 rgba(11,17,12,0.09), 0 0.0625rem 0.125rem -0.0625rem rgba(11,17,12,0.09)`): Karten, Metrik-Karten.
- **Schwebend** (`box-shadow: 0 0.25rem 0.375rem -0.0625rem rgba(11,17,12,0.09), 0 0.125rem 0.25rem -0.125rem rgba(11,17,12,0.09)`): Toasts, Ausklappmenüs.
- **Dialog** (`box-shadow: 0 1.25rem 1.5625rem -0.3125rem rgba(11,17,12,0.09), 0 0.5rem 0.625rem -0.375rem rgba(11,17,12,0.09)`): Modal-Panel.
- **Fokusring** (`box-shadow: 0 0 0 0.1875rem` in Rollenfarbe mit 35–40 % Deckkraft): jeder fokussierte Bedienpunkt.

### Named Rules
**The Schatten-ist-grün Rule.** Alle Schatten nutzen den Textton `rgba(11,17,12,…)`, nie reines Schwarz.

**The Fokus-ist-Schatten Rule.** Fokus wird als Ring über `box-shadow` gezeichnet, nicht über `outline`, und folgt der Rollenfarbe.

## Shapes

Sanft gerundete Rechtecke, Rundung wächst mit der Größe des Elements: Vorschaubilder 0.25rem, Knöpfe, Felder und Navigationspunkte 0.375rem, Karten und Tabellenrahmen 0.5rem, Modals 0.75rem. Pillen (`full`) nur für Status-Badges, Zähler und das Profilmenü; runde Kreise für Avatar und Metrik-Icons. Rahmen sind immer 1 px Haarlinien. Keine farbigen Seitenstreifen an Containern.

## Components

### Buttons
Klar als Knopf erkennbar, gefüllt oder umrandet, nie nur Text bei Hauptaktionen.
- **Shape:** sanft gerundet (0.375rem).
- **Primary:** Rollenfarbe gefüllt, weißer Text, 600, 0.875rem, Innenabstand 0.5rem 1rem. Hover eine Stufe dunkler.
- **Hero:** derselbe Primärknopf größer (0.75rem 1.5rem, 1rem Schrift, Icon 1.125rem) für genau eine Hauptaktion pro Seite, z. B. „Neue Bestellung“ im Partner-Dashboard.
- **Secondary:** weiße Fläche, starker Rahmen, Textfarbe; Hover gedämpfte Fläche.
- **Ghost / Danger:** im Design-System definiert (`.btn-ghost`, `.btn-danger`), im Portal noch nicht einheitlich übernommen; Vereinheitlichung ist Teil von `refine-portal-frontend`.
- **Focus:** Rollen-Fokusring.

### Cards / Containers
- **Corner Style:** 0.5rem.
- **Background:** Fläche auf Seite.
- **Shadow Strategy:** Karte (siehe Elevation).
- **Border:** 1 px `rahmen`.
- **Internal Padding:** 1.5rem; Kopf/Fuß 1rem 1.5rem, Fuß auf gedämpfter Fläche.

### Metric Card (Signature)
Kennzahl groß in Rollenfarbe über einem kleinen Label, links ein runder Icon-Kreis in heller Rollenfarbe, rechts optional Sparkline oder Ring. Trend nach oben grün, nach unten rot, jeweils mit Pfeil-Icon. Ein schwaches Wasserzeichen-Icon (7 % Deckkraft) liegt unten rechts.

### Inputs / Fields
- **Style:** weiß, 1 px starker Rahmen, 0.375rem, Höhe 2.5rem, 0.875rem Schrift; Label 600 in `text-label` darüber.
- **Focus:** Rahmen in Rollenfarbe plus Fokusring.
- **Error:** roter Rahmen, Fehlertext 0.75rem in `fehler` darunter. **Disabled:** gedämpfte Fläche, deaktivierter Text.
- Native Checkboxen, Radios und Datumsfelder übernehmen die Rollenfarbe über `accent-color`.

### Data Table
Weiße Fläche mit Rahmen, Kopfzeile auf heller Rollenfarbe mit Label-Typo in Großbuchstaben, Zeilen mit Haarlinie und Hover auf gedämpfter Fläche. Werkzeugleiste darüber (Suche, Filter, Zurücksetzen), Blättern darunter. Varianten: klickbare Zeile, ungelesene Zeile hervorgehoben, ruhende Zeile (Partner ohne Bestellung seit 60 Tagen) gedämpft.

### Status Badge
Pille, Label-Typo in Großbuchstaben, feste Farbpaare für Eingegangen / Geprüft / Erledigt / Aktiv / Inaktiv. Text trägt die Bedeutung; die Farbe unterstützt nur.

### Navigation
Sidebar auf Fläche mit Rahmen rechts. Oben Wortmarke „PAPO“, darunter die Rolle in Label-Typo und Rollenfarbe. Einträge 2.75rem hoch mit Lucide-Icon (1.25rem) und Text 500; Hover gedämpfte Fläche; aktiv helle Rollenfarbe mit Text in Rollenfarbe, 600. Zähler-Pille in Hinweis-Rot rechtsbündig, bei 0 ausgeblendet. Mobil als Schublade.

### Modal Dialog
Großzügiges Panel (42.5rem, max. 94vw / 96vh), 0.75rem Rundung, Dialog-Schatten, Kopf mit Titel und Schließen-Knopf über einer Haarlinie, Inhalt 1.5rem Innenabstand.

### Toast
Kleine Karte oben rechts, weiße Schrift auf dunklem Neutral (Info), Grün (Erfolg) oder Rot (Fehler), Schwebend-Schatten.

## Do's and Don'ts

### Do:
- **Do** Rollenfarbe immer über `var(--color-accent)` / `--color-accent-hover` / `--color-accent-light` lesen und die Kontextklasse am Shell-Root setzen.
- **Do** Fließtext in `text` (#0b110c) und Begleittext in `text-sekundaer` (#52675a) setzen.
- **Do** Status als Badge mit Text zeigen und überall dieselben Statusfarben verwenden.
- **Do** genau eine Hero-Aktion pro Seite verwenden; im Partnerbereich ist das „Neue Bestellung“.
- **Do** Kennzahlen und Beträge mit tabellarischen Ziffern setzen.
- **Do** `prefers-reduced-motion` respektieren; Übergänge bleiben kurz (100–250 ms, `cubic-bezier(0.4, 0, 0.2, 1)`).

### Don't:
- **Don't** Firmengrün und Partnerblau auf demselben Bildschirm mischen.
- **Don't** Hexwerte oder Palettenstufen (`--color-primary-500`, `--color-accent-600`) direkt in Bauteilen setzen, wenn `--color-accent` dasselbe leistet.
- **Don't** Stützteal verwenden, wenn die Partnerrolle gemeint ist.
- **Don't** Farbe als einzige Information verwenden.
- **Don't** Stufen 300/400 von Grün oder Blau als Text auf Weiß verwenden (zu wenig Kontrast).
- **Don't** farbige Seitenstreifen an Containern oder dekorative Elemente ohne Bedeutung einsetzen.
- **Don't** Schriften von Google Fonts nachladen; nur selbst gehostete Dateien.
