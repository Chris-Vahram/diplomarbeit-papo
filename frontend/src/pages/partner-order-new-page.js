import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

class PartnerOrderNewPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `<h1>Neue Bestellung</h1><div id="order-content"></div>`;
    this.content = this.querySelector("#order-content");
    this.load();
  }

  async load() {
    try {
      const [products, customers] = await Promise.all([
        apiFetch("/partner/products"),
        apiFetch("/partner/customers"),
      ]);
      this.products = products;
      this.customers = customers;
      if (this.products.length === 0) {
        this.content.innerHTML = `<p>Für Sie sind noch keine Produkte freigeschaltet. Eine Bestellung ist erst möglich, sobald die Firma Produkte für Sie freischaltet.</p>`;
        return;
      }
      this.renderForm();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderForm() {
    const productOptions = this.products
      .map((p) => `<option value="${p.id}" ${p.available ? "" : "disabled"}>${p.name}${p.available ? "" : " (nicht verfügbar)"}</option>`)
      .join("");
    const customerOptions = this.customers
      .map((c) => `<option value="${c.id}">${c.lastName}${c.firstName ? ", " + c.firstName : ""} – ${c.address}</option>`)
      .join("");
    const hasCustomers = this.customers.length > 0;

    this.content.innerHTML = `
      <form id="order-form" class="entity-form">
        <fieldset>
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
            <form-field label="Nachname" name="newLastName" required></form-field>
            <form-field label="Adresse" name="newAddress" required></form-field>
            <form-field label="Vorname" name="newFirstName"></form-field>
            <form-field label="E-Mail" name="newEmail" type="email"></form-field>
            <form-field label="Telefon" name="newPhone"></form-field>
          </div>
        </fieldset>
        <fieldset>
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

    this.addLine();
  }

  toggleKundeMode() {
    const mode = this.form.elements.kundeMode.value;
    this.existingBlock.hidden = mode !== "existing";
    this.newBlock.hidden = mode !== "new";
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
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderConfirmation(order) {
    const rows = order.items
      .map((item) => `<tr><td>${item.productName}</td><td>${item.quantity}</td><td>${item.unitPriceSnapshot.toFixed(2)}</td><td>${item.lineTotal.toFixed(2)}</td></tr>`)
      .join("");
    const kundeName = `${order.kunde.firstName ? order.kunde.firstName + " " : ""}${order.kunde.lastName}`;

    this.content.innerHTML = `
      <section class="order-confirmation">
        <h2>Bestellung Nr. ${order.id} bestätigt</h2>
        <p>Kunde: ${kundeName}, ${order.kunde.address}</p>
        <table class="data-table">
          <thead><tr><th>Artikel</th><th>Menge</th><th>Preis (€)</th><th>Summe (€)</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
        <p><strong>Gesamtwert: ${order.orderValue.toFixed(2)} €</strong></p>
        <section class="simulated-email">
          <h3>Simulierte Bestätigungs-E-Mail (nicht wirklich versendet)</h3>
          <p>An: ${order.kunde.email ?? "keine E-Mail-Adresse hinterlegt"}</p>
          <p>Betreff: Ihre Bestellung Nr. ${order.id}</p>
          <p>Sehr geehrte/r ${kundeName}, vielen Dank für Ihre Bestellung über ${order.orderValue.toFixed(2)} €. Wir bearbeiten sie umgehend.</p>
        </section>
        <button type="button" id="new-order-btn">Neue Bestellung</button>
      </section>
    `;
    this.querySelector("#new-order-btn").addEventListener("click", () => this.load());
  }
}

customElements.define("partner-order-new-page", PartnerOrderNewPage);
