---
name: PAPO Partnerportal
description: Internal B2B ordering portal for the chain Company → Partner → End Customer.
colors:
  company-green: "#689c6d"
  company-green-hover: "#578462"
  company-green-deep: "#456851"
  company-green-deeper: "#344e3d"
  company-green-tint: "#f0f7f1"
  partner-blue: "#5e838d"
  partner-blue-hover: "#4a6870"
  partner-blue-deep: "#374e54"
  partner-blue-tint: "#eff5f6"
  calm-teal: "#abc7c4"
  page-mist: "#f6f9f7"
  surface-white: "#ffffff"
  subtle-sage: "#edf2ee"
  border-sage: "#d6e1d8"
  border-strong: "#b5c8b9"
  text-disabled: "#8ca594"
  text-secondary: "#52675a"
  text-label: "#3d4f43"
  text-primary: "#0b110c"
  success: "#15803d"
  success-tint: "#d0fae5"
  warning-text: "#92400e"
  warning-tint: "#fef3c7"
  error: "#dc2626"
  error-deep: "#b91c1c"
  error-tint: "#fee2e2"
typography:
  headline:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.875rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.025em"
  title:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 600
    lineHeight: 1.3
  metric:
    fontFamily: "Plus Jakarta Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.025em"
    fontFeature: "tnum"
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
  mono:
    fontFamily: "Fira Code, JetBrains Mono, ui-monospace, monospace"
    fontSize: "0.875rem"
    fontWeight: 400
rounded:
  sm: "0.25rem"
  md: "0.375rem"
  lg: "0.5rem"
  xl: "0.75rem"
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
components:
  button-primary-company:
    backgroundColor: "{colors.company-green-deep}"
    textColor: "{colors.surface-white}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
  button-primary-company-hover:
    backgroundColor: "{colors.company-green-deeper}"
  button-primary-partner:
    backgroundColor: "{colors.partner-blue-hover}"
    textColor: "{colors.surface-white}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
  button-primary-partner-hover:
    backgroundColor: "{colors.partner-blue-deep}"
  button-primary-hero:
    rounded: "{rounded.md}"
    padding: "0.75rem 1.5rem"
    typography: "{typography.title}"
  button-secondary:
    backgroundColor: "{colors.surface-white}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "0.5rem 1rem"
  button-secondary-hover:
    backgroundColor: "{colors.subtle-sage}"
  input:
    backgroundColor: "{colors.surface-white}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "0.5rem 0.75rem"
    height: "2.5rem"
  card:
    backgroundColor: "{colors.surface-white}"
    rounded: "{rounded.lg}"
    padding: "1.5rem"
  status-badge:
    rounded: "{rounded.full}"
    padding: "0.125rem 0.75rem"
    typography: "{typography.label}"
  nav-item:
    textColor: "{colors.text-label}"
    rounded: "{rounded.md}"
    height: "2.75rem"
    padding: "0 0.75rem"
  nav-item-active-company:
    backgroundColor: "{colors.company-green-deep}"
    textColor: "{colors.surface-white}"
  nav-item-active-partner:
    backgroundColor: "{colors.partner-blue-hover}"
    textColor: "{colors.surface-white}"
---

# Design System: PAPO Partnerportal

<!-- Source of truth: Tobias' Design/ (variables.css, tokens.json, guidelines/) for tokens and intent;
     frontend/src/styles/base.css for what is implemented. This file records the implemented system
     and lists where it deliberately differs from Design/. Visual changes go through Tobias. -->

## Overview

**Creative North Star: "Der verlässliche Partner"**

PAPO is the tool a sales partner opens to get an order for their customer done, and the tool the company uses to process it. The interface behaves like a dependable colleague: calm, tidy, always clear about what happens next, and quietly encouraging. Partners see their own progress (orders, revenue, commission) without being sold to; the company sees the work queue without noise.

Density serves data. White surfaces on a soft green-tinted mist, one role accent that tells users which side they are on (green company, blue partner), Plus Jakarta Sans throughout, numbers in tabular figures. Colour is a signal, never decoration. Friendliness comes from generous spacing, rounded-but-firm shapes, plain German copy and visible success states, not from ornament.

This is a refinement system: it preserves Tobias' tokens and look. No dark mode, no new brand colours, no new fonts.

**Key Characteristics:**
- One role accent per area, switched once on the shell (`.area--company` / `.area--partner`).
- Light sidebar, white content cards, subtle green-tinted shadows.
- Status always shown as text badge plus colour.
- One main action per page, visibly larger ("Neue Bestellung").
- Money and counts in tabular figures; amounts never ambiguous.

## Colors

A muted, green-tinted palette: two calm role accents over sage neutrals, with standard semantic colours for status.

