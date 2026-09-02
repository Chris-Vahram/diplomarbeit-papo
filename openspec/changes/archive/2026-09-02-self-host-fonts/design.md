## Context

`base.css` line 8 does `@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Fira+Code:wght@400;500&display=swap')`. `--font-mono` already resolves to `'Fira Code', …`. No build step or bundler exists; CSS is served static.

## Goals / Non-Goals

**Goals:** all fonts served locally; zero external font requests; no visible typography change.

**Non-Goals:** changing the type scale or families; adding a font-build pipeline; subsetting fonts (the full weights are small enough).

## Decisions

- Download the exact weights already used — Plus Jakarta Sans 400/500/600/700 and Fira Code 400/500 — as woff2 into `frontend/vendor/fonts/`, and declare them with `@font-face` (`font-display: swap`) at the top of `base.css`, replacing the `@import`. woff2 only (universally supported in the target browsers); no woff/ttf fallbacks needed for a demo.
- Keep the `--font-*` token lines byte-identical so nothing downstream changes.

## Risks / Trade-offs

- Binary assets in the repo (~150–300 KB). Accepted — the offline demo/defense requirement outweighs repo size.
- Font-file licensing: both are OFL (open font license), redistribution allowed — include the license file alongside the woff2s.
