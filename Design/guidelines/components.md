# Component Patterns

All components use tokens from `variables.css`. The `.btn`, `.input`, `.card`, `.table`, `.badge`, `.alert` utility classes are included.

---

## Buttons

### Variants

| Variant | Token class | Use |
|---------|------------|-----|
| Primary | `.btn .btn-primary` | Main CTA — **context-aware**: green in `.context-company`, blue in `.context-partner`. This is the only button class you need on either side; it reads `var(--color-accent)`, never a role color directly. |
| Secondary | `.btn .btn-secondary` | Cancel, back, secondary actions |
| Ghost | `.btn .btn-ghost` | Toolbar actions, low-priority |
| Danger | `.btn .btn-danger` | Destructive actions (delete, deactivate) |
| Partner (explicit) | `.btn .btn-partner` | **Rare override, not a normal role button.** Always renders partner-blue regardless of context — only for a partner-owned action surfaced on a company screen. If you're building an ordinary partner-side CTA, use `.btn-primary`, not this. |

### Sizes

| Size | Class | Height |
|------|-------|--------|
| Small | `.btn .btn-sm` | 32px |
| Default | `.btn` | 40px |
| Large | `.btn .btn-lg` | 48px |

### States

```
Default → Hover → Active → Disabled (opacity: 0.45)
Focus:   box-shadow: var(--shadow-focus-ring)
```

### Button with icon

```html
<button class="btn btn-primary">
  <svg width="16" height="16">...</svg>
  Import Partners
</button>
```

Icon size: 16px for default buttons, 14px for small, 18px for large.

---

## Form Controls

### Text Input

```html
<div class="form-field">
  <label class="label label-required" for="company-name">Company Name</label>
  <input class="input" id="company-name" type="text" placeholder="e.g. PAPO GmbH" />
  <span class="field-hint">Displayed as the partner's official name</span>
</div>
```

### Select

```html
<select class="input">
  <option value="">Select status...</option>
  <option value="active">Active</option>
  <option value="inactive">Inactive</option>
</select>
```

### Textarea

```html
<textarea class="input" rows="4" placeholder="Product description..."></textarea>
```

### Error state

```html
<input class="input input-error" ... />
<span class="field-error">This field is required.</span>
```

### Commission Dropdown (special case — FR-19)

```html
<select class="input" name="commission">
  <option value="5">5%</option>
  <option value="10">10%</option>
  <option value="15">15%</option>
  <option value="custom">Custom...</option>
</select>
```

When "Custom" selected, reveal a number input inline.

---

## Cards

### Basic card

```html
<div class="card">
  <h2 class="text-xl font-semibold mb-4">Card Title</h2>
  <p class="text-sm text-secondary">Supporting description.</p>
</div>
```

### Card with header and footer

```html
<div class="card" style="padding: 0;">
  <div class="card-header">
    <h3 class="text-lg font-semibold">Section Title</h3>
  </div>
  <div class="card-body">
    <!-- content -->
  </div>
  <div class="card-footer">
    <button class="btn btn-primary">Save</button>
    <button class="btn btn-secondary">Cancel</button>
  </div>
</div>
```

### Dashboard stat card

```html
<div class="card">
  <p class="text-xs font-semibold text-secondary" style="text-transform:uppercase; letter-spacing:0.05em;">
    Total Commission
  </p>
  <p class="text-4xl font-bold mt-2">€ 1,240.00</p>
  <p class="text-sm text-secondary mt-1">This month</p>
</div>
```

Stat card grid: 4 columns at ≥ 1024px, 2 at ≥ 640px, 1 below.

```css
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-6);
}
@media (max-width: 1023px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 639px)  { .stats-grid { grid-template-columns: 1fr; } }
```

---

## Data Tables

```html
<div class="table-wrapper">
  <table class="table">
    <thead>
      <tr>
        <th>Partner Name</th>
        <th>Status</th>
        <th>Commission</th>
        <th>Orders</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="font-medium">ACME Vertrieb GmbH</td>
        <td><span class="badge badge-active">Active</span></td>
        <td class="font-mono">10%</td>
        <td>24</td>
        <td>
          <button class="btn btn-ghost btn-sm">Edit</button>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

### Table action bar (above table)

```html
<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--spacing-4);">
  <input class="input" style="max-width:280px;" placeholder="Search partners..." />
  <div style="display:flex; gap:var(--spacing-2);">
    <button class="btn btn-secondary">Filter</button>
    <button class="btn btn-primary">Import</button>
  </div>
</div>
```

---

## Status Badges

```html
<!-- Order statuses (FR-15) -->
<span class="badge badge-submitted">Submitted</span>
<span class="badge badge-reviewed">Reviewed</span>
<span class="badge badge-fulfilled">Fulfilled</span>

<!-- Partner statuses (FR-23) -->
<span class="badge badge-active">Active</span>
<span class="badge badge-inactive">Inactive</span>
```

Badges are always `text-transform: uppercase` with `letter-spacing: 0.1em`. Never use color alone — the text label is required.

---

## Alerts / Feedback

```html
<!-- Save success -->
<div class="alert alert-success">
  <svg>...</svg>
  Partner saved successfully.
</div>

<!-- Validation error -->
<div class="alert alert-error">
  <svg>...</svg>
  Please fill in all required fields before submitting.
</div>

<!-- Inactive partner warning -->
<div class="alert alert-warning">
  <svg>...</svg>
  This partner is inactive and cannot place new orders.
</div>

<!-- Import info -->
<div class="alert alert-info">
  <svg>...</svg>
  Import complete: 192 rows accepted, 8 rows rejected. See validation report below.
