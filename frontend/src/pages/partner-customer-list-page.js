import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatAddress, formatDate } from "../lib/format.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";

// Decided (Chris, 2026-07-13): 60 days for now, revisit with the client at the
// next Termin (design.md, decision 5).
const DORMANT_THRESHOLD_DAYS = 60;

// Whole-calendar-day diff, not elapsed milliseconds - avoids boundary
// misclassification across timezones/DST (design.md, decision 5b).
function daysSinceLastOrder(isoDate) {
  const [year, month, day] = isoDate.split("-").map(Number);
  const then = Date.UTC(year, month - 1, day);
  const now = new Date();
  const today = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
  return Math.round((today - then) / 86400000);
}

function isDormant(lastOrderDate) {
  if (!lastOrderDate) return true;
  return daysSinceLastOrder(lastOrderDate) > DORMANT_THRESHOLD_DAYS;
}

class PartnerCustomerListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <div class="list-header">
        <h1>Meine Endkunden</h1>
        <button type="button" class="button-primary" id="new-customer-btn">Neuer Endkunde</button>
      </div>
      <data-table id="customer-table"></data-table>
      <dialog class="customer-create-dialog" id="customer-create-dialog" aria-labelledby="customer-create-title">
        <form id="customer-create-form" class="entity-form entity-form--grid">
          <div class="customer-create-dialog__header">
            <h2 id="customer-create-title">Neuer Endkunde</h2>
            <button type="button" aria-label="Schließen" data-customer-dialog-close>&times;</button>
          </div>
          <form-field label="Nachname" name="lastName" required></form-field>
          <form-field label="Vorname" name="firstName"></form-field>
          ${addressFieldsHtml("customerAddress")}
          <form-field label="E-Mail" name="email" type="email"></form-field>
          <form-field label="Telefon" name="phone"></form-field>
          <div class="customer-create-dialog__unconfirmed" id="customer-unconfirmed-actions" hidden>
            <p>Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.</p>
            <button type="button" data-customer-check-list>Liste prüfen</button>
          </div>
          <div class="entity-form__actions">
            <button type="button" data-customer-dialog-close>Abbrechen</button>
            <button type="submit" class="button-primary">Speichern</button>
          </div>
        </form>
      </dialog>
      <div class="customer-create-status" id="customer-create-status" role="status" hidden>
        <p id="customer-create-status-message"></p>
        <button type="button" data-customer-retry-list hidden>Liste erneut laden</button>
        <button type="button" data-customer-resume hidden>Eingabe fortsetzen</button>
      </div>
    `;

    this.table = this.querySelector("#customer-table");
    this.table.columns = [
      { key: "name", label: "Name" },
      { key: "addressLabel", label: "Adresse" },
      { key: "lastOrderCell", label: "Letzte Bestellung", html: true },
      { key: "actions", label: "", html: true },
    ];
    this.table.filters = [
      {
        key: "aktivitaet",
        type: "select",
        label: "Aktivität",
        options: [
          { value: "inaktiv", label: "Länger keine Bestellung" },
          { value: "aktiv", label: "Kürzlich bestellt" },
        ],
      },
    ];
    this.table.emptyMessage = "Noch keine Endkunden angelegt.";
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.createButton = this.querySelector("#new-customer-btn");
    this.createDialog = this.querySelector("#customer-create-dialog");
    this.createForm = this.querySelector("#customer-create-form");
    this.unconfirmedActions = this.querySelector("#customer-unconfirmed-actions");
    this.createStatus = this.querySelector("#customer-create-status");
    this.createStatusMessage = this.querySelector("#customer-create-status-message");
    this.retryListButton = this.querySelector("[data-customer-retry-list]");
    this.resumeButton = this.querySelector("[data-customer-resume]");
    this.createPending = false;
    this.createdCustomerId = null;
    this.refreshPending = false;
    this.loadGeneration = 0;
    this.editingId = null;
    this.dialogOpener = this.createButton;
    this.createButton.addEventListener("click", () => this.openCreateDialog());
    this.table.addEventListener("click", (event) => {
      const button = event.target.closest("[data-edit-id]");
      if (button) this.openEditDialog(button);
    });
    this.createDialog.querySelectorAll("[data-customer-dialog-close]").forEach((button) => {
      button.addEventListener("click", () => this.closeCreateDialog());
    });
    this.createDialog.addEventListener("cancel", (event) => {
      event.preventDefault();
      this.closeCreateDialog();
    });
    this.createDialog.addEventListener("close", () =>
      (this.dialogOpener.isConnected ? this.dialogOpener : this.createButton).focus());
    this.createForm.addEventListener("submit", (event) => this.createCustomer(event));
    this.querySelector("[data-customer-check-list]").addEventListener("click", () => {
      this.closeCreateDialog();
      this.showStatus("Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.", { resume: true });
      this.load();
    });
    this.retryListButton.addEventListener("click", () => this.load(this.createdCustomerId));
    this.resumeButton.addEventListener("click", () => this.resumeCreateDialog());

    this.load();
  }

  openCreateDialog() {
    if (this.refreshPending) return;
    this.editingId = null;
    this.dialogOpener = this.createButton;
    this.querySelector("#customer-create-title").textContent = "Neuer Endkunde";
    this.createForm.reset();
    this.unconfirmedActions.hidden = true;
    this.hideStatus();
    this.createDialog.showModal();
    this.createForm.elements.lastName.focus();
  }

  // Edit reuses the create dialog; PUT is idempotent, so a failed save simply
  // keeps the dialog open for another attempt (no "unclear status" flow).
  openEditDialog(button) {
    if (this.refreshPending || this.createPending) return;
    const kunde = this.kunden?.find((k) => String(k.id) === button.dataset.editId);
    if (!kunde) return;
    this.editingId = kunde.id;
    this.dialogOpener = button;
    this.querySelector("#customer-create-title").textContent = "Endkunde bearbeiten";
    this.createForm.reset();
    this.unconfirmedActions.hidden = true;
    this.hideStatus();
    const { elements } = this.createForm;
    elements.lastName.value = kunde.lastName;
    elements.firstName.value = kunde.firstName ?? "";
    fillAddress(this.createForm, "customerAddress", kunde.address);
    elements.email.value = kunde.email ?? "";
    elements.phone.value = kunde.phone ?? "";
    this.createDialog.showModal();
    elements.lastName.focus();
  }

  resumeCreateDialog() {
    this.createDialog.showModal();
    this.createForm.elements.lastName.focus();
  }

  closeCreateDialog() {
    if (this.createPending) return;
    this.createDialog.close();
  }

  setCreatePending(pending) {
    this.createPending = pending;
    this.createForm.querySelectorAll("input, button").forEach((control) => {
      control.disabled = pending;
    });
  }

  showStatus(message, { retry = false, resume = false } = {}) {
    this.createStatus.hidden = false;
    this.createStatusMessage.textContent = message;
    this.retryListButton.hidden = !retry;
    this.resumeButton.hidden = !resume;
  }

  hideStatus() {
    this.createStatus.hidden = true;
  }

  async createCustomer(event) {
    event.preventDefault();
    if (this.createPending || !this.createForm.reportValidity()) return;

    const payload = {
      lastName: this.createForm.elements.lastName.value,
      firstName: this.createForm.elements.firstName.value || null,
      address: readAddress(this.createForm, "customerAddress"),
      email: this.createForm.elements.email.value || null,
      phone: this.createForm.elements.phone.value || null,
    };
    this.setCreatePending(true);

    if (this.editingId) {
      try {
        await apiFetch(`/partner/customers/${this.editingId}`, { method: "PUT", body: JSON.stringify(payload) });
        this.setCreatePending(false);
        this.closeCreateDialog();
        showToast("Endkunde aktualisiert.", "success");
        this.load();
      } catch (error) {
        this.setCreatePending(false);
        showToast(error.message, "error");
      }
      return;
    }

    try {
      const customer = await apiFetch("/partner/customers", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      this.setCreatePending(false);
      this.closeCreateDialog();
      showToast("Endkunde angelegt.", "success");
      this.createdCustomerId = customer.id;
      this.refreshPending = true;
      this.createButton.disabled = true;
      this.load(customer.id);
    } catch (error) {
      this.setCreatePending(false);
      this.unconfirmedActions.hidden = false;
      this.showStatus("Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.", { resume: true });
      showToast(error.message, "error");
      showToast("Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.", "error");
    }
  }

  async load(createdCustomerId = null) {
    const generation = ++this.loadGeneration;
    try {
      const kunden = await apiFetch("/partner/customers");
      if (!this.isConnected || generation !== this.loadGeneration) return;
      this.kunden = kunden;
      const rows = kunden
        .map((k) => {
          const dormant = isDormant(k.lastOrderDate);
          const dateText = k.lastOrderDate ? formatDate(k.lastOrderDate) : "Noch keine Bestellung";
          const badge = dormant ? ` <span class="dormant-badge">Inaktiv</span>` : "";
          return {
            id: k.id,
            name: `${k.firstName ? k.firstName + " " : ""}${k.lastName}`,
            addressLabel: formatAddress(k.address),
            lastOrderCell: `${dateText}${badge}`,
            lastOrderDate: k.lastOrderDate,
            aktivitaet: dormant ? "inaktiv" : "aktiv",
            rowClass: dormant ? "data-table__row--dormant" : "",
            actions: `<button type="button" data-edit-id="${k.id}">Bearbeiten</button>`,
          };
        })
        // Oldest-first default: never-ordered (null) Endkunden first, then oldest
        // to newest last order (design.md, decision 6).
        .sort((a, b) => {
          if (createdCustomerId && a.id === createdCustomerId) return -1;
          if (createdCustomerId && b.id === createdCustomerId) return 1;
          if (!a.lastOrderDate) return b.lastOrderDate ? -1 : 0;
          if (!b.lastOrderDate) return 1;
          return a.lastOrderDate < b.lastOrderDate ? -1 : a.lastOrderDate > b.lastOrderDate ? 1 : 0;
        });
      this.table.rows = rows;
      if (createdCustomerId) {
        this.table.querySelector("[data-table-reset]")?.click();
        this.createdCustomerId = null;
        this.refreshPending = false;
        this.createButton.disabled = false;
        this.hideStatus();
      }
    } catch (error) {
      if (!this.isConnected || generation !== this.loadGeneration) return;
      if (createdCustomerId) {
        this.showStatus("Endkunde angelegt. Liste konnte nicht aktualisiert werden.", { retry: true });
        return;
      }
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-customer-list-page", PartnerCustomerListPage);
