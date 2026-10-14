// Client-side dashboard aggregation. The order-list endpoints already return
// every order with orderDate/orderValue/commissionValue/status, so the dashboards
// compute period totals + charts here instead of relying on backend fields that
// don't exist (design: integrate-frontend-design). orderDate is ISO "YYYY-MM-DD",
// so range checks are plain string comparisons (chronological == lexicographic).

import { sumCents } from "./money.js";

const MONTHS_DE = ["Jän", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"];

export function toLocalIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function inPeriod(isoDate, fromIso, toIso) {
  return (!fromIso || isoDate >= fromIso) && (!toIso || isoDate <= toIso);
}

export function filterByPeriod(orders, fromIso, toIso) {
  return orders.filter((o) => inPeriod(o.orderDate, fromIso, toIso));
}

// Fulfilled-only totals — the dashboard convention (only fulfilled orders count
// toward revenue/commission/order-count).
export function fulfilledTotals(orders) {
  const fulfilled = orders.filter((o) => o.status === "FULFILLED");
  return {
    revenue: sumCents(fulfilled.map((o) => o.orderValue)),
    commission: sumCents(fulfilled.map((o) => o.commissionValue ?? 0)),
    count: fulfilled.length,
  };
}

// The last `count` calendar months up to today (trailing trend, independent of
// the period-picker) as { labels: ["Mai","Jun",…], keys: ["2026-05",…] }.
export function trailingMonths(count) {
  const now = new Date();
  const labels = [];
  const keys = [];
  for (let i = count - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    keys.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`);
    labels.push(MONTHS_DE[d.getMonth()]);
  }
  return { labels, keys };
}

// Fulfilled revenue summed per month key, aligned to `keys` (0 for empty months).
export function monthlyRevenue(orders, keys) {
  const byKey = Object.fromEntries(keys.map((k) => [k, 0]));
  for (const o of orders) {
    if (o.status !== "FULFILLED") continue;
    const key = (o.orderDate ?? "").slice(0, 7);
    if (key in byKey) byKey[key] += o.orderValue;
  }
  return keys.map((k) => byKey[k]);
}

// Calendar presets for the period picker, each running up to today (local time,
// week starts Monday). Returns the same { from, to } shape as a custom range.
export const PERIOD_PRESETS = { week: "Diese Woche", month: "Dieser Monat", year: "Dieses Jahr" };

export function presetPeriod(kind, now = new Date()) {
  const y = now.getFullYear();
  const m = now.getMonth();
  const d = now.getDate();
  const to = new Date(y, m, d, 23, 59, 59, 999);
  if (kind === "week") return { from: new Date(y, m, d - ((now.getDay() + 6) % 7)), to };
  if (kind === "month") return { from: new Date(y, m, 1), to };
  if (kind === "year") return { from: new Date(y, 0, 1), to };
  throw new Error(`Unbekannter Zeitraum: ${kind}`);
}

// The immediately preceding period of the same length as [from, to] — for the
// metric-card "up/down vs. previous period" arrows. Non-overlapping: ends the
// instant before `from`.
export function previousPeriod(from, to) {
  const lengthMs = to.getTime() - from.getTime();
  const prevTo = new Date(from.getTime() - 1);
  const prevFrom = new Date(prevTo.getTime() - lengthMs);
  return { from: prevFrom, to: prevTo };
}

// Trailing-window trend (last `days` vs. the `days` before that), for the
// metric-card arrows when no explicit period is picked. Independent of the
// headline totals, which stay the backend's all-time values.
export function trailingWindowTrend(orders, days = 30) {
  const now = new Date();
  const to = now;
  const from = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
  const prev = previousPeriod(from, to);

  const current = fulfilledTotals(filterByPeriod(orders, toLocalIsoDate(from), toLocalIsoDate(to)));
  const previous = fulfilledTotals(filterByPeriod(orders, toLocalIsoDate(prev.from), toLocalIsoDate(prev.to)));

  return {
    revenueTrend: trendInfo(current.revenue, previous.revenue),
    commissionTrend: trendInfo(current.commission, previous.commission),
    countTrend: trendInfo(current.count, previous.count),
  };
}

// metric-card's trend="up"|"down" + trend-label props from a current vs.
// previous-period value. null when there's nothing meaningful to compare
// (both zero) — the caller then omits the trend attributes and gets no arrow.
export function trendInfo(current, previous) {
  if (previous === 0) {
    if (current === 0) return null;
    return { trend: "up", label: "ggü. Vorperiode (neu)" };
  }
  const pct = ((current - previous) / previous) * 100;
  const trend = pct >= 0 ? "up" : "down";
  const sign = pct >= 0 ? "+" : "";
  const pctStr = pct.toFixed(1).replace(".", ",");
  return { trend, label: `${sign}${pctStr}% ggü. Vorperiode` };
}
