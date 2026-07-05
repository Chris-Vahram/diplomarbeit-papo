import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { escapeHtml } from "../lib/html.js";
import { formatEuro, formatNumber, formatDate } from "../lib/format.js";

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

    this.modal = this.querySelector("#order-modal");
    this.modalContent = this.querySelector("#order-modal-content");
    this.querySelector("#new-order-btn").addEventListener("click", () => this.openModal());

    this.load();
  }

  async load() {
    try {
      const orders = await apiFetch("/partner/orders");
      this.table.rows = orders.map((o) => ({
        orderDate: formatDate(o.orderDate),
        kundeName: `${o.kunde.firstName ? o.kunde.firstName + " " : ""}${o.kunde.lastName}`,
        orderValueLabel: formatNumber(o.orderValue),
        commissionLabel: o.commissionValue != null ? formatNumber(o.commissionValue) : "–",
        statusBadge: `<status-badge status="${o.status.toLowerCase()}"></status-badge>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
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
      .map((c) => `<option value="${c.id}">${escapeHtml(c.lastName)}${c.firstName ? ", " + escapeHtml(c.firstName) : ""} – ${escapeHtml(c.address)}</option>`)
      .join("");
    const hasCustomers = this.customers.length > 0;

    this.modalContent.innerHTML = `
      <form id="order-form" class="entity-form entity-form--grid">
        <fieldset class="form-field--wide">
          <legend>Kunde</legend>
          <label class="radio-field">
            <input type="radio" name="kundeMode" value="existing" ${hasCustomers ? "checked" : "disabled"} />
            Bestehenden Kunden wählen
          </label>
          <label class="radio-field">
            <input type="radio" name="kundeMode" value="new" ${hasCustomers ? "" : "checked"} />
            Neuen Kunden anlegen
          </label>
          <div id="existing-kunde-block" ${hasCustomers ? "" : "hidden"}>
            <form-field label="Kunde" name="kundeId">
              <select name="kundeId">${customerOptions}</select>
            </form-field>
          </div>
          <div id="new-kunde-block" ${hasCustomers ? "hidden" : ""}>
            <div class="entity-form--grid" style="margin-top:0">
              <form-field label="Nachname" name="newLastName" required></form-field>
              <form-field label="Vorname" name="newFirstName"></form-field>
              <form-field class="form-field--wide" label="Adresse" name="newAddress" required></form-field>
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
    this.form.elements.newAddress.required = isNew;
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
              address: data.get("newAddress"),
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
        <p>Kunde: ${kundeName}, ${escapeHtml(order.kunde.address)}</p>
        <table class="data-table">
          <thead><tr><th>Artikel</th><th>Menge</th><th>Preis (€)</th><th>Summe (€)</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
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
