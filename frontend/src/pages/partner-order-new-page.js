import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
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
    this.content.innerHTML = `
      <form id="order-form" class="entity-form">
        ${kundePickerHtml(this.customers)}
        ${articleLinesHtml()}
        <div class="entity-form__actions">
          <button type="submit">Bestellung aufgeben</button>
        </div>
      </form>
    `;

    this.form = this.querySelector("#order-form");
    wireKundePicker(this.form);
    wireArticleLines(this.form, productOptionsHtml(this.products));
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
  }

  async handleSubmit(event) {
    event.preventDefault();
    const payload = { ...readKundePayload(this.form), items: readItemsPayload(this.form) };

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.renderConfirmation(order);
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderConfirmation(order) {
    this.content.innerHTML = renderOrderConfirmationHtml(order, {
      actionsHtml: `<button type="button" id="new-order-btn">Neue Bestellung</button>`,
    });
    this.querySelector("#new-order-btn").addEventListener("click", () => this.load());
  }
}

customElements.define("partner-order-new-page", PartnerOrderNewPage);
