import { escapeHtml } from "../lib/html.js";

// Template picker of the builder (shop-templates spec "Applying a template"): lists the
// catalogue templates of the open page's type with thumbnail, name and description. The dialog
// itself is the confirmation; "template-apply" ({ template, applyTheme }) goes to the builder,
// which replaces only the draft. The theme suggestion is used only when the box is ticked.

const PAGE_OF_SLOT = { HOME: "startseite", ARTICLES: "artikel", ARTICLE_DETAIL: "artikeldetail" };

// One choice card, shared with the setup dialog. The demo button sits outside the label so it
// never toggles the radio; it carries the card index for "template-demo".
export function templateCardHtml(template, { name, value, index }) {
  return `<div class="template-card">
      <label class="template-card__choice">
        <input type="radio" name="${name}" value="${escapeHtml(value)}" />
        <img class="template-card__image" src="/assets/shop-templates/${escapeHtml(template.thumbnail)}" alt="" loading="lazy" width="320" height="200" />
        <span class="template-card__name">${escapeHtml(template.name)}</span>
        <span class="template-card__text">${escapeHtml(template.description)}</span>
      </label>
      <button type="button" class="button-secondary template-card__demo" data-demo="${index}">Vorlage ansehen</button>
    </div>`;
}

// Click on "Vorlage ansehen": asks the builder to show the demo; choosing there checks the radio.
export function bindDemoButtons(host, templates, radioName) {
  host.addEventListener("click", (event) => {
    const button = event.target.closest("[data-demo]");
    if (!button) return;
    const index = Number(button.dataset.demo);
    const radio = host.querySelectorAll(`input[name=${radioName}]`)[index];
    host.dispatchEvent(new CustomEvent("template-demo", {
      bubbles: true,
      detail: {
        template: templates()[index],
        choose: () => {
          radio.checked = true;
          radio.dispatchEvent(new Event("change", { bubbles: true }));
          radio.focus();
        },
      },
    }));
  });
}

class ShopTemplatePicker extends HTMLElement {
  templates = [];
  #choices = [];

  connectedCallback() {
    this.innerHTML = `
      <modal-dialog title="Vorlage wählen" id="template-dialog">
        <form class="template-picker" novalidate>
          <fieldset class="template-picker__choices">
            <legend class="builder-hint" id="template-legend"></legend>
            <div class="template-picker__grid" id="template-grid"></div>
          </fieldset>
          <p class="builder-hint">Farben, Schriften, Texte und Bilder können Sie jederzeit an Ihre eigene Marke anpassen.</p>
          <label class="template-picker__theme" id="template-theme" hidden>
            <input type="checkbox" name="applyTheme" />
            <span><strong>Farben und Schriften übernehmen</strong>
              <span class="builder-hint">Ändert den Design-Entwurf für alle Seiten. Ohne Haken bleiben Ihre Farben und Schriften, wie sie sind.</span></span>
          </label>
          <p class="template-picker__notice"><strong>Der aktuelle Entwurf wird ersetzt.</strong>
            Ihre veröffentlichte Seite bleibt unverändert, bis Sie veröffentlichen. Mit „Rückgängig“ holen Sie den bisherigen Entwurf zurück, auch Farben und Schriften.</p>
          <div class="entity-form__actions">
            <button type="button" class="button-secondary" data-close>Abbrechen</button>
            <button type="submit" class="button-primary" disabled>Vorlage übernehmen</button>
          </div>
        </form>
      </modal-dialog>`;
    const form = this.querySelector("form");
    this.querySelector("[data-close]").addEventListener("click", () => this.#dialog.close());
    form.addEventListener("change", () => this.#update());
    bindDemoButtons(this.querySelector("#template-grid"), () => this.#choices, "template");
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const template = this.#selected();
      if (!template) return;
      const applyTheme = Boolean(template.theme) && form.applyTheme.checked;
      this.#dialog.close();
      this.dispatchEvent(new CustomEvent("template-apply", { detail: { template, applyTheme }, bubbles: true }));
    });
  }

  get #dialog() {
    return this.querySelector("#template-dialog");
  }

  // page: ShopPageDto of the open page; free pages (no slot) get the free-page templates.
  open(page) {
    const kind = PAGE_OF_SLOT[page.slot] ?? "frei";
    this.#choices = this.templates.filter((template) => template.page === kind);
    this.querySelector("#template-legend").textContent = `Vorlagen für „${page.menuName}“`;
    this.querySelector("#template-grid").innerHTML = this.#choices
      .map((template, index) => templateCardHtml(template, { name: "template", value: String(index), index }))
      .join("");
    const form = this.querySelector("form");
    form.applyTheme.checked = false;
    this.#update();
    this.#dialog.open();
    this.querySelector("input[name=template]")?.focus();
  }

  #selected() {
    const checked = this.querySelector("input[name=template]:checked");
    return checked ? this.#choices[Number(checked.value)] : null;
  }

  #update() {
    const template = this.#selected();
    this.querySelector("#template-theme").hidden = !template?.theme;
    this.querySelector("button[type=submit]").disabled = !template;
  }
}

customElements.define("shop-template-picker", ShopTemplatePicker);
