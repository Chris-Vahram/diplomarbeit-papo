// Shared page renderer (design.md Decision 4): used by the public shop and later the editor
// preview. Each block type is one custom element "shop-block-<type>"; unknown types are skipped
// so an older renderer never breaks on a newer page.
import "./blocks/index.js";

class ShopPageView extends HTMLElement {
  // blocks: published block list; context: { api, article } shared by all blocks.
  set data({ blocks, context }) {
    this.replaceChildren();
    for (const block of blocks ?? []) {
      const tag = `shop-block-${block.type}`;
      if (!customElements.get(tag)) continue;
      const element = document.createElement(tag);
      element.dataset.blockId = block.id;
      element.context = context;
      element.config = block.config ?? {};
      this.append(element);
    }
  }
}

customElements.define("shop-page-view", ShopPageView);
