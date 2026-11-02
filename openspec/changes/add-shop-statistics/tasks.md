## 1. Backend (essential)

- [x] 1.1 Aggregate table and upsert counting; referrer-domain and device-class reduction
- [x] 1.2 Public batched event endpoint with validation, rate limit, bot filter
- [x] 1.3 Consent-based unique/returning counting with discard after aggregation
- [x] 1.4 Partner statistics endpoint incl. previous period; tests incl. "no IP stored"

## 2. Frontend (essential)

- [ ] 2.1 Shop event sender respecting consent
- [ ] 2.2 Page "Webshop-Statistik": period select, KPIs with comparison, chart, top articles, referrers, devices

## 3. Verification

- [ ] 3.1 Tests + package; `git diff --check`
- [ ] 3.2 Browser: consent declined/accepted flows; statistics page desktop/mobile
- [ ] 3.3 Update traceability
