// Shared article-line + Endkunde-picker rendering/wiring for order-creation forms.
// Extracted out of partner-order-new-page.js so the company's manual order-creation
// flow (design.md risk note) reuses this instead of duplicating the whole page.
import { escapeHtml } from "./html.js";
import { formatEuro, formatNumber, formatAddress, formatDate } from "./format.js";
import { addressFieldsHtml, readAddress, setAddressRequired } from "./address-fields.js";
import { toCents, commissionCentsHalfUp } from "./money.js";
import { cartTotals, orderTotalsHtml, priceStackHtml } from "./price.js";

const MAX_SUGGESTIONS = 20;
const percentFormat = new Intl.NumberFormat("de-AT", { maximumFractionDigits: 2 });

// Full Endkunde details (name, address, contact) — shown on the partner
// wizard's "Übersicht" and "Erfolg" steps so the partner can double-check who
// the order is for, without duplicating the (differently-shaped) one-line
// mention already baked into renderOrderConfirmationHtml below.
export function kundeDetailsHtml(kunde) {
  const name = `${kunde.firstName ? escapeHtml(kunde.firstName) + " " : ""}${escapeHtml(kunde.lastName)}`;
  return `
    <div class="kunde-details">
      <h2 class="kunde-details__heading">Endkunde</h2>
      <p class="kunde-details__name">${name}</p>
      <p class="kunde-details__address">${escapeHtml(formatAddress(kunde.address))}</p>
      ${kunde.email ? `<p class="kunde-details__contact">${escapeHtml(kunde.email)}</p>` : ""}
      ${kunde.phone ? `<p class="kunde-details__contact">${escapeHtml(kunde.phone)}</p>` : ""}
    </div>`;
}

function customerOptionsHtml(customers) {
  return customers
    .map(
      (c) =>
        `<option value="${c.id}">${escapeHtml(c.lastName)}${c.firstName ? ", " + escapeHtml(c.firstName) : ""} – ${escapeHtml(formatAddress(c.address))}</option>`,
    )
    .join("");
}

export function kundePickerHtml(customers) {
  const hasCustomers = customers.length > 0;
  return `
    <fieldset class="form-field--wide">
      <legend>Endkunde</legend>
      <label class="radio-field">
        <input type="radio" name="kundeMode" value="existing" ${hasCustomers ? "checked" : "disabled"} />
        Bestehenden Endkunden wählen
      </label>
      <label class="radio-field">
        <input type="radio" name="kundeMode" value="new" ${hasCustomers ? "" : "checked"} />
        Neuen Endkunden anlegen
      </label>
      <div id="existing-kunde-block" ${hasCustomers ? "" : "hidden"}>
        <form-field label="Endkunde" name="kundeId">
          <select name="kundeId" required>
            <option value="" disabled selected>Bitte Endkunden wählen…</option>
            ${customerOptionsHtml(customers)}
          </select>
        </form-field>
      </div>
      <div id="new-kunde-block" ${hasCustomers ? "hidden" : ""}>
        <div class="entity-form--grid" style="margin-top:0">
          <form-field label="Nachname" name="newLastName" required></form-field>
          <form-field label="Vorname" name="newFirstName"></form-field>
          ${addressFieldsHtml("newAddress")}
          <form-field label="E-Mail" name="newEmail" type="email"></form-field>
          <form-field label="Telefon" name="newPhone"></form-field>
        </div>
      </div>
    </fieldset>`;
}

export function articleLinesHtml() {
  return `
    <fieldset class="form-field--wide">
      <legend>Artikel</legend>
      <div id="order-lines"></div>
      <button type="button" id="add-line-btn">Artikel hinzufügen</button>
      <p class="order-lines-total">
        <strong>Zwischensumme: <span id="order-lines-total-value" aria-live="polite">€ 0,00</span></strong>
      </p>
    </fieldset>`;
}

