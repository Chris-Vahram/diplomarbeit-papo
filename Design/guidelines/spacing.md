# Spacing & Layout

## Spacing Scale

Base unit: **4px**. All spacing is a multiple of 4.

| Token | Value | Typical use |
|-------|-------|-------------|
| `--spacing-0`  | 0px   | Reset |
| `--spacing-1`  | 4px   | Micro gaps — icon + label, badge padding |
| `--spacing-2`  | 8px   | Button padding (vertical), form field internal gap |
| `--spacing-3`  | 12px  | Table cell padding, compact card padding |
| `--spacing-4`  | 16px  | Default content padding (mobile), form field gap |
| `--spacing-5`  | 20px  | — |
| `--spacing-6`  | 24px  | Card padding, section gap (default), modal padding |
| `--spacing-7`  | 28px  | — |
| `--spacing-8`  | 32px  | Content horizontal padding (desktop), large gaps |
| `--spacing-10` | 40px  | Section-to-section gap |
| `--spacing-12` | 48px  | — |
| `--spacing-14` | 56px  | — |
| `--spacing-16` | 64px  | Topbar height, major section separation |
| `--spacing-20` | 80px  | — |
| `--spacing-24` | 96px  | — |
| `--spacing-32` | 128px | Large empty states, hero sections |

---

## App Layout

### Shell Structure

```
┌─────────────────────────────────────────────────────────┐
│ Topbar (height: 64px)                                   │
├──────────────┬──────────────────────────────────────────┤
│              │                                          │
│  Sidebar     │  Content Area                            │
│  240px       │  max-width: 1280px                       │
│  (64px coll.)│  padding: 32px                           │
│              │                                          │
│              │                                          │
└──────────────┴──────────────────────────────────────────┘
```

| Measurement | Value | Token |
|-------------|-------|-------|
| Sidebar width (expanded)  | 240px | `--sidebar-width` |
| Sidebar width (collapsed) |  64px | `--sidebar-width-collapsed` |
| Topbar height             |  64px | `--topbar-height` |
| Content max width         | 1280px| `--content-max-width` |
| Content padding (desktop) |  32px | `--content-padding` |
| Content padding (mobile)  |  16px | (override at < 768px) |

### Topbar

- Fixed to top, full width
- Height: 64px
- bg: `var(--color-white)` with `border-bottom: 1px solid var(--border-default)`
- z-index: `var(--z-sticky)` = 200
- Contains: logo/wordmark (left), breadcrumb or page title (center), user avatar + role indicator (right)

### Sidebar

- Fixed left, full height minus topbar
- bg: `var(--color-neutral-950)` (near black)
- Width: 240px expanded, 64px icon-only collapsed
- z-index: `var(--z-sticky)` = 200
- Nav item: 44px height, 12px horizontal padding, 6px border-radius
- Active state: `bg: var(--color-accent)` (brand-600 in company, partner-600 in partner)
- Hover state: `bg: rgba(255,255,255,0.07)`
- Nav text: `var(--color-neutral-300)` inactive, `white` active

### Content Area

- Starts at left edge of sidebar + topbar offset
- All page content scrolls here, not the sidebar
- Padding: `32px` all sides on desktop, `16px` on mobile

---

## Grid

Use a simple **12-column fluid grid** with 24px gutters.

| Screen | Columns | Gutter | Margin |
|--------|---------|--------|--------|
| xs (< 640px)  | 4  | 16px | 16px |
| sm (≥ 640px)  | 8  | 16px | 24px |
| md (≥ 768px)  | 12 | 24px | 24px |
| lg (≥ 1024px) | 12 | 24px | 32px |
| xl (≥ 1280px) | 12 | 24px | 32px |

### Common Column Spans

| Content | Span |
|---------|------|
| Full content area | 12 col |
| Main content + sidebar | 8 + 4 |
| Three equal cards | 4 + 4 + 4 |
| Two cards | 6 + 6 |
| Wide main + narrow details | 8 + 4 |
| Form (centered) | 6 col, centered |
| Modal content | 6–8 col |

---

## Breakpoints

| Name | Min Width | Token |
|------|-----------|-------|
| `sm`  | 640px  | `--breakpoint-sm` |
| `md`  | 768px  | `--breakpoint-md` |
| `lg`  | 1024px | `--breakpoint-lg` |
| `xl`  | 1280px | `--breakpoint-xl` |
| `2xl` | 1536px | `--breakpoint-2xl` |

The portal targets **desktop-first** (primary users are company admins and partners at workstations). Mobile is secondary but must not break.

---

## Spacing in Components

### Cards

```
padding: 24px (--spacing-6)
gap between card sections: 20px (--spacing-5)
card-header bottom padding: 16px (--spacing-4)
```

### Forms

```
field vertical gap: 20px (--spacing-5)
label margin-bottom: 6px (--spacing-1 + 2px)
field group gap (inline fields): 16px (--spacing-4)
```

### Tables

```
header cell padding: 12px 16px (--spacing-3 --spacing-4)
body cell padding:   12px 16px (--spacing-3 --spacing-4)
row height (approx): 52px
```

### Buttons

```
padding (sm): 4px 12px
padding (md): 8px 16px (default)
padding (lg): 12px 24px
icon gap:      8px (--spacing-2)
```

### Modals / Dialogs

```
padding: 24px
header bottom border gap: 20px
footer top border gap: 20px
max-width: 560px (sm), 720px (md), 960px (lg)
```

### Dashboard Stat Cards

```
grid: 4 columns (desktop), 2 (tablet), 1 (mobile)
card padding: 24px
value font: 3xl / bold
label font: sm / medium / neutral-500
```

---

## Consistent Vertical Rhythm

Page anatomy (top to bottom):
1. Page heading + optional description: `mb-8` (32px)
2. Action bar (filter + buttons): `mb-6` (24px)
3. Primary content (table or card grid): —
4. Pagination or footer action: `mt-6` (24px)

Section separation within a card:
- Between sections: `20px`
- Between label and content: `6px`
- Between items in a list: `12px`
