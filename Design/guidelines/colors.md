# Color System

## Brand Philosophy

Two-tone role separation: **Green** for company/admin context, **Blue** for partner context. Both use the same neutral shell. Color signals *who* the user is without explaining it.

One mechanism only: apply `.context-company` or `.context-partner` on the app shell root. Every role-aware component reads the generic `--color-accent` / `--color-accent-hover` / `--color-accent-active` tokens (defined in `variables.css`) — never `--color-primary-*` or `--color-accent-*` directly. If a component hardcodes a role palette instead of going through `--color-accent`, that's a bug, not a variant.

---

## Palettes

### Primary Green — Company / Admin context

| Token | Hex | Use |
|-------|-----|-----|
| `--color-primary-50`  | `#F0F7F1` | Tint background, highlight surfaces |
| `--color-primary-100` | `#D8EEDB` | Badge backgrounds, hover highlights |
| `--color-primary-200` | `#B4DBB8` | — |
| `--color-primary-300` | `#8DC492` | Decorative, illustrations |
| `--color-primary-400` | `#78B07E` | — |
| **`--color-primary-500`** | **`#689C6D`** | **Core company accent — CTAs, links, active nav (via `--color-accent` in `.context-company`)** |
| `--color-primary-600` | `#578462` | Hover state |
| `--color-primary-700` | `#456851` | Active/pressed state |
| `--color-primary-800` | `#344E3D` | Deep surfaces |
| `--color-primary-900` | `#223529` | Deep text on light backgrounds |
| `--color-primary-950` | `#111A14` | Darkest |

### Accent Blue — Partner context

| Token | Hex | Use |
|-------|-----|-----|
| `--color-accent-50`  | `#EFF5F6` | Tint background |
| `--color-accent-100` | `#D4E8EB` | Badge backgrounds, hover highlights |
| `--color-accent-200` | `#B4D4DA` | — |
| `--color-accent-300` | `#9AC2C9` | Decorative |
| `--color-accent-400` | `#8CACB3` | — |
| **`--color-accent-600`** | **`#5E838D`** | **Core partner accent — CTAs, links, active nav (via `--color-accent` in `.context-partner`)** |
| `--color-accent-700` | `#4A6870` | Hover state |
| `--color-accent-800` | `#374E54` | Active/pressed state |
| `--color-accent-900` | `#243438` | Deep text |

Note the step offset: company's core step is `500` (hover `600`, active `700`); partner's core step is `600` (hover `700`, active `800`). This is intentional — it's how `variables.css` derives the same visual weight from ramps of different length, not an inconsistency. Don't "fix" it by renumbering.

### Secondary Teal — supporting elements (not a role color)

| Token | Hex | Use |
|-------|-----|-----|
| `--color-secondary-300` | `#ABC7C4` | Core secondary — backgrounds, dividers, non-CTA supporting UI |
| `--color-secondary-600` | `#598C89` | Stronger supporting surfaces |

**This ramp is not tied to company or partner.** It exists for decorative/supporting surfaces that should look calm in *either* context. Do not reach for `secondary` when you mean the partner role color (`accent`) — that mix-up is the most common source of "wrong color in the wrong area" bugs.

### Neutrals (green-tinted slate)

| Token | Hex | Use |
|-------|-----|-----|
| `--color-neutral-50`  | `#F6F9F7` | Page background |
| `--color-neutral-100` | `#EDF2EE` | Table headers, disabled bg |
| `--color-neutral-200` | `#D6E1D8` | Borders (default) |
| `--color-neutral-300` | `#B5C8B9` | Input borders, strong borders |
| `--color-neutral-400` | `#8CA594` | Placeholder / disabled text |
| `--color-neutral-500` | `#69836F` | Secondary text (icons) |
| `--color-neutral-600` | `#52675A` | Secondary text (body) |
| `--color-neutral-700` | `#3D4F43` | Labels, form text |
| `--color-neutral-800` | `#2A362E` | Sidebar background tint |
| `--color-neutral-900` | `#182019` | Headings |
| `--color-neutral-950` | `#0B110C` | Primary text |

---

## Semantic Colors

Semantic colors are shared identically by both roles — a status must read the same regardless of who's looking at it.

### Status Badges — Orders

| Status | Background | Text | Meaning |
|--------|-----------|------|---------|
| `submitted`  | `#FEF3C7` (warning-100) | `#92400E` (warning-800) | Order placed, awaiting review |
| `reviewed`   | `#D4E8EB` (info-100, = accent-100) | `#374E54` (info-800, = accent-800) | Company has reviewed |
| `fulfilled`  | `#D0FAE5` (success-100) | `#166534` (success-800) | Shipped to end customer |