export function wireKundePicker(form) {
  const existingBlock = form.querySelector("#existing-kunde-block");
  const newBlock = form.querySelector("#new-kunde-block");
  const existingSelect = form.elements.kundeId;
  const validateExistingCustomer = () => {
    const requiresExistingCustomer = form.elements.kundeMode.value === "existing";
    existingSelect.setCustomValidity(
      requiresExistingCustomer && !existingSelect.value ? "Bitte Endkunden wählen." : "",
    );
  };
  const toggle = () => {
    const mode = form.elements.kundeMode.value;
    existingBlock.hidden = mode !== "existing";
    newBlock.hidden = mode !== "new";
    // Required only while visible, so the browser doesn't block submit on hidden
    // "neuer Kunde" fields when "bestehenden Kunden wählen" is selected.
    const isNew = mode === "new";
    existingSelect.required = !isNew;
    form.elements.newLastName.required = isNew;
    setAddressRequired(form, "newAddress", isNew);
    validateExistingCustomer();
  };
  for (const radio of form.elements.kundeMode) {
    radio.addEventListener("change", toggle);
  }
  existingSelect.addEventListener("change", validateExistingCustomer);
  toggle();
}

function filterProducts(products, term) {
  const needle = term.trim().toLowerCase();
  if (!needle) return [];
  return products.filter(
    (p) => p.name.toLowerCase().includes(needle) || (p.category ?? "").toLowerCase().includes(needle),
  );
}

function suggestionHtml(product, index, lineId) {
  const disabled = !product.available;
  return `
    <li
      role="option"
      id="combobox-option-${lineId}-${index}"
      class="product-combobox__option${disabled ? " product-combobox__option--disabled" : ""}"
      data-product-id="${product.id}"
      aria-disabled="${disabled}"
    >
      <span class="product-combobox__option-name">${escapeHtml(product.name)}</span>
      <span class="product-combobox__option-category">${escapeHtml(product.category ?? "")}</span>
      <span class="product-combobox__option-price">${formatEuro(product.price)}</span>
      ${disabled ? `<span class="product-combobox__option-unavailable">(nicht verfügbar)</span>` : ""}
    </li>`;
}

function productLineHtml(lineId) {
  return `
    <div class="order-line" data-line-id="${lineId}">
      <div class="product-combobox">
        <input
          type="text"
          class="product-combobox__input"
          placeholder="Artikel suchen…"
          autocomplete="off"
          role="combobox"
          aria-expanded="false"
          aria-autocomplete="list"
          aria-controls="combobox-listbox-${lineId}"
        />
        <input type="hidden" name="productId" required />
        <ul class="product-combobox__listbox" id="combobox-listbox-${lineId}" role="listbox" hidden></ul>
      </div>
      <span class="order-line__unit-price" data-role="unit-price">–</span>
      <input type="number" name="quantity" min="1" step="1" value="1" required />
      <span class="order-line__total" data-role="line-total">–</span>
      <button type="button" class="remove-line-btn">Entfernen</button>
    </div>`;
}

