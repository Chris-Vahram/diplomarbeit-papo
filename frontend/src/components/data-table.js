// Set .columns = [{ key, label }] and .rows = [{...}] as JS properties (not attributes).
// Optional .emptyMessage = "..." shown when rows is empty (e.g. FR-25 no-enabled-products case).
class DataTable extends HTMLElement {
  constructor() {
    super();
    this._columns = [];
    this._rows = [];
    this._emptyMessage = "Keine Daten vorhanden.";
  }

  set columns(cols) {
    this._columns = cols ?? [];
    this.render();
  }

  set rows(rows) {
    this._rows = rows ?? [];
    this.render();
  }

  set emptyMessage(message) {
    this._emptyMessage = message;
    this.render();
  }

  connectedCallback() {
    this.render();
  }

  render() {
    if (this._rows.length === 0) {
      this.innerHTML = `<p class="data-table__empty">${this._emptyMessage}</p>`;
      return;
    }

    const head = this._columns.map((c) => `<th>${c.label}</th>`).join("");
    const body = this._rows
      .map((row) => {
        const cells = this._columns
          .map((c) => `<td>${row[c.key] ?? ""}</td>`)
          .join("");
        return `<tr>${cells}</tr>`;
      })
      .join("");

    this.innerHTML = `
      <table class="data-table">
        <thead><tr>${head}</tr></thead>
        <tbody>${body}</tbody>
      </table>
    `;
  }
}

customElements.define("data-table", DataTable);
