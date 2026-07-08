import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { escapeHtml } from "../lib/html.js";
import { formatEuro, formatNumber, formatDate, formatAddress } from "../lib/format.js";
import { addressFieldsHtml, readAddress, setAddressRequired } from "../lib/address-fields.js";
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
      <modal-dialog id="order-modal" title="Neue Bestellung">
        <div id="order-modal-content"></div>
      </modal-dialog>
      <modal-dialog id="details-modal" title="Bestellung">
        <div id="details-content"></div>
      </modal-dialog>
    `;

    this.table = this.querySelector("#order-table");
    this.table.columns = [
      { key: "orderDate", label: "Datum" },
      { key: "kundeName", label: "Endkunde" },
      { key: "orderValueLabel", label: "Wert (€)" },
      { key: "commissionLabel", label: "Provision (€)" },
      { key: "statusBadge", label: "Status", html: true },
    ];
    this.table.emptyMessage = "Noch keine Bestellungen vorhanden.";
    this.table.filters = [
      { key: "orderDateIso", type: "daterange", label: "Datum" },
      { key: "status", type: "select", label: "Status", options: STATUSES.map((s) => ({ value: s, label: STATUS_LABELS[s] })) },
    ];
    this.table.rowKey = "id";
    this.table.addEventListener("row-click", (event) => this.openDetails(event.detail.id));

    this.modal = this.querySelector("#order-modal");
    this.modalContent = this.querySelector("#order-modal-content");
    this.querySelector("#new-order-btn").addEventListener("click", () => this.openModal());

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
      showToast(error.message, "error");
    }
  }

  openDetails(id) {
    const order = this.orders.find((o) => String(o.id) === String(id));
    if (!order) return;
    this.detailsModal.setAttribute("title", `Bestellung Nr. ${order.id}`);
    this.detailsContent.innerHTML = renderOrderDetailsHtml(order, { showPartner: false });
    this.detailsModal.open();
  }

  async openModal() {
    this.modal.setAttribute("title", "Neue Bestellung");
    this.modal.open();
    await this.loadOrderForm();
  }

  async loadOrderForm() {
    try {
      const [products, customers] = await Promise.all([
        apiFetch("/partner/products"),
        apiFetch("/partner/customers"),
      ]);
      this.products = products;
      this.customers = customers;
      if (this.products.length === 0) {
        this.modalContent.innerHTML = `<p>Für Sie sind noch keine Produkte freigeschaltet. Eine Bestellung ist erst möglich, sobald die Firma Produkte für Sie freischaltet.</p>`;
        return;
      }
      this.renderForm();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderForm() {
    const productOptions = this.products
      .map((p) => `<option value="${p.id}" ${p.available ? "" : "disabled"}>${escapeHtml(p.name)}${p.available ? "" : " (nicht verfügbar)"}</option>`)
      .join("");
    const customerOptions = this.customers
      .map((c) => `<option value="${c.id}">${escapeHtml(c.lastName)}${c.firstName ? ", " + escapeHtml(c.firstName) : ""} – ${escapeHtml(formatAddress(c.address))}</option>`)
      .join("");
    const hasCustomers = this.customers.length > 0;

    this.modalContent.innerHTML = `
      <form id="order-form" class="entity-form entity-form--grid">
        <fieldset class="form-field--wide">
          <legend>Endkunde</legend>
          <label class="radio-field">
            <input type="radio" name="kundeMode" value="existing" ${hasCustomers ? "checked" : "disabled"} />
            Bestehenden Endkunden wählen
          </label>
          <label class="radio-field">
            <input type="radio" name="kundeMode" value="new" ${hasCustomers ? "" : "checked"} />
            Neuen Endkunden anlegen
          </label>
          <div id="existing-kunde-block" ${hasCustomers ? "" : "hidden"}>
            <form-field label="Endkunde" name="kundeId">
              <select name="kundeId">${customerOptions}</select>
            </form-field>
          </div>
          <div id="new-kunde-block" ${hasCustomers ? "hidden" : ""}>
            <div class="entity-form--grid" style="margin-top:0">
              <form-field label="Nachname" name="newLastName" required></form-field>
              <form-field label="Vorname" name="newFirstName"></form-field>
              ${addressFieldsHtml("newAddress")}
              <form-field label="E-Mail" name="newEmail" type="email"></form-field>
              <form-field label="Telefon" name="newPhone"></form-field>
            </div>
          </div>
        </fieldset>
        <fieldset class="form-field--wide">
          <legend>Artikel</legend>
          <div id="order-lines"></div>
          <button type="button" id="add-line-btn">Artikel hinzufügen</button>
        </fieldset>
        <div class="entity-form__actions">
          <button type="submit">Bestellung aufgeben</button>
        </div>
      </form>
    `;

    this.productOptions = productOptions;
    this.form = this.querySelector("#order-form");
    this.linesContainer = this.querySelector("#order-lines");
    this.existingBlock = this.querySelector("#existing-kunde-block");
    this.newBlock = this.querySelector("#new-kunde-block");

    for (const radio of this.form.elements.kundeMode) {
      radio.addEventListener("change", () => this.toggleKundeMode());
    }
    this.querySelector("#add-line-btn").addEventListener("click", () => this.addLine());
    this.linesContainer.addEventListener("click", (event) => {
      const button = event.target.closest(".remove-line-btn");
      if (button) button.closest(".order-line").remove();
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));

    this.toggleKundeMode();
    this.addLine();
  }

  toggleKundeMode() {
    const mode = this.form.elements.kundeMode.value;
    this.existingBlock.hidden = mode !== "existing";
    this.newBlock.hidden = mode !== "new";
    // Required only while visible, so the browser doesn't block submit on hidden
    // "neuer Kunde" fields when "bestehenden Kunden wählen" is selected.
    const isNew = mode === "new";
    this.form.elements.newLastName.required = isNew;
    setAddressRequired(this.form, "newAddress", isNew);
  }

  addLine() {
    const row = document.createElement("div");
    row.className = "order-line";
    row.innerHTML = `
      <select name="productId" required>${this.productOptions}</select>
      <input type="number" name="quantity" min="1" step="1" value="1" required />
      <button type="button" class="remove-line-btn">Entfernen</button>
    `;
    this.linesContainer.appendChild(row);
  }

  async handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(this.form);
    const mode = data.get("kundeMode");

    const payload = {
      kundeId: mode === "existing" ? Number(data.get("kundeId")) : null,
      newKunde:
        mode === "new"
          ? {
              lastName: data.get("newLastName"),
              address: readAddress(this.form, "newAddress"),
              firstName: data.get("newFirstName") || null,
              email: data.get("newEmail") || null,
              phone: data.get("newPhone") || null,
            }
          : null,
      items: data.getAll("productId").map((productId, i) => ({
        productId: Number(productId),
        quantity: Number(data.getAll("quantity")[i]),
      })),
    };

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.renderConfirmation(order);
      this.load();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderConfirmation(order) {
    const rows = order.items
      .map((item) => `<tr><td>${escapeHtml(item.productName)}</td><td>${item.quantity}</td><td>${formatNumber(item.unitPriceSnapshot)}</td><td>${formatNumber(item.lineTotal)}</td></tr>`)
      .join("");
    const kundeName = `${order.kunde.firstName ? escapeHtml(order.kunde.firstName) + " " : ""}${escapeHtml(order.kunde.lastName)}`;

    this.modal.setAttribute("title", `Bestellung Nr. ${order.id} bestätigt`);
    this.modalContent.innerHTML = `
      <section class="order-confirmation order-confirmation--modal">
        <p>Endkunde: ${kundeName}, ${escapeHtml(formatAddress(order.kunde.address))}</p>
        <div class="data-table__scroll">
          <table class="data-table">
            <thead><tr><th>Artikel</th><th>Menge</th><th>Preis (€)</th><th>Summe (€)</th></tr></thead>
            <tbody>${rows}</tbody>
          </table>
        </div>
        <p><strong>Gesamtwert: ${formatEuro(order.orderValue)}</strong></p>
        <section class="simulated-email">
          <h3>Simulierte Bestätigungs-E-Mail (nicht wirklich versendet)</h3>
          <p>An: ${escapeHtml(order.kunde.email) || "keine E-Mail-Adresse hinterlegt"}</p>
          <p>Betreff: Ihre Bestellung Nr. ${order.id}</p>
          <p>Sehr geehrte/r ${kundeName}, vielen Dank für Ihre Bestellung über ${formatEuro(order.orderValue)}. Wir bearbeiten sie umgehend.</p>
        </section>
        <div class="entity-form__actions">
          <button type="button" id="done-btn">Fertig</button>
        </div>
      </section>
    `;
    this.querySelector("#done-btn").addEventListener("click", () => this.modal.close());
  }
}

customElements.define("partner-order-list-page", PartnerOrderListPage);
