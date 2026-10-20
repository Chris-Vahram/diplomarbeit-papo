import { apiFetch } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { assetObjectUrl } from "./asset-library.js";
import { articleImageUrls, imageIds, postPreview, previewPayload } from "./preview-payload.js";

// Page overview of "Mein Webshop": one miniature per designable page, rendered by the shop
// itself (shop.html?preview, scaled down) with the current editing state, plus
// "Shop bearbeiten". A miniature opens the full-screen builder on that page.

class ShopPageThumbnails extends HTMLElement {
  #frames = new Map(); // contentWindow -> { frame, payload }

  connectedCallback() {
    window.addEventListener("message", this.#onMessage);
    this.innerHTML = `
      <section class="shop-pages-overview" aria-labelledby="shop-pages-heading">
        <div class="shop-pages-overview__head">
          <h2 id="shop-pages-heading">Seiten Ihres Shops</h2>
          <p class="builder-hint">Die Vorschau zeigt den aktuellen Bearbeitungsstand.</p>
        </div>
        <p class="builder-hint" id="thumbs-loading">Vorschau wird geladen …</p>
        <ul class="page-thumbs" id="page-thumbs"></ul>
        <a class="button-primary shop-pages-overview__edit" href="#/partner/shop/editor">
          <span>Shop bearbeiten</span><i data-lucide="arrow-right"></i>
        </a>
      </section>`;
    window.lucide?.createIcons();
    this.#load();
  }

  disconnectedCallback() {
    window.removeEventListener("message", this.#onMessage);
  }

  async #load() {
    const loading = this.querySelector("#thumbs-loading");
    try {
      const [list, theme, preview] = await Promise.all([
        apiFetch("/partner/shop/pages"),
        apiFetch("/partner/shop/theme"),
        apiFetch("/partner/shop/preview"),
      ]);
      const [details, imageUrls] = await Promise.all([
        Promise.all(list.pages.map((page) => apiFetch(`/partner/shop/pages/${page.id}`))),
        articleImageUrls(preview.articles),
      ]);
      const assetUrls = {};
      const ids = [...new Set(details.flatMap((detail) => imageIds(detail.draft.blocks)))];
      await Promise.all(ids.map(async (id) => {
        assetUrls[id] = await assetObjectUrl(id).catch(() => "");
      }));
      this.#render(details, (detail) => previewPayload({
        preview,
        theme: theme.draft,
        pages: list.pages,
        page: detail.page,
        blocks: detail.draft.blocks,
        assetUrls,
        imageUrls,
      }));
      loading.hidden = true;
    } catch (error) {
      loading.textContent = `Vorschau konnte nicht geladen werden: ${error.message}`;
    }
  }

  #render(details, payloadFor) {
    const grid = this.querySelector("#page-thumbs");
    grid.innerHTML = details
      .map(({ page }) => {
        const state = page.online ? (page.draftChanged ? "Online · Änderungen offen" : "Online") : "Entwurf";
        return `<li class="page-thumb">
          <a class="page-thumb__link" href="#/partner/shop/editor?page=${page.id}" aria-label="${escapeHtml(page.menuName)} bearbeiten">
            <span class="page-thumb__viewport">
              <iframe src="/shop.html?preview=1" title="Vorschau ${escapeHtml(page.menuName)}" tabindex="-1" aria-hidden="true" loading="lazy" data-page-id="${page.id}"></iframe>
            </span>
            <span class="page-thumb__caption">
              <span class="page-thumb__name">${escapeHtml(page.menuName)}</span>
              <span class="page-thumb__state${page.online ? " is-online" : ""}">${escapeHtml(state)}</span>
            </span>
          </a>
        </li>`;
      })
      .join("");
    const byId = new Map(details.map((detail) => [detail.page.id, detail]));
    for (const frame of grid.querySelectorAll("iframe")) {
      this.#frames.set(frame.contentWindow, { frame, payload: payloadFor(byId.get(Number(frame.dataset.pageId))) });
    }
  }

  // Each frame announces itself once its renderer is ready.
  #onMessage = (event) => {
    if (event.origin !== location.origin || event.data?.type !== "papo-shop-preview-ready") return;
    const entry = this.#frames.get(event.source);
    if (entry) postPreview(entry.frame, entry.payload);
  };
}

customElements.define("shop-page-thumbnails", ShopPageThumbnails);
