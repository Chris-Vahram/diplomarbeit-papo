import { escapeHtml } from "../lib/html.js";

// "Shop einrichten" (shop-templates spec "First-time shop setup"): shown by the builder while
// the shop is not set up. The styles are the Startseite templates ("startseite-<style>").
// "shop-setup" ({ style }) goes to the builder; style null = "Überspringen". Closing the dialog
// answers nothing, so it comes back next time.

const PREFIX = "startseite-";

class ShopSetupDialog extends HTMLElement {
  #styles = [];

  connectedCallback() {
    this.innerHTML = `
      <modal-dialog title="Shop einrichten" id="setup-dialog">
        <form class="template-picker" novalidate>
          <p class="setup-dialog__intro">Wählen Sie einen Stil für Ihren Shop. Er gilt für Startseite, Artikelübersicht und Artikeldetail und legt Farben und Schriften fest. Danach können Sie jede Seite frei anpassen.</p>
          <fieldset class="template-picker__choices">
            <legend class="builder-hint">Stil</legend>
            <div class="template-picker__grid" id="setup-grid"></div>
          </fieldset>
          <p class="builder-hint">Ihr Shop zeigt den gewählten Stil sofort. Öffentlich sichtbar ist er erst, wenn das Unternehmen den Shop freigegeben hat.</p>
          <div class="entity-form__actions">
            <button type="button" class="button-secondary" data-action="skip">Überspringen</button>
            <button type="submit" class="button-primary" disabled>Shop einrichten</button>
          </div>
        </form>
      </modal-dialog>`;
    const form = this.querySelector("form");
    form.addEventListener("change", () => {
      this.querySelector("button[type=submit]").disabled = !form.querySelector("input[name=style]:checked");
    });
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const checked = form.querySelector("input[name=style]:checked");
      if (checked) this.#answer(checked.value);
    });
    this.querySelector("[data-action=skip]").addEventListener("click", () => this.#answer(null));
  }

  // templates: the whole catalogue; only Startseite templates become styles.
  open(templates) {
    this.#styles = templates.filter((template) => template.page === "startseite" && template.id.startsWith(PREFIX));
    this.querySelector("#setup-grid").innerHTML = this.#styles
      .map((template) => `
        <label class="template-card">
          <input type="radio" name="style" value="${escapeHtml(template.id.slice(PREFIX.length))}" />
          <img class="template-card__image" src="/assets/shop-templates/${escapeHtml(template.thumbnail)}" alt="" width="320" height="200" />
          <span class="template-card__name">${escapeHtml(template.name)}</span>
          <span class="template-card__text">${escapeHtml(template.description)}</span>
        </label>`)
      .join("");
    this.querySelector("button[type=submit]").disabled = true;
    this.querySelector("#setup-dialog").open();
    this.querySelector("input[name=style]")?.focus();
  }

  close() {
    this.querySelector("#setup-dialog").close();
  }

  // After a failed request the partner can answer again.
  failed() {
    this.querySelector("[data-action=skip]").disabled = false;
    this.querySelector("button[type=submit]").disabled = !this.querySelector("input[name=style]:checked");
  }

  #answer(style) {
    this.querySelectorAll("form button").forEach((button) => { button.disabled = true; });
    this.dispatchEvent(new CustomEvent("shop-setup", { detail: { style }, bubbles: true }));
  }
}

customElements.define("shop-setup-dialog", ShopSetupDialog);
