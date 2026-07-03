import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";

class PartnerProductListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Produkte</h1>
      <data-table id="product-table"></data-table>
    `;

    this.table = this.querySelector("#product-table");
    this.table.columns = [
      { key: "name", label: "Name" },
      { key: "priceLabel", label: "Preis (€)" },
      { key: "category", label: "Kategorie" },
    ];
    this.table.emptyMessage = "Noch keine Produkte für Sie freigeschaltet.";

    this.load();
  }

  async load() {
    try {
      const products = await apiFetch("/partner/products");
      this.table.rows = products.map((p) => ({
        name: p.name,
        priceLabel: p.price.toFixed(2),
        category: p.category ?? "–",
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-product-list-page", PartnerProductListPage);
