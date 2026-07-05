import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });
const dateLabel = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

const STATUS_META = {
  submitted: { label: "Eingereicht", bg: "--status-submitted-bg" },
  reviewed: { label: "Geprüft", bg: "--status-reviewed-bg" },
  fulfilled: { label: "Abgeschlossen", bg: "--status-fulfilled-bg" },
};

function toIsoDate(date) {
  return date.toISOString().slice(0, 10);
}

function defaultPeriod() {
  const now = new Date();
  const to = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
  const from = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6, 0, 0, 0, 0);
  return { from, to };
}

function pctDelta(current, previous) {
  if (previous === 0) return current === 0 ? 0 : null;
  return ((current - previous) / previous) * 100;
}

function trendAttrs(current, previous) {
  const delta = pctDelta(current, previous);
  if (delta === null) return { trend: "", label: "" };
  const direction = delta >= 0 ? "up" : "down";
  const sign = delta >= 0 ? "+" : "";
  return { trend: direction, label: `${sign}${delta.toFixed(1)}% ggü. Vorperiode` };
}

function formatCompactEur(value) {
  return value >= 1000 ? `${(value / 1000).toFixed(1).replace(".", ",")} T€` : eur.format(value);
}

function renderMonthChart(title, monthLabels, monthlyValues) {
  const max = Math.max(1, ...monthlyValues);
  const lastIndex = monthlyValues.length - 1;
  const bars = monthlyValues
    .map((value, i) => {
      const pct = Math.max(2, Math.round((value / max) * 100));
      return `
        <div class="month-chart__bar-wrap">
          <div class="month-chart__bar${i === lastIndex ? " month-chart__bar--current" : ""}" style="height:${pct}%" title="${monthLabels[i] ?? ""}: ${eur.format(value)}"></div>
        </div>`;
    })
    .join("");
  const xlabels = monthLabels.map((m) => `<span class="month-chart__xlabel">${m}</span>`).join("");

  return `
    <div class="metric-card metric-card--chart month-chart-card">
      <div class="metric-card__watermark"><i data-lucide="line-chart"></i></div>
      <div class="metric-card__chart-label">${title}</div>
      <div class="month-chart">
        <div class="month-chart__bars">${bars}</div>
        <div class="month-chart__yaxis">
          <span>${formatCompactEur(max)}</span>
          <span>${formatCompactEur(max / 2)}</span>
          <span>0€</span>
        </div>
        <div class="month-chart__xaxis">${xlabels}</div>
      </div>
    </div>`;
}

class PartnerDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.period = defaultPeriod();

    const greeting = "Willkommen zurück zum";

    this.innerHTML = `
      <div class="dashboard-header">
        <div class="page-heading">
          <p class="page-heading__greeting">${greeting}</p>
          <h1>Partner-Dashboard</h1>
        </div>
        <div class="period-picker">
          <button type="button" class="period-picker__button" id="period-button">
            <i data-lucide="calendar"></i>
            <span id="period-label"></span>
            <i data-lucide="chevron-down" class="period-picker__caret"></i>
          </button>
          <div class="period-picker__panel is-closed" id="period-panel">
            <div class="period-picker__field">
              <label for="period-from">Von</label>
              <input type="date" id="period-from" />
            </div>
            <div class="period-picker__field">
              <label for="period-to">Bis</label>
              <input type="date" id="period-to" />
            </div>
            <div class="period-picker__actions">
              <button type="button" class="period-picker__reset" id="period-reset">Zurücksetzen</button>
              <button type="button" class="period-picker__apply" id="period-apply">Anwenden</button>
            </div>
          </div>
        </div>
      </div>
      <div class="metric-row" id="metric-row"></div>
      <div class="chart-row" id="chart-row"></div>
      <h2>Letzte Bestellungen</h2>
      <data-table id="latest-orders-table"></data-table>
    `;

    this.metricRow = this.querySelector("#metric-row");
    this.chartRow = this.querySelector("#chart-row");
    this.table = this.querySelector("#latest-orders-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "kundeName", label: "Kunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "statusBadge", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.wirePeriodPicker();
    this.load();
  }

  wirePeriodPicker() {
    this.periodButton = this.querySelector("#period-button");
    this.periodPanel = this.querySelector("#period-panel");
    this.periodFromInput = this.querySelector("#period-from");
    this.periodToInput = this.querySelector("#period-to");

    this.updatePeriodUi();

    this.periodButton.addEventListener("click", (event) => {
      event.stopPropagation();
      this.periodPanel.classList.toggle("is-closed");
    });
    document.addEventListener("click", (event) => {
      if (!this.contains(event.target)) this.periodPanel.classList.add("is-closed");
    });
    this.periodPanel.addEventListener("click", (event) => event.stopPropagation());

    this.querySelector("#period-apply").addEventListener("click", () => {
      const [fy, fm, fd] = this.periodFromInput.value.split("-").map(Number);
      const [ty, tm, td] = this.periodToInput.value.split("-").map(Number);
      const from = new Date(fy, fm - 1, fd, 0, 0, 0, 0);
      const to = new Date(ty, tm - 1, td, 23, 59, 59, 999);
      if (isNaN(from) || isNaN(to) || from > to) {
        showToast("Bitte einen gültigen Zeitraum wählen.", "error");
        return;
      }
      this.period = { from, to };
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.load();
    });

    this.querySelector("#period-reset").addEventListener("click", () => {
      this.period = defaultPeriod();
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.load();
    });
  }

  updatePeriodUi() {
    this.periodFromInput.value = toIsoDate(this.period.from);
    this.periodToInput.value = toIsoDate(this.period.to);
    this.querySelector("#period-label").textContent =
      `${dateLabel.format(this.period.from)} – ${dateLabel.format(this.period.to)}`;
  }

  async load() {
    try {
      const params = new URLSearchParams({
        from: toIsoDate(this.period.from),
        to: toIsoDate(this.period.to),
      });
      const dashboard = await apiFetch(`/partner/dashboard?${params}`);
      const prev = dashboard.previous;
      const revenueTrend = prev ? trendAttrs(dashboard.totalRevenue, prev.totalRevenue) : { trend: "", label: "" };
      const commissionTrend = prev ? trendAttrs(dashboard.totalCommission, prev.totalCommission) : { trend: "", label: "" };
      const ordersTrend = prev ? trendAttrs(dashboard.fulfilledOrderCount, prev.fulfilledOrderCount) : { trend: "", label: "" };

      this.metricRow.innerHTML = `
        <metric-card label="Provision (abgeschlossen)" value="${formatEuro(dashboard.totalCommission)}" icon="percent" trend="${commissionTrend.trend}" trend-label="${commissionTrend.label}"></metric-card>
        <metric-card label="Umsatz (abgeschlossen)" value="${formatEuro(dashboard.totalRevenue)}" icon="trending-up" trend="${revenueTrend.trend}" trend-label="${revenueTrend.label}"></metric-card>
        <metric-card label="Abgeschlossene Bestellungen" value="${dashboard.fulfilledOrderCount}" icon="check-circle" trend="${ordersTrend.trend}" trend-label="${ordersTrend.label}"></metric-card>
        <metric-card label="Freigeschaltete Produkte" value="${dashboard.enabledProductCount}" ring="${Math.round((dashboard.enabledProductCount / dashboard.totalProductCount) * 100)}" ring-layout="full" ring-caption="von ${dashboard.totalProductCount} Produkten"></metric-card>
        ${renderMonthChart("Umsatz nach Monat", dashboard.monthLabels ?? [], dashboard.monthlyRevenue ?? [])}
      `;

      this.chartRow.innerHTML = this.renderCharts(dashboard);
      window.lucide?.createIcons();

      this.table.rows = dashboard.latestOrders.map((o) => ({
        orderDate: formatDate(o.orderDate),
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: formatNumber(o.orderValue),
        statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderCharts(dashboard) {
    const topClients = dashboard.topClients.slice(0, 6);
    const maxValue = Math.max(1, ...topClients.map((c) => c.value));

    const bars = topClients
      .map((c) => {
        const isLeader = c.value === maxValue;
        return `
        <div class="bar-chart__row${isLeader ? " bar-chart__row--leader" : ""}">
          <span class="bar-chart__rank">${isLeader ? '<i data-lucide="trophy"></i>' : ""}</span>
          <span class="bar-chart__label" title="${c.name}">${c.name}</span>
          <span class="bar-chart__track"><span class="bar-chart__fill" style="width:${((c.value / maxValue) * 100).toFixed(1)}%"></span></span>
          <span class="bar-chart__value">${eur.format(c.value)}</span>
        </div>`;
      })
      .join("");

    const orders = dashboard.latestOrders;
    const counts = { submitted: 0, reviewed: 0, fulfilled: 0 };
    for (const o of orders) {
      const key = o.status.toLowerCase();
      if (key in counts) counts[key] += 1;
    }
    const total = Math.max(1, orders.length);

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
        <h3 class="chart-card__title">Top-Kunden im gewählten Zeitraum</h3>
        <div class="bar-chart">${bars || '<p class="data-table__empty">Keine Bestellungen im gewählten Zeitraum.</p>'}</div>
      </div>
      <div class="chart-card">
        <h3 class="chart-card__title">Bestellstatus</h3>
        <p class="status-donut__headline">${orders.length > 0 ? `${completionRate}% bereits abgeschlossen` : "Keine Daten im Zeitraum"}</p>
        <div class="status-donut">
          <div class="status-donut__circle" style="background:conic-gradient(${gradientStops})"></div>
          <div class="status-donut__legend">${legend}</div>
        </div>
      </div>
    `;
  }
}

customElements.define("partner-dashboard-page", PartnerDashboardPage);