### Primary
- **Company Green** (#689c6d, `--color-primary-500`): the company role accent — primary buttons, active navigation, focus ring, brand text in the company area. Hover Company Green Hover (#578462), pressed Company Green Deep (#456851), tint background Company Green Tint (#f0f7f1).

### Secondary
- **Partner Blue** (#5e838d, `--color-accent-600`): the partner role accent, same roles as Company Green in the partner area. Hover #4a6870, pressed #374e54, tint #eff5f6. The core step differs (500 vs 600) on purpose; do not renumber.

### Tertiary
- **Calm Teal** (#abc7c4, `--color-secondary-300`): supporting surfaces only, valid in both areas. Not a role colour.

### Neutral
- **Page Mist** (#f6f9f7): page background.
- **Surface White** (#ffffff): cards, sidebar, topbar, dialogs, tables.
- **Subtle Sage** (#edf2ee): hover fills, table header fallback, disabled backgrounds.
- **Border Sage** (#d6e1d8) / **Border Strong** (#b5c8b9): default dividers / input and secondary-button borders.
- **Text Primary** (#0b110c), **Text Label** (#3d4f43), **Text Secondary** (#52675a, 6.1:1 on white), **Text Disabled** (#8ca594, placeholder only; fails text contrast).
- **Semantic:** success #15803d on #d0fae5, warning #92400e on #fef3c7, error #dc2626 / #b91c1c on #fee2e2, info reuses the partner-blue ramp (#374e54 on #d4e8eb). Order statuses: submitted = warning, reviewed = info, fulfilled = success.

### Named Rules
**The One Accent Rule.** Every role-aware element reads `--color-accent`, `--color-accent-hover`, `--color-accent-light`, `--text-brand`, `--border-focus` or `--shadow-focus-ring`. A component that names `--color-primary-*` or `--color-accent-*` directly for a role colour is a bug (open in `base.css`: `.button-primary` and `.area--partner .button-primary`, task 2.1).

**The Signal Not Paint Rule.** Green and blue never appear together as decoration on one screen; semantic colours appear only for their status.

**The Strong Accent Rule.** Text and fills that carry white text use the strong accent step 700 (`--color-accent-strong`: company #456851 = 6.3:1, partner #4a6870 = 6.0:1), hover step 800. The core steps (company 500, partner 600) stay for tints, rings, lines and icons, never behind white text: they fail AA (3.2:1 / 4.1:1), and green 600 still fails (4.3:1). Decided by Chris 16.10.2026 (variant comparison 1B/2C), confirmed by Tobias.

## Typography

**Display/Body Font:** Plus Jakarta Sans (self-hosted woff2 in `frontend/vendor/fonts/`, fallback `ui-sans-serif, system-ui`)
**Mono Font:** Fira Code (order numbers, IDs, import row numbers)

**Character:** one friendly geometric sans for everything, with a mono for identifiers; hierarchy comes from weight and size, not from font changes.

### Hierarchy
- **Headline** (700, 1.875rem, 1.2, −0.025em): page titles.
- **Title** (600, 1.25rem, 1.3): card and dialog titles, section headings.
- **Metric** (700, 1.5rem, tabular figures, `--text-brand`): dashboard values.
- **Body** (400, 0.875rem, 1.5): tables, forms, most UI text. 1rem for longer prose.
- **Label** (600, 0.75rem, 0.05em, uppercase): table headers, status badges, sidebar role label.
- **Wordmark** (800, 1.75rem): "PAPO" in the sidebar only.

### Named Rules
**The Tabular Money Rule.** Amounts, counts and percentages use `font-variant-numeric: tabular-nums`; money is formatted in German (`1.240,00 €`).

## Layout

App shell: fixed light sidebar (15rem) + topbar (4rem) + content (`max-width: 80rem`, padding 2rem; 1rem below 62em). Below 62em (≈992px) the sidebar becomes an off-canvas drawer with backdrop; below 48em forms and metric rows collapse to one column. Spacing scale is 0.25rem-based (`--spacing-1` … `--spacing-20`); sections are separated by 2.5rem, cards by 1.25rem. Metric rows use a 3-column grid. Desktop is the primary context (PRODUCT.md); mobile must work without horizontal page scroll. Planned (task 3.1): below 640px main lists become compact two-line cards (primary field + amount on top, date/secondary + status below; decision 5C).

## Elevation & Depth

Mostly flat: surfaces separate through borders (Border Sage) and a faint green-tinted shadow. Shadows grow only with layer height.

### Shadow Vocabulary
- **Resting** (`--shadow-sm`): cards, metric cards, tables.
- **Floating** (`--shadow-md`): profile menu, period picker panel.
- **Overlay** (`--shadow-xl`): dialogs, mobile drawer.
- **Focus** (`--shadow-focus-ring`, 3px accent ring at 35–40%): keyboard focus on every control.

### Named Rules
**The Border First Rule.** Separate with a 1px border before reaching for a stronger shadow; no coloured accent stripes on containers.

## Shapes

Gently rounded, firm rectangles: controls 0.375rem, cards and tables 0.5rem, dialogs 0.75rem, badges and avatar pills fully round. Borders are 1px. Icons are Lucide line icons, self-hosted (`frontend/vendor/lucide-1.23.0.min.js`): 16px in buttons, 20px in navigation, as in `Design/guidelines/components.md`.

## Components

### Buttons
- **Shape:** gently rounded (0.375rem), semibold 0.875rem, padding 0.5rem 1rem.
- **Primary (`.button-primary`):** strong accent background (step 700), white text; hover step 800.
- **Hero (`.button-primary--hero`):** the single main action of a page (partner dashboard "Neue Bestellung"): padding 0.75rem 1.5rem, 1rem text, leading icon.
- **Secondary (`.button-secondary`):** white, Border Strong outline, primary text; hover Subtle Sage.
- **Planned (task 2.2):** `.button-ghost` (transparent, toolbar/row actions) and `.button-danger` (error-600, destructive). Class names stay `button-*` instead of Design/'s `.btn-*`.
- **Focus:** accent focus ring; disabled 45% opacity.

### Status Badges
- **Style:** pill, uppercase label type, tinted background with dark text of the same hue (`.status-badge--submitted|reviewed|fulfilled|active|inactive`). The text label is mandatory.

### Cards / Containers
- **Corner Style:** 0.5rem. **Background:** Surface White. **Border:** 1px Border Sage. **Shadow:** Resting. **Padding:** 1.5rem.
- **Metric card:** value above label, role-tinted round icon (2.75rem), faint watermark icon; trend arrows green/red when a comparison exists. Planned (decision 3C): neutral icon without trend (3.3); the lead metric (partner: Provision) spans the full row on the accent tint with a larger value and trend as text, the other metrics sit below as compact cards (4.1).

### Inputs / Fields
- **Style:** 2.5rem high, Border Strong, 0.375rem radius, white; label semibold Text Label above.
- **Focus:** border switches to `--border-focus` plus focus ring.
- **Error:** error-500 border, error-600 message below in 0.75rem.

### Navigation
- **Sidebar:** white surface with right border; items 2.75rem high, 20px icon, Text Label colour; hover Subtle Sage; active item is a solid strong-accent fill (step 700) with white semibold text (decision 2C, matches Design/'s solid style). Unread count as small red pill. Mobile: off-canvas drawer via topbar toggle.

### Data Table (`<data-table>`)
- Search, filters, sorting and pager above/below; header row on accent tint with uppercase labels; rows separated by Border Sage; clickable rows, "neu" badge for unseen orders, dormant marker. Empty state centred secondary text. Planned: compact two-line cards below 640px (3.1); shared states (3.2, decision 6C): icon + title + hint, empty state offers the page's main action where one exists, error state offers "Erneut laden".

### Order Wizard Steps
- Checkpoints in a row joined by a line. Done: filled strong accent with ✓. Current (decision 4B): white circle with strong-accent border and soft accent ring, bold label, plus the text "Schritt N von M". Upcoming: outlined number. Never colour alone.

### Dialog (`<modal-dialog>`)
- Overlay backdrop, white panel 0.75rem radius, Overlay shadow, header with title and close button, roomy body (42.5rem wide, max 94vw/96vh).

## Do's and Don'ts

### Do:
- **Do** read role colours only through `--color-accent*` / `--text-brand`.
- **Do** give each page at most one hero action; everything else regular or secondary.
- **Do** pair every status colour with a German text label.
- **Do** keep visible focus (accent ring) on every interactive element and 44px touch targets on mobile.
- **Do** use tabular figures and German formatting for money.
- **Do** respect `prefers-reduced-motion`; transitions stay at 100–250ms.

### Don't:
- **Don't** introduce new colours, fonts, dark mode or a CSS framework.
- **Don't** hardcode `--color-primary-*` / `--color-accent-*` or hex values for role colour in components.
- **Don't** use the teal secondary ramp as partner colour.
- **Don't** use Text Disabled (#8ca594) or 300/400 accent steps for readable text.
- **Don't** add coloured side stripes or decorative gradients to containers.

## Deviations from Design/ (for Tobias)

Recorded as implemented; Chris chose to keep the current app state (16.10.2026). Tobias may adjust `Design/` or request a change.

| Topic | `Design/` | Implemented |
|---|---|---|
| Sidebar | dark (`neutral-950`), light text | white surface, dark text |
| Active nav item | full accent background, white text | planned: solid strong accent (700), white text |
| Class names | `.btn-*`, `.context-company/-partner` | `.button-*`, `.area--company/-partner` (both context selectors defined) |
| Button contrast | known AA failure, fix open | planned: strong accent 700 (The Strong Accent Rule) |
| Fonts | Google Fonts import | self-hosted woff2 (no CDN) |
