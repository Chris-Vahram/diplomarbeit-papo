# Partnerportal — Design System

**Version:** 1.0  
**Project:** Web-Based Partner Portal (PAPO Diplomarbeit)  
**Stack target:** Web Components + Quarkus backend

---

## Index

| File | Contents |
|------|----------|
| `tokens.json` | All design tokens (colors, type, spacing, effects) |
| `variables.css` | CSS custom properties — import directly |
| `guidelines/colors.md` | Color system, role themes, semantic usage |
| `guidelines/typography.md` | Font scale, weights, usage patterns |
| `guidelines/spacing.md` | Spacing scale, layout grid, breakpoints |
| `guidelines/components.md` | Button, input, card, table, badge, nav patterns |

---

## Core Principles

**Minimal.** No decorative noise. Every visual element must carry meaning.

**Role-distinct.** Company admin area and Partner area share the same token system but use different accent colors — users always know which context they are in.

**Data-forward.** Tables, forms, and status badges are the primary UI primitives. Visual hierarchy serves data density, not aesthetics.

**Accessible.** All color combinations meet WCAG AA (4.5:1 text, 3:1 UI). Focus states are always visible.

---

## Two Role Contexts

| Area | Users | Accent Color |
|------|-------|-------------|
| Company Admin | Company / Operator | Blue — `#2563EB` |
| Partner Area | Sales Partners | Teal — `#0D9488` |

Both share the same neutral shell (Slate sidebar on dark, white content area). Only the accent color changes.

---

## Quick-start: CSS

```html
<link rel="stylesheet" href="Design/variables.css" />
```

Then use tokens:
```css
.button-primary {
  background: var(--color-brand-600);
  color: var(--color-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2) var(--spacing-4);
}
```
