import { escapeHtml } from "../lib/html.js";
import { BLOCK_TYPES, SAMPLE_ASSET_URLS, sampleBlock } from "./block-schema.js";
import { postPreview, previewPayload } from "./preview-payload.js";

// "Hinzufügen" in the builder's sidebar: one entry per block type with a "+" to add it,
// a hover preview of what the block looks like (rendered by the shop itself with sample
// content) and drag & drop into the page preview.
//
// context = { preview, theme, pages, imageUrls } (set by the builder). Dispatches "palette-add"
// ({ type }) on click, "palette-drag" ({ type }) / "palette-drag-end" while dragging.

export const BLOCK_DRAG_TYPE = "application/x-papo-block";

const PREVIEW_SCALE = 0.3;
const PREVIEW_MAX_HEIGHT = 960;

class ShopBlockPalette extends HTMLElement {
  context = null;
  #popover = null;
  #frame = null;
  #frameReady = false;
  #hoverType = null;
  #hoverTimer = null;
  #hoverItem = null;

  connectedCallback() {
    this.innerHTML = `<ul class="builder-palette">${Object.entries(BLOCK_TYPES)
      .filter(([, type]) => !type.fixed)
      .map(([id, type]) => `<li>
        <button type="button" class="palette-item" data-type="${id}" draggable="true"
          aria-label="${escapeHtml(type.label)} hinzufügen">
          <span class="palette-item__text">
            <span class="palette-item__label">${escapeHtml(type.label)}</span>
            <span class="palette-item__desc">${escapeHtml(type.description)}</span>
          </span>
          <span class="palette-item__add" aria-hidden="true"><i data-lucide="plus"></i></span>
        </button>
      </li>`)
      .join("")}</ul>`;
    window.lucide?.createIcons();

    this.addEventListener("click", (event) => {
      const item = event.target.closest(".palette-item");
      if (item) this.dispatchEvent(new CustomEvent("palette-add", { detail: { type: item.dataset.type }, bubbles: true }));
    });
    this.addEventListener("pointerover", (event) => {
      const item = event.target.closest(".palette-item");
      if (item && item.dataset.type !== this.#hoverType) this.#scheduleHover(item);
    });
    this.addEventListener("pointerleave", () => this.#hideHover());
    this.addEventListener("focusin", (event) => {
      const item = event.target.closest(".palette-item");
      if (item) this.#scheduleHover(item);
    });
    this.addEventListener("focusout", () => this.#hideHover());
    this.addEventListener("dragstart", (event) => {
      const item = event.target.closest(".palette-item");
      if (!item) return;
      this.#hideHover();
      event.dataTransfer.effectAllowed = "copy";
      event.dataTransfer.setData(BLOCK_DRAG_TYPE, item.dataset.type);
      event.dataTransfer.setData("text/plain", BLOCK_TYPES[item.dataset.type].label);
      this.dispatchEvent(new CustomEvent("palette-drag", { detail: { type: item.dataset.type }, bubbles: true }));
    });
    this.addEventListener("dragend", () => this.dispatchEvent(new CustomEvent("palette-drag-end", { bubbles: true })));
    window.addEventListener("message", this.#onMessage);
  }

  disconnectedCallback() {
    window.removeEventListener("message", this.#onMessage);
    clearTimeout(this.#hoverTimer);
    this.#popover?.remove();
  }

  // ---- Hover preview: one shared, scaled-down shop frame next to the sidebar ----------

  #ensurePopover() {
    if (this.#popover) return;
    this.#popover = document.createElement("div");
    this.#popover.className = "palette-preview";
    this.#popover.hidden = true;
    this.#popover.setAttribute("aria-hidden", "true");
    this.#popover.innerHTML = `<p class="palette-preview__title"></p>
      <div class="palette-preview__viewport"><iframe src="/shop.html?preview=1" tabindex="-1" title="Vorschau des Abschnitts"></iframe></div>`;
    this.#frame = this.#popover.querySelector("iframe");
    document.body.append(this.#popover);
  }

  #onMessage = (event) => {
    if (!this.#frame || event.source !== this.#frame.contentWindow || event.origin !== location.origin) return;
    if (event.data?.type === "papo-shop-preview-ready") {
      this.#frameReady = true;
      if (this.#hoverType) this.#renderHover();
    } else if (event.data?.type === "papo-shop-rendered" && this.#hoverType) {
      this.#fitAndReveal(this.#hoverType);
    }
  };

  #scheduleHover(item) {
    clearTimeout(this.#hoverTimer);
    this.#hoverTimer = setTimeout(() => this.#showHover(item), 150);
  }

  // The popover stays invisible until the block is drawn and measured, so it appears once
  // at its final size instead of resizing in front of the user.
  #showHover(item) {
    if (!this.context) return;
    this.#ensurePopover();
    this.#hoverType = item.dataset.type;
    this.#hoverItem = item;
    this.#popover.querySelector(".palette-preview__title").textContent = BLOCK_TYPES[this.#hoverType].label;
    this.#popover.classList.remove("is-ready");
    this.#popover.hidden = false;
    if (this.#frameReady) this.#renderHover();
  }

  #hideHover() {
    clearTimeout(this.#hoverTimer);
    this.#hoverType = null;
    if (this.#popover) this.#popover.hidden = true;
  }

  #renderHover() {
    const { preview, theme, pages, imageUrls } = this.context;
    const block = sampleBlock(this.#hoverType, preview.articles.map((article) => article.id));
    const payload = previewPayload({
      preview,
      theme,
      pages,
      page: { menuName: BLOCK_TYPES[this.#hoverType].label, slot: null, segment: "" },
      blocks: [block],
      assetUrls: SAMPLE_ASSET_URLS,
      imageUrls,
    });
    postPreview(this.#frame, { ...payload, bare: true });
  }

  // After the frame reported the block as drawn (same origin, so it can be measured).
  async #fitAndReveal(type) {
    await this.#imagesLoaded();
    await new Promise((resolve) => requestAnimationFrame(resolve));
    if (type !== this.#hoverType) return;
    // Fit the frame to the rendered block (same origin), then place and reveal the popover.
    // <shop-page-view> is inline, so the blocks' own boxes give the height.
    const blocks = [...(this.#frame.contentDocument?.querySelectorAll("shop-page-view > *") ?? [])];
    const bottom = Math.max(0, ...blocks.map((element) => element.getBoundingClientRect().bottom));
    const height = Math.min(PREVIEW_MAX_HEIGHT, Math.max(120, Math.ceil(bottom)));
    this.#frame.style.height = `${height}px`;
    const viewportHeight = Math.ceil(height * PREVIEW_SCALE);
    this.#popover.querySelector(".palette-preview__viewport").style.height = `${viewportHeight}px`;
    const rect = this.#hoverItem.getBoundingClientRect();
    this.#popover.style.left = `${rect.right + 12}px`;
    this.#popover.style.top = `${Math.max(8, Math.min(rect.top, window.innerHeight - viewportHeight - 48))}px`;
    this.#popover.classList.add("is-ready");
  }

  // Images change the block height; waits for them, but at most 500 ms.
  #imagesLoaded() {
    const images = [...(this.#frame.contentDocument?.images ?? [])].filter((image) => !image.complete);
    const loaded = Promise.all(images.map((image) => new Promise((resolve) => {
      image.loading = "eager";
      image.addEventListener("load", resolve, { once: true });
      image.addEventListener("error", resolve, { once: true });
    })));
    return Promise.race([loaded, new Promise((resolve) => setTimeout(resolve, 500))]);
  }
}

customElements.define("shop-block-palette", ShopBlockPalette);