</div>
```

---

## Navigation — Sidebar

```
bg: var(--color-neutral-950)
width: 240px
padding: 16px 12px

.nav-section-label:
  font-size: 11px
  font-weight: 600
  text-transform: uppercase
  letter-spacing: 0.08em
  color: var(--color-neutral-500)
  padding: 8px 12px
  margin-top: 20px

.nav-item:
  display: flex
  align-items: center
  gap: 12px
  height: 44px
  padding: 0 12px
  border-radius: var(--radius-md)
  font-size: 14px
  font-weight: 500
  color: var(--color-neutral-300)
  cursor: pointer
  transition: background 100ms ease

.nav-item:hover:
  background: rgba(255, 255, 255, 0.07)
  color: white

.nav-item.active:
  background: var(--color-accent)    /* primary-500 in .context-company, accent-600 in .context-partner */
  color: white
```

### Company sidebar nav items

```
Dashboard        /company/dashboard
Partners         /company/partners
Products         /company/products
Product Releases /company/enablements
Orders           /company/orders
Import           /company/import
```

### Partner sidebar nav items

```
Dashboard        /partner/dashboard
My Products      /partner/products
New Order        /partner/orders/new
My Orders        /partner/orders
My Profile       /partner/profile
Customers        /partner/customers
```

---

## Modals / Dialogs

```css
.modal-backdrop {
  position: fixed; inset: 0;
  background: var(--bg-overlay);
  z-index: var(--z-overlay);
  display: flex; align-items: center; justify-content: center;
}
.modal {
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-6);
  width: 100%; max-width: 560px;
  animation: modal-enter var(--duration-slow) var(--ease-out);
}
@keyframes modal-enter {
  from { opacity: 0; transform: scale(0.96) translateY(8px); }
  to   { opacity: 1; transform: scale(1)    translateY(0);   }
}
```

Modal anatomy:
1. **Header** — title (heading-md) + close button (ghost icon btn)
2. **Body** — content, form
3. **Footer** — action buttons (right-aligned, primary last)

---

## Order Confirmation Screen (FR-16)

Full-page confirmation after order submit. Not a modal.

```
┌─────────────────────────────────────┐
│  ✓  Order submitted successfully    │  (success-600 icon, heading-md)
│                                     │
│  Order #PP-2024-0042                │  (font-mono, text-secondary)
│  Kunde: Müller, Max                 │
│  Partner: ACME Vertrieb GmbH        │
│  Date: 2026-07-02                   │
│                                     │
│  ── Order Lines ──────────────────  │
│  Article A   × 2   € 48.00          │
│  Article B   × 1   € 24.00          │
│  ─────────────────────────────────  │
│  Total:              € 72.00        │  (font-bold)
│  Commission (10%):   €  7.20        │  (text-secondary)
│                                     │
│  [ Place Another Order ]  [ Orders ]│
└─────────────────────────────────────┘
```

---

## Simulated E-mail (FR-17)

Rendered in-app in a styled "e-mail preview" box:

```css
.email-preview {
  background: var(--color-neutral-50);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  padding: var(--spacing-6);
  font-family: var(--font-sans);
  max-width: 640px;
}
.email-preview .email-header {
  border-bottom: 2px solid var(--color-accent);
  padding-bottom: var(--spacing-4);
  margin-bottom: var(--spacing-6);
}
```

---

## Icons

Use **Lucide Icons** (MIT license, tree-shakeable, consistent stroke style).

- Size in buttons/labels: **16px**
- Size in nav items: **20px**
- Size in stat cards / empty states: **32px–48px**
- Stroke width: **1.5px** (Lucide default)
- Color: inherit from text, or explicit token

Recommended icons:
| Action / Status | Icon name |
|-----------------|-----------|
| Partner         | `users` |
| Product/Article | `package` |
| Order           | `shopping-cart` |
| Commission      | `percent` |
| Dashboard       | `layout-dashboard` |
| Import          | `upload` |
| Edit            | `pencil` |
| Delete          | `trash-2` |
| Enable          | `toggle-right` |
| Success         | `check-circle` |
| Warning         | `alert-triangle` |
| Error           | `x-circle` |
| Info            | `info` |
| Search          | `search` |
| Filter          | `filter` |
| Customer        | `user` |
| Profile         | `circle-user` |
| Settings        | `settings` |
| Logout          | `log-out` |

---

## Empty States

When a table has no rows or a section has no data:

```html
<div style="
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-16) var(--spacing-8);
  text-align: center;
  color: var(--text-secondary);
">
  <svg width="48" height="48" style="color:var(--color-neutral-300); margin-bottom:var(--spacing-4);">
    <!-- e.g. package icon for no products -->
  </svg>
  <p class="text-base font-medium text-primary">No products enabled yet</p>
  <p class="text-sm text-secondary mt-2">Enable products from the Company Admin to start ordering.</p>
</div>
```

---

## Import Validation Report (FR-04)

After CSV/Excel import, render a structured result:

```
┌─ Import Report ──────────────────────────────────┐
│  ✓ 192 rows accepted                             │  (success)
│  ✗   8 rows rejected                             │  (error)
│                                                  │
│  Rejected rows:                                  │
│  ┌──────┬──────────┬────────────────────────┐    │
│  │ Row  │ Field    │ Reason                 │    │
│  ├──────┼──────────┼────────────────────────┤    │
│  │  12  │ email    │ Invalid format         │    │
│  │  47  │ name     │ Required field missing │    │
│  └──────┴──────────┴────────────────────────┘    │
└──────────────────────────────────────────────────┘
```

Row number in `.font-mono`. Use `.badge-fulfilled` / `.badge-submitted` style colors.
