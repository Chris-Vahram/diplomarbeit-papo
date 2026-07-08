import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";
import { filterByPeriod, fulfilledTotals, trailingMonths, monthlyRevenue, previousPeriod, trendInfo } from "../lib/dashboard-data.js";
import {
  productOptionsHtml,
  kundePickerHtml,
  articleLinesHtml,
  wireKundePicker,
  wireArticleLines,
  readKundePayload,
  readItemsPayload,
  renderOrderConfirmationHtml,
} from "../lib/order-form.js";

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
  // Trailing 6 months so the headline KPIs cover the same window as the month
  // chart and the dashboard is populated by default. The picker narrows from here.
  const now = new Date();
  const to = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
  const from = new Date(now.getFullYear(), now.getMonth() - 5, 1, 0, 0, 0, 0);
  return { from, to };
}

function formatCompactEur(value) {
  return value >= 1000 ? `${(value / 1000).toFixed(1).replace(".", ",")} T€` : eur.format(value);
}

function kundeName(kunde) {
  return `${kunde.firstName ? kunde.firstName + " " : ""}${kunde.lastName}`;
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
        <div class="dashboard-header__actions">
          <button type="button" class="button-primary" id="new-order-btn">Neue Bestellung</button>
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
      </div>
      <div class="metric-row" id="metric-row"></div>
      <div class="chart-row" id="chart-row"></div>
      <h2>Letzte Bestellungen</h2>
      <data-table id="latest-orders-table"></data-table>
      <modal-dialog id="order-modal" title="Neue Bestellung">
        <div id="order-modal-content"></div>
      </modal-dialog>
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

    this.modal = this.querySelector("#order-modal");
    this.modalContent = this.querySelector("#order-modal-content");
    this.querySelector("#new-order-btn").addEventListener("click", () => this.openOrderModal());

    this.wirePeriodPicker();
    this.load();
  }

  async openOrderModal() {
    this.modal.setAttribute("title", "Neue Bestellung");
    this.modal.open();
    this.modalContent.innerHTML = `<p>Lade…</p>`;
    try {
      const [products, customers] = await Promise.all([
        apiFetch("/partner/products"),
        apiFetch("/partner/customers"),
      ]);
      if (products.length === 0) {
        this.modalContent.innerHTML = `<p>Für Sie sind noch keine Produkte freigeschaltet. Eine Bestellung ist erst möglich, sobald die Firma Produkte für Sie freischaltet.</p>`;
        return;
      }
      this.modalContent.innerHTML = `
        <form id="order-form" class="entity-form entity-form--grid">
          ${kundePickerHtml(customers)}
          ${articleLinesHtml()}
          <div class="entity-form__actions">
            <button type="submit">Bestellung aufgeben</button>
          </div>
        </form>
      `;
      const form = this.modalContent.querySelector("#order-form");
      wireKundePicker(form);
      wireArticleLines(form, productOptionsHtml(products));
      form.addEventListener("submit", (event) => this.handleOrderSubmit(event, form));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async handleOrderSubmit(event, form) {
    event.preventDefault();
    const payload = { ...readKundePayload(form), items: readItemsPayload(form) };

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.modal.setAttribute("title", `Bestellung Nr. ${order.id} bestätigt`);
      this.modalContent.innerHTML = renderOrderConfirmationHtml(order, {
        actionsHtml: `<button type="button" id="order-done-btn">Fertig</button>`,
      });
      this.modalContent.querySelector("#order-done-btn").addEventListener("click", () => {
        this.modal.close();
        this.load();
      });
    } catch (error) {
      showToast(error.message, "error");
    }
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
      this.render();
    });

    this.querySelector("#period-reset").addEventListener("click", () => {
      this.period = defaultPeriod();
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.render();
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
    const fromIso = toIsoDate(this.period.from);
    const toIso = toIsoDate(this.period.to);
    const inRange = filterByPeriod(this.orders, fromIso, toIso);
    const { revenue, commission, count } = fulfilledTotals(inRange);

    // Same-length period immediately before the current one, for the metric-card
    // up/down arrows ("... ggü. Vorperiode").
    const prev = previousPeriod(this.period.from, this.period.to);
    const prevInRange = filterByPeriod(this.orders, toIsoDate(prev.from), toIsoDate(prev.to));
    const prevTotals = fulfilledTotals(prevInRange);
    const revenueTrend = trendInfo(revenue, prevTotals.revenue);
    const commissionTrend = trendInfo(commission, prevTotals.commission);
    const countTrend = trendInfo(count, prevTotals.count);
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
      <metric-card label="Freigeschaltete Produkte" value="${this.base.enabledProductCount}" ring="${ringPct}" ring-layout="full" ring-caption="von ${total} Produkten"></metric-card>
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
          <span class="bar-chart__label" title="${c.name}">${c.name}</span>
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
