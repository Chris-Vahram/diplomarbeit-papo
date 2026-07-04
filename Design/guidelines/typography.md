# Typography System

## Fonts

### Primary — Inter
Google Fonts, free, variable weight. Excellent legibility at small sizes (data tables, forms).

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

CSS token: `var(--font-sans)`

**Load weights:** 400, 500, 600, 700 only — no extras.

### Monospace — JetBrains Mono
Used for: order IDs, partner IDs, codes, import validation reports.

```html
<link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
```

CSS token: `var(--font-mono)`

---

## Type Scale

| Step | Size | Line Height | Use |
|------|------|-------------|-----|
| `5xl` | 48px | 1.1 | Hero / splash (rare) |
| `4xl` | 36px | 1.2 | Page titles |
| `3xl` | 30px | 1.2 | Large section headings |
| `2xl` | 24px | 1.3 | Card titles, modal headings |
| `xl`  | 20px | 1.4 | Subsection headings |
| `lg`  | 18px | 1.5 | Emphasized body, lead text |
| `base`| 16px | 1.5 | Body text (default) |
| `sm`  | 14px | 1.5 | Secondary text, descriptions, most UI labels |
| `xs`  | 12px | 1.5 | Meta, timestamps, captions, table column headers |

---

## Weights

| Weight | Value | Name in code |
|--------|-------|-------------|
| Regular  | 400 | `--font-weight-regular`  |
| Medium   | 500 | `--font-weight-medium`   |
| Semibold | 600 | `--font-weight-semibold` |
| Bold     | 700 | `--font-weight-bold`     |

---

## Named Text Styles

### Headings

**heading-xl** — Page title (e.g. "Partner Management", "Order Overview")
```
font-size: 36px
font-weight: 700
line-height: 1.2
letter-spacing: -0.025em
color: var(--text-primary)
```

**heading-lg** — Section title within a page
```
font-size: 30px
font-weight: 700
line-height: 1.2
```

**heading-md** — Card heading, modal title, panel title
```
font-size: 24px
font-weight: 600
line-height: 1.3
```

**heading-sm** — Sub-section label, sidebar section header
```
font-size: 18px
font-weight: 600
line-height: 1.3
```

---

### Body

**body-lg** — Main body text, paragraph descriptions
```
font-size: 16px
font-weight: 400
line-height: 1.5
color: var(--text-primary)
```

**body-sm** — Supporting descriptions, secondary info under headings
```
font-size: 14px
font-weight: 400
line-height: 1.5
color: var(--text-secondary)
```

---

### UI

**label** — Form field labels, filter labels
```
font-size: 14px
font-weight: 500
line-height: 1.5
color: var(--color-neutral-700)
margin-bottom: 4px
```

**caption** — Timestamps, metadata, helper text, table column headers
```
font-size: 12px
font-weight: 400
line-height: 1.5
color: var(--text-secondary)
letter-spacing: 0.025em
```

**table-header** — Column headers in data tables
```
font-size: 12px
font-weight: 600
line-height: 1.5
color: var(--text-secondary)
letter-spacing: 0.05em
text-transform: uppercase
```

**code** — Order IDs, partner codes, import error rows
```
font-family: var(--font-mono)
font-size: 14px
font-weight: 400
line-height: 1.6
```

---

## Usage Rules

### Hierarchy

Only three text hierarchy levels per screen:
1. **Page heading** — one per page, heading-xl or heading-lg
2. **Section/card heading** — heading-md or heading-sm  
3. **Body / data** — body-lg or body-sm

Never skip levels (e.g. heading-xl directly to body-sm with nothing in between).

### Colors

| Context | Color |
|---------|-------|
| Headings | `var(--text-primary)` — neutral-900 |
| Body | `var(--text-primary)` — neutral-900 |
| Supporting / secondary | `var(--text-secondary)` — neutral-600 |
| Disabled / placeholder | `var(--text-disabled)` — neutral-400 |
| Links / brand actions | `var(--text-brand)` — brand-600 |
| Partner links | `var(--text-partner)` — partner-600 |

### Don'ts

- Don't use more than 2 font weights on the same card
- Don't set body text below 14px (accessibility)
- Don't use all-caps for text longer than 4 words
- Don't use letter-spacing on body text (only headers and labels)
- Don't rely on italic for emphasis in data-dense UIs — use color or weight instead
- Don't mix Inter and JetBrains Mono casually — mono signals a code/ID value

---

## Responsive Scaling

At `< 768px`, reduce heading scale by one step:
- heading-xl → 30px (`3xl`)
- heading-lg → 24px (`2xl`)
- heading-md → 20px (`xl`)

Body and label sizes stay fixed.

---

## Letter Spacing

| Name | Value | Apply to |
|------|-------|----------|
| `tight`   | `-0.025em` | Large headings (3xl+) |
| `normal`  | `0em`      | All body text |
| `wide`    | `0.025em`  | Labels, link text |
| `wider`   | `0.05em`   | Table column headers |
| `widest`  | `0.1em`    | Badge / pill text |
