// Self-check for presetPeriod: `node tools/check_dashboard_presets.mjs`
import assert from "node:assert/strict";
import { presetPeriod, toLocalIsoDate } from "../frontend/src/lib/dashboard-data.js";

const iso = (kind, y, m, d) => {
  const p = presetPeriod(kind, new Date(y, m - 1, d, 15, 30));
  return [toLocalIsoDate(p.from), toLocalIsoDate(p.to)];
};

assert.deepEqual(iso("week", 2026, 10, 18), ["2026-10-12", "2026-10-18"]); // Sunday
assert.deepEqual(iso("week", 2026, 10, 12), ["2026-10-12", "2026-10-12"]); // Monday
assert.deepEqual(iso("week", 2027, 1, 1), ["2026-12-28", "2027-01-01"]); // week across New Year
assert.deepEqual(iso("month", 2026, 11, 1), ["2026-11-01", "2026-11-01"]); // 1st of month
assert.deepEqual(iso("year", 2027, 1, 1), ["2027-01-01", "2027-01-01"]); // 1 January
const p = presetPeriod("month", new Date(2026, 9, 14, 8));
assert.equal(p.from.getHours(), 0);
assert.equal(p.to.getHours() * 60 + p.to.getMinutes(), 23 * 60 + 59);
assert.throws(() => presetPeriod("decade"));
console.log("presetPeriod ok");