// Wires one article line's combobox (search/filter/keyboard/outside-click) and
// its price/line-total display. Returns a controller so the caller (wireArticleLines)
// can read the current selection and forward outside-click events without each
// line registering its own document-level listener.
function wireComboboxLine(lineEl, products, lineId, notify) {
  const searchInput = lineEl.querySelector(".product-combobox__input");
  const hiddenInput = lineEl.querySelector('input[name="productId"]');
  const listbox = lineEl.querySelector(".product-combobox__listbox");
  const quantityInput = lineEl.querySelector('input[name="quantity"]');
  const unitPriceEl = lineEl.querySelector('[data-role="unit-price"]');
  const lineTotalEl = lineEl.querySelector('[data-role="line-total"]');

  let activeIndex = -1;
  let currentMatches = [];

  function selectedProduct() {
    const id = hiddenInput.value;
    return id ? products.find((p) => String(p.id) === id) : null;
  }

  function updateLineDisplay() {
    const product = selectedProduct();
    const quantity = Number(quantityInput.value) || 0;
    if (product) {
      unitPriceEl.textContent = formatEuro(product.price);
      const line = cartTotals([{ product, quantity }]);
      lineTotalEl.innerHTML = priceStackHtml(line.gross, line.net);
    } else {
      unitPriceEl.textContent = "–";
      lineTotalEl.textContent = "–";
    }
    notify();
  }

  function closeListbox() {
    listbox.hidden = true;
    listbox.innerHTML = "";
    searchInput.setAttribute("aria-expanded", "false");
    searchInput.removeAttribute("aria-activedescendant");
    activeIndex = -1;
    currentMatches = [];
  }

  function selectableIndexes() {
    return currentMatches.reduce((acc, p, i) => {
      if (p.available) acc.push(i);
      return acc;
    }, []);
  }

  function setActive(index) {
    activeIndex = index;
    const options = listbox.querySelectorAll("li[data-product-id]");
    options.forEach((el, i) => el.classList.toggle("product-combobox__option--active", i === index));
    if (index >= 0) {
      searchInput.setAttribute("aria-activedescendant", `combobox-option-${lineId}-${index}`);
      options[index]?.scrollIntoView({ block: "nearest" });
    } else {
      searchInput.removeAttribute("aria-activedescendant");
    }
  }

  function choose(product) {
    if (!product.available) return;
    hiddenInput.value = String(product.id);
    searchInput.value = product.name;
    closeListbox();
    updateLineDisplay();
  }

  function openListbox() {
    const term = searchInput.value;
    const matches = filterProducts(products, term);
    if (!term.trim()) {
      closeListbox();
      return;
    }
    if (matches.length === 0) {
      currentMatches = [];
      listbox.innerHTML = `<li class="product-combobox__empty">Keine Artikel gefunden</li>`;
      listbox.hidden = false;
      searchInput.setAttribute("aria-expanded", "true");
      activeIndex = -1;
      return;
    }
    const visible = matches.slice(0, MAX_SUGGESTIONS);
    const hasMore = matches.length > MAX_SUGGESTIONS;
    listbox.innerHTML =
      visible.map((p, i) => suggestionHtml(p, i, lineId)).join("") +
      (hasMore ? `<li class="product-combobox__hint">Weitere Treffer eingrenzen…</li>` : "");
    currentMatches = visible;
    listbox.hidden = false;
    searchInput.setAttribute("aria-expanded", "true");
    activeIndex = -1;
  }

  searchInput.addEventListener("input", () => {
    if (hiddenInput.value) {
      // Stale-selection guard: editing after a choice clears it, so the line can
      // never display one product while submitting another.
      hiddenInput.value = "";
      updateLineDisplay();
    }
    openListbox();
  });

  searchInput.addEventListener("focus", () => {
    if (searchInput.value.trim()) openListbox();
  });

  searchInput.addEventListener("keydown", (event) => {
    const selectable = selectableIndexes();
    if (event.key === "ArrowDown") {
      event.preventDefault();
      if (!selectable.length) return;
      const pos = selectable.indexOf(activeIndex);
      setActive(selectable[(pos + 1) % selectable.length]);
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      if (!selectable.length) return;
      const pos = selectable.indexOf(activeIndex);
      setActive(selectable[(pos - 1 + selectable.length) % selectable.length]);
    } else if (event.key === "Enter") {
      // Prevent the native implicit form submission whenever the dropdown is open
      // (searching, or only a disabled/no-match result showing) - Enter's job here
      // is to confirm a suggestion, never to submit the form out from under the user.
      if (!listbox.hidden) {
        event.preventDefault();
        if (activeIndex >= 0 && currentMatches[activeIndex]) {
          choose(currentMatches[activeIndex]);
        }
      }
    } else if (event.key === "Escape") {
      closeListbox();
    }
  });

  listbox.addEventListener("click", (event) => {
    const li = event.target.closest("li[data-product-id]");
    if (!li) return;
    const product = products.find((p) => String(p.id) === li.dataset.productId);
    if (product) choose(product);
  });

  quantityInput.addEventListener("input", updateLineDisplay);

  updateLineDisplay();

  return {
    el: lineEl,
    getSelection() {
      const product = selectedProduct();
      const quantity = Number(quantityInput.value) || 0;
      return { product, quantity, lineTotalCents: product ? toCents(product.price) * quantity : 0 };
    },
    handleOutsideClick(event) {
      if (!lineEl.contains(event.target)) closeListbox();
    },
  };
}

