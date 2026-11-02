import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { taxIdentifierFieldsHtml, readTaxIdentifier, fillTaxIdentifier } from "../lib/tax-identifier-fields.js";
import { tradeRegisterIdentifierFieldsHtml, readTradeRegisterIdentifier, fillTradeRegisterIdentifier } from "../lib/trade-register-fields.js";

class CompanyProfilePage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Firmenprofil</h1>
      <p class="page-intro">Diese Angaben gelten für Ihr Unternehmen im Partnerportal.</p>
      <form id="company-profile-form" class="entity-form entity-form--grid">
        <form-field class="form-field--wide" label="Firmenname" name="businessName" required></form-field>
        ${taxIdentifierFieldsHtml()}
        ${tradeRegisterIdentifierFieldsHtml()}
        <form-field class="form-field--wide" label="E-Mail für Bestellhinweise" name="orderNoticeEmail" type="email"></form-field>
        <div class="entity-form__actions">
          <button type="submit">Speichern</button>
        </div>
      </form>
    `;
    this.form = this.querySelector("#company-profile-form");
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.load();
  }

  async load() {
    try {
      const profile = await apiFetch("/company/profile");
      this.form.elements.businessName.value = profile.businessName ?? "";
      fillTaxIdentifier(this.form, profile.taxIdentifier);
      fillTradeRegisterIdentifier(this.form, profile.tradeRegisterIdentifier);
      this.form.elements.orderNoticeEmail.value = profile.orderNoticeEmail ?? "";
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    if (this.submitting) return;
    this.submitting = true;
    const payload = {
      businessName: this.form.elements.businessName.value,
      taxIdentifier: readTaxIdentifier(this.form),
      tradeRegisterIdentifier: readTradeRegisterIdentifier(this.form),
      // Empty string clears the address; the backend keeps it only when the field is absent.
      orderNoticeEmail: this.form.elements.orderNoticeEmail.value.trim(),
    };
    try {
      await apiFetch("/company/profile", { method: "PUT", body: JSON.stringify(payload) });
      showToast("Firmenprofil aktualisiert.", "success");
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      this.submitting = false;
    }
  }
}

customElements.define("company-profile-page", CompanyProfilePage);
