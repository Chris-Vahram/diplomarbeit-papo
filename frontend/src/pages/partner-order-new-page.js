import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { resolveImageUrls } from "../lib/product-images.js";
import { kundePickerHtml, wireKundePicker, readKundePayload, renderOrderConfirmationHtml } from "../lib/order-form.js";
import { orderCartHtml, wireOrderCart } from "../lib/order-cart.js";

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
      this.customers = customers;
      this.commissionPercent = profile.commissionPercent;
      if (products.length === 0) {
        this.content.innerHTML = `<p>Für Sie sind noch keine Produkte freigeschaltet. Eine Bestellung ist erst möglich, sobald die Firma Produkte für Sie freischaltet.</p>`;
        return;
      }
      this.products = await resolveImageUrls(products);
      this.renderForm();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderForm() {
    this.content.innerHTML = `
      <a href="#/partner/orders" class="back-link">&laquo; Zurück zu Meine Bestellungen</a>
      <form id="order-form" class="entity-form">
        ${kundePickerHtml(this.customers)}
      </form>
      ${orderCartHtml(this.products)}
    `;

    this.form = this.querySelector("#order-form");
    wireKundePicker(this.form);
    this.cart = wireOrderCart(this.content, this.products, this.commissionPercent);
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
  }

  async handleSubmit(event) {
    event.preventDefault();
    // Flag-based guard, not just disabling the clicked button: the cart panel's
    // submit button is recreated (and un-disabled) by cart.js's innerHTML reset
    // on every add/remove/quantity change, so a mutation during the in-flight
    // request would otherwise leave a fresh, clickable button behind. The flag
    // is checked regardless of which button instance dispatched the submit.
    if (this.submitting || this.cart.isEmpty()) return;
    this.submitting = true;
    const payload = { ...readKundePayload(this.form), items: this.cart.getItemsPayload() };

    const submitButton = this.content.querySelector('button[form="order-form"]');
    if (submitButton) submitButton.disabled = true;

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.renderConfirmation(order);
    } catch (error) {
      showToast(error.message, "error");
      if (submitButton) submitButton.disabled = false;
    } finally {
      // Reset even on success: this.submitting lives on the page instance, which
      // persists across "Neue Bestellung" cycles (renderForm() re-runs, but the
      // custom element itself is not recreated) - leaving it true would silently
      // block every later order in the same page visit.
      this.submitting = false;
    }
  }

  renderConfirmation(order) {
    this.content.innerHTML = renderOrderConfirmationHtml(order, {
      actionsHtml: `
        <button type="button" id="new-order-btn">Neue Bestellung</button>
        <a href="#/partner/orders" class="back-link">&laquo; Zurück zu Meine Bestellungen</a>
      `,
    });
    this.querySelector("#new-order-btn").addEventListener("click", () => this.load());
  }
}

customElements.define("partner-order-new-page", PartnerOrderNewPage);
