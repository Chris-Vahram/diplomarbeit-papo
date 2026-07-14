// Product catalog + shopping cart for the partner order-compose page
// (partner-order-cart spec). Browsing/search/category-filter mirror the pattern
// already used in partner-product-list-page.js; totals/commission reuse money.js
// verbatim so the preview matches the stored CommissionRecord to the cent.
import { escapeHtml } from "./html.js";
import { formatEuro } from "./format.js";
import { toCents, commissionCentsHalfUp } from "./money.js";

// Demo data is tiny; at the reference ~800 articles this caps the rendered grid
// so search/category narrowing (not a full-catalog render) becomes the normal
// path - a rendering optimization, not a behavior change (design.md).
const CATALOG_RENDER_CAP = 60;
// Matches OrderItemRequest's @Min(1) @Max(10000) (backend/.../order/OrderItemRequest.java).
const MAX_QUANTITY = 10000;
const percentFormat = new Intl.NumberFormat("de-AT", { maximumFractionDigits: 2 });

function clampQuantity(rawValue) {
  const parsed = Math.trunc(Number(rawValue));
  if (!Number.isFinite(parsed)) return 1;
  return Math.min(MAX_QUANTITY, Math.max(1, parsed));
}

function thumbHtml(url, className) {
  if (!url) return `<span class="${className} ${className}--empty"></span>`;
  return `<img class="${className}" src="${escapeHtml(url)}" alt="" loading="lazy">`;
}

function filterCatalog(products, term, category) {
  const needle = term.trim().toLowerCase();
  return products.filter((p) => {
    if (category && p.category !== category) return false;
    if (!needle) return true;
    return p.name.toLowerCase().includes(needle) || (p.category ?? "").toLowerCase().includes(needle);
  });
}

function productCardHtml(product) {
  const disabled = !product.available;
  return `
    <article class="product-card${disabled ? " product-card--disabled" : ""}" data-product-id="${product.id}">
      ${thumbHtml(product.imageUrl, "product-card__thumb")}
      <div class="product-card__body">
        <div class="product-card__name">${escapeHtml(product.name)}</div>
        ${product.category ? `<div class="product-card__category">${escapeHtml(product.category)}</div>` : ""}
        <div class="product-card__price">${formatEuro(product.price)}</div>
      </div>
      <button type="button" class="product-card__add-btn button-primary" data-add-to-cart ${disabled ? "disabled" : ""}>
        ${disabled ? "Nicht verfügbar" : "In den Warenkorb"}
      </button>
    </article>`;
}

function catalogGridHtml(products) {
  if (products.length === 0) {
    return `<p class="catalog__empty">Keine Artikel gefunden.</p>`;
  }
  const visible = products.slice(0, CATALOG_RENDER_CAP);
  const hasMore = products.length > CATALOG_RENDER_CAP;
  return (
    visible.map(productCardHtml).join("") +
    (hasMore ? `<p class="catalog__hint">Weitere Treffer eingrenzen…</p>` : "")
  );
}

