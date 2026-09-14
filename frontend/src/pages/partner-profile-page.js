import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";

// FR-08: partner edits contact data only. partnerNumber/commissionPercent/active are
// shown read-only for context but never sent in the update payload.
class PartnerProfilePage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Mein Profil</h1>
      <dl class="profile-readonly">
        <dt>Benutzername</dt><dd id="profile-number">–</dd>
        <dt>Provision</dt><dd id="profile-commission">–</dd>
        <dt>Status</dt><dd id="profile-status">–</dd>
      </dl>
      <form id="profile-form" class="entity-form entity-form--grid">
        <form-field label="Name" name="name" required></form-field>
        ${addressFieldsHtml("address")}
        <form-field label="E-Mail" name="email" type="email"></form-field>
        <form-field label="Telefon" name="phone"></form-field>
        <div class="entity-form__actions">
          <button type="submit">Speichern</button>
        </div>
      </form>
    `;

    this.form = this.querySelector("#profile-form");
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));

    this.load();
  }

  async load() {
    try {
      const partner = await apiFetch("/partner/profile");
      this.querySelector("#profile-number").textContent = partner.partnerNumber;
      this.querySelector("#profile-commission").textContent = `${partner.commissionPercent}%`;
      this.querySelector("#profile-status").textContent = partner.active ? "Aktiv" : "Inaktiv";
      this.form.elements.name.value = partner.name;
      fillAddress(this.form, "address", partner.address);
      this.form.elements.email.value = partner.email ?? "";
      this.form.elements.phone.value = partner.phone ?? "";
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(this.form);
    const payload = {
      name: data.get("name"),
      address: readAddress(this.form, "address"),
      email: data.get("email") || null,
      phone: data.get("phone") || null,
    };
    try {
      await apiFetch("/partner/profile", { method: "PUT", body: JSON.stringify(payload) });
      showToast("Profil aktualisiert.", "success");
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-profile-page", PartnerProfilePage);
