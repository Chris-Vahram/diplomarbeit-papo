import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

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
      { key: "statusBadge", label: "Status" },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.load();
  }

  async load() {
    try {
      const dashboard = await apiFetch("/partner/dashboard");
      this.metricRow.innerHTML = `
        <metric-card label="Provision (abgeschlossen)" value="${dashboard.totalCommission.toFixed(2)} €"></metric-card>
        <metric-card label="Umsatz (abgeschlossen)" value="${dashboard.totalRevenue.toFixed(2)} €"></metric-card>
        <metric-card label="Abgeschlossene Bestellungen" value="${dashboard.fulfilledOrderCount}"></metric-card>
        <metric-card label="Freigeschaltete Produkte" value="${dashboard.enabledProductCount}"></metric-card>
      `;
      this.table.rows = dashboard.latestOrders.map((o) => ({
        orderDate: o.orderDate,
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: o.orderValue.toFixed(2),
        statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-dashboard-page", PartnerDashboardPage);
