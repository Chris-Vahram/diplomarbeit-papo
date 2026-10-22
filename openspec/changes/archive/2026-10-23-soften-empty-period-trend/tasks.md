## 1. Implementation

- [x] 1.1 `trendInfo`: current 0 and previous > 0 returns a neutral trend with "Noch keine Abschlüsse im Zeitraum"
- [x] 1.2 `metric-card`: neutral trend label in secondary text, neutral icon (no arrow)

## 2. Verification

- [x] 2.1 Self-check of `trendInfo` cases (node), `node --check`, `git diff --check`, `openspec validate soften-empty-period-trend --strict`, browser check of both dashboards
