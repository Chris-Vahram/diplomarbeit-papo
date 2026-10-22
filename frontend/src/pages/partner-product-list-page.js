import { apiFetch } from "../lib/api.js";
import { formatNumber } from "../lib/format.js";
import { resolveImageUrls } from "../lib/product-images.js";

class PartnerProductListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Artikel</h1>
      <data-table id="product-table"></data-table>
    `;

    this.table = this.querySelector("#product-table");
    this.table.columns = [
      { key: "imageUrl", label: "Bild", image: true, mobile: "hide" },
      { key: "name", label: "Name", mobile: "title" },
      { key: "priceLabel", label: "Preis (€)", mobile: "amount" },
      { key: "category", label: "Artikelkategorie", mobile: "meta" },
    ];
    this.table.emptyMessage = "Noch keine Artikel für Sie freigeschaltet.";
    this.table.emptyHint = "Die Firma schaltet Artikel für Sie frei.";
    this.table.retry = () => this.load();
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.load();
  }

  async load() {
    try {
      const products = await apiFetch("/partner/products");
      const categories = [...new Set(products.map((p) => p.category).filter(Boolean))].sort();
      this.table.filters = [
        { key: "category", type: "select", label: "Artikelkategorie", options: categories.map((c) => ({ value: c, label: c })) },
      ];
      const rows = products.map((p) => ({
        imageUrl: p.imageUrl ?? "",
        name: p.name,
        priceLabel: formatNumber(p.price),
        category: p.category ?? "–",
      }));
      this.table.rows = await resolveImageUrls(rows);
    } catch (error) {
      this.table.error = error.message;
    }
  }
}

customElements.define("partner-product-list-page", PartnerProductListPage);
