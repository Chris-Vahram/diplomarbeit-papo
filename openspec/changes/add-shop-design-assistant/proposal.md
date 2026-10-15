## Why

Partners shall design their shop freely (free colours) yet without technical knowledge. Instead of restricting choices, a rule-based assistant guides them: it points out problems such as poor contrast and explains why they matter (`Specification/ENDKUNDENSHOP_DECISIONS.md` §4, decided 2026-10-15: rule-based, no AI; three severities; missing alt text as an emphatic warning).

## What Changes

- Rule engine with individual, extensible check rules evaluated on page and theme drafts.
- Three severities: **Tipp** (suggestion), **Warnung** (publish allowed after confirmation), **Blocker** (publish refused; only functional or legal problems).
- First rule set: contrast (WCAG AA 4.5:1 warning, below 3:1 blocker), missing alt text (emphatic warning), empty page, missing logo, Startseite without articles, image too small/too large, missing SEO title, empty required block field (blocker), missing mandatory page content (blocker).
- Assistant panel in the builder with plain-language explanation and a "Zum Problem springen" action; hints inline at the affected block.
- Server re-checks blockers on publish; warnings require explicit confirmation.
- Not included: AI-generated suggestions (possible later as an additional rule).

## Capabilities

### New Capabilities

- `shop-design-assistant`: Rule set, severities, explanations, publish gating.

### Modified Capabilities

None (the builder integrates the assistant; publish gating is specified here).

## Impact

Shared rule definitions: client-side for live feedback, server-side for blockers on publish (contrast and required-field rules implemented in both). Frontend: assistant panel and inline markers in the builder. Depends on `add-shop-builder-editor`.