### Status Badges — Partners

| Status | Background | Text |
|--------|-----------|------|
| `active`   | `#ECFDF4` (success-50) | `#15803D` (success-700) |
| `inactive` | `#EDF2EE` (neutral-100) | `#69836F` (neutral-500) |

### Feedback Colors

| Semantic | 500 | 100 (bg) | 800 (text) |
|----------|-----|----------|------------|
| Success  | `#22C55E` | `#D0FAE5` | `#166534` |
| Warning  | `#F59E0B` | `#FEF3C7` | `#92400E` |
| Error    | `#EF4444` | `#FEE2E2` | `#991B1B` |
| Info     | `#8CACB3` (reuses accent scale) | `#D4E8EB` | `#374E54` |

`info` deliberately reuses the accent (blue) ramp — a coincidence of hue, not a role signal. Don't read "reviewed" badges as "this is partner-colored."

---

## Role Context — How to apply

```html
<!-- Company admin shell -->
<div class="app-shell context-company">
  <button class="btn btn-primary">Save Partner</button>  <!-- renders green -->
</div>

<!-- Partner shell -->
<div class="app-shell context-partner">
  <button class="btn btn-primary">Place Order</button>    <!-- renders blue -->
</div>
```

Apply the context class once, on the shell root. `--color-accent`, `--color-accent-hover`, `--color-accent-active`, `--border-focus`, `--shadow-focus-ring`, and `--text-brand` all update automatically — sidebar active state, focus rings, primary buttons, brand links. A component should never need its own company/partner branch; if it does, that's a sign it's reading the wrong token.

The one sanctioned exception is `.btn-partner` (see `components.md`) — an explicit, always-blue override for showing a partner-owned action on a company screen. It is not a second "partner CTA" class.

---

## Contrast Ratios (WCAG AA)

| Combination | Ratio | Pass AA (4.5:1 text / 3:1 UI) |
|-------------|-------|-------------------------------|
| neutral-950 on white (body text) | 19.1:1 | ✓ |
| neutral-600 on white (secondary text) | 6.1:1 | ✓ |
| primary-700 on white | 6.3:1 | ✓ |
| accent-700 on white | 6.0:1 | ✓ |
| warning-800 on warning-100 | 6.4:1 | ✓ |
| success-800 on success-100 | 6.3:1 | ✓ |
| error-800 on error-100 | 6.8:1 | ✓ |
| **white on primary-500 (default `.btn-primary` bg, company)** | **3.2:1** | **✗ fails text AA** |
| **white on primary-600 (hover)** | **4.3:1** | **✗ fails text AA (barely)** |
| **white on accent-600 (default `.btn-primary` bg, partner)** | **4.1:1** | **✗ fails text AA (barely)** |
| white on accent-700 (hover) | 6.0:1 | ✓ |

Ratios computed directly from WCAG relative-luminance formula on the actual token hex values — don't reuse ratios from an older draft of this doc without recomputing; several were wrong.

**Known issue, not yet fixed:** the default button background (`primary-500` / `accent-600`, i.e. the core "500/600" step used by `--color-accent`) does not meet 4.5:1 with white text — only the hover/active shades do. This contradicts the "Accessible… WCAG AA" claim below. Two ways to fix, pick one before relying on this for the thesis's accessibility claims:
1. Shift `.btn-primary`'s default background one step darker (`--color-accent-hover` as the resting state), or
2. Use dark text (`neutral-950`) on the default button instead of white.
Don't silently ship either — it changes the button's look either way.

---

## Do / Don't

**Do:**
- Use `var(--color-accent)` for anything that should switch between green and blue based on role — never the raw `--color-primary-*` / `--color-accent-*` step.
- Use `neutral-950` for body text on white.
- Use `neutral-600` for secondary/supporting text.
- Use semantic status colors only for their designated status, in both contexts identically.

**Don't:**
- Mix green and blue role color on the same screen (they're role signals, not decoration).
- Reach for `secondary` (teal) when you mean the partner role color (`accent`, blue) — they're visually adjacent and easy to confuse.
- Use color alone to convey information — pair with text or icon.
- Use primary-300/400 or accent-300/400 for text on white (fails contrast).
- Hardcode a hex value or a `-primary-`/`-accent-` step directly in a component's CSS when the generic `--color-accent` token would do the same job and stay context-aware.
