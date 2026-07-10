// Shared article-line + Endkunde-picker rendering/wiring for order-creation forms.
// Extracted out of partner-order-new-page.js so the company's manual order-creation
// flow (design.md risk note) reuses this instead of duplicating the whole page.
import { escapeHtml } from "./html.js";
import { formatEuro, formatNumber, formatAddress } from "./format.js";
import { addressFieldsHtml, readAddress, setAddressRequired } from "./address-fields.js";

export function productOptionsHtml(products) {
  return products
    .map(
      (p) =>
        `<option value="${p.id}" ${p.available ? "" : "disabled"}>${escapeHtml(p.name)}${p.available ? "" : " (nicht verfügbar)"}</option>`,
    )
    .join("");
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
          <select name="kundeId">${customerOptionsHtml(customers)}</select>
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
    </fieldset>`;
}

export function wireKundePicker(form) {
  const existingBlock = form.querySelector("#existing-kunde-block");
  const newBlock = form.querySelector("#new-kunde-block");
  const toggle = () => {
    const mode = form.elements.kundeMode.value;
    existingBlock.hidden = mode !== "existing";
    newBlock.hidden = mode !== "new";
    // Required only while visible, so the browser doesn't block submit on hidden
    // "neuer Kunde" fields when "bestehenden Kunden wählen" is selected.
    const isNew = mode === "new";
    form.elements.newLastName.required = isNew;
    setAddressRequired(form, "newAddress", isNew);
  };
  for (const radio of form.elements.kundeMode) {
    radio.addEventListener("change", toggle);
  }
  toggle();
}

export function wireArticleLines(form, productOptions) {
  const linesContainer = form.querySelector("#order-lines");
  const addLine = () => {
    const row = document.createElement("div");
    row.className = "order-line";
    row.innerHTML = `
      <select name="productId" required>${productOptions}</select>
      <input type="number" name="quantity" min="1" step="1" value="1" required />
      <button type="button" class="remove-line-btn">Entfernen</button>
    `;
    linesContainer.appendChild(row);
  };
  form.querySelector("#add-line-btn").addEventListener("click", addLine);
  linesContainer.addEventListener("click", (event) => {
    const button = event.target.closest(".remove-line-btn");
    if (button) button.closest(".order-line").remove();
  });
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

// On-screen confirmation + simulated ("not really sent") e-mail shown right after an
// order is placed — same pattern for the partner's own flow and the company's manual
// creation flow. `actionsHtml` (e.g. a "Fertig"/"Neue Bestellung" button) is placed
// inside the card, as its last child. `kind: "shipping"` renders the FR-17 simulated
// shipping confirmation shown when the company sets an order to fulfilled — same
// layout, distinct heading/e-mail text so it isn't mistaken for the order confirmation.
export function renderOrderConfirmationHtml(order, { showPartner = false, actionsHtml = "", kind = "order" } = {}) {
  const isShipping = kind === "shipping";
  const rows = order.items
    .map(
      (item) =>
        `<tr><td>${escapeHtml(item.productName)}</td><td>${item.quantity}</td><td>${formatNumber(item.unitPriceSnapshot)}</td><td>${formatNumber(item.lineTotal)}</td></tr>`,
    )
    .join("");
  const kundeName = `${order.kunde.firstName ? escapeHtml(order.kunde.firstName) + " " : ""}${escapeHtml(order.kunde.lastName)}`;

  return `
    <section class="order-confirmation">
      <h2>${isShipping ? `Versandbestätigung für Bestellung Nr. ${order.id}` : `Bestellung Nr. ${order.id} bestätigt`}</h2>
      ${showPartner ? `<p>Partner: ${escapeHtml(order.partnerName)}</p>` : ""}
      <p>Endkunde: ${kundeName}, ${escapeHtml(formatAddress(order.kunde.address))}</p>
      <div class="data-table__scroll">
        <table class="data-table">
          <thead><tr><th>Artikel</th><th>Menge</th><th>Preis (€)</th><th>Summe (€)</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
      <p><strong>Gesamtwert: ${formatEuro(order.orderValue)}</strong></p>
      <section class="simulated-email">
        <h3>${isShipping ? "Simulierte Versandbestätigung (nicht wirklich versendet)" : "Simulierte Bestätigungs-E-Mail (nicht wirklich versendet)"}</h3>
        <p>An: ${escapeHtml(order.kunde.email) || "keine E-Mail-Adresse hinterlegt"}</p>
        <p>Betreff: ${isShipping ? `Ihre Bestellung Nr. ${order.id} wurde versendet` : `Ihre Bestellung Nr. ${order.id}`}</p>
        <p>${
          isShipping
            ? `Sehr geehrte/r ${kundeName}, Ihre Bestellung über ${formatEuro(order.orderValue)} wurde versendet und ist auf dem Weg zu Ihnen.`
            : `Sehr geehrte/r ${kundeName}, vielen Dank für Ihre Bestellung über ${formatEuro(order.orderValue)}. Wir bearbeiten sie umgehend.`
        }</p>
      </section>
      ${actionsHtml}
    </section>`;
}
