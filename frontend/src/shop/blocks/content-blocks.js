import { escapeHtml } from "../../lib/html.js";
import { ShopBlock } from "./shop-block.js";
import { textField } from "./text-field.js";

function headingHtml(api, text, level = "h2", field = "heading") {
  return textField(api, level, "shop-heading", field, text);
}

function textHtml(api, text, field = "text") {
  return textField(api, "p", "shop-text", field, text);
}

// Without image the hero is type on the block background; with image the theme's form decides
// the composition (shop.css): split (klar), photo with panel (handwerk), full-bleed (kraeftig).
class HeroBlock extends ShopBlock {
  render(config) {
    const api = this.context.api;
    const image = config.imageId
      ? `<div class="shop-hero__media"><img src="${escapeHtml(api.assetUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" /></div>`
      : "";
    // The logo stands for the shop's name, so screen readers hear the name (shop-design-assistant).
    const logo = config.logoId
      ? `<img class="shop-hero__logo" src="${escapeHtml(api.assetUrl(config.logoId))}" alt="${escapeHtml(this.context.shopName ?? "Logo")}" />`
      : "";
    const target = config.buttonPage === "startseite" ? "" : config.buttonPage;
    const button =
      config.buttonLabel && config.buttonPage
        ? `<a class="shop-button" href="${escapeHtml(api.href(target))}" data-field="buttonLabel">${escapeHtml(config.buttonLabel)}</a>`
        : "";
    this.innerHTML = `<div class="shop-hero shop-hero--${image ? "image" : "plain"} shop-align-${escapeHtml(config.align ?? "left")}">
      ${image}
      <div class="shop-hero__body"><div class="shop-hero__content">
        ${logo}
        ${headingHtml(api, config.title, "h1", "title")}
        ${textHtml(this.context.api, config.text)}
        ${button}
      </div></div>
    </div>`;
  }
}

class HeadingTextBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-align-${escapeHtml(config.align ?? "left")}">
      ${headingHtml(this.context.api, config.heading)}
      ${textHtml(this.context.api, config.text)}
    </div>`;
  }
}

// A draft block may have no image chosen yet (editor preview); published blocks always have one.
function assetImageHtml(api, config, className = "") {
  const classes = className ? ` class="${className}"` : "";
  return config.imageId
    ? `<img${classes} src="${escapeHtml(api.assetUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" loading="lazy" />`
    : `<div class="${className} shop-image-placeholder" aria-hidden="true"></div>`;
}

class ImageBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<figure class="shop-inner shop-figure shop-figure--${escapeHtml(config.width ?? "wide")}">
      ${assetImageHtml(this.context.api, config)}
      ${textField(this.context.api, "figcaption", "shop-muted", "caption", config.caption)}
    </figure>`;
  }
}

class ImageTextBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-split shop-split--image-${escapeHtml(config.imagePosition ?? "left")}">
      ${assetImageHtml(this.context.api, config, "shop-split__image")}
      <div class="shop-split__text">
        ${headingHtml(this.context.api, config.heading)}
        ${textHtml(this.context.api, config.text)}
      </div>
    </div>`;
  }
}

class ContactPersonBlock extends ShopBlock {
  render(config) {
    const phoneDigits = (config.phone ?? "").replace(/[^\d+]/g, "");
    this.innerHTML = `<div class="shop-inner shop-contact"><div class="shop-contact__card">
      ${headingHtml(this.context.api, config.heading)}
      ${textField(this.context.api, "p", "shop-contact__name", "name", config.name)}
      ${textField(this.context.api, "p", "shop-muted", "role", config.role)}
      ${textHtml(this.context.api, config.text)}
      <p class="shop-contact__links">
        ${config.email ? `<a href="mailto:${escapeHtml(encodeURIComponent(config.email).replace("%40", "@"))}">${escapeHtml(config.email)}</a>` : ""}
        ${phoneDigits ? `<a href="tel:${escapeHtml(phoneDigits)}">${escapeHtml(config.phone)}</a>` : ""}
      </p>
    </div></div>`;
  }
}

class SpacerBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-spacer shop-spacer--${escapeHtml(config.size ?? "medium")}">
      ${config.divider ? '<hr class="shop-divider" />' : ""}
    </div>`;
  }
}

customElements.define("shop-block-hero", HeroBlock);
customElements.define("shop-block-heading-text", HeadingTextBlock);
customElements.define("shop-block-image", ImageBlock);
customElements.define("shop-block-image-text", ImageTextBlock);
customElements.define("shop-block-contact-person", ContactPersonBlock);
customElements.define("shop-block-spacer", SpacerBlock);
