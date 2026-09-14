import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatNumber } from "../lib/format.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";

// Commission dropdown presets per FSD FR-19/partner-management spec: 5/10/15 or a custom value.
const COMMISSION_PRESETS = ["5", "10", "15"];

class CompanyPartnerListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Partnerverwaltung</h1>
        <button type="button" class="button-primary" id="new-partner-btn">Neuer Partner</button>
      </div>
      <data-table id="partner-table"></data-table>
      <modal-dialog id="partner-modal" title="Neuer Partner">
        <form id="partner-form" class="entity-form entity-form--grid">
          <input type="hidden" name="id" />
          <form-field label="Partnernummer" name="partnerNumber" required></form-field>
          <p class="partner-form__hint" id="partner-number-hint" hidden>
            Die Partnernummer ist der Login des Partners (Keycloak-Benutzername). Bei einer Änderung muss der Keycloak-Benutzername entsprechend angepasst werden.
          </p>
          <form-field label="Name" name="name" required></form-field>
          ${addressFieldsHtml("address")}
          <form-field label="E-Mail" name="email" type="email"></form-field>
          <form-field label="Telefon" name="phone"></form-field>
          <form-field class="form-field--wide" label="Interne Notiz – nur für das Unternehmen" name="internalNote">
            <textarea name="internalNote" maxlength="4000" rows="5"></textarea>
          </form-field>
          <form-field label="Provision" name="commissionPercentPreset">
            <select name="commissionPercentPreset">
              <option value="5">5%</option>
              <option value="10">10%</option>
              <option value="15">15%</option>
              <option value="custom">Individuell</option>
            </select>
          </form-field>
          <form-field label="Individueller Wert (%)" name="commissionPercentCustom" type="number" step="0.01" min="0" max="100"></form-field>
          <label class="checkbox-field">
            <input type="checkbox" name="active" checked />
            Aktiv
          </label>
          <div class="entity-form__actions">
            <button type="submit">Speichern</button>
          </div>
        </form>
      </modal-dialog>
      <modal-dialog id="enablement-modal" title="Artikel freischalten">
        <data-table id="enablement-table"></data-table>
      </modal-dialog>
    `;

    this.table = this.querySelector("#partner-table");
    this.table.columns = [
      { key: "partnerNumber", label: "Nr." },
      { key: "name", label: "Name" },
      { key: "commissionLabel", label: "Provision" },
      { key: "statusBadge", label: "Status", html: true },
      { key: "actions", label: "", html: true },
    ];
    this.table.emptyMessage = "Noch keine Partner angelegt.";
    this.table.searchKeys = ["name", "partnerNumber"];
    this.table.pageSize = 20;
    this.table.filters = [
      {
        key: "activeLabel",
        type: "select",
        label: "Status",
        options: [
          { value: "active", label: "Aktiv" },
          { value: "inactive", label: "Inaktiv" },
        ],
      },
    ];

    this.modal = this.querySelector("#partner-modal");
    this.form = this.querySelector("#partner-form");
    this.partnerNumberHint = this.querySelector("#partner-number-hint");
    this.customField = this.querySelector('form-field[name="commissionPercentCustom"]');
    this.presetSelect = this.form.elements.commissionPercentPreset;

    this.enablementModal = this.querySelector("#enablement-modal");
    this.enablementTable = this.querySelector("#enablement-table");
    this.enablementTable.columns = [
      { key: "name", label: "Name" },
      { key: "priceLabel", label: "Preis (€)" },
      { key: "category", label: "Artikelkategorie" },
      { key: "enabledCheckbox", label: "Freigeschaltet", html: true },
    ];
    this.enablementTable.emptyMessage = "Noch keine Artikel angelegt.";

    this.presetSelect.addEventListener("change", () => this.toggleCustomField());

    this.querySelector("#new-partner-btn").addEventListener("click", () => this.openCreate());
    this.table.addEventListener("click", (event) => {
      const editButton = event.target.closest("[data-edit-id]");
      if (editButton) this.openEdit(editButton.dataset.editId);
      const manageButton = event.target.closest("[data-manage-id]");
      if (manageButton) this.openEnablement(manageButton.dataset.manageId);
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.enablementTable.addEventListener("change", (event) => {
      const checkbox = event.target.closest("[data-toggle-product-id]");
      if (checkbox) this.toggleEnablement(checkbox.dataset.toggleProductId, checkbox.checked);
    });

    this.toggleCustomField();
    this.load();
  }

  toggleCustomField() {
    const isCustom = this.presetSelect.value === "custom";
    this.customField.hidden = !isCustom;
    // Required only while visible, so an empty custom value can no longer
    // silently submit as 0% (Number("") === 0) — the browser blocks submit instead.
    const customInput = this.customField.querySelector("input");
    if (customInput) customInput.required = isCustom;
  }

  async load() {
    try {
      this.partners = await apiFetch("/company/partners");
      this.table.rows = this.partners.map((p) => ({
        partnerNumber: p.partnerNumber,
        name: p.name,
        commissionLabel: `${p.commissionPercent}%`,
        activeLabel: p.active ? "active" : "inactive",
        statusBadge: `<status-badge status="${p.active ? "active" : "inactive"}"></status-badge>`,
        actions: `<div class="row-actions">
          <button type="button" data-edit-id="${p.id}">Partnerdaten Bearbeiten</button>
          <button type="button" class="row-actions__secondary" data-manage-id="${p.id}">Artikel</button>
        </div>`,
      }));
      const requestedPartnerId = new URLSearchParams(location.hash.split("?")[1] ?? "").get("enablement");
      if (requestedPartnerId) await this.openEnablement(requestedPartnerId);
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  openCreate() {
    this.form.reset();
    this.form.elements.id.value = "";
    this.presetSelect.value = "5";
    this.toggleCustomField();
    this.partnerNumberHint.hidden = true;
    this.modal.setAttribute("title", "Neuer Partner");
    this.modal.open();
  }

  openEdit(id) {
    const partner = this.partners.find((p) => String(p.id) === String(id));
    if (!partner) return;
    this.form.elements.id.value = partner.id;
    this.form.elements.partnerNumber.value = partner.partnerNumber;
    this.form.elements.name.value = partner.name;
    fillAddress(this.form, "address", partner.address);
    this.form.elements.email.value = partner.email ?? "";
    this.form.elements.phone.value = partner.phone ?? "";
    this.form.elements.internalNote.value = partner.internalNote ?? "";
    this.form.elements.active.checked = partner.active;
    this.partnerNumberHint.hidden = false;

    const commissionStr = String(partner.commissionPercent);
    if (COMMISSION_PRESETS.includes(commissionStr)) {
      this.presetSelect.value = commissionStr;
    } else {
      this.presetSelect.value = "custom";
      this.customField.value = commissionStr;
    }
    this.toggleCustomField();

    this.modal.setAttribute("title", "Partner bearbeiten");
    this.modal.open();
  }

  async openEnablement(partnerId) {
    const partner = this.partners.find((p) => String(p.id) === String(partnerId));
    if (!partner) return;
    this.enablementPartnerId = partnerId;
    this.enablementModal.setAttribute("title", `Artikel freischalten – ${partner.name}`);
    this.enablementModal.open();
    await this.loadEnablement();
  }

  async loadEnablement() {
    try {
      const products = await apiFetch(`/company/partners/${this.enablementPartnerId}/products`);
      this.enablementTable.rows = products.map((p) => ({
        name: p.name,
        priceLabel: formatNumber(p.price),
        category: p.category ?? "–",
        enabledCheckbox: `<input type="checkbox" data-toggle-product-id="${p.productId}" ${p.enabled ? "checked" : ""} />`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async toggleEnablement(productId, enabled) {
    try {
      await apiFetch(`/company/partners/${this.enablementPartnerId}/products/${productId}`, {
        method: enabled ? "PUT" : "DELETE",
      });
      showToast(enabled ? "Artikel freigeschaltet." : "Artikel gesperrt.", "success");
    } catch (error) {
      showToast(error.message, "error");
      await this.loadEnablement();
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(this.form);
    const id = data.get("id");
    const preset = data.get("commissionPercentPreset");
    const commissionPercent = preset === "custom" ? Number(data.get("commissionPercentCustom")) : Number(preset);

    const payload = {
      partnerNumber: data.get("partnerNumber"),
      name: data.get("name"),
      address: readAddress(this.form, "address"),
      email: data.get("email") || null,
      phone: data.get("phone") || null,
      internalNote: data.get("internalNote"),
      commissionPercent,
      active: this.form.elements.active.checked,
    };

    try {
      if (id) {
        await apiFetch(`/company/partners/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        showToast("Partner aktualisiert.", "success");
      } else {
        await apiFetch("/company/partners", { method: "POST", body: JSON.stringify(payload) });
        showToast("Partner angelegt.", "success");
      }
      this.modal.close();
      this.load();
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("company-partner-list-page", CompanyPartnerListPage);
