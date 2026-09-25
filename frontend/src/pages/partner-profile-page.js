import { apiFetch, apiFetchForm } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";
import { tradeRegisterIdentifierFieldsHtml, readTradeRegisterIdentifier, fillTradeRegisterIdentifier } from "../lib/trade-register-fields.js";

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
      <section class="trade-proof-card" aria-labelledby="trade-proof-heading">
        <h2 id="trade-proof-heading">Gewerbeberechtigungsnachweis</h2>
        <p class="page-intro">Laden Sie den aktuellen Nachweis Ihres Unternehmens hoch. Er wird anschließend von der Firma geprüft.</p>
        <p id="trade-proof-status" class="trade-proof-card__status" aria-live="polite">Nachweis wird geladen …</p>
        <p id="trade-proof-error" class="trade-proof-card__error" role="alert" hidden></p>
        <button type="button" id="trade-proof-retry" hidden>Erneut laden</button>
        <form id="trade-proof-form" class="entity-form entity-form--grid">
          ${tradeRegisterIdentifierFieldsHtml()}
          <label class="form-field form-field--wide">
            <span class="form-field__label">Datei (PDF, JPEG oder PNG; maximal 10 MiB) *</span>
            <input type="file" name="proofFile" accept="application/pdf,.pdf,image/jpeg,.jpg,.jpeg,image/png,.png" required />
          </label>
          <div class="entity-form__actions">
            <button type="submit">Nachweis hochladen/ersetzen</button>
          </div>
        </form>
      </section>
    `;

    this.form = this.querySelector("#profile-form");
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.proofForm = this.querySelector("#trade-proof-form");
    this.proofForm.addEventListener("submit", (event) => this.handleProofSubmit(event));
    this.proofStatus = this.querySelector("#trade-proof-status");
    this.proofError = this.querySelector("#trade-proof-error");
    this.proofRetry = this.querySelector("#trade-proof-retry");
    this.proofRetry.addEventListener("click", () => this.loadProof());

    this.load();
    this.loadProof();
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

  async loadProof() {
    this.proofError.hidden = true;
    this.proofRetry.hidden = true;
    this.proofStatus.textContent = "Nachweis wird geladen …";
    try {
      const proof = await apiFetch("/partner/trade-proof");
      this.proofStatus.textContent = this.proofStatusText(proof);
      fillTradeRegisterIdentifier(this.proofForm, proof.registerIdentifier);
      if (proof.rejectionReason) {
        this.proofStatus.textContent += ` Grund: ${proof.rejectionReason}`;
      }
    } catch (error) {
      this.proofStatus.textContent = "Nachweis konnte nicht geladen werden.";
      this.proofError.textContent = error.message;
      this.proofError.hidden = false;
      this.proofRetry.hidden = false;
    }
  }

  async handleProofSubmit(event) {
    event.preventDefault();
    if (this.proofSubmitting) return;
    const file = this.proofForm.elements.proofFile.files[0];
    if (!file) return;
    this.proofSubmitting = true;
    const formData = new FormData();
    const identifier = readTradeRegisterIdentifier(this.proofForm);
    formData.append("registerCountry", identifier.country);
    formData.append("registerType", identifier.type);
    formData.append("registerValue", identifier.value);
    formData.append("file", file);
    try {
      const proof = await apiFetchForm("/partner/trade-proof", formData);
      this.proofStatus.textContent = this.proofStatusText(proof);
      this.proofError.hidden = true;
      this.proofForm.elements.proofFile.value = "";
      showToast("Nachweis hochgeladen und zur Prüfung eingereicht.", "success");
    } catch (error) {
      this.proofError.textContent = error.message;
      this.proofError.hidden = false;
      showToast(error.message, "error");
    } finally {
      this.proofSubmitting = false;
    }
  }

  proofStatusText(proof) {
    if (!proof.present) return "Noch kein Nachweis hinterlegt.";
    return {
      PENDING: "Prüfung ausstehend.",
      ACCEPTED: "Nachweis akzeptiert.",
      REJECTED: "Nachweis abgelehnt.",
    }[proof.status] ?? "Unbekannter Prüfstatus.";
  }
}

customElements.define("partner-profile-page", PartnerProfilePage);
