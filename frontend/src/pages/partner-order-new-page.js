import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
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

class PartnerOrderNewPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `<h1>Neue Bestellung</h1><div id="order-content"></div>`;
    this.content = this.querySelector("#order-content");
    this.load();
  }

  async load() {
    try {
      const [products, customers, profile] = await Promise.all([
        apiFetch("/partner/products"),
        apiFetch("/partner/customers"),
        apiFetch("/partner/profile"),
      ]);
      this.products = products;
      this.customers = customers;
      this.commissionPercent = profile.commissionPercent;
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
      <div class="order-form-layout">
        <form id="order-form" class="entity-form">
          ${kundePickerHtml(this.customers)}
          ${articleLinesHtml()}
          <div class="entity-form__actions">
            <button type="submit">Bestellung aufgeben</button>
          </div>
        </form>
        <div id="order-summary"></div>
      </div>
    `;

    this.form = this.querySelector("#order-form");
    this.summaryEl = this.querySelector("#order-summary");
    wireKundePicker(this.form);
    wireArticleLines(this.form, this.products, (state) => this.renderSummary(state));
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.renderSummary({ items: [], totalCents: 0 });
  }

  renderSummary({ items, totalCents }) {
    this.summaryEl.innerHTML = renderOrderSummaryHtml({ items, totalCents, commissionPercent: this.commissionPercent });
  }

  async handleSubmit(event) {
    event.preventDefault();
    const payload = { ...readKundePayload(this.form), items: readItemsPayload(this.form) };

    // Guard against a double-click firing two requests while the first is in flight.
    const submitButton = this.form.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.renderConfirmation(order);
    } catch (error) {
      showToast(error.message, "error");
      submitButton.disabled = false;
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
