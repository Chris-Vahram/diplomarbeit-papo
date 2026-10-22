import { apiFetch } from "../lib/api.js";
import { formatEuro, formatNumber } from "../lib/format.js";
import { filterByPeriod, fulfilledTotals, trailingMonths, monthlyRevenue, toLocalIsoDate, presetPeriod, previousPeriod, periodTrends, trailingWindowTrend } from "../lib/dashboard-data.js";
import { escapeHtml } from "../lib/html.js";
import { renderMonthChart, periodPickerHtml, wirePeriodPicker } from "../lib/dashboard-ui.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });

class CompanyDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    // Opens on the current month (meeting 12.10.2026); "Gesamter Zeitraum" sets
    // period = null and shows the backend's exact all-time totals.
    this.periodPreset = "month";
    this.period = presetPeriod(this.periodPreset);

    const greeting = "Willkommen zurück zum";

    this.innerHTML = `
      <div class="dashboard-header">
        <div class="page-heading">
          <p class="page-heading__greeting">${greeting}</p>
          <h1>Firmen-Dashboard</h1>
        </div>
        ${periodPickerHtml()}
      </div>
      <div class="metric-row" id="metric-row"></div>
      <div class="chart-row" id="chart-row"></div>
      <h2>Provisionsabrechnung nach Partner</h2>
      <data-table id="settlement-table"></data-table>
    `;

    this.metricRow = this.querySelector("#metric-row");
    this.chartRow = this.querySelector("#chart-row");
    this.table = this.querySelector("#settlement-table");
    this.table.columns = [
      { key: "partnerName", label: "Partner", mobile: "title" },
      { key: "fulfilledOrderCount", label: "Abgeschlossene Bestellungen" },
      { key: "revenueLabel", label: "Umsatz (€)" },
      { key: "commissionLabel", label: "Provision (€)", mobile: "amount" },
    ];
    this.table.emptyMessage = "Noch keine Partner angelegt.";
    this.table.retry = () => this.load();
    this.table.searchKeys = ["partnerName"];
    this.table.pageSize = 20;

    wirePeriodPicker(this);
    this.load();
  }

  async load() {
    try {
      // /company/dashboard for the full partner roster (incl. zero-order partners);
      // /company/orders for the per-order data the charts + period filter run on.
      const [roster, orders] = await Promise.all([
        apiFetch("/company/dashboard"),
        apiFetch("/company/orders"),
      ]);
      this.roster = roster;
      this.orders = orders;
      this.render();
    } catch (error) {
      this.table.error = error.message;
    }
  }

  render() {
    if (!this.orders) return;

    let inRange, revenue, commission, count, trends;
    if (this.period) {
      const fromIso = toLocalIsoDate(this.period.from);
      const toIso = toLocalIsoDate(this.period.to);
      inRange = filterByPeriod(this.orders, fromIso, toIso);
      const current = fulfilledTotals(inRange);
      ({ revenue, commission, count } = current);
      // Same-length period immediately before, as on the partner dashboard (B-08).
      const prev = previousPeriod(this.period.from, this.period.to);
      trends = periodTrends(current, fulfilledTotals(filterByPeriod(this.orders, toLocalIsoDate(prev.from), toLocalIsoDate(prev.to))));
    } else {
      // No explicit range: headline totals are the backend's true all-time values
      // (BigDecimal-exact, no client-side recompute) - settlement/charts show all orders.
      inRange = this.orders;
      revenue = this.roster.totalRevenue;
      commission = this.roster.totalCommission;
      count = this.roster.fulfilledOrderCount;
      // Arrows: last 30 days vs. the 30 before, decoupled from the all-time totals.
      trends = trailingWindowTrend(this.orders);
    }
    const trendAttrs = (t) => (t ? `trend="${t.trend}" trend-label="${t.label}"` : "");
    const avgOrderValue = count > 0 ? revenue / count : 0;

    // Monthly chart: trailing 6 months (stable trend), independent of the picker.
    const { labels, keys } = trailingMonths(6);
    const monthly = monthlyRevenue(this.orders, keys);

    this.metricRow.innerHTML = `
      <metric-card lead label="Umsatz (abgeschlossen)" value="${formatEuro(revenue)}" icon="trending-up" tone="positive" ${trendAttrs(trends.revenueTrend)}></metric-card>
      <metric-card label="Provision (abgeschlossen)" value="${formatEuro(commission)}" icon="percent" tone="positive" ${trendAttrs(trends.commissionTrend)}></metric-card>
      <metric-card label="Abgeschlossene Bestellungen" value="${count}" icon="check-circle" tone="positive" ${trendAttrs(trends.countTrend)}></metric-card>
      <metric-card label="Ø Bestellwert" value="${formatEuro(avgOrderValue)}" icon="receipt" tone="positive" ${trendAttrs(trends.avgTrend)}></metric-card>
      ${renderMonthChart("Umsatz nach Monat", labels, monthly)}
    `;

    const settlement = this.buildSettlement(inRange);
    this.chartRow.innerHTML = this.renderCharts(settlement, revenue, commission);
    window.lucide?.createIcons();

    this.table.rows = settlement.map((s) => ({
      partnerName: s.partnerName,
      fulfilledOrderCount: s.fulfilledOrderCount,
      revenueLabel: formatNumber(s.revenue),
      commissionLabel: formatNumber(s.commission),
    }));
  }

  // Per-partner settlement over the in-range fulfilled orders. Seeds every partner
  // from the roster so partners with no orders in the period still show a zero row.
  buildSettlement(inRange) {
    const byId = new Map();
    for (const s of this.roster.settlement) {
      byId.set(s.partnerId, { partnerName: s.partnerName, fulfilledOrderCount: 0, revenue: 0, commission: 0 });
    }
    for (const o of inRange) {
      if (o.status !== "FULFILLED") continue;
      let row = byId.get(o.partnerId);
      if (!row) {
        row = { partnerName: o.partnerName, fulfilledOrderCount: 0, revenue: 0, commission: 0 };
        byId.set(o.partnerId, row);
      }
      row.fulfilledOrderCount += 1;
      row.revenue += o.orderValue;
      row.commission += o.commissionValue ?? 0;
    }
    return [...byId.values()];
  }

  renderCharts(settlement, totalRevenue, totalCommission) {
    const topPartners = [...settlement].sort((a, b) => b.revenue - a.revenue).slice(0, 5);
    const maxRevenue = Math.max(1, ...topPartners.map((p) => p.revenue));

    const bars = topPartners
      .map(
        (p, i) => `
        <div class="bar-chart__row${i === 0 ? " bar-chart__row--leader" : ""}">
          <span class="bar-chart__rank">${i === 0 ? '<i data-lucide="trophy"></i>' : ""}</span>
          <span class="bar-chart__label" title="${escapeHtml(p.partnerName)}">${escapeHtml(p.partnerName)}</span>
          <span class="bar-chart__track"><span class="bar-chart__fill" style="width:${((p.revenue / maxRevenue) * 100).toFixed(1)}%"></span></span>
          <span class="bar-chart__value">${eur.format(p.revenue)}</span>
        </div>`,
      )
      .join("");

    const rate = totalRevenue > 0 ? (totalCommission / totalRevenue) * 100 : 0;

    return `
      <div class="chart-card">
        <h3 class="chart-card__title">Umsatz nach Partner (Top ${topPartners.length})</h3>
        <div class="bar-chart">${bars}</div>
      </div>
      <div class="chart-card">
        <h3 class="chart-card__title">Provisionsquote</h3>
        <div class="donut-ring__wrap">
          <div class="donut-ring__row">
            <div class="donut-ring" style="--pct:${rate.toFixed(1)}">
              <span class="donut-ring__label">${rate.toFixed(1)}%</span>
            </div>
            <p class="donut-ring__figure">${eur.format(totalCommission)} von ${eur.format(totalRevenue)}</p>
          </div>
          <p class="donut-ring__caption">Anteil der Provision am abgeschlossenen Gesamtumsatz.</p>
        </div>
      </div>
    `;
  }
}

customElements.define("company-dashboard-page", CompanyDashboardPage);
