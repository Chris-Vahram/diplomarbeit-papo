import { apiFetch, apiDownload } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { escapeHtml } from "../lib/html.js";
import { formatNumber, formatDate } from "../lib/format.js";
import { renderOrderDetailsHtml } from "../lib/order-details.js";
import { refreshOrdersBadge } from "../lib/router.js";
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

const STATUSES = ["SUBMITTED", "REVIEWED", "FULFILLED"];
const STATUS_LABELS = { SUBMITTED: "Eingereicht", REVIEWED: "Geprüft", FULFILLED: "Abgeschlossen" };

class CompanyOrderListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Bestellungen</h1>
        <div class="list-header__actions">
          <button type="button" id="manual-order-btn" class="button-secondary">Bestellung manuell anlegen</button>
          <button type="button" id="export-btn" class="button-primary">CSV exportieren</button>
        </div>
      </div>
      <data-table id="order-table"></data-table>
      <modal-dialog id="details-modal" title="Bestellung">
        <div id="details-content"></div>
      </modal-dialog>
      <modal-dialog id="manual-order-modal" title="Bestellung manuell anlegen">
        <div id="manual-order-content"></div>
      </modal-dialog>
    `;

    this.querySelector("#export-btn").addEventListener("click", () => this.exportCsv());
    this.querySelector("#manual-order-btn").addEventListener("click", () => this.openManualOrderModal());

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum", html: true },
      { key: "partnerName", label: "Partner" },
      { key: "kundeName", label: "Endkunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusSelect", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";
    this.table.searchKeys = ["partnerName", "kundeName"];
    this.table.pageSize = 20;
    this.table.filters = [
      { key: "orderDateIso", type: "daterange", label: "Datum" },
      { key: "status", type: "select", label: "Status", options: STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] })) },
    ];
    this.table.rowKey = "id";

    this.table.addEventListener("change", (event) => {
      const select = event.target.closest("[data-order-id]");
      if (select) this.updateStatus(select.dataset.orderId, select.value);
    });
    this.table.addEventListener("row-click", (event) => this.openDetails(event.detail.id));

    this.detailsModal = this.querySelector("#details-modal");
    this.detailsContent = this.querySelector("#details-content");
    this.manualOrderModal = this.querySelector("#manual-order-modal");
    this.manualOrderContent = this.querySelector("#manual-order-content");

    this.load();
  }

  async load() {
    try {
      this.orders = await apiFetch("/company/orders");
      this.renderRows();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderRows() {
    // Newest first: id is a reliable creation-order tiebreaker where orderDate alone isn't
    // (multiple orders can share a calendar date, incl. same-day manual company orders).
    this.orders.sort((a, b) => b.id - a.id);
    this.table.rows = this.orders.map((o) => ({
      id: o.id,
      orderDate: `${formatDate(o.orderDate)}${o.seenByCompany ? "" : ' <span class="badge-new">NEU</span>'}`,
      orderDateIso: o.orderDate,
      status: o.status,
      partnerName: o.partnerName,
      kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
      orderValueLabel: formatNumber(o.orderValue),
      commissionLabel: o.commissionValue != null ? formatNumber(o.commissionValue) : "–",
      statusSelect: `<select class="status-select status-select--${o.status.toLowerCase()}" data-order-id="${o.id}">${STATUSES.map(
        (s) => `<option value="${s}" ${s === o.status ? "selected" : ""}>${STATUS_LABELS[s]}</option>`
      ).join("")}</select>`,
      rowClass: o.seenByCompany ? "" : "data-table__row--unseen",
    }));
  }

  async openDetails(id) {
    const order = this.orders.find((o) => String(o.id) === String(id));
    if (!order) return;
    this.detailsModal.setAttribute("title", `Bestellung Nr. ${order.id}`);
    this.detailsContent.innerHTML = renderOrderDetailsHtml(order, { showPartner: true });
    this.detailsModal.open();

    if (!order.seenByCompany) {
      try {
        await apiFetch(`/company/orders/${order.id}/seen`, { method: "PUT" });
        order.seenByCompany = true;
        refreshOrdersBadge();
        this.renderRows();
      } catch (error) {
        showToast(error.message, "error");
      }
    }
  }

  async openManualOrderModal() {
    this.manualOrderModal.setAttribute("title", "Bestellung manuell anlegen");
    this.manualOrderContent.innerHTML = `<p>Lade Partner…</p>`;
    this.manualOrderModal.open();
    try {
      this.manualOrderPartners = await apiFetch("/company/partners");
      this.renderPartnerStep();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderPartnerStep() {
    const partnerOptions = this.manualOrderPartners
      .map((p) => `<option value="${p.id}">${escapeHtml(p.name)} (${escapeHtml(p.partnerNumber)})</option>`)
      .join("");
    this.manualOrderContent.innerHTML = `
      <form-field label="Partner" name="manualPartnerId">
        <select name="manualPartnerId">
          <option value="">Bitte wählen…</option>
          ${partnerOptions}
        </select>
      </form-field>
      <div id="manual-order-form-container"></div>
    `;
    this.manualOrderContent
      .querySelector('select[name="manualPartnerId"]')
      .addEventListener("change", (event) => {
        const container = this.manualOrderContent.querySelector("#manual-order-form-container");
        if (event.target.value) {
          this.loadPartnerOrderForm(event.target.value, container);
        } else {
          container.innerHTML = "";
        }
      });
  }

  async loadPartnerOrderForm(partnerId, container) {
    container.innerHTML = `<p>Lade…</p>`;
    try {
      const [enablement, customers] = await Promise.all([
        apiFetch(`/company/partners/${partnerId}/products`),
        apiFetch(`/company/partners/${partnerId}/customers`),
      ]);
      const products = enablement
        .filter((p) => p.enabled)
        .map((p) => ({ id: p.productId, name: p.name, available: p.available }));
      if (products.length === 0) {
        container.innerHTML = `<p>Für diesen Partner sind noch keine Produkte freigeschaltet.</p>`;
        return;
      }
      container.innerHTML = `
        <form id="manual-order-form" class="entity-form">
          ${kundePickerHtml(customers)}
          ${articleLinesHtml()}
          <div class="entity-form__actions">
            <button type="submit">Bestellung anlegen</button>
          </div>
        </form>
      `;
      const form = container.querySelector("#manual-order-form");
      wireKundePicker(form);
      wireArticleLines(form, productOptionsHtml(products));
      form.addEventListener("submit", (event) => this.handleManualSubmit(event, partnerId, form));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async handleManualSubmit(event, partnerId, form) {
    event.preventDefault();
    const payload = { partnerId: Number(partnerId), ...readKundePayload(form), items: readItemsPayload(form) };

    // Guard against a double-click firing two requests while the first is in flight.
    const submitButton = form.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    try {
      const order = await apiFetch("/company/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung angelegt.", "success");

      // Reflect the new order in the table immediately - not only once the modal is
      // closed via "Fertig" - so it's visible even if the company closes the modal
      // via Escape/backdrop instead, and there's no ambiguity about whether the
      // submit went through (which invited accidental duplicate submissions).
      this.orders.push(order);
      this.renderRows();

      this.manualOrderModal.setAttribute("title", `Bestellung Nr. ${order.id} angelegt`);
      this.manualOrderContent.innerHTML = renderOrderConfirmationHtml(order, {
        showPartner: true,
        actionsHtml: `<button type="button" id="manual-order-done-btn">Fertig</button>`,
      });
      this.manualOrderContent.querySelector("#manual-order-done-btn").addEventListener("click", () => {
        this.manualOrderModal.close();
      });
    } catch (error) {
      showToast(error.message, "error");
      submitButton.disabled = false;
    }
  }

  async updateStatus(orderId, status) {
    try {
      await apiFetch(`/company/orders/${orderId}/status`, { method: "PUT", body: JSON.stringify({ status }) });
      showToast("Status aktualisiert.", "success");
      this.load();
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
