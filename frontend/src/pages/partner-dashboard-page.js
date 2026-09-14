import { apiFetch } from "../lib/api.js";
import { ensureValidSession, getSession } from "../lib/auth.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";
import { filterByPeriod, fulfilledTotals, trailingMonths, monthlyRevenue, previousPeriod, trendInfo, toLocalIsoDate } from "../lib/dashboard-data.js";
import { escapeHtml } from "../lib/html.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });
const dateLabel = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

const STATUS_META = {
  submitted: { label: "Eingereicht", bg: "--status-submitted-bg" },
  reviewed: { label: "Geprüft", bg: "--status-reviewed-bg" },
  fulfilled: { label: "Abgeschlossen", bg: "--status-fulfilled-bg" },
};

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
    // No explicit range by default - headline totals show the backend's true
    // all-time values (partner-dashboard spec: "without a range, all-time values").
    this.period = null;

    const greeting = "Willkommen zurück zum";

    this.innerHTML = `
      <div class="dashboard-header">
        <div class="page-heading">
          <p class="page-heading__greeting">${greeting}</p>
          <h1 id="dashboard-heading">Dashboard</h1>
          <p id="profile-name-status" hidden></p>
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

    this.wirePeriodPicker();
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
      this.period = null;
      this.periodPanel.classList.add("is-closed");
      this.updatePeriodUi();
      this.render();
    });
  }

  updatePeriodUi() {
    if (this.period) {
      this.periodFromInput.value = toLocalIsoDate(this.period.from);
      this.periodToInput.value = toLocalIsoDate(this.period.to);
      this.querySelector("#period-label").textContent =
        `${dateLabel.format(this.period.from)} – ${dateLabel.format(this.period.to)}`;
    } else {
      this.periodFromInput.value = "";
      this.periodToInput.value = "";
      this.querySelector("#period-label").textContent = "Gesamter Zeitraum";
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
      // No "previous period" concept without an explicit range, so no trend arrows.
      inRange = this.orders;
      revenue = this.base.totalRevenue;
      commission = this.base.totalCommission;
      count = this.base.fulfilledOrderCount;
      revenueTrend = commissionTrend = countTrend = null;
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
