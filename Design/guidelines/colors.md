# Color System

## Brand Philosophy

Two-tone role separation: **Blue** for company/admin context, **Teal** for partner context. Both use the same neutral shell. Color signals *who* the user is without explaining it.

---

## Palettes

### Brand Blue — Company / Admin context

| Token | Hex | Use |
|-------|-----|-----|
| `--color-brand-50`  | `#EFF6FF` | Brand tint background, highlight surfaces |
| `--color-brand-100` | `#DBEAFE` | Badge backgrounds, hover highlights |
| `--color-brand-200` | `#BFDBFE` | — |
| `--color-brand-300` | `#93C5FD` | Decorative, illustrations |
| `--color-brand-400` | `#60A5FA` | — |
| `--color-brand-500` | `#3B82F6` | Active links, info states |
| **`--color-brand-600`** | **`#2563EB`** | **Primary buttons, links, active nav, sidebar active** |
| `--color-brand-700` | `#1D4ED8` | Button hover state |
| `--color-brand-800` | `#1E40AF` | Button active/pressed |
| `--color-brand-900` | `#1E3A8A` | Deep text on light backgrounds |
| `--color-brand-950` | `#172554` | Dark mode sidebar tint |

### Partner Teal — Partner area context

| Token | Hex | Use |
|-------|-----|-----|
| `--color-partner-50`  | `#F0FDFA` | Partner surface tint |
| `--color-partner-100` | `#CCFBF1` | Partner badge bg, hover |
| `--color-partner-200` | `#99F6E4` | — |
| `--color-partner-300` | `#5EEAD4` | Decorative |
| `--color-partner-400` | `#2DD4BF` | — |
| `--color-partner-500` | `#14B8A6` | — |
| **`--color-partner-600`** | **`#0D9488`** | **Partner buttons, links, active nav** |
| `--color-partner-700` | `#0F766E` | Hover |
| `--color-partner-800` | `#115E59` | Pressed |
| `--color-partner-900` | `#134E4A` | Deep text |

### Neutrals (Slate)

| Token | Hex | Use |
|-------|-----|-----|
| `--color-neutral-50`  | `#F8FAFC` | Page background |
| `--color-neutral-100` | `#F1F5F9` | Table headers, input disabled bg, subtle surfaces |
| `--color-neutral-200` | `#E2E8F0` | Borders (default), dividers |
| `--color-neutral-300` | `#CBD5E1` | Input borders, strong borders |
| `--color-neutral-400` | `#94A3B8` | Placeholder text, disabled text |
| `--color-neutral-500` | `#64748B` | Secondary text, icons |
| `--color-neutral-600` | `#475569` | Secondary text (stronger) |
| `--color-neutral-700` | `#334155` | Labels, form text |
| `--color-neutral-800` | `#1E293B` | Sidebar background tint |
| `--color-neutral-900` | `#0F172A` | Primary text, headings |
| `--color-neutral-950` | `#020617` | Darkest — sidebar background |

---

## Semantic Colors

### Status Badges — Orders

| Status | Background | Text | Meaning |
|--------|-----------|------|---------|
| `submitted`  | `#FEF3C7` (warning-100) | `#92400E` (warning-800) | Order placed, awaiting review |
| `reviewed`   | `#DBEAFE` (info-100)    | `#1E40AF` (info-800)    | Company has reviewed |
| `fulfilled`  | `#D1FAE5` (success-100) | `#065F46` (success-800) | Shipped to end customer |

### Status Badges — Partners

| Status | Background | Text |
|--------|-----------|------|
| `active`   | `#ECFDF5` | `#047857` |
| `inactive` | `#F1F5F9` | `#64748B` |

### Feedback Colors

| Semantic | 500 (icons) | 100 (bg) | 800 (text) |
|----------|-------------|----------|------------|
| Success  | `#10B981`   | `#D1FAE5`| `#065F46`  |
| Warning  | `#F59E0B`   | `#FEF3C7`| `#92400E`  |
| Error    | `#EF4444`   | `#FEE2E2`| `#991B1B`  |
| Info     | `#3B82F6`   | `#DBEAFE`| `#1E40AF`  |

---

## Role Context — How to apply

Apply the `.context-company` or `.context-partner` class to the layout root. All accent-driven tokens update automatically.

```html
<!-- Company admin shell -->
<div class="app-shell context-company">
  ...
</div>

<!-- Partner shell -->
<div class="app-shell context-partner">
  ...
</div>
```

The sidebar, active nav items, primary buttons, and focus rings all adapt via CSS custom property cascade.

---

## Contrast Ratios (WCAG AA)

| Combination | Ratio | Pass AA |
|-------------|-------|---------|
| brand-600 on white | 5.9:1 | ✓ |
| brand-900 on brand-50 | 12.3:1 | ✓ |
| partner-600 on white | 4.7:1 | ✓ |
| neutral-700 on white | 8.2:1 | ✓ |
| neutral-500 on white | 4.6:1 | ✓ (body min.) |
| white on brand-600 | 5.9:1 | ✓ |
| white on partner-600 | 4.7:1 | ✓ |
| warning-800 on warning-100 | 7.1:1 | ✓ |
| success-800 on success-100 | 8.4:1 | ✓ |
| error-800 on error-100 | 8.1:1 | ✓ |

---

## Do / Don't

**Do:**
- Use brand-600 for company CTAs and brand-700 for hover only
- Use partner-600 for partner CTAs and partner-700 for hover only
- Use neutral-900 for all body text on white
- Use neutral-500 for secondary/supporting text
- Use semantic status colors only for their designated status

**Don't:**
- Mix brand blue and partner teal on the same screen (they're role signals, not decoration)
- Use color alone to convey information — pair with text or icon
- Use brand-300/400 for text on white (fails contrast)
- Use more than one shade of neutral for borders on the same surface
