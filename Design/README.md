# Partnerportal — Design System

**Version:** 1.1  
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

**Accessible.** Most color combinations meet WCAG AA (4.5:1 text, 3:1 UI); one known exception — see `guidelines/colors.md` § Contrast Ratios. Focus states are always visible.

---

## Two Role Contexts

| Area | Users | Accent Color |
|------|-------|-------------|
| Company Admin | Company / Operator | Green — `#689C6D` (`--color-primary-500`) |
| Partner Area | Sales Partners | Blue — `#5E838D` (`--color-accent-600`) |

Both share the same neutral shell (green-tinted Slate sidebar on dark, white content area). Only the accent color changes — driven by `.context-company` / `.context-partner` on the shell root, never hardcoded per component.

---

## Quick-start: CSS

```html
<link rel="stylesheet" href="Design/variables.css" />
```

Then use tokens — role-aware components read `var(--color-accent)`, never a raw palette step:
```css
.button-primary {
  background: var(--color-accent);   /* green in .context-company, blue in .context-partner */
  color: var(--color-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2) var(--spacing-4);
}
```
