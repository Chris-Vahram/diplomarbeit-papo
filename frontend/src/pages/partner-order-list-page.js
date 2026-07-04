import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

class PartnerOrderListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Meine Bestellungen</h1>
      <a href="#/partner/orders/new" class="button-primary">Neue Bestellung</a>
      <data-table id="order-table"></data-table>
    `;

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "kundeName", label: "Kunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusBadge", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";

    this.load();
  }

  async load() {
    try {
      const orders = await apiFetch("/partner/orders");
      this.table.rows = orders.map((o) => ({
        orderDate: o.orderDate,
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: o.orderValue.toFixed(2),
        commissionLabel: o.commissionValue != null ? o.commissionValue.toFixed(2) : "–",
        statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-order-list-page", PartnerOrderListPage);
