import { escapeHtml } from "../../lib/html.js";
import { ShopBlock } from "./shop-block.js";

// data-field names the config field a text comes from (inline editing in the editor preview).
function headingHtml(text, level = "h2", field = "heading") {
  return text ? `<${level} class="shop-heading" data-field="${field}">${escapeHtml(text)}</${level}>` : "";
}

function textHtml(text, field = "text") {
  return text ? `<p class="shop-text" data-field="${field}">${escapeHtml(text)}</p>` : "";
}

class HeroBlock extends ShopBlock {
  render(config) {
    const api = this.context.api;
    const image = config.imageId
      ? `<img class="shop-hero__image" src="${escapeHtml(api.assetUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" />`
      : "";
    const target = config.buttonPage === "startseite" ? "" : config.buttonPage;
    const button =
      config.buttonLabel && config.buttonPage
        ? `<a class="shop-button" href="${escapeHtml(api.href(target))}" data-field="buttonLabel">${escapeHtml(config.buttonLabel)}</a>`
        : "";
    this.innerHTML = `<div class="shop-inner shop-hero shop-align-${escapeHtml(config.align ?? "left")}${image ? " shop-hero--image" : ""}">
      ${image}
      <div class="shop-hero__content">
        ${headingHtml(config.title, "h1", "title")}
        ${textHtml(config.text)}
        ${button}
      </div>
    </div>`;
  }
}

class HeadingTextBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-align-${escapeHtml(config.align ?? "left")}">
      ${headingHtml(config.heading)}
      ${textHtml(config.text)}
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
      ${config.caption ? `<figcaption class="shop-muted" data-field="caption">${escapeHtml(config.caption)}</figcaption>` : ""}
    </figure>`;
  }
}

class ImageTextBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-split shop-split--image-${escapeHtml(config.imagePosition ?? "left")}">
      ${assetImageHtml(this.context.api, config, "shop-split__image")}
      <div class="shop-split__text">
        ${headingHtml(config.heading)}
        ${textHtml(config.text)}
      </div>
    </div>`;
  }
}

class ContactPersonBlock extends ShopBlock {
  render(config) {
    const phoneDigits = (config.phone ?? "").replace(/[^\d+]/g, "");
    this.innerHTML = `<div class="shop-inner shop-contact">
      ${headingHtml(config.heading)}
      <p class="shop-contact__name" data-field="name">${escapeHtml(config.name)}</p>
      ${config.role ? `<p class="shop-muted" data-field="role">${escapeHtml(config.role)}</p>` : ""}
      ${textHtml(config.text)}
      <p class="shop-contact__links">
        ${config.email ? `<a href="mailto:${escapeHtml(encodeURIComponent(config.email).replace("%40", "@"))}">${escapeHtml(config.email)}</a>` : ""}
        ${phoneDigits ? `<a href="tel:${escapeHtml(phoneDigits)}">${escapeHtml(config.phone)}</a>` : ""}
      </p>
    </div>`;
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
