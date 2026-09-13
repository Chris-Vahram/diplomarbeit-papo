import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

class CompanyCategoryListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Artikelkategorien</h1>
        <button type="button" class="button-primary" id="new-category-btn">Neue Artikelkategorie</button>
      </div>
      <p id="category-load-error" class="form-field__error" hidden>
        Kategorien konnten nicht geladen werden.
        <button type="button" class="button-secondary" id="category-retry">Erneut laden</button>
      </p>
      <data-table id="category-table"></data-table>
      <modal-dialog id="category-modal" title="Neue Artikelkategorie">
        <form id="category-form" class="entity-form">
          <input type="hidden" name="id" />
          <form-field label="Name" name="name" required></form-field>
          <label class="checkbox-field"><input type="checkbox" name="active" checked /> Aktiv</label>
          <div class="entity-form__actions"><button type="submit">Speichern</button></div>
        </form>
      </modal-dialog>
    `;
    this.table = this.querySelector("#category-table");
    this.table.columns = [
      { key: "name", label: "Name" },
      { key: "activeLabel", label: "Status" },
      { key: "actions", label: "", html: true },
    ];
    this.table.emptyMessage = "Noch keine Artikelkategorien angelegt.";
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;
    this.table.filters = [{
      key: "activeLabel", type: "select", label: "Status",
      options: [{ value: "Aktiv", label: "Aktiv" }, { value: "Deaktiviert", label: "Deaktiviert" }],
    }];
    this.modal = this.querySelector("#category-modal");
    this.form = this.querySelector("#category-form");
    this.loadError = this.querySelector("#category-load-error");
    this.querySelector("#new-category-btn").addEventListener("click", () => this.openCreate());
    this.querySelector("#category-retry").addEventListener("click", () => this.load());
    this.table.addEventListener("click", (event) => {
      const button = event.target.closest("[data-edit-id]");
      if (button) this.openEdit(button.dataset.editId);
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.load();
  }

  async load() {
    this.loadError.hidden = true;
    try {
      this.categories = await apiFetch("/company/categories");
      this.table.rows = this.categories.map((category) => ({
        name: category.name,
        activeLabel: category.active ? "Aktiv" : "Deaktiviert",
        actions: `<button type="button" data-edit-id="${category.id}">Bearbeiten</button>`,
      }));
    } catch (error) {
      this.categories = [];
      this.table.rows = [];
      this.loadError.hidden = false;
      showToast(error.message, "error");
    }
  }

  openCreate() {
    this.form.reset();
    this.form.elements.id.value = "";
    this.form.elements.active.checked = true;
    this.modal.setAttribute("title", "Neue Artikelkategorie");
    this.modal.open();
  }

  openEdit(id) {
    const category = this.categories.find((item) => String(item.id) === String(id));
    if (!category) return;
    this.form.elements.id.value = category.id;
    this.form.elements.name.value = category.name;
    this.form.elements.active.checked = category.active;
    this.modal.setAttribute("title", "Artikelkategorie bearbeiten");
    this.modal.open();
  }

  async handleSubmit(event) {
    event.preventDefault();
    const button = this.form.querySelector("button[type='submit']");
    button.disabled = true;
    const id = this.form.elements.id.value;
    const payload = { name: this.form.elements.name.value, active: this.form.elements.active.checked };
    try {
      await apiFetch(id ? `/company/categories/${id}` : "/company/categories", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(id ? payload : { name: payload.name }),
      });
      showToast(id ? "Artikelkategorie aktualisiert." : "Artikelkategorie angelegt.", "success");
      this.modal.close();
      await this.load();
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      button.disabled = false;
    }
  }
}

customElements.define("company-category-list-page", CompanyCategoryListPage);
