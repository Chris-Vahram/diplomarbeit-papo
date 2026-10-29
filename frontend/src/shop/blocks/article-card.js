import { escapeHtml } from "../../lib/html.js";
import { formatEuro } from "../../lib/format.js";

// Gross price with the article's actual tax rate (shop-public-catalog spec "Price display").
export function priceHtml(article) {
  return `<p class="shop-price"><span class="shop-price__value">${escapeHtml(formatEuro(article.grossPrice))}</span>
    <span class="shop-price__tax">inkl. ${escapeHtml(article.taxRatePercent)} % USt</span></p>`;
}

export function imageHtml(api, article, className) {
  const url = api.resolveUrl(article.imageUrl);
  return url
    ? `<img class="${className}" src="${escapeHtml(url)}" alt="${escapeHtml(article.name)}" loading="lazy" />`
    : `<div class="${className} shop-image-placeholder" aria-hidden="true"></div>`;
}

export function articleCardsHtml(api, articles, columns) {
  if (!articles.length) {
    return `<p class="shop-muted">Derzeit sind hier keine Artikel verfügbar.</p>`;
  }
  const cards = articles
    .map(
      (article) => `<li class="shop-card">
        <a class="shop-card__link" href="${escapeHtml(api.href(`artikel/${article.id}`))}">
          <span class="shop-card__media">${imageHtml(api, article, "shop-card__image")}</span>
          <span class="shop-card__body">
            <span class="shop-card__name">${escapeHtml(article.name)}</span>
            ${article.category ? `<span class="shop-card__category">${escapeHtml(article.category)}</span>` : ""}
            ${priceHtml(article)}
          </span>
        </a>
      </li>`,
    )
    .join("");
  return `<ul class="shop-grid shop-grid--cols-${escapeHtml(columns ?? "3")}">${cards}</ul>`;
}
