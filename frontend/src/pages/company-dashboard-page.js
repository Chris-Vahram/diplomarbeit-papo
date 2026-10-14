import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber } from "../lib/format.js";
import { filterByPeriod, fulfilledTotals, trailingMonths, monthlyRevenue, toLocalIsoDate, presetPeriod, PERIOD_PRESETS } from "../lib/dashboard-data.js";
import { escapeHtml } from "../lib/html.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });
const dateLabel = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

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
        <div class="period-picker">
          <button type="button" class="period-picker__button" id="period-button">
            <i data-lucide="calendar"></i>
            <span id="period-label"></span>
            <i data-lucide="chevron-down" class="period-picker__caret"></i>
          </button>
          <div class="period-picker__panel is-closed" id="period-panel">
            <div class="period-picker__presets">
              ${Object.entries(PERIOD_PRESETS).map(([kind, label]) => `<button type="button" data-preset="${kind}">${label}</button>`).join("")}
            </div>
            <div class="period-picker__field">
              <label for="period-from">Von</label>
              <input type="date" id="period-from" />
            </div>
            <div class="period-picker__field">
              <label for="period-to">Bis</label>
              <input type="date" id="period-to" />
            </div>
            <div class="period-picker__actions">
              <button type="button" class="period-picker__reset" id="period-reset">Gesamter Zeitraum</button>
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

    for (const button of this.periodPanel.querySelectorAll("[data-preset]")) {
      button.addEventListener("click", () => {
        this.periodPreset = button.dataset.preset;
        this.period = presetPeriod(this.periodPreset);
        this.periodPanel.classList.add("is-closed");
        this.updatePeriodUi();
        this.render();
      });
    }

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
      this.periodPreset = null;
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.render();
    });

    this.querySelector("#period-reset").addEventListener("click", () => {
      this.period = null;
      this.periodPreset = null;
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.render();
    });
  }

  updatePeriodUi() {
    for (const button of this.periodPanel.querySelectorAll("[data-preset]")) {
      button.setAttribute("aria-pressed", String(button.dataset.preset === this.periodPreset));
    }
    if (this.period) {
      this.periodFromInput.value = toLocalIsoDate(this.period.from);
      this.periodToInput.value = toLocalIsoDate(this.period.to);
      this.querySelector("#period-label").textContent = this.periodPreset
        ? PERIOD_PRESETS[this.periodPreset]
        : `${dateLabel.format(this.period.from)} – ${dateLabel.format(this.period.to)}`;
    } else {
      this.periodFromInput.value = "";
      this.periodToInput.value = "";
      this.querySelector("#period-label").textContent = "Gesamter Zeitraum";
    }
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
      showToast(error.message, "error");
    }
  }

  render() {
    if (!this.orders) return;

    let inRange, revenue, commission, count;
    if (this.period) {
      const fromIso = toLocalIsoDate(this.period.from);
      const toIso = toLocalIsoDate(this.period.to);
      inRange = filterByPeriod(this.orders, fromIso, toIso);
      ({ revenue, commission, count } = fulfilledTotals(inRange));
    } else {
      // No explicit range: headline totals are the backend's true all-time values
      // (BigDecimal-exact, no client-side recompute) - settlement/charts show all orders.
      inRange = this.orders;
      revenue = this.roster.totalRevenue;
      commission = this.roster.totalCommission;
      count = this.roster.fulfilledOrderCount;
    }
    const avgOrderValue = count > 0 ? revenue / count : 0;

    // Monthly chart: trailing 6 months (stable trend), independent of the picker.
    const { labels, keys } = trailingMonths(6);
    const monthly = monthlyRevenue(this.orders, keys);

    this.metricRow.innerHTML = `
      <metric-card label="Umsatz (abgeschlossen)" value="${formatEuro(revenue)}" icon="trending-up" tone="positive"></metric-card>
      <metric-card label="Provision (abgeschlossen)" value="${formatEuro(commission)}" icon="percent" tone="positive"></metric-card>
      <metric-card label="Abgeschlossene Bestellungen" value="${count}" icon="check-circle" tone="positive"></metric-card>
      <metric-card label="Ø Bestellwert" value="${formatEuro(avgOrderValue)}" icon="receipt" tone="positive"></metric-card>
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
