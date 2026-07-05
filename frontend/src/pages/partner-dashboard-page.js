import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";

class PartnerDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Partner-Dashboard</h1>
      <div class="metric-row" id="metric-row"></div>
      <h2>Letzte Bestellungen</h2>
      <data-table id="latest-orders-table"></data-table>
    `;

    this.metricRow = this.querySelector("#metric-row");
    this.table = this.querySelector("#latest-orders-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "kundeName", label: "Kunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "statusBadge", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.load();
  }

  async load() {
    try {
      const dashboard = await apiFetch("/partner/dashboard");
      this.metricRow.innerHTML = `
        <metric-card label="Provision (abgeschlossen)" value="${formatEuro(dashboard.totalCommission)}"></metric-card>
        <metric-card label="Umsatz (abgeschlossen)" value="${formatEuro(dashboard.totalRevenue)}"></metric-card>
        <metric-card label="Abgeschlossene Bestellungen" value="${dashboard.fulfilledOrderCount}"></metric-card>
        <metric-card label="Freigeschaltete Produkte" value="${dashboard.enabledProductCount}"></metric-card>
      `;
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
}

customElements.define("partner-dashboard-page", PartnerDashboardPage);
