## 1. Component

- [ ] 1.1 `frontend/src/components/date-range-filter.js`: Light-DOM component with two `<input type="date">` (labels "Von"/"Bis") + "Zurücksetzen" button; emits `range-change` `{ from, to }` (ISO string or null per side); reset clears both and emits; register in `components/index.js`; minimal CSS in `base.css`

## 2. Dashboards

- [ ] 2.1 `company-dashboard-page.js`: fetch `/company/orders` alongside `/company/dashboard`; on `range-change` recompute metric cards (Umsatz/Provision/abgeschlossene Bestellungen — fulfilled orders with `from <= orderDate <= to`, open sides unbounded) + settlement rows (group by partner, zero rows for partners without matches, names from the server settlement list); Partner-count card stays server value; reset → server values
- [ ] 2.2 `partner-dashboard-page.js`: fetch `/partner/orders` alongside `/partner/dashboard`; on `range-change` recompute Provision/Umsatz/abgeschlossene Bestellungen (fulfilled in range) + "Letzte Bestellungen" (5 newest any-status in range); Freigeschaltete-Produkte card stays server value; reset → server values

## 3. Verification

- [ ] 3.1 Browser-verify (both roles): unbounded/reset view identical to server numbers; a range covering only one order shows exactly that order's value/commission; a range with no orders shows 0,00-values and zero rows/empty list; one-sided ranges work; settlement search still works on filtered rows
- [ ] 3.2 Full backend test suite green (no backend changes — sanity run)