export function wireArticleLines(form, products, onLinesChange) {
  const linesContainer = form.querySelector("#order-lines");
  const totalValueEl = form.querySelector("#order-lines-total-value");
  const lineControllers = [];
  let lineIdCounter = 0;

  function notifyChange() {
    const items = lineControllers.map((c) => c.getSelection()).filter((s) => s.product);
    const totalCents = items.reduce((sum, i) => sum + i.lineTotalCents, 0);
    if (totalValueEl) totalValueEl.textContent = formatEuro(totalCents / 100);
    onLinesChange?.({ items, totalCents });
  }

  function addLine() {
    lineIdCounter += 1;
    const wrapper = document.createElement("div");
    wrapper.innerHTML = productLineHtml(lineIdCounter);
    const lineEl = wrapper.firstElementChild;
    linesContainer.appendChild(lineEl);
    lineControllers.push(wireComboboxLine(lineEl, products, lineIdCounter, notifyChange));
  }

  form.querySelector("#add-line-btn").addEventListener("click", addLine);
  linesContainer.addEventListener("click", (event) => {
    const button = event.target.closest(".remove-line-btn");
    if (!button) return;
    const lineEl = button.closest(".order-line");
    const index = lineControllers.findIndex((c) => c.el === lineEl);
    if (index >= 0) lineControllers.splice(index, 1);
    lineEl.remove();
    notifyChange();
  });
  // wireArticleLines() runs again every time an order form is (re-)rendered - a
  // partner placing a second order, the company switching partners, a dashboard
  // quick-order reopened. Without this, each run would leave behind a permanent
  // document listener over stale (detached) lines. Self-unsubscribes the first time
  // it fires after linesContainer is no longer in the document.
  function handleDocumentClick(event) {
    if (!linesContainer.isConnected) {
      document.removeEventListener("click", handleDocumentClick);
      return;
    }
    for (const controller of lineControllers) controller.handleOutsideClick(event);
  }
  document.addEventListener("click", handleDocumentClick);

  addLine();
}

export function readKundePayload(form) {
  const data = new FormData(form);
  const mode = data.get("kundeMode");
  return {
    kundeId: mode === "existing" ? Number(data.get("kundeId")) : null,
    newKunde:
      mode === "new"
        ? {
            lastName: data.get("newLastName"),
            address: readAddress(form, "newAddress"),
            firstName: data.get("newFirstName") || null,
            email: data.get("newEmail") || null,
            phone: data.get("newPhone") || null,
          }
        : null,
  };
}

export function readItemsPayload(form) {
  const data = new FormData(form);
  return data.getAll("productId").map((productId, i) => ({
    productId: Number(productId),
    quantity: Number(data.getAll("quantity")[i]),
  }));
}

// Shared by the post-submit confirmation and the live order summary, so the two
// can't drift apart (design.md risk note).
// `items` is [{name, quantity, unitPrice, unitNet, lineTotal, lineNet}], prices gross.
export function itemsTableFragment(items) {
  const rows = items
    .map(
      (i) =>
        `<tr><td>${escapeHtml(i.name)}</td><td>${i.quantity}</td>`
        + `<td>${priceStackHtml(i.unitPrice, i.unitNet, { euro: false })}</td>`
        + `<td>${priceStackHtml(i.lineTotal, i.lineNet, { euro: false })}</td></tr>`,
    )
    .join("");
  return `
    <div class="data-table__scroll">
      <table class="data-table order-items-table">
        <thead><tr><th>Artikel</th><th>Menge</th><th>Preis inkl. USt (€)</th><th>Summe (€)</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    </div>`;
}

// Commission preview on the net value (commission spec), same rounding as the backend.
function commissionLineHtml(percent, netCents) {
  const commissionCents = commissionCentsHalfUp(netCents, percent);
  return `
    <p class="order-summary__commission">
      Ihre Provision (${percentFormat.format(percent)} % vom Nettowert): ${formatEuro(commissionCents / 100)}
      <span class="order-summary__estimate-hint">(Vorschau)</span>
    </p>`;
}