function categoryOptionsHtml(products) {
  const categories = [...new Set(products.map((p) => p.category).filter(Boolean))].sort();
  return categories.map((c) => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join("");
}

// Initial shell: toolbar + catalog grid + an (initially empty) cart mount point.
// The catalog grid and cart panel are re-rendered independently by wireOrderCart
// as state changes, so neither disturbs the other (e.g. typing in the search
// field never touches the cart, adding to the cart never re-renders the grid).
export function orderCartHtml(products) {
  return `
    <div class="order-cart-layout">
      <div class="catalog">
        <div class="catalog__toolbar">
          <input type="search" class="catalog__search" placeholder="Artikel suchen…" aria-label="Artikel suchen" />
          <select class="catalog__category-filter" aria-label="Kategorie">
            <option value="">Alle Kategorien</option>
            ${categoryOptionsHtml(products)}
          </select>
        </div>
        <div class="catalog__grid" id="catalog-grid">${catalogGridHtml(products)}</div>
      </div>
      <div class="cart" id="cart-panel"></div>
    </div>`;
}

function cartLineHtml({ product, quantity }) {
  const lineTotalCents = toCents(product.price) * quantity;
  return `
    <div class="cart-line" data-product-id="${product.id}">
      ${thumbHtml(product.imageUrl, "cart-line__thumb")}
      <div class="cart-line__name">${escapeHtml(product.name)}</div>
      <span class="cart-line__unit-price">${formatEuro(product.price)}</span>
      <input type="number" class="cart-line__quantity" data-quantity min="1" step="1" value="${quantity}" aria-label="Menge" />
      <span class="cart-line__total" data-line-total>${formatEuro(lineTotalCents / 100)}</span>
      <button type="button" class="cart-line__remove" data-remove>Entfernen</button>
    </div>`;
}

// Integer cents throughout, no float round-trip (matches wireArticleLines's
// notifyChange() in order-form.js exactly) - the same pattern used for the
// existing order-form live totals, so this preview matches the backend to the cent.
function cartTotalCents(cart) {
  return cart.reduce((sum, i) => sum + toCents(i.product.price) * i.quantity, 0);
}

function cartTotalsHtml(cart, commissionPercent) {
  const totalCents = cartTotalCents(cart);
  const commissionHtml =
    commissionPercent != null
      ? `<p class="order-summary__commission">
           Ihre Provision (${percentFormat.format(commissionPercent)} %): ${formatEuro(commissionCentsHalfUp(totalCents, commissionPercent) / 100)}
           <span class="order-summary__estimate-hint">(Vorschau)</span>
         </p>`
      : "";
  return `
    <p class="order-summary__total"><strong>Gesamtwert: <span aria-live="polite">${formatEuro(totalCents / 100)}</span></strong></p>
    ${commissionHtml}`;
}

// Cart panel: empty state (no submit button - an empty cart cannot be submitted,
// spec requirement) or lines + totals + the submit button, associated to
// #order-form via the form="" attribute so it can live outside that <form>
// element (the Endkunde picker) while still participating in its submit/validation.
export function cartPanelHtml(cart, commissionPercent) {
  if (cart.length === 0) {
    return `<div class="cart__empty"><p>Warenkorb ist leer</p></div>`;
  }
  return `
    <div class="cart__lines">${cart.map(cartLineHtml).join("")}</div>
    <div class="cart__totals">${cartTotalsHtml(cart, commissionPercent)}</div>
    <button type="submit" form="order-form" class="cart__submit-btn button-primary">Bestellung aufgeben</button>`;
}

// Wires catalog search/filter/add-to-cart and cart quantity/remove interactions.
// Cart state lives here (component state, per design.md - not persisted, not
// scraped from form elements); the returned controller is how the page reads it
// back at submit time.
export function wireOrderCart(root, products, commissionPercent) {
  const searchInput = root.querySelector(".catalog__search");
  const categorySelect = root.querySelector(".catalog__category-filter");
  const grid = root.querySelector("#catalog-grid");
  const cartPanel = root.querySelector("#cart-panel");

  let cart = [];

  function renderCart() {
    cartPanel.innerHTML = cartPanelHtml(cart, commissionPercent);
  }

  function renderGrid() {
    const filtered = filterCatalog(products, searchInput.value, categorySelect.value);
    grid.innerHTML = catalogGridHtml(filtered);
  }

  searchInput.addEventListener("input", renderGrid);
  categorySelect.addEventListener("change", renderGrid);

  // Image load failures (broken/empty blob URL) fall back to the neutral
  // placeholder, same technique as data-table.js's thumbnail handling.
  // Attached to grid/cartPanel (recreated by the page's innerHTML reset on every
  // "Neue Bestellung" cycle), not root (this.content, which persists across those
  // resets) - otherwise repeated cycles within one page visit would accumulate one
  // stale capture-phase listener per cycle instead of being discarded with the DOM.
  function handleThumbError(event) {
    const img = event.target;
    if (!(img instanceof HTMLImageElement)) return;
    if (!img.classList.contains("product-card__thumb") && !img.classList.contains("cart-line__thumb")) return;
    const placeholder = document.createElement("span");
    placeholder.className = `${img.className} ${img.className}--empty`;
    img.replaceWith(placeholder);
  }
  grid.addEventListener("error", handleThumbError, true);
  cartPanel.addEventListener("error", handleThumbError, true);

  grid.addEventListener("click", (event) => {
    const button = event.target.closest("[data-add-to-cart]");
    if (!button || button.disabled) return;
    const card = button.closest("[data-product-id]");
    const productId = Number(card.dataset.productId);
    const product = products.find((p) => p.id === productId);
    if (!product) return;
    const existing = cart.find((i) => i.product.id === productId);
    if (existing) {
      existing.quantity += 1;
    } else {
      cart.push({ product, quantity: 1 });
    }
    renderCart();
  });

  cartPanel.addEventListener("click", (event) => {
    if (event.target.closest("[data-remove]")) {
      const line = event.target.closest("[data-product-id]");
      const productId = Number(line.dataset.productId);
      cart = cart.filter((i) => i.product.id !== productId);
      renderCart();
    }
  });

  cartPanel.addEventListener("input", (event) => {
    const quantityInput = event.target.closest("[data-quantity]");
    if (!quantityInput) return;
    const line = quantityInput.closest("[data-product-id]");
    const productId = Number(line.dataset.productId);
    const item = cart.find((i) => i.product.id === productId);
    if (!item) return;
    // The input lives outside #order-form (by design - see cartPanelHtml), so
    // its min/step attributes are display hints only, never enforced by the
    // browser. Clamp to a whole number in the backend's own accepted range
    // (OrderItemRequest: @Min(1) @Max(10000)) and resync the field itself, so
    // a fraction/blank/out-of-range entry can never reach the total, the
    // preview, or the submit payload - matches the value actually stored.
    const quantity = clampQuantity(quantityInput.value);
    item.quantity = quantity;
    if (quantityInput.value !== String(quantity)) quantityInput.value = quantity;
    // Update just this line's total + the overall totals, not the whole cart
    // panel, so the quantity input doesn't lose focus while the partner types.
    line.querySelector("[data-line-total]").textContent = formatEuro((toCents(item.product.price) * quantity) / 100);
    cartPanel.querySelector(".cart__totals").innerHTML = cartTotalsHtml(cart, commissionPercent);
  });

  renderCart();

  return {
    isEmpty: () => cart.length === 0,
    getItemsPayload: () => cart.map((i) => ({ productId: i.product.id, quantity: i.quantity })),
  };
}
