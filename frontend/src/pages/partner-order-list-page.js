import { apiFetch } from "../lib/api.js";
import { formatNumber, formatDate } from "../lib/format.js";
import { renderOrderDetailsHtml } from "../lib/order-details.js";

const STATUSES = ["SUBMITTED", "REVIEWED", "FULFILLED"];
const STATUS_LABELS = { SUBMITTED: "Eingereicht", REVIEWED: "Geprüft", FULFILLED: "Abgeschlossen" };

class PartnerOrderListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <div class="list-header">
        <h1>Meine Bestellungen</h1>
        <button type="button" class="button-primary" id="new-order-btn">Neue Bestellung</button>
      </div>
      <data-table id="order-table"></data-table>
      <modal-dialog id="details-modal" title="Bestellung">
        <div id="details-content"></div>
      </modal-dialog>
    `;

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum", mobile: "meta" },
      { key: "kundeName", label: "Endkunde", mobile: "title" },
      { key: "orderValueLabel", label: "Wert brutto (€)", mobile: "amount" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusBadge", label: "Status", html: true, mobile: "status" },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";
    this.table.emptyHint = "Neue Bestellungen für Ihre Endkunden erscheinen hier.";
    this.table.emptyAction = `<a class="button-primary" href="#/partner/orders/new">Neue Bestellung</a>`;
    this.table.retry = () => this.load();
    this.table.filters = [
      { key: "kundeName", type: "text", label: "Endkunde" },
      { key: "orderDateIso", type: "daterange", label: "Datum" },
      { key: "status", type: "select", label: "Status", options: STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] })) },
    ];
    this.table.rowKey = "id";
    this.table.addEventListener("row-click", (event) => this.openDetails(event.detail.id));

    this.querySelector("#new-order-btn").addEventListener("click", () => {
      location.hash = "#/partner/orders/new";
    });

    this.detailsModal = this.querySelector("#details-modal");
    this.detailsContent = this.querySelector("#details-content");

    this.load();
  }

  async load() {
    try {
      const orders = await apiFetch("/partner/orders");
      this.orders = orders;
      this.table.rows = orders.map((o) => ({
        id: o.id,
        orderDate: formatDate(o.orderDate),
        orderDateIso: o.orderDate,
        status: o.status,
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: formatNumber(o.orderValue),
        commissionLabel: o.commissionValue != null ? formatNumber(o.commissionValue) : "–",
        statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
      }));
    } catch (error) {
      this.table.error = error.message;
    }
  }

  openDetails(id) {
    const order = this.orders.find((o) => String(o.id) === String(id));
    if (!order) return;
    this.detailsModal.setAttribute("title", `Bestellung Nr. ${order.id}`);
    this.detailsContent.innerHTML = renderOrderDetailsHtml(order, { showPartner: false });
    this.detailsModal.open();
  }
}

customElements.define("partner-order-list-page", PartnerOrderListPage);
