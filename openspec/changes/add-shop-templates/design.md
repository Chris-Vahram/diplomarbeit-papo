## Context

Template format and default templates exist since `add-shop-page-renderer`; the builder exists since `add-shop-builder-editor`. Tobias designs the templates.

## Goals / Non-Goals

**Goals:** 2–3 polished templates per mandatory page type; data-only extensibility.

**Non-Goals:** template marketplace, company-specific templates, new block types (separate change if needed).

## Decisions

1. **Templates as JSON resource files with a manifest (`index.json`: id, page kind, name, description, thumbnail)**; loaded and validated at startup with the same block validators as drafts.
2. **Thumbnails as static WebP files** generated from the renderer during design; no runtime screenshotting.
3. **Quality gate as automated test** running assistant rules (server blocker rules + a JS test for warnings) over every template.

## Risks / Trade-offs

- [Templates need block types not yet available] → new block types go into a separate change first.

## Migration Plan

Additive resource files.

## Open Questions

- Visual direction of each template (Tobias).
