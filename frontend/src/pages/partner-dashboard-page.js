import { apiFetch } from "../lib/api.js";
import { ensureValidSession, getSession } from "../lib/auth.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";
import { filterByPeriod, fulfilledTotals, trailingMonths, monthlyRevenue, previousPeriod, trendInfo, trailingWindowTrend, toLocalIsoDate, presetPeriod } from "../lib/dashboard-data.js";
import { escapeHtml } from "../lib/html.js";
import { renderMonthChart, periodPickerHtml, wirePeriodPicker } from "../lib/dashboard-ui.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });

const STATUS_META = {
  submitted: { label: "Eingereicht", bg: "--status-submitted-bg" },
  reviewed: { label: "Geprüft", bg: "--status-reviewed-bg" },
  fulfilled: { label: "Abgeschlossen", bg: "--status-fulfilled-bg" },
};

function kundeName(kunde) {
  return `${kunde.firstName ? kunde.firstName + " " : ""}${kunde.lastName}`;
}

class PartnerDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    // Opens on the current month (meeting 12.10.2026); "Gesamter Zeitraum" sets
    // period = null and shows the backend's exact all-time totals.
    this.periodPreset = "month";
    this.period = presetPeriod(this.periodPreset);

    const greeting = "Willkommen zurück zum";

    this.innerHTML = `
      <div class="dashboard-header">
        <div class="page-heading">
          <p class="page-heading__greeting">${greeting}</p>
          <h1 id="dashboard-heading">Dashboard</h1>
          <p id="profile-name-status" hidden></p>
        </div>
        <div class="dashboard-header__actions">
          <button type="button" class="button-primary button-primary--hero" id="new-order-btn"><i data-lucide="plus"></i>Neue Bestellung</button>
          ${periodPickerHtml()}
        </div>
      </div>
      <h2>Letzte Bestellungen</h2>
      <data-table id="latest-orders-table"></data-table>
      <div class="metric-row" id="metric-row"></div>
      <div class="chart-row" id="chart-row"></div>
    `;

    this.metricRow = this.querySelector("#metric-row");
    this.chartRow = this.querySelector("#chart-row");
    this.table = this.querySelector("#latest-orders-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "kundeName", label: "Endkunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "statusBadge", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.querySelector("#new-order-btn").addEventListener("click", () => {
      location.hash = "#/partner/orders/new";
    });

    wirePeriodPicker(this);
    this.load();
    this.loadProfileName();
  }

  async loadProfileName() {
    const session = await ensureValidSession();
    if (!session?.isPartner || !this.isConnected) return;
    const accessToken = session.accessToken;
    const heading = this.querySelector("#dashboard-heading");
    const status = this.querySelector("#profile-name-status");
    try {
      const profile = await apiFetch("/partner/profile");
      const current = getSession();
      if (!this.isConnected || !current?.isPartner || current.accessToken !== accessToken) return;
      heading.textContent = `Dashboard der Firma ${profile.name}`;
      status.hidden = true;
    } catch {
      const current = getSession();
      if (!this.isConnected || !current?.isPartner || current.accessToken !== accessToken) return;
      status.textContent = "Firmenname nicht verfügbar.";
      status.hidden = false;
      const retry = document.createElement("button");
      retry.type = "button";
      retry.textContent = "Erneut laden";
      retry.addEventListener("click", () => this.loadProfileName(), { once: true });
      status.append(" ", retry);
    }
  }

  async load() {
    try {
      // /partner/dashboard for enabled/total product counts; /partner/orders for
      // the per-order data the charts + period filter run on.
      const [base, orders] = await Promise.all([
        apiFetch("/partner/dashboard"),
        apiFetch("/partner/orders"),
      ]);
      this.base = base;
      this.orders = orders;
      this.render();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  render() {
    if (!this.orders) return;

    let inRange, revenue, commission, count, revenueTrend, commissionTrend, countTrend;
    if (this.period) {
      const fromIso = toLocalIsoDate(this.period.from);
      const toIso = toLocalIsoDate(this.period.to);
      inRange = filterByPeriod(this.orders, fromIso, toIso);
      ({ revenue, commission, count } = fulfilledTotals(inRange));

      // Same-length period immediately before the current one, for the metric-card
      // up/down arrows ("... ggü. Vorperiode").
      const prev = previousPeriod(this.period.from, this.period.to);
      const prevInRange = filterByPeriod(this.orders, toLocalIsoDate(prev.from), toLocalIsoDate(prev.to));
      const prevTotals = fulfilledTotals(prevInRange);
      revenueTrend = trendInfo(revenue, prevTotals.revenue);
      commissionTrend = trendInfo(commission, prevTotals.commission);
      countTrend = trendInfo(count, prevTotals.count);
    } else {
      // No explicit range: headline totals are the backend's true all-time values
      // (BigDecimal-exact, no client-side recompute) - charts show all orders.
      // Trend arrows use a fixed trailing-30-days-vs-prior-30-days comparison,
      // decoupled from the all-time headline totals.
      inRange = this.orders;
      revenue = this.base.totalRevenue;
      commission = this.base.totalCommission;
      count = this.base.fulfilledOrderCount;
      ({ revenueTrend, commissionTrend, countTrend } = trailingWindowTrend(this.orders));
    }
    const trendAttrs = (t) => (t ? `trend="${t.trend}" trend-label="${t.label}"` : "");

    const total = this.base.totalProductCount ?? 0;
    const ringPct = total > 0 ? Math.round((this.base.enabledProductCount / total) * 100) : 0;

    // Monthly chart: trailing 6 months (stable trend), independent of the picker.
    const { labels, keys } = trailingMonths(6);
    const monthly = monthlyRevenue(this.orders, keys);

    this.metricRow.innerHTML = `
      <metric-card label="Provision (abgeschlossen)" value="${formatEuro(commission)}" icon="percent" ${trendAttrs(commissionTrend)}></metric-card>
      <metric-card label="Umsatz (abgeschlossen)" value="${formatEuro(revenue)}" icon="trending-up" ${trendAttrs(revenueTrend)}></metric-card>
      <metric-card label="Abgeschlossene Bestellungen" value="${count}" icon="check-circle" ${trendAttrs(countTrend)}></metric-card>
      <metric-card label="Freigeschaltete Artikel" value="${this.base.enabledProductCount}" ring="${ringPct}" ring-layout="full" ring-caption="von ${total} Artikeln"></metric-card>
      ${renderMonthChart("Umsatz nach Monat", labels, monthly)}
    `;

    this.chartRow.innerHTML = this.renderCharts(inRange);
    window.lucide?.createIcons();

    // Newest 5 orders in the period (any status), matching the latest-orders convention.
    const latest = [...inRange].sort((a, b) => b.orderDate.localeCompare(a.orderDate)).slice(0, 5);
    this.table.rows = latest.map((o) => ({
      orderDate: formatDate(o.orderDate),
      kundeName: kundeName(o.kunde),
      orderValueLabel: formatNumber(o.orderValue),
      statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
    }));
  }

  renderCharts(inRange) {
    // Top-Kunden: group the in-range orders by Kunde, sum order value, top 6.
    const byKunde = new Map();
    for (const o of inRange) {
      const key = o.kunde.id;
      const entry = byKunde.get(key) ?? { name: kundeName(o.kunde), value: 0 };
      entry.value += o.orderValue;
      byKunde.set(key, entry);
    }
    const topClients = [...byKunde.values()].sort((a, b) => b.value - a.value).slice(0, 6);
    const maxValue = Math.max(1, ...topClients.map((c) => c.value));

    const bars = topClients
      .map((c) => {
        const isLeader = c.value === maxValue;
        return `
        <div class="bar-chart__row${isLeader ? " bar-chart__row--leader" : ""}">
          <span class="bar-chart__rank">${isLeader ? '<i data-lucide="trophy"></i>' : ""}</span>
          <span class="bar-chart__label" title="${escapeHtml(c.name)}">${escapeHtml(c.name)}</span>
          <span class="bar-chart__track"><span class="bar-chart__fill" style="width:${((c.value / maxValue) * 100).toFixed(1)}%"></span></span>
          <span class="bar-chart__value">${eur.format(c.value)}</span>
        </div>`;
      })
      .join("");

    // Bestellstatus donut: count the in-range orders by status.
    const counts = { submitted: 0, reviewed: 0, fulfilled: 0 };
    for (const o of inRange) {
      const key = o.status.toLowerCase();
      if (key in counts) counts[key] += 1;
    }
    const total = Math.max(1, inRange.length);

    let cumulative = 0;
    const gradientStops = Object.entries(counts)
      .map(([key, count]) => {
        const start = (cumulative / total) * 100;
        cumulative += count;
        const end = (cumulative / total) * 100;
        return `var(${STATUS_META[key].bg}) ${start}% ${end}%`;
      })
      .join(", ");

    const legend = Object.entries(counts)
      .map(
        ([key, count]) => `
        <div class="status-donut__item">
          <span class="status-donut__swatch" style="background:var(${STATUS_META[key].bg})"></span>
          <span>${STATUS_META[key].label}</span>
          <span class="status-donut__count">${count}</span>
        </div>`,
      )
      .join("");

    const completionRate = Math.round((counts.fulfilled / total) * 100);

    return `
      <div class="chart-card">
        <h3 class="chart-card__title">Top-Endkunden im gewählten Zeitraum</h3>
        <div class="bar-chart">${bars || '<p class="data-table__empty">Keine Bestellungen im gewählten Zeitraum.</p>'}</div>
      </div>
      <div class="chart-card">
        <h3 class="chart-card__title">Bestellstatus</h3>
        <p class="status-donut__headline">${inRange.length > 0 ? `${completionRate}% bereits abgeschlossen` : "Keine Daten im Zeitraum"}</p>
        <div class="status-donut">
          <div class="status-donut__circle" style="background:conic-gradient(${gradientStops})"></div>
          <div class="status-donut__legend">${legend}</div>
        </div>
      </div>
    `;
  }
}

customElements.define("partner-dashboard-page", PartnerDashboardPage);
