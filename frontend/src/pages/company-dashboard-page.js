import { apiFetch, apiDownload } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

class CompanyDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Firmen-Dashboard</h1>
      <div class="metric-row" id="metric-row"></div>
      <h2>Provisionsabrechnung nach Partner</h2>
      <button type="button" id="export-btn">CSV exportieren</button>
      <data-table id="settlement-table"></data-table>
    `;

    this.querySelector("#export-btn").addEventListener("click", () => this.exportCsv());

    this.metricRow = this.querySelector("#metric-row");
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

    this.load();
  }

  async load() {
    try {
      const dashboard = await apiFetch("/company/dashboard");
      this.metricRow.innerHTML = `
        <metric-card label="Umsatz (abgeschlossen)" value="${dashboard.totalRevenue.toFixed(2)} €"></metric-card>
        <metric-card label="Provision (abgeschlossen)" value="${dashboard.totalCommission.toFixed(2)} €"></metric-card>
        <metric-card label="Abgeschlossene Bestellungen" value="${dashboard.fulfilledOrderCount}"></metric-card>
        <metric-card label="Partner" value="${dashboard.partnerCount}"></metric-card>
      `;
      this.table.rows = dashboard.settlement.map((s) => ({
        partnerName: s.partnerName,
        fulfilledOrderCount: s.fulfilledOrderCount,
        revenueLabel: s.revenue.toFixed(2),
        commissionLabel: s.commission.toFixed(2),
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async exportCsv() {
    try {
      await apiDownload("/company/commissions/export", "provisionen.csv");
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("company-dashboard-page", CompanyDashboardPage);
