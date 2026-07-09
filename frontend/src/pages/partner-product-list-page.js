import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatNumber } from "../lib/format.js";
import { resolveImageUrls } from "../lib/product-images.js";

class PartnerProductListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Produkte</h1>
      <data-table id="product-table"></data-table>
    `;

    this.table = this.querySelector("#product-table");
    this.table.columns = [
      { key: "imageUrl", label: "Bild", image: true },
      { key: "name", label: "Name" },
      { key: "priceLabel", label: "Preis (€)" },
      { key: "category", label: "Kategorie" },
    ];
    this.table.emptyMessage = "Noch keine Produkte für Sie freigeschaltet.";
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.load();
  }

  async load() {
    try {
      const products = await apiFetch("/partner/products");
      const categories = [...new Set(products.map((p) => p.category).filter(Boolean))].sort();
      this.table.filters = [
        { key: "category", type: "select", label: "Kategorie", options: categories.map((c) => ({ value: c, label: c })) },
      ];
      const rows = products.map((p) => ({
        imageUrl: p.imageUrl ?? "",
        name: p.name,
        priceLabel: formatNumber(p.price),
        category: p.category ?? "–",
      }));
      this.table.rows = await resolveImageUrls(rows);
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-product-list-page", PartnerProductListPage);
