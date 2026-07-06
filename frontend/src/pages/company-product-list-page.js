import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatNumber } from "../lib/format.js";

class CompanyProductListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Produkte</h1>
        <button type="button" class="button-primary" id="new-product-btn">Neues Produkt</button>
      </div>
      <data-table id="product-table"></data-table>
      <modal-dialog id="product-modal" title="Neues Produkt">
        <form id="product-form" class="entity-form entity-form--grid">
          <input type="hidden" name="id" />
          <form-field label="Name" name="name" required></form-field>
          <form-field label="Preis (€)" name="price" type="number" step="0.01" min="0" required></form-field>
          <form-field class="form-field--wide" label="Beschreibung" name="description">
            <textarea name="description"></textarea>
          </form-field>
          <form-field label="Kategorie" name="category"></form-field>
          <form-field label="Bild-URL" name="imageUrl"></form-field>
          <label class="checkbox-field">
            <input type="checkbox" name="available" checked />
            Verfügbar
          </label>
          <div class="entity-form__actions">
            <button type="submit">Speichern</button>
          </div>
        </form>
      </modal-dialog>
    `;

    this.table = this.querySelector("#product-table");
    this.table.columns = [
      { key: "imageUrl", label: "Bild", image: true },
      { key: "name", label: "Name" },
      { key: "priceLabel", label: "Preis (€)" },
      { key: "category", label: "Kategorie" },
      { key: "availableLabel", label: "Verfügbar" },
      { key: "actions", label: "", html: true },
    ];
    this.table.emptyMessage = "Noch keine Produkte angelegt.";
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.modal = this.querySelector("#product-modal");
    this.form = this.querySelector("#product-form");

    this.querySelector("#new-product-btn").addEventListener("click", () => this.openCreate());
    this.table.addEventListener("click", (event) => {
      const button = event.target.closest("[data-edit-id]");
      if (button) this.openEdit(button.dataset.editId);
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));

    this.load();
  }

  async load() {
    try {
      this.products = await apiFetch("/company/products");
      this.table.rows = this.products.map((p) => ({
        imageUrl: p.imageUrl ?? "",
        name: p.name,
        priceLabel: formatNumber(p.price),
        category: p.category ?? "–",
        availableLabel: p.available ? "Ja" : "Nein",
        actions: `<button type="button" data-edit-id="${p.id}">Bearbeiten</button>`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  openCreate() {
    this.form.reset();
    this.form.elements.id.value = "";
    this.modal.setAttribute("title", "Neues Produkt");
    this.modal.open();
  }

  openEdit(id) {
    const product = this.products.find((p) => String(p.id) === String(id));
    if (!product) return;
    this.form.elements.id.value = product.id;
    this.form.elements.name.value = product.name;
    this.form.elements.price.value = product.price;
    this.form.elements.description.value = product.description ?? "";
    this.form.elements.category.value = product.category ?? "";
    this.form.elements.imageUrl.value = product.imageUrl ?? "";
    this.form.elements.available.checked = product.available;
    this.modal.setAttribute("title", "Produkt bearbeiten");
    this.modal.open();
  }

  async handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(this.form);
    const id = data.get("id");
    const payload = {
      name: data.get("name"),
      price: Number(data.get("price")),
      description: data.get("description") || null,
      category: data.get("category") || null,
      imageUrl: data.get("imageUrl") || null,
      available: this.form.elements.available.checked,
    };

    try {
      if (id) {
        await apiFetch(`/company/products/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        showToast("Produkt aktualisiert.", "success");
      } else {
        await apiFetch("/company/products", { method: "POST", body: JSON.stringify(payload) });
        showToast("Produkt angelegt.", "success");
      }
      this.modal.close();
      this.load();
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("company-product-list-page", CompanyProductListPage);
