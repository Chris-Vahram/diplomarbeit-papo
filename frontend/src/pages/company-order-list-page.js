import { apiFetch, apiDownload } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { escapeHtml } from "../lib/html.js";
import { formatNumber, formatDate } from "../lib/format.js";
import { renderOrderDetailsHtml } from "../lib/order-details.js";
import { refreshOrdersBadge } from "../lib/router.js";
import {
  kundePickerHtml,
  articleLinesHtml,
  wireKundePicker,
  wireArticleLines,
  readKundePayload,
  readItemsPayload,
  renderOrderConfirmationHtml,
  renderOrderSummaryHtml,
} from "../lib/order-form.js";

const STATUSES = ["SUBMITTED", "REVIEWED", "FULFILLED"];
const STATUS_LABELS = { SUBMITTED: "Eingereicht", REVIEWED: "Geprüft", FULFILLED: "Abgeschlossen" };

const EMAIL_TYPES = {
  ORDER_CONFIRMATION: "Bestellbestätigung",
  ORDER_NOTICE_PARTNER: "Bestellinfo",
  ORDER_NOTICE_COMPANY: "Bestellinfo",
  SHIPPING_CONFIRMATION: "Abschlussbestätigung",
};
const EMAIL_ROLES = { CUSTOMER: "Endkunde", PARTNER: "Partner", COMPANY: "Firma" };
const EMAIL_STATUS = {
  PENDING: "wartet auf Versand",
  SENDING: "wird gesendet",
  SENT: "vom Mailserver angenommen",
  FAILED: "fehlgeschlagen",
  SKIPPED: "nicht gesendet",
};
const EMAIL_REASONS = {
  DISABLED: "Versand nicht aktiviert",
  INVALID_ADDRESS: "ungültige Adresse",
  CONFIG_CHANGED: "Konfiguration geändert",
  RECIPIENT_CHANGED: "Empfänger geändert",
};

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
      <modal-dialog id="shipping-confirmation-modal" title="Bestellung abgeschlossen">
        <div id="shipping-confirmation-content"></div>
      </modal-dialog>
    `;

    this.querySelector("#export-btn").addEventListener("click", () => this.exportCsv());
    this.querySelector("#manual-order-btn").addEventListener("click", () => this.openManualOrderModal());

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum", html: true, mobile: "meta" },
      { key: "partnerName", label: "Partner" },
      { key: "kundeName", label: "Endkunde", mobile: "title" },
      { key: "orderValueLabel", label: "Wert (€)", mobile: "amount" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusSelect", label: "Status", html: true, mobile: "status" },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";
    this.table.retry = () => this.load();
    this.table.searchKeys = ["partnerName", "kundeName"];
    this.table.pageSize = 20;
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
    this.shippingConfirmationModal = this.querySelector("#shipping-confirmation-modal");
    this.shippingConfirmationContent = this.querySelector("#shipping-confirmation-content");

    this.load();
  }

  async load() {
    try {
      this.orders = await apiFetch("/company/orders");
      // Filter options (esp. the Partner dropdown, built from the loaded orders) are set
      // once here, not in renderRows() - renderRows() also runs on unrelated updates (e.g.
      // marking an order seen), and re-assigning .filters resets the user's active filters.
      this.table.filters = [
        { key: "partnerId", type: "select", label: "Partner", options: this.partnerFilterOptions() },
        { key: "kundeName", type: "text", label: "Endkunde" },
        { key: "orderDateIso", type: "daterange", label: "Datum" },
        { key: "status", type: "select", label: "Status", options: STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] })) },
      ];
      this.renderRows();
    } catch (error) {
      this.table.error = error.message;
    }
  }

  // Distinct partners among the loaded orders, id-keyed so two partners sharing a display
  // name stay separate filter entries (design.md).
  partnerFilterOptions() {
    const byId = new Map();
    for (const o of this.orders) {
      if (!byId.has(o.partnerId)) byId.set(o.partnerId, o.partnerName);
    }
    return [...byId.entries()]
      .map(([id, name]) => ({ value: String(id), label: name }))
      .sort((a, b) => a.label.localeCompare(b.label, "de-AT"));
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
      partnerId: o.partnerId,
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
    this.detailsContent.innerHTML = `${renderOrderDetailsHtml(order, { showPartner: true })}
      <section class="email-status"><h3>E-Mails</h3><div data-email-status><p>Lade E-Mail-Status…</p></div></section>`;
    this.detailsModal.open();
    this.loadEmailStatus(order.id);

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

  // Company-only delivery status per order (order-emails spec). Addresses arrive masked.
  async loadEmailStatus(orderId) {
    const target = this.detailsContent.querySelector("[data-email-status]");
    try {
      const emails = await apiFetch(`/company/orders/${orderId}/emails`);
      if (!target.isConnected) return;
      target.innerHTML = emails.length === 0
        ? "<p>Für diese Bestellung wurden keine E-Mails erfasst.</p>"
        : `<ul class="email-status__list">${emails.map((e) => `
            <li>
              <strong>${EMAIL_TYPES[e.type]}</strong> an ${EMAIL_ROLES[e.recipientRole]}${e.maskedAddress ? ` (${escapeHtml(e.maskedAddress)})` : ""}:
              ${EMAIL_STATUS[e.status]}${e.skipReason ? ` – ${EMAIL_REASONS[e.skipReason] ?? ""}` : ""}${e.attempts > 0 ? `, ${e.attempts} Versuch(e)` : ""}
              ${e.status === "FAILED" || e.skipReason === "RECIPIENT_CHANGED" ? `<button type="button" data-retry-email="${e.id}">Erneut senden</button>` : ""}
            </li>`).join("")}</ul>`;
      target.querySelectorAll("[data-retry-email]").forEach((button) => {
        button.addEventListener("click", async () => {
          button.disabled = true;
          try {
            await apiFetch(`/company/orders/${orderId}/emails/${button.dataset.retryEmail}/retry`, { method: "POST" });
            showToast("E-Mail wird erneut gesendet.", "success");
          } catch (error) {
            showToast(error.message, "error");
          }
          this.loadEmailStatus(orderId);
        });
      });
    } catch (error) {
      if (target.isConnected) target.innerHTML = "<p>E-Mail-Status konnte nicht geladen werden.</p>";
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
        .map((p) => ({ id: p.productId, name: p.name, price: p.price, category: p.category, available: p.available }));
      if (products.length === 0) {
        container.innerHTML = `
          <p>Für diesen Partner sind noch keine Artikel freigeschaltet.</p>
          <button type="button" class="button-secondary" data-manage-partner-id="${escapeHtml(String(partnerId))}">
            Artikel für diesen Partner verwalten
          </button>`;
        container.querySelector("[data-manage-partner-id]").addEventListener("click", (event) => {
          this.manualOrderModal.close();
          location.hash = `#/company/partners?enablement=${encodeURIComponent(event.currentTarget.dataset.managePartnerId)}`;
        });
        return;
      }
      const partner = this.manualOrderPartners.find((p) => String(p.id) === String(partnerId));
      // Stacked, not side-by-side: the modal panel (42.5rem, shared by every modal
      // in the app) isn't wide enough for a 2-column layout without widening it for
      // unrelated modals too.
      container.innerHTML = `
        <form id="manual-order-form" class="entity-form">
          ${kundePickerHtml(customers)}
          ${articleLinesHtml()}
          <div class="entity-form__actions">
            <button type="submit">Bestellung anlegen</button>
          </div>
        </form>
        <div id="manual-order-summary"></div>
      `;
      const form = container.querySelector("#manual-order-form");
      const summaryEl = container.querySelector("#manual-order-summary");
      const renderSummary = ({ items, totalCents }) => {
        summaryEl.innerHTML = renderOrderSummaryHtml({ items, totalCents, commissionPercent: partner?.commissionPercent ?? null });
      };
      wireKundePicker(form);
      wireArticleLines(form, products, renderSummary);
      renderSummary({ items: [], totalCents: 0 });
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
      const order = await apiFetch(`/company/orders/${orderId}/status`, { method: "PUT", body: JSON.stringify({ status }) });
      showToast("Status aktualisiert.", "success");
      this.load();

      // FR-17: the completion confirmation (with the customer e-mail notice) is shown when an order reaches
      // "fulfilled" — not on the submitted->reviewed step.
      if (status === "FULFILLED") {
        this.shippingConfirmationContent.innerHTML = renderOrderConfirmationHtml(order, {
          showPartner: true,
          kind: "shipping",
          actionsHtml: `<button type="button" id="shipping-confirmation-done-btn">Fertig</button>`,
        });
        this.shippingConfirmationContent.querySelector("#shipping-confirmation-done-btn").addEventListener("click", () => {
          this.shippingConfirmationModal.close();
        });
        this.shippingConfirmationModal.open();
      }
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
