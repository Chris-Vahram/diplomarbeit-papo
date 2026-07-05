import { apiFetch, apiDownload } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

const STATUSES = ["SUBMITTED", "REVIEWED", "FULFILLED"];
const STATUS_LABELS = { SUBMITTED: "Eingereicht", REVIEWED: "Geprüft", FULFILLED: "Abgeschlossen" };

class CompanyOrderListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Bestellungen</h1>
      <button type="button" id="export-btn">CSV exportieren</button>
      <data-table id="order-table"></data-table>
    `;

    this.querySelector("#export-btn").addEventListener("click", () => this.exportCsv());

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "partnerName", label: "Partner" },
      { key: "kundeName", label: "Kunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusSelect", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.table.addEventListener("change", (event) => {
      const select = event.target.closest("[data-order-id]");
      if (select) this.updateStatus(select.dataset.orderId, select.value);
    });

    this.load();
  }

  async load() {
    try {
      this.orders = await apiFetch("/company/orders");
      this.table.rows = this.orders.map((o) => ({
        orderDate: o.orderDate,
        partnerName: o.partnerName,
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: o.orderValue.toFixed(2),
        commissionLabel: o.commissionValue != null ? o.commissionValue.toFixed(2) : "–",
        statusSelect: `<select data-order-id="${o.id}">${STATUSES.map(
          (s) => `<option value="${s}" ${s === o.status ? "selected" : ""}>${STATUS_LABELS[s]}</option>`
        ).join("")}</select>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async updateStatus(orderId, status) {
    try {
      await apiFetch(`/company/orders/${orderId}/status`, { method: "PUT", body: JSON.stringify({ status }) });
      showToast("Status aktualisiert.", "success");
    } catch (error) {
      showToast(error.message, "error");
      this.load();
    }
  }

  async exportCsv() {
    try {
      await apiDownload("/company/orders/export", "bestellungen.csv");
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("company-order-list-page", CompanyOrderListPage);
