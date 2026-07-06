// Client-side dashboard aggregation. The order-list endpoints already return
// every order with orderDate/orderValue/commissionValue/status, so the dashboards
// compute period totals + charts here instead of relying on backend fields that
// don't exist (design: integrate-frontend-design). orderDate is ISO "YYYY-MM-DD",
// so range checks are plain string comparisons (chronological == lexicographic).

const MONTHS_DE = ["Jän", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"];

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
    revenue: fulfilled.reduce((sum, o) => sum + o.orderValue, 0),
    commission: fulfilled.reduce((sum, o) => sum + (o.commissionValue ?? 0), 0),
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
