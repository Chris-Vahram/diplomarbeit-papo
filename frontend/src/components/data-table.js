import { escapeHtml } from "../lib/html.js";

// Set .columns = [{ key, label, html?, image? }] and .rows = [{...}] as JS properties (not
// attributes). Cell values are HTML-escaped by default (row data may contain partner/Kunde/
// import-file text); pass `html: true` on a column def for cells that intentionally render
// markup (action buttons, status selects/badges, checkboxes). Pass `image: true` for a
// thumbnail cell — the raw value is treated as a URL, escaped into `src`, never as markup;
// `image` wins over `html` if both are set. Empty value or a failed image load renders a
// neutral `.data-table__thumb--empty` placeholder instead of a broken-image glyph.
// Optional .emptyMessage = "..." shown when rows is empty (e.g. FR-25 no-enabled-products case).
// Optional .searchKeys = [key, ...] + .pageSize = n opt this table into a client-side search
// input + pager. Search matches raw values at .searchKeys (never rendered cell HTML), so it
// never hits markup like action buttons or status selects. Both unset = identical to before.
class DataTable extends HTMLElement {
  constructor() {
    super();
    this._columns = [];
    this._rows = [];
    this._emptyMessage = "Keine Daten vorhanden.";
    this._searchKeys = null;
    this._pageSize = null;
    this._searchQuery = "";
    this._page = 1;
  }

  set columns(cols) {
    this._columns = cols ?? [];
    this.render();
  }

  set rows(rows) {
    this._rows = rows ?? [];
    this._page = 1;
    this.render();
  }

  set emptyMessage(message) {
    this._emptyMessage = message;
    this.render();
  }

  set searchKeys(keys) {
    this._searchKeys = keys ?? null;
    this.render();
  }

  set pageSize(size) {
    this._pageSize = size ?? null;
    this.render();
  }

  connectedCallback() {
    this.addEventListener("input", (event) => {
      if (!event.target.matches("[data-table-search]")) return;
      this._searchQuery = event.target.value;
      this._page = 1;
      const caret = event.target.selectionStart;
      this.render();
      const input = this.querySelector("[data-table-search]");
      if (input) {
        input.focus();
        input.setSelectionRange(caret, caret);
      }
    });
    this.addEventListener("click", (event) => {
      if (event.target.closest("[data-table-prev]")) {
        this._page = Math.max(1, this._page - 1);
        this.render();
      } else if (event.target.closest("[data-table-next]")) {
        this._page = this._page + 1;
        this.render();
      }
    });
    this.addEventListener(
      "error",
      (event) => {
        const img = event.target;
        if (!(img instanceof HTMLImageElement) || !img.classList.contains("data-table__thumb")) return;
        const placeholder = document.createElement("span");
        placeholder.className = "data-table__thumb data-table__thumb--empty";
        img.replaceWith(placeholder);
      },
      true,
    );
    this.render();
  }

  _renderImageCell(value) {
    if (!value) return `<span class="data-table__thumb data-table__thumb--empty"></span>`;
    return `<img class="data-table__thumb" src="${escapeHtml(value)}" alt="" loading="lazy">`;
  }

  _filteredRows() {
    const query = this._searchQuery.trim().toLowerCase();
    if (!this._searchKeys || !query) return this._rows;
    return this._rows.filter((row) =>
      this._searchKeys.some((key) => String(row[key] ?? "").toLowerCase().includes(query)),
    );
  }

  render() {
    if (this._rows.length === 0) {
      this.innerHTML = `<p class="data-table__empty">${this._emptyMessage}</p>`;
      return;
    }

    const searchHtml = this._searchKeys
      ? `<input type="search" class="data-table__search" data-table-search
          placeholder="Suchen…" value="${escapeHtml(this._searchQuery)}" />`
      : "";

    const filtered = this._filteredRows();
    if (filtered.length === 0) {
      this.innerHTML = `${searchHtml}<p class="data-table__empty">Keine Treffer.</p>`;
      return;
    }

    let pageRows = filtered;
    let totalPages = 1;
    if (this._pageSize) {
      totalPages = Math.max(1, Math.ceil(filtered.length / this._pageSize));
      if (this._page > totalPages) this._page = totalPages;
      const start = (this._page - 1) * this._pageSize;
      pageRows = filtered.slice(start, start + this._pageSize);
    }

    const head = this._columns.map((c) => `<th>${c.label}</th>`).join("");
    const body = pageRows
      .map((row) => {
        const cells = this._columns
          .map((c) => {
            const value = row[c.key] ?? "";
            if (c.image) return `<td>${this._renderImageCell(value)}</td>`;
            return `<td>${c.html ? value : escapeHtml(value)}</td>`;
          })
          .join("");
        return `<tr>${cells}</tr>`;
      })
      .join("");

    const pagerHtml =
      this._pageSize && totalPages > 1
        ? `<div class="data-table__pager">
            <button type="button" data-table-prev ${this._page <= 1 ? "disabled" : ""}>&laquo; Zurück</button>
            <span>Seite ${this._page} von ${totalPages}</span>
            <button type="button" data-table-next ${this._page >= totalPages ? "disabled" : ""}>Weiter &raquo;</button>
          </div>`
        : "";

    this.innerHTML = `
      ${searchHtml}
      <div class="data-table__scroll">
        <table class="data-table">
          <thead><tr>${head}</tr></thead>
          <tbody>${body}</tbody>
        </table>
      </div>
      ${pagerHtml}
    `;
  }
}

customElements.define("data-table", DataTable);
