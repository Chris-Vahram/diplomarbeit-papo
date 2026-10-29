import { escapeHtml } from "../lib/html.js";
import { COLOR_ROLES } from "./block-schema.js";

// "Design" tab (shop-builder spec "Design tab"): all theme colours with picker and hex input,
// heading and body font from the curated list. Edits dispatch "theme-change" ({ theme, key });
// the builder saves the draft and shows it in the preview. "Design veröffentlichen"
// dispatches "theme-publish".

const HEX = /^#[0-9a-f]{6}$/i;
// Form presets of the shop renderer (shop.css [data-form]); themes without form mean "klar".
const FORMS = [
  { id: "klar", label: "Klar", hint: "Hell und präzise: Foto neben dem Text, feine Linien." },
  { id: "handwerk", label: "Handwerk", hint: "Warm: Foto mit Textfeld, Artikel im Passepartout, runde Knöpfe." },
  { id: "kraeftig", label: "Kräftig", hint: "Markant: Foto über die ganze Breite, große Schrift, eckige Formen." },
];

class ShopDesignPanel extends HTMLElement {
  #theme = null;
  #fonts = [];

  // dto = ShopThemeDto from the server; draft = the editor's current theme draft.
  set data({ draft, fonts, draftChanged }) {
    const first = this.#theme === null;
    this.#theme = draft;
    this.#fonts = fonts;
    if (first || !this.contains(document.activeElement)) this.#render();
    this.#renderState(draftChanged);
  }

  connectedCallback() {
    this.addEventListener("input", this.#onInput);
    this.addEventListener("change", this.#onChange);
    this.addEventListener("click", (event) => {
      if (event.target.closest("[data-action=publish]")) {
        this.dispatchEvent(new CustomEvent("theme-publish", { bubbles: true }));
      }
    });
    if (this.#theme) this.#render();
  }

  #render() {
    if (!this.#theme) {
      this.innerHTML = `<p class="builder-empty">Design wird geladen …</p>`;
      return;
    }
    const fontOptions = (selected) => this.#fonts
      .map((font) => `<option value="${escapeHtml(font.id)}"${font.id === selected ? " selected" : ""}>${escapeHtml(font.family)}</option>`)
      .join("");
    this.innerHTML = `
      <h3 class="builder-panel__title">Design</h3>
      <p class="builder-hint">Form, Farben und Schriften gelten für alle Seiten. Einzelne Abschnitte können eigene Farben haben; die bleiben beim Veröffentlichen erhalten.</p>
      <div class="builder-props">
        <h4 class="builder-props__group">Form</h4>
        <div class="builder-field"><label for="theme-form">Gestaltung von Kopfbereich, Karten und Knöpfen</label>
          <select id="theme-form" data-form>${FORMS.map((form) => `<option value="${form.id}"${form.id === (this.#theme.form ?? "klar") ? " selected" : ""}>${form.label}</option>`).join("")}</select></div>
        <p class="builder-hint" id="theme-form-hint">${escapeHtml(FORMS.find((form) => form.id === (this.#theme.form ?? "klar"))?.hint ?? "")}</p>
        <h4 class="builder-props__group">Farben</h4>
        ${COLOR_ROLES.map(({ role, label }) => {
          const value = this.#theme.colors[role];
          return `<div class="builder-color is-overridden">
            <label for="theme-color-${role}">${escapeHtml(label)}</label>
            <input type="color" id="theme-color-${role}" data-color="${role}" value="${escapeHtml(value.toLowerCase())}" />
            <input type="text" data-color-hex="${role}" value="${escapeHtml(value.toUpperCase())}" maxlength="7" aria-label="${escapeHtml(label)} als Hex-Wert" />
          </div>`;
        }).join("")}
        <h4 class="builder-props__group">Schriften</h4>
        <div class="builder-field"><label for="theme-heading-font">Überschriften</label>
          <select id="theme-heading-font" data-font="headingFont">${fontOptions(this.#theme.headingFont)}</select></div>
        <div class="builder-field"><label for="theme-body-font">Fließtext</label>
          <select id="theme-body-font" data-font="bodyFont">${fontOptions(this.#theme.bodyFont)}</select></div>
        <p class="builder-hint">Alle Schriften liegen auf dem PAPO-Server; Ihr Shop lädt nichts von fremden Anbietern nach.</p>
      </div>
      <div class="design-publish">
        <p class="design-publish__state" id="design-state" aria-live="polite"></p>
        <button type="button" class="button-primary" data-action="publish">Design veröffentlichen</button>
        <p class="builder-hint">Veröffentlichen ändert sofort alle Seiten, die online sind.</p>
      </div>`;
  }

  #renderState(draftChanged) {
    const state = this.querySelector("#design-state");
    if (!state) return;
    state.textContent = draftChanged
      ? "Änderungen sind nur in der Vorschau sichtbar, bis Sie das Design veröffentlichen."
      : "Das veröffentlichte Design ist aktuell.";
    this.querySelector("[data-action=publish]").disabled = !draftChanged;
  }

  #emit(theme, key = null) {
    this.#theme = theme;
    this.dispatchEvent(new CustomEvent("theme-change", { detail: { theme, key }, bubbles: true }));
  }

  #setColor(role, value) {
    this.#emit({ ...this.#theme, colors: { ...this.#theme.colors, [role]: value.toUpperCase() } }, `theme:${role}`);
    this.querySelector(`[data-color="${role}"]`).value = value.toLowerCase();
    const hex = this.querySelector(`[data-color-hex="${role}"]`);
    if (document.activeElement !== hex) hex.value = value.toUpperCase();
  }

  #onInput = (event) => {
    const target = event.target;
    if (target.dataset.color) this.#setColor(target.dataset.color, target.value);
    else if (target.dataset.colorHex && HEX.test(target.value)) this.#setColor(target.dataset.colorHex, target.value);
  };

  #onChange = (event) => {
    const target = event.target;
    if (target.dataset.form !== undefined) {
      this.#emit({ ...this.#theme, form: target.value });
      this.querySelector("#theme-form-hint").textContent = FORMS.find((form) => form.id === target.value)?.hint ?? "";
    } else if (target.dataset.font) {
      this.#emit({ ...this.#theme, [target.dataset.font]: target.value });
    } else if (target.dataset.colorHex && !HEX.test(target.value)) {
      target.value = this.#theme.colors[target.dataset.colorHex];
    }
  };
}

customElements.define("shop-design-panel", ShopDesignPanel);
