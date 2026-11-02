import { apiFetchForm } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { escapeHtml } from "../lib/html.js";

function looksNet(header) {
  return /netto|\bnet\b/i.test(header ?? "");
}

class CompanyImportPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Datenimport</h1>
      <div class="import-controls">
        <label>
          Typ
          <select id="entity-select">
            <option value="partners">Partner</option>
            <option value="products">Artikel</option>
          </select>
        </label>
        <label>
          Datei (.csv oder .xlsx)
          <input type="file" id="file-input" accept=".csv,.xlsx" />
        </label>
        <button type="button" id="preview-btn" class="button-primary">Vorschau laden</button>
      </div>
      <div id="import-content"></div>
    `;

    this.entitySelect = this.querySelector("#entity-select");
    this.fileInput = this.querySelector("#file-input");
    this.content = this.querySelector("#import-content");
    this.querySelector("#preview-btn").addEventListener("click", () => this.loadPreview());
  }

  async loadPreview() {
    const file = this.fileInput.files[0];
    if (!file) {
      showToast("Bitte zuerst eine Datei auswählen.", "error");
      return;
    }
    this.currentFile = file;
    this.currentEntity = this.entitySelect.value;

    const formData = new FormData();
    formData.append("file", file);

    try {
      const preview = await apiFetchForm(`/company/import/${this.currentEntity}/preview`, formData);
      this.renderMapping(preview);
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderMapping(preview) {
    const fieldRows = preview.fields
      .map(
        (field) => `
          <div class="import-field-row">
            <label>
              ${field.label}${field.required ? " *" : ""}
              <select data-field-key="${field.key}">
                <option value="">– nicht zuordnen –</option>
                ${preview.headers
                  .map(
                    (header) =>
                      `<option value="${escapeHtml(header)}" ${preview.suggestedMapping[field.key] === header ? "selected" : ""}>${escapeHtml(header)}</option>`
                  )
                  .join("")}
              </select>
            </label>
          </div>
          ${this.currentEntity === "products" && field.key === "price" ? this.priceTypeRowHtml(preview.suggestedMapping.price) : ""}
        `
      )
      .join("");

    const sampleHead = preview.headers.map((h) => `<th>${escapeHtml(h)}</th>`).join("");
    const sampleBody = preview.sampleRows
      .map((row) => `<tr>${preview.headers.map((h) => `<td>${escapeHtml(row[h] ?? "")}</td>`).join("")}</tr>`)
      .join("");

    this.content.innerHTML = `
      <h2>Spaltenzuordnung (${preview.totalRows} Zeile${preview.totalRows === 1 ? "" : "n"} erkannt)</h2>
      <div id="mapping-fields">${fieldRows}</div>
      <h3>Beispieldaten</h3>
      <div class="data-table__scroll"><table class="data-table"><thead><tr>${sampleHead}</tr></thead><tbody>${sampleBody}</tbody></table></div>
      <button type="button" id="commit-btn" class="button-primary">Import bestätigen</button>
    `;
    this.querySelector("#commit-btn").addEventListener("click", () => this.commitImport());
    this.wirePriceType();
  }

  // Article import (data-import spec): the company says whether the mapped price column holds
  // net or gross prices; net prices are converted with the row's tax rate (default 20 %).
  priceTypeRowHtml(suggestedHeader) {
    const net = looksNet(suggestedHeader);
    return `
      <div class="import-field-row import-field-row--sub">
        <label>
          Preisspalte enthält
          <select id="price-type-select">
            <option value="GROSS" ${net ? "" : "selected"}>Bruttopreise (inkl. USt)</option>
            <option value="NET" ${net ? "selected" : ""}>Nettopreise (ohne USt)</option>
          </select>
        </label>
        <p class="import-field-row__hint">Nettopreise werden mit dem Steuersatz der Zeile in Bruttopreise umgerechnet, ohne Steuersatz mit 20 %. Im Portal wird immer der Bruttopreis gespeichert.</p>
      </div>`;
  }

  // Follows the price-column choice ("Preis netto" -> Nettopreise) until the company picks a type itself.
  wirePriceType() {
    const typeSelect = this.content.querySelector("#price-type-select");
    const priceSelect = this.content.querySelector('[data-field-key="price"]');
    if (!typeSelect || !priceSelect) return;
    let chosen = false;
    typeSelect.addEventListener("change", () => { chosen = true; });
    priceSelect.addEventListener("change", () => {
      if (!chosen) typeSelect.value = looksNet(priceSelect.value) ? "NET" : "GROSS";
    });
  }

  async commitImport() {
    const mapping = {};
    this.content.querySelectorAll("[data-field-key]").forEach((select) => {
      if (select.value) mapping[select.dataset.fieldKey] = select.value;
    });

    const formData = new FormData();
    formData.append("file", this.currentFile);
    formData.append("mapping", JSON.stringify(mapping));
    const priceType = this.content.querySelector("#price-type-select");
    if (priceType) formData.append("priceType", priceType.value);

    try {
      const report = await apiFetchForm(`/company/import/${this.currentEntity}/commit`, formData);
      this.renderReport(report);
      showToast(`${report.acceptedCount} von ${report.totalRows} Zeilen importiert.`, "success");
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  renderReport(report) {
    const rows = report.rows
      .map(
        (row) => `
          <tr>
            <td>${row.rowNumber}</td>
            <td>${row.accepted ? "Übernommen" : "Abgelehnt"}</td>
            <td>${row.errors.length ? row.errors.join(", ") : "–"}</td>
          </tr>
        `
      )
      .join("");

    this.content.innerHTML = `
      <h2>Ergebnis</h2>
      <p>${report.acceptedCount} von ${report.totalRows} Zeilen importiert, ${report.rejectedCount} abgelehnt.</p>
      <div class="data-table__scroll">
        <table class="data-table">
          <thead><tr><th>Zeile</th><th>Status</th><th>Fehler</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
    `;
  }
}

customElements.define("company-import-page", CompanyImportPage);
