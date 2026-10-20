// Base for all block elements: no shadow DOM, so theme custom properties and shop.css cascade
// in (design.md Decision 4). Layout options and colour overrides live on the host element;
// all text is plain text and goes through escapeHtml.
import { applyColors } from "../shop-theme.js";

const OVERRIDE_PROPS = ["primary", "accent", "background", "surface", "text"].flatMap((role) => [
  `--shop-${role}`,
  `--shop-on-${role}`,
]);

export class ShopBlock extends HTMLElement {
  context = {};
  #config = {};

  get config() {
    return this.#config;
  }

  set config(value) {
    this.#config = value ?? {};
    if (this.isConnected) this.update();
  }

  connectedCallback() {
    this.update();
  }

  // Editor preview: the DOM already shows an inline edit, so only the config is taken over.
  adoptConfig(value) {
    this.#config = value ?? {};
  }

  update() {
    const config = this.#config;
    this.className = [
      "shop-block",
      `shop-block--spacing-${config.spacing ?? "medium"}`,
      `shop-block--bg-${config.background ?? "none"}`,
    ].join(" ");
    // Removing an override must fall back to the theme, so stale values are cleared first.
    OVERRIDE_PROPS.forEach((prop) => this.style.removeProperty(prop));
    applyColors(this, config.colors);
    this.render(config);
  }

  // Subclasses fill the block; async content may render in a second step.
  render() {}
}
