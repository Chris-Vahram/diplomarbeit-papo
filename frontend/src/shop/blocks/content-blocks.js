import { escapeHtml } from "../../lib/html.js";
import { ShopBlock } from "./shop-block.js";

function headingHtml(text, level = "h2") {
  return text ? `<${level} class="shop-heading">${escapeHtml(text)}</${level}>` : "";
}

function textHtml(text) {
  return text ? `<p class="shop-text">${escapeHtml(text)}</p>` : "";
}

class HeroBlock extends ShopBlock {
  render(config) {
    const api = this.context.api;
    const image = config.imageId
      ? `<img class="shop-hero__image" src="${escapeHtml(api.imageUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" />`
      : "";
    const target = config.buttonPage === "startseite" ? "" : config.buttonPage;
    const button =
      config.buttonLabel && config.buttonPage
        ? `<a class="shop-button" href="${escapeHtml(api.href(target))}">${escapeHtml(config.buttonLabel)}</a>`
        : "";
    this.innerHTML = `<div class="shop-inner shop-hero shop-align-${escapeHtml(config.align ?? "left")}${image ? " shop-hero--image" : ""}">
      ${image}
      <div class="shop-hero__content">
        ${headingHtml(config.title, "h1")}
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

class ImageBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<figure class="shop-inner shop-figure shop-figure--${escapeHtml(config.width ?? "wide")}">
      <img src="${escapeHtml(this.context.api.imageUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" loading="lazy" />
      ${config.caption ? `<figcaption class="shop-muted">${escapeHtml(config.caption)}</figcaption>` : ""}
    </figure>`;
  }
}

class ImageTextBlock extends ShopBlock {
  render(config) {
    this.innerHTML = `<div class="shop-inner shop-split shop-split--image-${escapeHtml(config.imagePosition ?? "left")}">
      <img class="shop-split__image" src="${escapeHtml(this.context.api.imageUrl(config.imageId))}" alt="${escapeHtml(config.alt ?? "")}" loading="lazy" />
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
      <p class="shop-contact__name">${escapeHtml(config.name)}</p>
      ${config.role ? `<p class="shop-muted">${escapeHtml(config.role)}</p>` : ""}
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