// Live order-summary panel (order-form-experience spec): tracks the composed
// order as the partner/company works. `commissionPercent` is null until known
// (company flow, before a partner is chosen) - the commission line is then omitted.
export function renderOrderSummaryHtml({ items, commissionPercent }) {
  if (items.length === 0) {
    return `<div class="order-summary order-summary--empty"><p>Noch keine Artikel</p></div>`;
  }
  const rows = items.map((i) => {
    const line = cartTotals([i]);
    return {
      name: i.product.name,
      quantity: i.quantity,
      unitPrice: i.product.price,
      unitNet: cartTotals([{ product: i.product, quantity: 1 }]).net,
      lineTotal: line.gross,
      lineNet: line.net,
    };
  });
  const totals = cartTotals(items);
  return `
    <div class="order-summary">
      ${itemsTableFragment(rows)}
      ${orderTotalsHtml(totals, { live: true })}
      ${commissionPercent != null ? commissionLineHtml(commissionPercent, totals.netCents) : ""}
    </div>`;
}

// On-screen confirmation shown right after an order is placed — same pattern for the
// partner's own flow and the company's manual creation flow. `actionsHtml` (e.g. a
// "Fertig"/"Neue Bestellung" button) is placed inside the card, as its last child.
// `kind: "shipping"` is the confirmation shown when the company sets an order to
// fulfilled. The e-mail notice reflects what the backend queued for the customer
// (order.customerEmailNotice, order-emails spec); no mail content is shown here.
const EMAIL_NOTICES = {
  QUEUED: (to, what) => `${what} wird an ${to} gesendet.`,
  REDIRECTED: (to, what) => `Testbetrieb: ${what} geht an das Test-Postfach, nicht an ${to}.`,
  NO_ADDRESS: (to, what) => `Keine E-Mail-Adresse hinterlegt – ${what} wird nicht gesendet.`,
  INVALID_ADDRESS: (to, what) => `Die hinterlegte E-Mail-Adresse ist ungültig – ${what} wird nicht gesendet.`,
  DISABLED: (to, what) => `Der E-Mail-Versand ist derzeit nicht aktiviert – ${what} wird nicht gesendet.`,
};

function emailNoticeHtml(order, isShipping) {
  const notice = EMAIL_NOTICES[order.customerEmailNotice];
  if (!notice) return "";
  const what = isShipping ? "die Abschlussbestätigung" : "die Bestellbestätigung";
  const text = notice(escapeHtml(order.kunde.email ?? ""), what);
  return `
      <section class="simulated-email">
        <h3>E-Mail an den Endkunden</h3>
        <p>${text.charAt(0).toUpperCase()}${text.slice(1)}</p>
      </section>`;
}

export function renderOrderConfirmationHtml(order, { showPartner = false, actionsHtml = "", kind = "order" } = {}) {
  const isShipping = kind === "shipping";
  const items = order.items.map((item) => ({
    name: item.productName,
    quantity: item.quantity,
    unitPrice: item.unitPriceSnapshot,
    unitNet: item.unitNetPrice,
    lineTotal: item.lineTotal,
    lineNet: item.lineNetTotal,
  }));
  const kundeName = `${order.kunde.firstName ? escapeHtml(order.kunde.firstName) + " " : ""}${escapeHtml(order.kunde.lastName)}`;

  return `
    <section class="order-confirmation">
      <h2>${isShipping ? `Bestellung Nr. ${order.id} abgeschlossen` : `Bestellung Nr. ${order.id} bestätigt`}</h2>
      <div class="print-document">
        <header class="print-letterhead print-only">
          <div class="print-letterhead__brand">PAPO</div>
          <div class="print-letterhead__title">Auftragsbeleg</div>
          <div class="print-letterhead__meta">
            <span class="print-letterhead__order-no">Bestellnummer ${order.id}</span>
            <span>${formatDate(order.orderDate)}</span>
          </div>
        </header>
        ${showPartner ? `<p>Partner: ${escapeHtml(order.partnerName)}</p>` : ""}
        <p>Endkunde: ${kundeName}, ${escapeHtml(formatAddress(order.kunde.address))}</p>
        ${itemsTableFragment(items)}
        ${orderTotalsHtml({ gross: order.orderValue, net: order.netValue, tax: order.taxValue })}
        ${order.commissionValue != null ? `<p class="print-only">Provision (${percentFormat.format(order.commissionPercentUsed)} %): ${formatEuro(order.commissionValue)}</p>` : ""}
      </div>
      <button type="button" class="print-button" data-action="print">Beleg drucken</button>
      ${emailNoticeHtml(order, isShipping)}
      ${actionsHtml}
    </section>`;
}
