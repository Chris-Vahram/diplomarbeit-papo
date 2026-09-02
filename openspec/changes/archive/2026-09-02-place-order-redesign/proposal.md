## Why

The partner order-creation page (`partner-order-new-page.js`) works but reads as a single long, dense scroll: a top section for Endkunde selection whose "neu anlegen" form pops in and pushes everything else down, followed immediately by a product-search grid and an easy-to-miss cart panel with no heading in its empty state. Partners are frequently first-time or infrequent digital-tool users, and the PO has asked for the flow to feel guided and rewarding, not just functional — "almost fun to use." A step-by-step wizard with a visible progress indicator and small completion animations addresses both problems: it breaks the page into one focused decision at a time, and it gives the partner positive feedback (checkmarks, a confetti moment on success) as they move through it.

## What Changes

- Replace the single-scroll partner order-creation page with a 5-checkpoint, full-content-area wizard: **Endkunde wählen → (Neuer Endkunde, conditional) → Produkte wählen → Übersicht → Erfolg**. App sidebar/topbar chrome stays visible; only the page content area becomes the wizard.
- Add a horizontal progress bar with clickable checkpoints above the step content. Checkpoints for already-visited steps are clickable to jump back; the "Neuer Endkunde" checkpoint is shown but greyed/skipped when the partner picks an existing Endkunde instead. Forward navigation past an incomplete step stays blocked.
- Wizard state (Endkunde selection/draft, cart items) lives in one JS state object per page instance, not in the DOM — each step renders fresh from it, so moving backward/forward never loses entered data.
- After finishing the "Neuer Endkunde" form, the partner is taken directly to "Produkte wählen" — **not** back to the Endkunde-selection step. The just-entered Endkunde becomes the selected one for this order (shown as such if the partner navigates back to step 1).
- "Bestellung aufgeben" stays the call-to-action on the Übersicht step (no separate "senden" checkpoint); submitting shows an inline spinner/disabled state on the button while the request is in flight, then transitions straight to the Erfolg step.
- Add small completion feedback: a per-step "done" animation on the progress bar and a confetti-style celebration on the Erfolg step, both skipped/simplified when the browser reports `prefers-reduced-motion: reduce` (not currently handled anywhere in `base.css`).
- Reuses the existing product catalog/cart building blocks (`order-cart.js`) and Endkunde-picker/new-Endkunde form (`order-form.js`) inside the new step shell rather than rewriting their internals; the order submission API contract is unchanged.
- **BREAKING** (internal, not user-facing): `partner-order-new-page.js`'s DOM structure and rendering flow change completely; any styling that targeted the old flat layout (`.order-cart-layout` top-level placement, `.entity-form` as the whole page) no longer applies as-is to this page.
- Non-goal / explicitly out of scope: the company's manual order-creation form (`company-order-list-page.js`, built on the same `order-form.js` helpers) is **not** touched — company staff are internal power users and keep the current dense form. No literal game mechanics (points, badges, streaks) — "gamify" here means guided steps + motion feedback only, confirmed with the user.

## Capabilities

### New Capabilities
- `partner-order-wizard`: the stepped, checkpoint-driven order-creation experience for partners — step sequence, state/back-navigation behavior, per-step validation gating, and the completion/motion feedback (progress animation, confetti, reduced-motion fallback).

### Modified Capabilities
(none — order submission itself, its validation rules, and the backend contract are unchanged; this is a frontend presentation/flow change on top of existing `order-management` behavior)

## Impact

- `frontend/src/pages/partner-order-new-page.js` — rewritten to host the wizard/step-controller instead of one flat render.
- `frontend/src/lib/order-form.js`, `frontend/src/lib/order-cart.js` — reused for their existing Endkunde-picker, new-Endkunde form, product catalog, and cart rendering/wiring; called per-step instead of all-at-once. No change to `company-order-list-page.js`'s usage of `order-form.js`.
- `frontend/src/styles/base.css` — new styles for the progress/checkpoint bar, step transitions, completion/confetti animation, and a `prefers-reduced-motion` fallback (new to this file).
- No backend changes: `POST /partner/orders` request/response shape is unchanged, so `OrderService`, `OrderRequest`, and Kunde-creation-via-order-payload behavior stay as-is.
- No changes to the `order-management` capability spec (backend-facing) or to any other page/role.
