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
// Optional .filters = [{ key, type: "select"|"daterange"|"text", label, options? }] renders
// filter controls next to the search input; predicates compose with search in _filteredRows()
// (all must match, client-side, consistent with the existing search/pagination — see design.md).
// "select" reads row[key] exactly; "daterange" reads row[key] as an ISO "YYYY-MM-DD" string;
// "text" matches row[key] case-insensitively as a substring, same semantics as search (no
// ASCII folding - umlauts must match themselves). A "Filter zurücksetzen" control is rendered
// automatically whenever search or filters are configured, clearing both in one click.
// Optional .rowKey = "id" makes rows clickable: a click anywhere in a row (that isn't on an
// interactive control already in the row - button/select/a/input) dispatches a bubbling
// "row-click" CustomEvent with detail.id = row[rowKey], for callers to open a details overlay.
// Optional per-row `rowClass` field (e.g. row.rowClass = "data-table__row--unseen") adds that
// class to the <tr> - for row-level state that isn't its own column (e.g. "new" highlighting).
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
    this._filterDefs = [];
    this._filterValues = {};
    this._rowKey = null;
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

  set rowKey(key) {
    this._rowKey = key ?? null;
    this.render();
  }

  set filters(defs) {
    this._filterDefs = defs ?? [];
    this._filterValues = {};
    for (const def of this._filterDefs) {
      this._filterValues[def.key] = def.type === "daterange" ? { from: "", to: "" } : "";
    }
    this.render();
  }

  connectedCallback() {
    this.addEventListener("input", (event) => {
      if (event.target.matches("[data-table-search]")) {
        this._searchQuery = event.target.value;
        this._page = 1;
        const caret = event.target.selectionStart;
        this.render();
        const input = this.querySelector("[data-table-search]");
        if (input) {
          input.focus();
          input.setSelectionRange(caret, caret);
        }
        return;
      }
      const textFilter = event.target.closest("[data-table-filter-text]");
      if (textFilter) {
        const key = textFilter.dataset.tableFilterText;
        this._filterValues[key] = textFilter.value;
        this._page = 1;
        const caret = textFilter.selectionStart;
        this.render();
        const input = this.querySelector(`[data-table-filter-text="${key}"]`);
        if (input) {
          input.focus();
          input.setSelectionRange(caret, caret);
        }
        return;
      }
      const dateFilter = event.target.closest("[data-table-filter-from], [data-table-filter-to]");
      if (dateFilter) {
        const key = dateFilter.dataset.tableFilterFrom ?? dateFilter.dataset.tableFilterTo;
        const attr = dateFilter.hasAttribute("data-table-filter-from") ? "data-table-filter-from" : "data-table-filter-to";
        const edge = attr === "data-table-filter-from" ? "from" : "to";
        this._filterValues[key][edge] = dateFilter.value;
        this._page = 1;
        this.render();
        this.querySelector(`[${attr}="${key}"]`)?.focus();
      }
    });
    this.addEventListener("change", (event) => {
      const select = event.target.closest("[data-table-filter]");
      if (select) {
        this._filterValues[select.dataset.tableFilter] = select.value;
        this._page = 1;
        this.render();
      }
    });
    this.addEventListener("click", (event) => {
      if (event.target.closest("[data-table-reset]")) {
        this._searchQuery = "";
        for (const def of this._filterDefs) {
          this._filterValues[def.key] = def.type === "daterange" ? { from: "", to: "" } : "";
        }
        this._page = 1;
        this.render();
        return;
      }
      if (event.target.closest("[data-table-prev]")) {
        this._page = Math.max(1, this._page - 1);
        this.render();
        return;
      }
      if (event.target.closest("[data-table-next]")) {
        this._page = this._page + 1;
        this.render();
        return;
      }
      if (this._rowKey && !event.target.closest("select, button, a, input, option")) {
        const tr = event.target.closest("tr[data-row-id]");
        if (tr) this.dispatchEvent(new CustomEvent("row-click", { detail: { id: tr.dataset.rowId }, bubbles: true }));
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

  _matchesFilters(row) {
    return this._filterDefs.every((def) => {
      const value = this._filterValues[def.key];
      if (def.type === "daterange") {
        const rowValue = row[def.key] ?? "";
        if (value.from && rowValue < value.from) return false;
        if (value.to && rowValue > value.to) return false;
        return true;
      }
      if (def.type === "text") {
        if (!value) return true;
        return String(row[def.key] ?? "").toLowerCase().includes(value.toLowerCase());
      }
      if (!value) return true;
      return String(row[def.key] ?? "") === value;
    });
  }

  _filteredRows() {
    const query = this._searchQuery.trim().toLowerCase();
    return this._rows
      .filter((row) => !query || !this._searchKeys || this._searchKeys.some((key) => String(row[key] ?? "").toLowerCase().includes(query)))
      .filter((row) => this._matchesFilters(row));
  }

  _renderFilterControl(def) {
    if (def.type === "text") {
      const value = this._filterValues[def.key] ?? "";
      return `
        <div class="data-table__filter">
          ${def.label ? `<span class="data-table__filter-label">${escapeHtml(def.label)}</span>` : ""}
          <input type="text" data-table-filter-text="${def.key}" placeholder="${escapeHtml(def.label ?? "")}" value="${escapeHtml(value)}" />
        </div>`;
    }
    if (def.type === "daterange") {
      const value = this._filterValues[def.key];
      return `
        <div class="data-table__filter data-table__filter--daterange">
          ${def.label ? `<span class="data-table__filter-label">${escapeHtml(def.label)}</span>` : ""}
          <input type="date" data-table-filter-from="${def.key}" value="${escapeHtml(value.from)}" />
          <span class="data-table__filter-sep">–</span>
          <input type="date" data-table-filter-to="${def.key}" value="${escapeHtml(value.to)}" />
        </div>`;
    }
    const value = this._filterValues[def.key] ?? "";
    const options = (def.options ?? [])
      .map((o) => `<option value="${escapeHtml(o.value)}" ${o.value === value ? "selected" : ""}>${escapeHtml(o.label)}</option>`)
      .join("");
    return `
      <div class="data-table__filter">
        ${def.label ? `<span class="data-table__filter-label">${escapeHtml(def.label)}</span>` : ""}
        <select data-table-filter="${def.key}" aria-label="${escapeHtml(def.label ?? "")}">
          <option value="">Alle</option>
          ${options}
        </select>
      </div>`;
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
    const filtersHtml = this._filterDefs.length
      ? `<div class="data-table__filters">${this._filterDefs.map((def) => this._renderFilterControl(def)).join("")}</div>`
      : "";
    const resetHtml =
      searchHtml || filtersHtml
        ? `<button type="button" class="data-table__reset" data-table-reset>Filter zurücksetzen</button>`
        : "";
    const toolbarHtml = searchHtml || filtersHtml
      ? `<div class="data-table__toolbar">${searchHtml}${filtersHtml}${resetHtml}</div>`
      : "";

    const filtered = this._filteredRows();
    if (filtered.length === 0) {
      this.innerHTML = `${toolbarHtml}<p class="data-table__empty">Keine Treffer.</p>`;
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
        const classes = [this._rowKey ? "data-table__row--clickable" : "", row.rowClass ?? ""]
          .filter(Boolean)
          .join(" ");
        const idAttr = this._rowKey ? ` data-row-id="${escapeHtml(String(row[this._rowKey] ?? ""))}"` : "";
        const classAttr = classes ? ` class="${escapeHtml(classes)}"` : "";
        return `<tr${idAttr}${classAttr}>${cells}</tr>`;
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
      ${toolbarHtml}
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
