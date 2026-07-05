import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });
const dateLabel = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

function toIsoDate(date) {
  return date.toISOString().slice(0, 10);
}

function defaultPeriod() {
  const now = new Date();
  const to = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
  const from = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
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

class CompanyDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.period = defaultPeriod();

    const greeting = "Willkommen zurück zum";

    this.innerHTML = `
      <div class="dashboard-header">
        <div class="page-heading">
          <p class="page-heading__greeting">${greeting}</p>
          <h1>Firmen-Dashboard</h1>
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
      <h2>Provisionsabrechnung nach Partner</h2>
      <data-table id="settlement-table"></data-table>
    `;

    this.metricRow = this.querySelector("#metric-row");
    this.chartRow = this.querySelector("#chart-row");
    this.table = this.querySelector("#settlement-table");
    this.table.columns = [
      { key: "partnerName", label: "Partner" },
      { key: "fulfilledOrderCount", label: "Abgeschlossene Bestellungen" },
      { key: "revenueLabel", label: "Umsatz (€)" },
      { key: "commissionLabel", label: "Provision (€)" },
    ];
    this.table.emptyMessage = "Noch keine Partner angelegt.";
    this.table.searchKeys = ["partnerName"];
    this.table.pageSize = 20;

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
      const dashboard = await apiFetch(`/company/dashboard?${params}`);

      const monthlyRevenue = dashboard.monthlyRevenue ?? [];
      const monthlyOrderCount = dashboard.monthlyOrderCount ?? [];
      const prevRevenue = monthlyRevenue.length > 1 ? monthlyRevenue[monthlyRevenue.length - 2] : null;
      const prevOrderCount = monthlyOrderCount.length > 1 ? monthlyOrderCount[monthlyOrderCount.length - 2] : null;
      const prevCommission = prevRevenue !== null ? prevRevenue * (dashboard.totalCommission / dashboard.totalRevenue) : null;

      const revenueTrend = prevRevenue !== null ? trendAttrs(dashboard.totalRevenue, prevRevenue) : { trend: "", label: "" };
      const commissionTrend = prevCommission !== null ? trendAttrs(dashboard.totalCommission, prevCommission) : { trend: "", label: "" };
      const ordersTrend = prevOrderCount !== null ? trendAttrs(dashboard.fulfilledOrderCount, prevOrderCount) : { trend: "", label: "" };

      const avgOrderValue = dashboard.fulfilledOrderCount > 0 ? dashboard.totalRevenue / dashboard.fulfilledOrderCount : 0;
      const prevAvgOrderValue = prevRevenue !== null && prevOrderCount ? prevRevenue / prevOrderCount : null;
      const avgOrderValueTrend = prevAvgOrderValue !== null ? trendAttrs(avgOrderValue, prevAvgOrderValue) : { trend: "", label: "" };

      this.metricRow.innerHTML = `
        <metric-card label="Umsatz (abgeschlossen)" value="${eur.format(dashboard.totalRevenue)}" icon="trending-up" tone="positive" trend="${revenueTrend.trend}" trend-label="${revenueTrend.label}"></metric-card>
        <metric-card label="Provision (abgeschlossen)" value="${eur.format(dashboard.totalCommission)}" icon="percent" tone="positive" trend="${commissionTrend.trend}" trend-label="${commissionTrend.label}"></metric-card>
        <metric-card label="Abgeschlossene Bestellungen" value="${dashboard.fulfilledOrderCount}" icon="check-circle" tone="positive" trend="${ordersTrend.trend}" trend-label="${ordersTrend.label}"></metric-card>
        <metric-card label="Ø Bestellwert" value="${eur.format(avgOrderValue)}" icon="receipt" tone="positive" trend="${avgOrderValueTrend.trend}" trend-label="${avgOrderValueTrend.label}"></metric-card>
        ${renderMonthChart("Umsatz nach Monat", dashboard.monthLabels ?? [], monthlyRevenue)}
      `;

      this.chartRow.innerHTML = this.renderCharts(dashboard);
      window.lucide?.createIcons();

      this.table.rows = dashboard.settlement.map((s) => ({
        partnerName: s.partnerName,
        fulfilledOrderCount: s.fulfilledOrderCount,
        revenueLabel: eur.format(s.revenue),
        commissionLabel: eur.format(s.commission),
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderCharts(dashboard) {
    const topPartners = [...dashboard.settlement].sort((a, b) => b.revenue - a.revenue).slice(0, 5);
    const maxRevenue = Math.max(1, ...topPartners.map((p) => p.revenue));

    const bars = topPartners
      .map(
        (p, i) => `
        <div class="bar-chart__row${i === 0 ? " bar-chart__row--leader" : ""}">
          <span class="bar-chart__rank">${i === 0 ? '<i data-lucide="trophy"></i>' : ""}</span>
          <span class="bar-chart__label" title="${p.partnerName}">${p.partnerName}</span>
          <span class="bar-chart__track"><span class="bar-chart__fill" style="width:${((p.revenue / maxRevenue) * 100).toFixed(1)}%"></span></span>
          <span class="bar-chart__value">${eur.format(p.revenue)}</span>
        </div>`,
      )
      .join("");

    const rate = dashboard.totalRevenue > 0 ? (dashboard.totalCommission / dashboard.totalRevenue) * 100 : 0;

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
            <p class="donut-ring__figure">${eur.format(dashboard.totalCommission)} von ${eur.format(dashboard.totalRevenue)}</p>
          </div>
          <p class="donut-ring__caption">Anteil der Provision am abgeschlossenen Gesamtumsatz.</p>
        </div>
      </div>
    `;
  }
}

customElements.define("company-dashboard-page", CompanyDashboardPage);
