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
      ${
        disabled
          ? `<button type="button" class="product-card__add-btn button-primary" data-add-to-cart disabled>Nicht verfügbar</button>`
          : `<div class="product-card__add-row">
               <input type="number" class="product-card__qty" data-qty min="1" step="1" value="1" aria-label="Menge" />
               <button type="button" class="product-card__add-btn button-primary" data-add-to-cart>In den Warenkorb</button>
             </div>`
      }
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

// "Produkte wählen" wizard step: catalog toolbar + grid only, no cart panel
// (the cart is the next step's content — partner-order-wizard design.md decision #2).
export function catalogStepHtml(products) {
  return `
    <div class="catalog">
      <div class="catalog__toolbar">
        <input type="search" class="catalog__search" placeholder="Artikel suchen…" aria-label="Artikel suchen" />
        <select class="catalog__category-filter" aria-label="Artikelkategorie">
          <option value="">Alle Artikelkategorien</option>
          ${categoryOptionsHtml(products)}
        </select>
      </div>
      <div class="catalog__grid" id="catalog-grid">${catalogGridHtml(products)}</div>
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

// Cart lines + totals, editable (quantity/remove) via wireCartStep - the
// "Produkte wählen" step's own live preview while browsing/adding. Empty state
// has no submit button (an empty cart cannot be submitted, spec requirement).
export function cartStepHtml(cart, commissionPercent, { showSubmit = true } = {}) {
  if (cart.length === 0) {
    return `<div class="cart__empty"><p>Warenkorb ist leer</p></div>`;
  }
  return `
    <div class="cart__lines">${cart.map(cartLineHtml).join("")}</div>
    <div class="cart__totals">${cartTotalsHtml(cart, commissionPercent)}</div>
    ${showSubmit ? `<button type="button" id="cart-submit-btn" class="cart__submit-btn button-primary">Bestellung aufgeben</button>` : ""}`;
}

function cartSummaryLineHtml({ product, quantity }) {
  const lineTotalCents = toCents(product.price) * quantity;
  return `
    <div class="cart-summary-line">
      ${thumbHtml(product.imageUrl, "cart-summary-line__thumb")}
      <span class="cart-summary-line__name">${escapeHtml(product.name)}</span>
      <span class="cart-summary-line__qty">× ${quantity}</span>
      <span class="cart-summary-line__total">${formatEuro(lineTotalCents / 100)}</span>
    </div>`;
}

// Read-only cart review for the "Übersicht" step, plus the submit button - no
// quantity/remove controls here (per the wizard spec, "Übersicht" only reviews
// and submits; changing the order means going back to "Produkte wählen" via
// the checkpoint bar, not editing in place).
export function cartSummaryHtml(cart, commissionPercent, { showSubmit = true } = {}) {
  if (cart.length === 0) {
    return `<div class="cart__empty"><p>Warenkorb ist leer</p></div>`;
  }
  return `
    <div class="cart-summary__lines">${cart.map(cartSummaryLineHtml).join("")}</div>
    <div class="cart__totals">${cartTotalsHtml(cart, commissionPercent)}</div>
    ${showSubmit ? `<button type="button" id="cart-submit-btn" class="cart__submit-btn button-primary">Bestellung aufgeben</button>` : ""}`;
}

// Wires catalog search/filter/add-to-cart for the "Produkte wählen" step. `cart`
// is the wizard's own state array (partner-order-new-page.js), mutated in place
// here so it stays the single source of truth across step navigation; `onCartChange`
// lets the page refresh its own cart-preview panel and Weiter-button.
export function wireCatalogStep(root, products, cart, onCartChange) {
  const searchInput = root.querySelector(".catalog__search");
  const categorySelect = root.querySelector(".catalog__category-filter");
  const grid = root.querySelector("#catalog-grid");

  function renderGrid() {
    const filtered = filterCatalog(products, searchInput.value, categorySelect.value);
    grid.innerHTML = catalogGridHtml(filtered);
  }

  searchInput.addEventListener("input", renderGrid);
  categorySelect.addEventListener("change", renderGrid);

  // Image load failures (broken/empty blob URL) fall back to the neutral
  // placeholder, same technique as data-table.js's thumbnail handling.
  function handleThumbError(event) {
    const img = event.target;
    if (!(img instanceof HTMLImageElement)) return;
    if (!img.classList.contains("product-card__thumb")) return;
    const placeholder = document.createElement("span");
    placeholder.className = `${img.className} ${img.className}--empty`;
    img.replaceWith(placeholder);
  }
  grid.addEventListener("error", handleThumbError, true);

  grid.addEventListener("click", (event) => {
    const button = event.target.closest("[data-add-to-cart]");
    if (!button || button.disabled) return;
    const card = button.closest("[data-product-id]");
    const productId = Number(card.dataset.productId);
    const product = products.find((p) => p.id === productId);
    if (!product) return;
    const qtyInput = card.querySelector("[data-qty]");
    const quantity = qtyInput ? clampQuantity(qtyInput.value) : 1;
    const existing = cart.find((i) => i.product.id === productId);
    if (existing) {
      existing.quantity += quantity;
    } else {
      cart.push({ product, quantity });
    }
    // Reset for the next add rather than leaving the just-added amount showing.
    if (qtyInput) qtyInput.value = "1";
    onCartChange();
  });
}

// The "Übersicht" summary has no other interactions (cartSummaryHtml is
// read-only) - just the same broken/empty-thumbnail fallback wireCatalogStep/
// wireCartStep use elsewhere.
export function wireCartSummaryThumbs(root) {
  root.addEventListener(
    "error",
    (event) => {
      const img = event.target;
      if (!(img instanceof HTMLImageElement)) return;
      if (!img.classList.contains("cart-summary-line__thumb")) return;
      const placeholder = document.createElement("span");
      placeholder.className = `${img.className} ${img.className}--empty`;
      img.replaceWith(placeholder);
    },
    true,
  );
}

// Wires cart quantity/remove interactions - shared by the "Produkte wählen"
// step's editable preview and the "Übersicht" step (see cartStepHtml). `cart` is
// the wizard's own state array, mutated in place; `onCartChange` lets the page
// know (e.g. to refresh a "Weiter" button's disabled state elsewhere).
export function wireCartStep(root, cart, commissionPercent, onCartChange, { showSubmit = true } = {}) {
  function render() {
    root.innerHTML = cartStepHtml(cart, commissionPercent, { showSubmit });
  }

  function handleThumbError(event) {
    const img = event.target;
    if (!(img instanceof HTMLImageElement)) return;
    if (!img.classList.contains("cart-line__thumb")) return;
    const placeholder = document.createElement("span");
    placeholder.className = `${img.className} ${img.className}--empty`;
    img.replaceWith(placeholder);
  }
  root.addEventListener("error", handleThumbError, true);

  root.addEventListener("click", (event) => {
    if (event.target.closest("[data-remove]")) {
      const line = event.target.closest("[data-product-id]");
      const productId = Number(line.dataset.productId);
      const index = cart.findIndex((i) => i.product.id === productId);
      if (index >= 0) cart.splice(index, 1);
      render();
      onCartChange();
    }
  });

  root.addEventListener("input", (event) => {
    const quantityInput = event.target.closest("[data-quantity]");
    if (!quantityInput) return;
    const line = quantityInput.closest("[data-product-id]");
    const productId = Number(line.dataset.productId);
    const item = cart.find((i) => i.product.id === productId);
    if (!item) return;
    // Clamp to a whole number in the backend's own accepted range
    // (OrderItemRequest: @Min(1) @Max(10000)) and resync the field itself, so
    // a fraction/blank/out-of-range entry can never reach the total, the
    // preview, or the submit payload - matches the value actually stored.
    const quantity = clampQuantity(quantityInput.value);
    item.quantity = quantity;
    if (quantityInput.value !== String(quantity)) quantityInput.value = quantity;
    // Update just this line's total + the overall totals, not the whole cart
    // panel, so the quantity input doesn't lose focus while the partner types.
    line.querySelector("[data-line-total]").textContent = formatEuro((toCents(item.product.price) * quantity) / 100);
    root.querySelector(".cart__totals").innerHTML = cartTotalsHtml(cart, commissionPercent);
    onCartChange();
  });
}
