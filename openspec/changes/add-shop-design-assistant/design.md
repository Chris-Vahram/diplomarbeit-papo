## Context

Free colour choice replaced fixed palettes; guidance must come from an assistant (`Specification/ENDKUNDENSHOP_DECISIONS.md` §4). Rule-based only: no AI service, consistent with the project's data-sovereignty argument and the fixed stack.

## Goals / Non-Goals

**Goals:** immediate, understandable guidance; hard stop only for functional/legal problems; server-enforced blockers.

**Non-Goals:** AI suggestions, automatic fixing, full WCAG audit.

## Decisions

1. **Rules as plain JS modules (`frontend/src/shop-editor/assistant/rules/*.js`) with `{id, severity, scope, check(ctx), explain}`**, run on every draft change. They live with the editor because the public shop never runs them; only the contrast formula (`frontend/src/shop/contrast.js`) is shared with the renderer. Alternative: server-only checks — rejected, feedback would lag.
2. **Server mirrors only blocker rules (contrast < 3:1, required fields, mandatory content)** in Java, executed on publish. Required fields and the mandatory Artikeldetail block were already enforced by `ShopBlockValidator`; `ShopDesignRules` adds the contrast blockers. A page is checked with the published design (what visitors see), the design with all online pages. Warnings/Tipps stay client-side; the confirmation is a dialog only and is not sent to the server, which has nothing to verify it against.
3. **Contrast via WCAG relative luminance formula**, shared test vectors in both implementations to keep results identical (`tools/fixtures/shop-contrast-vectors.json`, checked by `ShopContrastTest` and `tools/check_shop_assistant.mjs`). Text/background pairs mirror `shop.css`; failing pairs that one colour fixes (text and secondary text) form one finding, and its suggestion makes all of them readable. A block reports only pairs that differ from the design's, so a bad design colour is reported once.
4. **Findings keyed by block id** so inline markers and the panel stay in sync.
5. **Placement:** the inspector (right column) shows the page's findings while no block is selected and the selected block's findings above its fields; the Design tab lists the design's findings under the colours. A status button next to "Veröffentlichen" summarises and opens the page list. The left sidebar has no room for a third tab.
6. **Logo:** the shop had no logo; the Hero block gets an optional image field `logoId` (decided by Tobias 26.10.2026), so the "missing logo" Tipp has something to point to.

## Risks / Trade-offs

- [Client and server contrast results differ] → shared test vectors.
- [Too many hints annoy] → Tipps collapsed by default; Blocker and Warnung first.

## Migration Plan

Additive; no data changes.

## Open Questions

None.
