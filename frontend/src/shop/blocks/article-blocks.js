import { escapeHtml } from "../../lib/html.js";
import { MAX_QUANTITY, addToCart } from "../shop-cart.js";
import { articleCardsHtml, imageHtml, priceHtml } from "./article-card.js";
import { ShopBlock } from "./shop-block.js";
import { textField } from "./text-field.js";

function headingHtml(api, text) {
  return textField(api, "h2", "shop-heading", "heading", text);
}

// Shared by grid and featured list: loads the shop catalogue once, then renders cards.
class ArticleListBlock extends ShopBlock {
  #renderId = 0;

  render(config) {
    const renderId = ++this.#renderId;
    this.hidden = false;
    this.innerHTML = `<div class="shop-inner shop-list">${headingHtml(this.context.api, config.heading)}<p class="shop-muted">Artikel werden geladen …</p></div>`;
    this.context.api
      .articles()
      .then((articles) => {
        if (renderId !== this.#renderId) return;
        const selected = this.select(articles, config);
        // A featured block whose articles all disappeared renders nothing instead of an error;
        // the host is hidden too, so its spacing and background do not leave an empty band.
        if (selected === null) {
          this.replaceChildren();
          this.hidden = true;
          return;
        }
        this.innerHTML = `<div class="shop-inner shop-list">${headingHtml(this.context.api, config.heading)}${articleCardsHtml(this.context.api, selected, config.columns)}</div>`;
      })
      .catch(() => {
        if (renderId !== this.#renderId) return;
        this.innerHTML = `<div class="shop-inner"><p class="shop-muted">Artikel konnten nicht geladen werden.</p></div>`;
      });
  }
}

class ArticleGridBlock extends ArticleListBlock {
  select(articles, config) {
    return config.categoryId ? articles.filter((article) => article.categoryId === config.categoryId) : articles;
  }
}

// Keeps the chosen order; ids no longer in the public catalogue are silently omitted.
class FeaturedArticlesBlock extends ArticleListBlock {
  select(articles, config) {
    const byId = new Map(articles.map((article) => [article.id, article]));
    const selected = (config.articleIds ?? []).map((id) => byId.get(id)).filter(Boolean);
    return selected.length ? selected : null;
  }
}

// Central article data only (shop-pages spec "Single article-detail template").
class ArticleDetailBlock extends ShopBlock {
  render(config) {
    const { api, article } = this.context;
    if (!article) {
      this.innerHTML = `<div class="shop-inner"><p class="shop-muted">Dieser Artikel ist nicht verfügbar.</p></div>`;
      return;
    }
    this.innerHTML = `<article class="shop-inner shop-split shop-detail shop-split--image-${escapeHtml(config.imagePosition ?? "left")}">
      <div class="shop-detail__media">${imageHtml(api, article, "shop-split__image")}</div>
      <div class="shop-split__text">
        ${article.category ? `<p class="shop-muted shop-detail__category">${escapeHtml(article.category)}</p>` : ""}
        <h1 class="shop-heading">${escapeHtml(article.name)}</h1>
        ${priceHtml(article)}
        ${article.description ? `<p class="shop-text">${escapeHtml(article.description)}</p>` : ""}
        <div class="shop-detail__actions" data-detail-actions>${buyHtml(article)}</div>
      </div>
    </article>`;
    this.#wireBuy(api, article);
    this.#inquiryLink(api, article);
  }

  // Quantity and "In den Warenkorb" (shop-checkout "Shop cart"). The editor preview shows the
  // same controls so the page looks as published, but only the live shop stores anything.
  #wireBuy(api, article) {
    const form = this.querySelector("[data-buy]");
    if (!form) return;
    const input = form.elements.quantity;
    const status = form.querySelector("[data-buy-status]");
    form.addEventListener("click", (event) => {
      const step = event.target.closest("[data-step]");
      if (!step) return;
      const next = Math.min(MAX_QUANTITY, Math.max(1, (Number(input.value) || 1) + Number(step.dataset.step)));
      input.value = String(next);
    });
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (!api.live) return;
      const quantity = Math.min(MAX_QUANTITY, Math.max(1, Math.trunc(Number(input.value)) || 1));
      if (!addToCart(api.kennung, article.id, quantity)) {
        status.innerHTML = "Der Warenkorb ist voll. Bitte bestellen Sie zuerst die vorhandenen Artikel.";
        return;
      }
      input.value = "1";
      const button = form.querySelector("[type=submit]");
      button.classList.add("shop-buy__button--done");
      button.querySelector("[data-label]").textContent = "Im Warenkorb";
      clearTimeout(this.buyTimer);
      this.buyTimer = setTimeout(() => {
        button.classList.remove("shop-buy__button--done");
        button.querySelector("[data-label]").textContent = "In den Warenkorb";
      }, 1800);
      status.innerHTML = `${escapeHtml(quantity)} × hinzugefügt. <a href="${escapeHtml(api.href("warenkorb"))}">Zum Warenkorb</a>`;
    });
  }

  // "Weitere Fragen?" only when an online page carries the inquiry form (shop-inquiries);
  // the form preselects the article from ?artikel=. It sits below the cart controls.
  #inquiryLink(api, article) {
    const renderedFor = article.id;
    api.navigation?.()
      .then((navigation) => {
        const path = navigation?.inquiryPath;
        const actions = this.querySelector("[data-detail-actions]");
        if (path == null || !actions || this.context.article?.id !== renderedFor || actions.querySelector(".shop-detail__ask")) return;
        actions.insertAdjacentHTML("beforeend", `<p class="shop-detail__ask">Weitere Fragen?
          <a href="${escapeHtml(api.href(`${path}?artikel=${encodeURIComponent(article.id)}`))}">Schreiben Sie uns eine Nachricht</a></p>`);
      })
      .catch(() => {});
  }
}

function buyHtml(article) {
  const id = `buy-${article.id}`;
  return `<form class="shop-buy" data-buy novalidate>
    <div class="shop-buy__row">
      <div class="shop-stepper" role="group" aria-labelledby="${id}-label">
        <span class="shop-visually-hidden" id="${id}-label">Menge</span>
        <button type="button" data-step="-1" aria-label="Eins weniger"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M5 12h14"/></svg></button>
        <input type="number" name="quantity" inputmode="numeric" min="1" max="${MAX_QUANTITY}" value="1" aria-label="Menge" />
        <button type="button" data-step="1" aria-label="Eins mehr"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg></button>
      </div>
      <button type="submit" class="shop-button shop-buy__button"><svg class="shop-buy__check" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6 9 17l-5-5"/></svg><span data-label>In den Warenkorb</span></button>
    </div>
    <p class="shop-buy__status" role="status" data-buy-status></p>
  </form>`;
}

customElements.define("shop-block-article-grid", ArticleGridBlock);
customElements.define("shop-block-featured-articles", FeaturedArticlesBlock);
customElements.define("shop-block-article-detail", ArticleDetailBlock);
