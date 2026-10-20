// Shared page renderer (design.md Decision 4): used by the public shop and the editor
// preview. Each block type is one custom element "shop-block-<type>"; unknown types are skipped
// so an older renderer never breaks on a newer page.
import "./blocks/index.js";

function isEditing(element) {
  const active = document.activeElement;
  return document.hasFocus() && active?.isContentEditable && element.contains(active);
}

class ShopPageView extends HTMLElement {
  // Config JSON each block element was last rendered with.
  #rendered = new WeakMap();

  // blocks: block list; context: { api, article } shared by all blocks.
  set data({ blocks, context }) {
    this.replaceChildren();
    this.update({ blocks, context });
  }

  // Inline edit in the editor preview: config taken over without re-rendering the block.
  adopt(element, config) {
    element.adoptConfig(config);
    this.#rendered.set(element, JSON.stringify(config));
  }

  // Editor preview: re-renders only blocks whose config or context changed and keeps the
  // others in place, so the preview does not jump (images reloading) on every keystroke.
  update({ blocks, context }) {
    const existing = new Map([...this.children].map((element) => [element.dataset.blockId, element]));
    const ordered = [];
    for (const block of blocks ?? []) {
      const tag = `shop-block-${block.type}`;
      if (!customElements.get(tag)) continue;
      const json = JSON.stringify(block.config ?? {});
      let element = existing.get(block.id);
      if (element?.localName === tag) {
        existing.delete(block.id);
        // Someone is typing in this block: its text is newer than the round trip through the
        // editor, and re-rendering would drop the caret.
        if (isEditing(element)) {
          ordered.push(element);
          continue;
        }
        const contextChanged = element.context !== context;
        if (contextChanged) element.context = context;
        if (contextChanged || this.#rendered.get(element) !== json) element.config = block.config ?? {};
      } else {
        element = document.createElement(tag);
        element.dataset.blockId = block.id;
        element.context = context;
        element.config = block.config ?? {};
      }
      this.#rendered.set(element, json);
      ordered.push(element);
    }
    existing.forEach((element) => element.remove());
    // Move only what is out of place: moving re-connects (and re-renders) an element.
    ordered.forEach((element, index) => {
      if (this.children[index] !== element) this.insertBefore(element, this.children[index] ?? null);
    });
  }
}

customElements.define("shop-page-view", ShopPageView);
