import { bindDemoButtons, templateCardHtml } from "./template-picker.js";

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
          <p class="setup-dialog__intro">Wählen Sie einen Stil für Ihren Shop. Er gilt für Startseite, Artikelübersicht und Artikeldetail und legt Farben und Schriften fest. Mit „Vorlage ansehen“ klicken Sie sich vorher durch einen Demo-Shop.</p>
          <fieldset class="template-picker__choices">
            <legend class="builder-hint">Stil</legend>
            <div class="template-picker__grid" id="setup-grid"></div>
          </fieldset>
          <p class="builder-hint">Farben, Schriften, Texte und Bilder können Sie danach jederzeit an Ihre eigene Marke anpassen. Ihr Shop zeigt den gewählten Stil sofort. Öffentlich sichtbar ist er erst, wenn das Unternehmen den Shop freigegeben hat.</p>
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
    bindDemoButtons(this.querySelector("#setup-grid"), () => this.#styles, "style");
  }

  // templates: the whole catalogue; only Startseite templates become styles.
  open(templates) {
    this.#styles = templates.filter((template) => template.page === "startseite" && template.id.startsWith(PREFIX));
    this.querySelector("#setup-grid").innerHTML = this.#styles
      .map((template, index) => templateCardHtml(template, { name: "style", value: template.id.slice(PREFIX.length), index }))
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
