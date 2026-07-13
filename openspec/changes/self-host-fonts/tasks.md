## 1. Vendor the fonts

- [ ] 1.1 Download Plus Jakarta Sans (400/500/600/700) and Fira Code (400/500) as woff2 into `frontend/vendor/fonts/`; include the OFL license file.
- [ ] 1.2 Replace the Google-Fonts `@import` in `frontend/src/styles/base.css` with `@font-face` declarations (`font-display: swap`) pointing at the vendored files; leave the `--font-*` tokens unchanged.

## 2. Verify

- [ ] 2.1 With external network blocked, open every main page — fonts render correctly and the browser makes no request to an external font host (check DevTools Network).
- [ ] 2.2 Visual spot-check: body text, headings, and monospace amounts look identical to before.
