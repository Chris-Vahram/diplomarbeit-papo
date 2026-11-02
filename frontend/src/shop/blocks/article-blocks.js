import { escapeHtml } from "../../lib/html.js";
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
        <div class="shop-detail__actions" data-detail-actions></div>
      </div>
    </article>`;
    this.#inquiryLink(api, article);
  }

  // "Weitere Fragen?" only when an online page carries the inquiry form (shop-inquiries);
  // the form preselects the article from ?artikel=. The actions area is where the cart
  // button goes later, above this link.
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

customElements.define("shop-block-article-grid", ArticleGridBlock);
customElements.define("shop-block-featured-articles", FeaturedArticlesBlock);
customElements.define("shop-block-article-detail", ArticleDetailBlock);
