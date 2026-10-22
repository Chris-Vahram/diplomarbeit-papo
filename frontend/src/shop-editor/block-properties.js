import { escapeHtml } from "../lib/html.js";
import { BLOCK_TYPES, COLOR_ROLES, COMMON_FIELDS } from "./block-schema.js";
import { assetObjectUrl } from "./asset-library.js";

// Properties panel for the selected block (shop-builder spec "Block editing", "Block colour
// override in the editor"). Only schema fields are editable. Every edit dispatches
// "block-change" with { config, key }; key groups typing for undo (editor-history.js).
//
// data: { block, theme, linkPages: [{segment, menuName}], articles, categories }

const HEX = /^#[0-9a-f]{6}$/i;

function fieldId(name) {
  return `prop-${name}`;
}

class ShopBlockProperties extends HTMLElement {
  #data = null;

  set data(value) {
    // Re-rendering while typing would steal the focus; only re-render for another block
    // or when the block changed from outside (undo, preview click).
    const ownEdit = Boolean(value.block) && this.#data?.block?.id === value.block.id
      && this.#data.block.config === value.block.config;
    this.#data = value;
    if (!ownEdit) this.#render();
  }

  #config() {
    return this.#data.block.config;
  }

  #emit(config, key = null) {
    this.#data.block = { ...this.#data.block, config };
    this.dispatchEvent(new CustomEvent("block-change", { detail: { config, key }, bubbles: true }));
  }

  #set(name, value, key = null) {
    const config = { ...this.#config() };
    if (value === undefined || value === null || value === "") {
      delete config[name];
    } else {
      config[name] = value;
    }
    this.#emit(config, key);
  }

  #render() {
    const block = this.#data?.block;
    if (!block) {
      this.innerHTML = `<p class="builder-empty">Wählen Sie links einen Abschnitt oder klicken Sie in der Vorschau darauf.</p>`;
      return;
    }
    const type = BLOCK_TYPES[block.type];
    if (!type) {
      this.innerHTML = `<p class="builder-empty">Dieser Abschnitt kann hier nicht bearbeitet werden.</p>`;
      return;
    }
    this.innerHTML = `
      <h3 class="builder-panel__title">${escapeHtml(type.label)}</h3>
      <div class="builder-props">
        ${type.fields.map((field) => this.#fieldHtml(field)).join("")}
        <h4 class="builder-props__group">Layout</h4>
        ${COMMON_FIELDS.map((field) => this.#fieldHtml(field)).join("")}
        <h4 class="builder-props__group">Farben dieses Abschnitts</h4>
        <p class="builder-hint">Ohne Änderung gelten die Farben aus „Design“.</p>
        ${this.#colorsHtml()}
        <button type="button" class="button-secondary builder-props__reset" data-action="reset-colors">Auf Design zurücksetzen</button>
      </div>`;
    this.#loadThumb(type);
  }

  #loadThumb(type) {
    const field = type.fields.find((candidate) => candidate.kind === "image");
    const id = field && this.#config()[field.name];
    const thumb = this.querySelector("[data-image-thumb]");
    if (!id || !thumb) return;
    assetObjectUrl(id)
      .then((url) => {
        thumb.src = url;
      })
      .catch(() => {});
  }

  #fieldHtml(field) {
    const value = this.#config()[field.name];
    const id = fieldId(field.name);
    const required = field.required ? ' <span class="builder-required">Pflichtfeld</span>' : "";
    const label = `<label for="${id}">${escapeHtml(field.label)}${required}</label>`;
    const hint = field.hint ? `<p class="builder-hint">${escapeHtml(field.hint)}</p>` : "";
    switch (field.kind) {
      case "text": {
        const attrs = `id="${id}" data-field="${field.name}" maxlength="${field.maxLength}"`;
        const input = field.multiline
          ? `<textarea ${attrs} rows="${field.maxLength > 1000 ? 6 : 3}">${escapeHtml(value ?? "")}</textarea>`
          : `<input ${attrs} type="${field.type ?? "text"}" value="${escapeHtml(value ?? "")}" />`;
        return `<div class="builder-field">${label}${input}
          <span class="builder-counter" data-counter="${field.name}">${(value ?? "").length} / ${field.maxLength}</span>${hint}</div>`;
      }
      case "choice": {
        const options = Object.entries(field.choices)
          .map(([key, text]) => `<option value="${escapeHtml(key)}"${String(value) === key ? " selected" : ""}>${escapeHtml(text)}</option>`)
          .join("");
        return `<div class="builder-field">${label}<select id="${id}" data-field="${field.name}">
          <option value=""${value === undefined ? " selected" : ""}>Standard</option>${options}</select>${hint}</div>`;
      }
      case "boolean":
        return `<div class="builder-field builder-field--check"><input id="${id}" type="checkbox" data-field="${field.name}"${value ? " checked" : ""} />${label}</div>`;
      case "segment": {
        const options = this.#data.linkPages
          .map((page) => `<option value="${escapeHtml(page.segment)}"${value === page.segment ? " selected" : ""}>${escapeHtml(page.menuName)}</option>`)
          .join("");
        const missing = value && !this.#data.linkPages.some((page) => page.segment === value)
          ? `<option value="${escapeHtml(value)}" selected>${escapeHtml(value)} (nicht online)</option>` : "";
        return `<div class="builder-field">${label}<select id="${id}" data-field="${field.name}">
          <option value="">Keine Schaltfläche</option>${options}${missing}</select>
          <p class="builder-hint">Verlinkt werden können nur veröffentlichte Seiten.</p></div>`;
      }
      case "category": {
        const options = this.#data.categories
          .map((category) => `<option value="${category.id}"${value === category.id ? " selected" : ""}>${escapeHtml(category.name)}</option>`)
          .join("");
        return `<div class="builder-field">${label}<select id="${id}" data-field="${field.name}" data-number>
          <option value="">Alle Artikel</option>${options}</select></div>`;
      }
      case "articles":
        return this.#articlesHtml(field, value ?? []);
      case "image":
        return `<div class="builder-field">${label}
          <div class="builder-image">
            <img class="builder-image__thumb" data-image-thumb alt=""${value ? "" : " hidden"} />
            <span class="builder-hint" data-image-none${value ? " hidden" : ""}>Kein Bild gewählt.</span>
            <button type="button" class="button-secondary" data-action="pick-image" data-field="${field.name}">${value ? "Bild ändern" : "Bild wählen"}</button>
            ${value && !field.required ? `<button type="button" class="button-secondary" data-action="clear" data-field="${field.name}">Entfernen</button>` : ""}
          </div></div>`;
      default:
        return "";
    }
  }

  #articlesHtml(field, ids) {
    const byId = new Map(this.#data.articles.map((article) => [article.id, article]));
    const chosen = ids
      .map((id, index) => `<li>
          <span>${escapeHtml(byId.get(id)?.name ?? "Nicht mehr verfügbar")}</span>
          <button type="button" class="builder-icon" data-action="article-up" data-index="${index}" aria-label="Nach oben"${index === 0 ? " disabled" : ""}>↑</button>
          <button type="button" class="builder-icon" data-action="article-down" data-index="${index}" aria-label="Nach unten"${index === ids.length - 1 ? " disabled" : ""}>↓</button>
          <button type="button" class="builder-icon" data-action="article-remove" data-index="${index}" aria-label="Entfernen">×</button>
        </li>`)
      .join("");
    const options = this.#data.articles
      .filter((article) => !ids.includes(article.id))
      .map((article) => `<option value="${article.id}">${escapeHtml(article.name)}</option>`)
      .join("");
    const full = ids.length >= field.maxLength;
    return `<div class="builder-field"><label for="${fieldId(field.name)}">${escapeHtml(field.label)} (${ids.length} von ${field.maxLength})</label>
      <ul class="builder-articles">${chosen}</ul>
      <select id="${fieldId(field.name)}" data-action="article-add"${full ? " disabled" : ""}>
        <option value="">${full ? "Höchstzahl erreicht" : "Artikel hinzufügen …"}</option>${options}</select></div>`;
  }

  #colorsHtml() {
    const overrides = this.#config().colors ?? {};
    return COLOR_ROLES.map(({ role, label }) => {
      const own = overrides[role];
      const value = own ?? this.#data.theme?.colors?.[role] ?? "#000000";
      return `<div class="builder-color${own ? " is-overridden" : ""}">
        <label for="color-${role}">${escapeHtml(label)}</label>
        <input type="color" id="color-${role}" data-color="${role}" value="${escapeHtml(value.toLowerCase())}" />
        <input type="text" data-color-hex="${role}" value="${escapeHtml(value.toUpperCase())}" maxlength="7" aria-label="${escapeHtml(label)} als Hex-Wert" />
        <span class="builder-hint">${own ? "Eigene Farbe" : "Aus Design"}</span>
      </div>`;
    }).join("");
  }

  connectedCallback() {
    this.addEventListener("input", this.#onInput);
    this.addEventListener("change", this.#onChange);
    this.addEventListener("click", this.#onClick);
    if (!this.#data) this.#render();
  }

  disconnectedCallback() {
    this.removeEventListener("input", this.#onInput);
    this.removeEventListener("change", this.#onChange);
    this.removeEventListener("click", this.#onClick);
  }

  #onInput = (event) => {
    const target = event.target;
    if (target.matches("input[data-field]:not([type=checkbox]), textarea[data-field]")) {
      const name = target.dataset.field;
      this.querySelector(`[data-counter="${name}"]`).textContent = `${target.value.length} / ${target.maxLength}`;
      this.#set(name, target.value, `${this.#data.block.id}:${name}`);
    } else if (target.dataset.color) {
      this.#setColor(target.dataset.color, target.value.toUpperCase(), `${this.#data.block.id}:color`);
    } else if (target.dataset.colorHex && HEX.test(target.value)) {
      this.#setColor(target.dataset.colorHex, target.value.toUpperCase(), `${this.#data.block.id}:color`);
    }
  };

  #onChange = (event) => {
    const target = event.target;
    if (target.matches("select[data-field]")) {
      const value = target.value === "" ? null : target.hasAttribute("data-number") ? Number(target.value) : target.value;
      this.#set(target.dataset.field, value);
    } else if (target.matches("input[type=checkbox][data-field]")) {
      this.#set(target.dataset.field, target.checked);
    } else if (target.dataset.action === "article-add" && target.value) {
      this.#set("articleIds", [...(this.#config().articleIds ?? []), Number(target.value)]);
      this.#render();
    } else if (target.dataset.colorHex && !HEX.test(target.value)) {
      // Invalid hex input: show the effective value again.
      this.#render();
    }
  };

  #onClick = (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const ids = [...(this.#config().articleIds ?? [])];
    const index = Number(button.dataset.index);
    switch (button.dataset.action) {
      case "pick-image":
        this.dispatchEvent(new CustomEvent("pick-image", { detail: { field: button.dataset.field }, bubbles: true }));
        return;
      case "clear":
        this.#set(button.dataset.field, null);
        break;
      case "article-up":
        [ids[index - 1], ids[index]] = [ids[index], ids[index - 1]];
        this.#set("articleIds", ids);
        break;
      case "article-down":
        [ids[index + 1], ids[index]] = [ids[index], ids[index + 1]];
        this.#set("articleIds", ids);
        break;
      case "article-remove":
        ids.splice(index, 1);
        this.#set("articleIds", ids);
        break;
      case "reset-colors":
        this.#set("colors", null);
        break;
      default:
        return;
    }
    this.#render();
  };

  #setColor(role, value, key) {
    const colors = { ...(this.#config().colors ?? {}), [role]: value };
    this.#set("colors", colors, key);
    const row = this.querySelector(`[data-color="${role}"]`).closest(".builder-color");
    row.classList.add("is-overridden");
    row.querySelector(".builder-hint").textContent = "Eigene Farbe";
    row.querySelector(`[data-color="${role}"]`).value = value.toLowerCase();
    const hex = row.querySelector(`[data-color-hex="${role}"]`);
    if (document.activeElement !== hex) hex.value = value;
  }

  // Called by the builder after an image was chosen.
  setImage(field, assetId) {
    this.#set(field, assetId);
    this.#render();
  }
}

customElements.define("shop-block-properties", ShopBlockProperties);
