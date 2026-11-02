import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatEuro } from "../lib/format.js";
import { netOf, priceStackHtml } from "../lib/price.js";

// Allowed USt rates (article-tax spec); the backend validates the same set.
const TAX_RATES = [
  { value: "20", label: "20 % (Normalsatz)" },
  { value: "13", label: "13 % (ermäßigt)" },
  { value: "10", label: "10 % (ermäßigt)" },
  { value: "0", label: "0 % (steuerfrei)" },
];
const percentFormat = new Intl.NumberFormat("de-AT", { maximumFractionDigits: 2 });
import { resolveImageUrls } from "../lib/product-images.js";

class CompanyProductListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Artikelverwaltung</h1>
        <div class="list-header__actions">
          <a class="button-secondary" href="#/company/categories">Kategorien verwalten</a>
          <button type="button" class="button-primary" id="new-product-btn">Neuer Artikel</button>
        </div>
      </div>
      <data-table id="product-table"></data-table>
      <modal-dialog id="product-modal" title="Neuer Artikel">
        <form id="product-form" class="entity-form entity-form--grid">
          <input type="hidden" name="id" />
          <form-field class="form-field--wide" label="Name" name="name" required></form-field>
          <form-field label="Bruttopreis inkl. USt (€)" name="price" type="number" step="0.01" min="0" required></form-field>
          <form-field label="Steuersatz" name="taxRatePercent">
            <select name="taxRatePercent">
              ${TAX_RATES.map((r) => `<option value="${r.value}">${r.label}</option>`).join("")}
            </select>
          </form-field>
          <output class="price-preview form-field--wide" name="netPreview" for="price taxRatePercent" aria-live="polite"></output>
          <form-field class="form-field--wide" label="Beschreibung" name="description">
            <textarea name="description"></textarea>
          </form-field>
          <form-field label="Artikelkategorie" name="categoryId">
            <select name="categoryId"><option value="">Keine Artikelkategorie</option></select>
          </form-field>
          <image-chooser name="imageUrl" class="form-field--wide"></image-chooser>
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
      { key: "imageUrl", label: "Bild", image: true, mobile: "hide" },
      { key: "name", label: "Name", mobile: "title" },
      { key: "priceLabel", label: "Preis inkl. USt", mobile: "amount", html: true },
      { key: "taxLabel", label: "USt" },
      { key: "category", label: "Artikelkategorie", mobile: "meta" },
      { key: "availableLabel", label: "Verfügbar", mobile: "status" },
      { key: "actions", label: "", html: true },
    ];
    this.table.emptyMessage = "Noch keine Artikel angelegt.";
    this.table.retry = () => this.load();
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.modal = this.querySelector("#product-modal");
    this.form = this.querySelector("#product-form");
    this.imageChooser = this.querySelector("image-chooser");

    this.querySelector("#new-product-btn").addEventListener("click", () => this.openCreate());
    this.table.addEventListener("click", (event) => {
      const button = event.target.closest("[data-edit-id]");
      if (button) this.openEdit(button.dataset.editId);
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.form.addEventListener("input", () => this.renderNetPreview());
    this.form.addEventListener("change", () => this.renderNetPreview());

    this.load();
  }

  async load() {
    try {
      [this.products, this.categories] = await Promise.all([
        apiFetch("/company/products"),
        apiFetch("/company/categories"),
      ]);
      const categories = [...new Set(this.products.map((p) => p.category).filter(Boolean))].sort();
      this.table.filters = [
        { key: "category", type: "select", label: "Artikelkategorie", options: categories.map((c) => ({ value: c, label: c })) },
        {
          key: "availableLabel",
          type: "select",
          label: "Verfügbar",
          options: [
            { value: "Ja", label: "Verfügbar" },
            { value: "Nein", label: "Nicht verfügbar" },
          ],
        },
      ];
      this.renderCategoryOptions();
      const rows = this.products.map((p) => ({
        imageUrl: p.imageUrl ?? "",
        name: p.name,
        priceLabel: priceStackHtml(p.price, p.netPrice),
        taxLabel: `${percentFormat.format(p.taxRatePercent)} %`,
        category: p.category ?? "–",
        availableLabel: p.available ? "Ja" : "Nein",
        actions: `<button type="button" data-edit-id="${p.id}">Bearbeiten</button>`,
      }));
      this.table.rows = await resolveImageUrls(rows);
    } catch (error) {
      this.table.error = error.message;
    }
  }

  openCreate() {
    this.form.reset();
    this.form.elements.id.value = "";
    this.renderCategoryOptions();
    this.imageChooser.reset();
    this.form.elements.taxRatePercent.value = "20";
    this.renderNetPreview();
    this.modal.setAttribute("title", "Neuer Artikel");
    this.modal.open();
  }

  openEdit(id) {
    const product = this.products.find((p) => String(p.id) === String(id));
    if (!product) return;
    this.form.elements.id.value = product.id;
    this.form.elements.name.value = product.name;
    this.form.elements.price.value = product.price;
    this.form.elements.taxRatePercent.value = String(Number(product.taxRatePercent));
    this.renderNetPreview();
    this.form.elements.description.value = product.description ?? "";
    this.renderCategoryOptions(product.categoryId ?? "");
    this.imageChooser.value = product.imageUrl ?? "";
    this.form.elements.available.checked = product.available;
    this.modal.setAttribute("title", "Artikel bearbeiten");
    this.modal.open();
  }

  async handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(this.form);
    const id = data.get("id");
    const payload = {
      name: data.get("name"),
      price: Number(data.get("price")),
      taxRatePercent: Number(data.get("taxRatePercent")),
      description: data.get("description") || null,
      categoryId: data.get("categoryId") ? Number(data.get("categoryId")) : null,
      imageUrl: data.get("imageUrl") || null,
      available: this.form.elements.available.checked,
    };

    try {
      if (id) {
        await apiFetch(`/company/products/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        showToast("Artikel aktualisiert.", "success");
      } else {
        await apiFetch("/company/products", { method: "POST", body: JSON.stringify(payload) });
        showToast("Artikel angelegt.", "success");
      }
      this.modal.close();
      this.load();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  // Live net/USt split of the entered gross price - the company enters what the end customer pays.
  renderNetPreview() {
    const output = this.form.elements.netPreview;
    const raw = this.form.elements.price.value;
    const gross = Number(raw);
    if (raw === "" || !Number.isFinite(gross) || gross < 0) {
      output.textContent = "Netto und USt werden aus dem Bruttopreis berechnet.";
      return;
    }
    const net = netOf(gross, this.form.elements.taxRatePercent.value);
    output.textContent = `Netto ${formatEuro(net)} · USt ${formatEuro(gross - net)}`;
  }

  renderCategoryOptions(selectedId = this.form.elements.categoryId.value) {
    const select = this.form.elements.categoryId;
    select.innerHTML = `<option value="">Keine Artikelkategorie</option>`;
    for (const category of this.categories ?? []) {
      if (category.active || String(category.id) === String(selectedId)) {
        const option = document.createElement("option");
        option.value = category.id;
        option.textContent = `${category.name}${category.active ? "" : " (deaktiviert)"}`;
        select.appendChild(option);
      }
    }
    select.value = selectedId;
  }
}

customElements.define("company-product-list-page", CompanyProductListPage);
