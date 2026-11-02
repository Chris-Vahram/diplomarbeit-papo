// Cart page (/warenkorb), checkout (/kasse) and order confirmation with printable receipt
// (shop-checkout spec). Only the live public shop renders these; the editor preview and the
// template demo never reach them. Prices shown here come from the current public catalogue;
// the order itself is priced by the server.
import { escapeHtml } from "../lib/html.js";
import { formatDate, formatEuro } from "../lib/format.js";
import { imageHtml } from "./blocks/article-card.js";
import {
  CART_EVENT,
  MAX_QUANTITY,
  clearCart,
  readCart,
  removeFromCart,
  setQuantity,
  summarize,
} from "./shop-cart.js";
import { accountRequest, currentCustomer, startLogin } from "./shop-account.js";

const PAYMENT_NOTE = "Die Rechnung erhalten Sie mit der Lieferung.";
const UNAVAILABLE = "Nicht mehr verfügbar";
const SEND_FAILED = "Die Bestellung konnte nicht gesendet werden. Bitte prüfen Sie Ihre Verbindung und versuchen Sie es erneut.";

const ICONS = {
  minus: '<path d="M5 12h14"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  remove: '<path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6"/>',
  check: '<path d="M20 6 9 17l-5-5"/>',
  print: '<path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><path d="M6 14h12v8H6z"/>',
  bag: '<path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><path d="M3 6h18M16 10a4 4 0 0 1-8 0"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  download: '<path d="M12 3v12M7 10l5 5 5-5M5 21h14"/>',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>',
  key: '<circle cx="7.5" cy="15.5" r="4.5"/><path d="m10.7 12.3 9.8-9.8M17 6l3 3M14 9l2 2"/>',
  chevron: '<path d="m6 9 6 6 6-6"/>',
  receipt: '<path d="M5 3h14v18l-3-2-2 2-2-2-2 2-2-2-3 2z"/><path d="M9 8h6M9 12h6"/>',
};

export function icon(name, size = 18) {
  return `<svg class="shop-icon" viewBox="0 0 24 24" width="${size}" height="${size}" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONS[name]}</svg>`;
}

const euro = (cents) => formatEuro(cents / 100);
const rateLabel = (rate) => `${String(rate).replace(".", ",")} %`;

function taxRowsHtml(taxes) {
  return taxes
    .map((entry) => `<div class="shop-sum__row shop-sum__row--minor"><dt>enthaltene USt ${escapeHtml(rateLabel(entry.rate))}</dt>
      <dd>${escapeHtml(euro(entry.taxCents))}</dd></div>`)
    .join("");
}

function totalsHtml(summary) {
  return `<dl class="shop-sum">
    <div class="shop-sum__row shop-sum__row--total"><dt>Gesamt <span>inkl. USt</span></dt>
      <dd aria-live="polite">${escapeHtml(euro(summary.grossCents))}</dd></div>
    ${taxRowsHtml(summary.taxes)}
  </dl>`;
}

function pageHead(title, lead) {
  return `<header class="shop-checkout__head">
    <h1 class="shop-heading shop-checkout__title">${escapeHtml(title)}</h1>
    ${lead ? `<p class="shop-muted">${lead}</p>` : ""}
  </header>`;
}

// ---- Cart page --------------------------------------------------------------------------

export async function renderCartPage(main, { api, shop }) {
  document.title = `Warenkorb – ${shop.displayName}`;
  const section = document.createElement("section");
  section.className = "shop-checkout shop-inner";
  main.replaceChildren(section);
  section.innerHTML = `${pageHead("Warenkorb")}<p class="shop-muted">Warenkorb wird geladen …</p>`;

  let articles;
  try {
    articles = await api.articles();
  } catch {
    section.innerHTML = `${pageHead("Warenkorb")}<p class="shop-muted">Der Warenkorb konnte nicht geladen werden. Bitte laden Sie die Seite neu.</p>`;
    return;
  }

  const draw = () => {
    const summary = summarize(api.kennung, articles);
    if (!summary.lines.length) {
      section.innerHTML = `${pageHead("Warenkorb")}
        <div class="shop-empty">
          <span class="shop-empty__icon">${icon("bag", 26)}</span>
          <p class="shop-empty__title">Ihr Warenkorb ist leer.</p>
          <p class="shop-muted">Legen Sie Artikel über die Artikelseite in den Warenkorb.</p>
          <a class="shop-button" href="${escapeHtml(api.href("artikel"))}">Artikel ansehen</a>
        </div>`;
      return;
    }
    const count = summary.available.reduce((sum, line) => sum + line.quantity, 0);
    section.innerHTML = `${pageHead("Warenkorb", `${count} Artikel · Preise inkl. USt`)}
      <div class="shop-checkout__layout">
        <ul class="shop-cart" aria-label="Artikel im Warenkorb">${summary.lines.map((line) => cartLineHtml(api, line)).join("")}</ul>
        <aside class="shop-checkout__aside" aria-label="Summe">
          <div class="shop-panel">
            ${totalsHtml(summary)}
            ${summary.hasUnavailable ? `<p class="shop-note shop-note--warn" role="status">Nicht mehr verfügbare Artikel werden nicht bestellt.</p>` : ""}
            ${summary.available.length
              ? `<a class="shop-button shop-button--block" href="${escapeHtml(api.href("kasse"))}">Zur Kasse</a>`
              : `<p class="shop-note">Bitte entfernen Sie die nicht mehr verfügbaren Artikel.</p>`}
            <p class="shop-panel__foot">${escapeHtml(PAYMENT_NOTE)}</p>
          </div>
          <a class="shop-link-back" href="${escapeHtml(api.href("artikel"))}">Weiter einkaufen</a>
        </aside>
      </div>`;
  };

  draw();
  section.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.closest("[data-line]").dataset.line);
    const line = readCart(api.kennung).find((entry) => entry.id === id);
    if (button.dataset.action === "remove") {
      removeFromCart(api.kennung, id);
      announce(section, "Artikel entfernt.");
    } else if (line) {
      setQuantity(api.kennung, id, line.quantity + (button.dataset.action === "plus" ? 1 : -1));
    }
    focusAfterRedraw(section, id, button.dataset.action);
  });
  section.addEventListener("change", (event) => {
    const input = event.target.closest("input[data-quantity]");
    if (!input) return;
    const id = Number(input.closest("[data-line]").dataset.line);
    setQuantity(api.kennung, id, input.value);
    focusAfterRedraw(section, id, "quantity");
  });
  // Changes from the header badge's other tabs or this page's own actions.
  const onChange = (event) => {
    if (event.type === "storage" || event.detail?.kennung === api.kennung) draw();
  };
  window.addEventListener(CART_EVENT, onChange);
  window.addEventListener("storage", onChange);
}

function cartLineHtml(api, line) {
  const { article } = line;
  if (!article) {
    return `<li class="shop-cart__line shop-cart__line--gone" data-line="${line.id}">
      <span class="shop-cart__media shop-image-placeholder" aria-hidden="true"></span>
      <div class="shop-cart__info">
        <p class="shop-cart__name">Artikel nicht mehr im Sortiment</p>
        <p class="shop-badge">${UNAVAILABLE}</p>
      </div>
      <button type="button" class="shop-cart__remove" data-action="remove">${icon("remove")}<span>Entfernen</span></button>
    </li>`;
  }
  const href = api.href(`artikel/${article.id}`);
  return `<li class="shop-cart__line" data-line="${line.id}">
    <a class="shop-cart__media" href="${escapeHtml(href)}" tabindex="-1" aria-hidden="true">${imageHtml(api, article, "shop-cart__image")}</a>
    <div class="shop-cart__info">
      <a class="shop-cart__name" href="${escapeHtml(href)}">${escapeHtml(article.name)}</a>
      <p class="shop-cart__unit">je ${escapeHtml(formatEuro(article.grossPrice))} · inkl. ${escapeHtml(rateLabel(article.taxRatePercent))} USt</p>
    </div>
    <div class="shop-stepper" role="group" aria-label="Menge ${escapeHtml(article.name)}">
      <button type="button" data-action="minus" aria-label="Eins weniger" ${line.quantity <= 1 ? "disabled" : ""}>${icon("minus", 16)}</button>
      <input type="number" inputmode="numeric" min="1" max="${MAX_QUANTITY}" value="${line.quantity}" data-quantity aria-label="Menge" />
      <button type="button" data-action="plus" aria-label="Eins mehr" ${line.quantity >= MAX_QUANTITY ? "disabled" : ""}>${icon("plus", 16)}</button>
    </div>
    <p class="shop-cart__total">${escapeHtml(euro(line.lineCents))}</p>
    <button type="button" class="shop-cart__remove" data-action="remove" aria-label="${escapeHtml(article.name)} entfernen">${icon("remove")}<span>Entfernen</span></button>
  </li>`;
}

// The list is redrawn on every change; focus returns to the same control so keyboard users
// can keep pressing "+" without hunting for it again.
function focusAfterRedraw(section, id, action) {
  const line = section.querySelector(`[data-line="${id}"]`);
  const target = action === "quantity"
    ? line?.querySelector("[data-quantity]")
    : line?.querySelector(`[data-action="${action}"]:not(:disabled)`) ?? line?.querySelector("[data-quantity]");
  (target ?? section.querySelector(".shop-cart__line [data-action], .shop-button"))?.focus();
}

function announce(section, text) {
  let region = section.querySelector("[data-announce]");
  if (!region) {
    region = document.createElement("p");
    region.className = "shop-visually-hidden";
    region.setAttribute("role", "status");
    region.dataset.announce = "";
    section.append(region);
  }
  region.textContent = text;
}

// ---- Checkout ---------------------------------------------------------------------------

export const FIELDS = [
  { name: "firstName", label: "Vorname", optional: true, autocomplete: "given-name", max: 120 },
  { name: "lastName", label: "Nachname", autocomplete: "family-name", max: 120, missing: "Bitte einen Nachnamen angeben." },
  { name: "street", label: "Straße", autocomplete: "address-line1", max: 120, missing: "Bitte eine Straße angeben.", wide: true },
  { name: "houseNumber", label: "Hausnummer", autocomplete: "off", max: 120, missing: "Bitte eine Hausnummer angeben.", narrow: true },
  { name: "postalCode", label: "PLZ", autocomplete: "postal-code", max: 120, inputmode: "numeric", missing: "Bitte eine Postleitzahl angeben.", narrow: true },
  { name: "city", label: "Ort", autocomplete: "address-level2", max: 120, missing: "Bitte einen Ort angeben.", wide: true },
  { name: "country", label: "Land", autocomplete: "country-name", max: 120, value: "Österreich", missing: "Bitte ein Land angeben." },
  { name: "email", label: "E-Mail", type: "email", autocomplete: "email", inputmode: "email", max: 254, missing: "Bitte eine E-Mail-Adresse angeben." },
  { name: "phone", label: "Telefon", type: "tel", optional: true, autocomplete: "tel", inputmode: "tel", max: 40 },
];

function newKey() {
  if (crypto.randomUUID) return crypto.randomUUID();
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = [...bytes].map((byte) => byte.toString(16).padStart(2, "0")).join("");
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

// prefix keeps ids unique when the account page renders the same fields.
export function fieldHtml(field, prefix = "co") {
  const id = `${prefix}-${field.name}`;
  return `<div class="shop-field${field.wide ? " shop-field--wide" : ""}${field.narrow ? " shop-field--narrow" : ""}" data-field="${field.name}">
    <label for="${id}">${escapeHtml(field.label)}${field.optional && !field.quiet ? ' <span class="shop-field__optional">optional</span>' : ""}</label>
    <input id="${id}" name="${field.name}" type="${field.type ?? "text"}" autocomplete="${field.autocomplete}"
      maxlength="${field.max}" ${field.inputmode ? `inputmode="${field.inputmode}"` : ""}
      ${field.optional ? "" : 'required aria-required="true"'} aria-describedby="${id}-error"
      value="${escapeHtml(field.value ?? "")}" />
    <p class="shop-field__error" id="${id}-error" hidden></p>
  </div>`;
}

export async function renderCheckoutPage(main, { api, shop, navigation }) {
  document.title = `Kasse – ${shop.displayName}`;
  const section = document.createElement("section");
  section.className = "shop-checkout shop-inner";
  main.replaceChildren(section);
  section.innerHTML = `${pageHead("Kasse")}<p class="shop-muted">Wird geladen …</p>`;

  let articles;
  try {
    articles = await api.articles();
  } catch {
    section.innerHTML = `${pageHead("Kasse")}<p class="shop-muted">Die Kasse konnte nicht geladen werden. Bitte laden Sie die Seite neu.</p>`;
    return;
  }
  const summary = summarize(api.kennung, articles);
  if (!summary.available.length || summary.hasUnavailable) {
    section.innerHTML = `${pageHead("Kasse")}
      <div class="shop-empty">
        <span class="shop-empty__icon">${icon("bag", 26)}</span>
        <p class="shop-empty__title">${summary.lines.length ? "Bitte prüfen Sie Ihren Warenkorb." : "Ihr Warenkorb ist leer."}</p>
        <p class="shop-muted">${summary.lines.length ? "Einige Artikel sind nicht mehr verfügbar." : "Legen Sie zuerst Artikel in den Warenkorb."}</p>
        <a class="shop-button" href="${escapeHtml(api.href(summary.lines.length ? "warenkorb" : "artikel"))}">${summary.lines.length ? "Zum Warenkorb" : "Artikel ansehen"}</a>
      </div>`;
    return;
  }

  // Logged-in customers (shop-customer-accounts spec): the form starts from the profile and
  // the order is linked to the account. A profile that cannot load leaves the form empty.
  const customer = shop.accountsEnabled ? currentCustomer() : null;
  let profile = null;
  if (customer) {
    profile = await accountRequest("/profile").catch(() => null);
  }
  const account = customer && profile ? customer : null;
  const fields = FIELDS.map((field) => {
    const stored = profile?.[field.name] || (field.name === "email" ? profile?.loginEmail : null);
    return stored ? { ...field, value: stored } : field;
  });
  const lead = account
    ? `Angemeldet als <strong>${escapeHtml(account.email)}</strong>. Ihre Daten aus dem Kundenkonto sind eingetragen.`
    : `Bestellung als Gast – ohne Kundenkonto.${shop.accountsEnabled
      ? ' <button type="button" class="shop-inline-link" data-login>Anmelden</button> und Ihre Daten werden eingetragen.'
      : ""}`;

  // One key per checkout attempt: a retry after a lost response cannot create a second order.
  const key = newKey();
  section.innerHTML = `${pageHead("Kasse", lead)}
    <form class="shop-checkout__layout" novalidate>
      <div class="shop-panel shop-checkout__form">
        <fieldset>
          <legend class="shop-checkout__legend">Lieferadresse</legend>
          <div class="shop-fields">${fields.slice(0, 7).map((field) => fieldHtml(field)).join("")}</div>
        </fieldset>
        <fieldset>
          <legend class="shop-checkout__legend">Kontakt</legend>
          <p class="shop-checkout__hint">Für Rückfragen zur Bestellung und die Bestellbestätigung.</p>
          <div class="shop-fields">${fields.slice(7).map((field) => fieldHtml(field)).join("")}</div>
        </fieldset>
        <div class="shop-inquiry__trap" aria-hidden="true">
          <label for="co-website">Website</label>
          <input id="co-website" type="text" name="website" tabindex="-1" autocomplete="off" />
        </div>
      </div>
      <aside class="shop-checkout__aside" aria-label="Ihre Bestellung">
        <div class="shop-panel">
          <h2 class="shop-checkout__legend">Ihre Bestellung</h2>
          <ul class="shop-mini">${summary.available.map((line) => `<li>
            <span class="shop-mini__qty">${line.quantity} ×</span>
            <span class="shop-mini__name">${escapeHtml(line.article.name)}</span>
            <span class="shop-mini__price">${escapeHtml(euro(line.lineCents))}</span></li>`).join("")}</ul>
          ${totalsHtml(summary)}
          <div class="shop-pay">
            <p class="shop-pay__title">Zahlung</p>
            <p class="shop-pay__text">${escapeHtml(PAYMENT_NOTE)}</p>
          </div>
          ${legalChecksHtml(api)}
          <p class="shop-note shop-note--error" role="alert" data-status hidden></p>
          <button type="submit" class="shop-button shop-button--block shop-button--submit">Zahlungspflichtig bestellen</button>
          <p class="shop-panel__foot">Ihre Bestellung geht an ${escapeHtml(shop.displayName)}${navigation?.sellerName && navigation.sellerName !== shop.displayName ? `, Verkauf durch ${escapeHtml(navigation.sellerName)}` : ""}.</p>
        </div>
        <a class="shop-link-back" href="${escapeHtml(api.href("warenkorb"))}">Zurück zum Warenkorb</a>
      </aside>
    </form>`;

  const form = section.querySelector("form");
  form.addEventListener("input", (event) => setError(form, event.target.name, null));
  form.addEventListener("focusout", (event) => {
    const field = FIELDS.find((entry) => entry.name === event.target.name);
    // Inline check once the visitor leaves a field they actually typed in.
    if (field && event.target.value.trim()) setError(form, field.name, problemOf(form, field));
  });
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    submit(form, section, { api, shop, key, summary, account });
  });
  section.querySelector("[data-login]")?.addEventListener("click", (event) => {
    event.currentTarget.disabled = true;
    startLogin(api.kennung, { returnTo: "kasse" });
  });
}

// Checkout legal elements (shop-legal spec), directly above the order button. The links open in
// a new tab so reading the AGB never loses the entered address.
const TERMS_MISSING = "Bitte die AGB akzeptieren.";

function legalChecksHtml(api) {
  const link = (segment, label) =>
    `<a href="${escapeHtml(api.href(segment))}" target="_blank" rel="noopener">${label}</a>`;
  return `<div class="shop-terms" data-field="terms">
      <div class="shop-check">
        <input id="co-terms" type="checkbox" name="terms" required aria-required="true" aria-describedby="co-terms-error" />
        <label for="co-terms">Ich akzeptiere die ${link("agb", "AGB")}.</label>
      </div>
      <p class="shop-field__error" id="co-terms-error" hidden></p>
    </div>
    <p class="shop-terms__note">Informationen zu Ihrem Widerrufsrecht finden Sie in der ${link("widerruf", "Widerrufsbelehrung")},
      zum Umgang mit Ihren Daten in der ${link("datenschutz", "Datenschutzerklärung")}.</p>`;
}

export function problemOf(form, field) {
  const input = form.elements[field.name];
  const value = input.value.trim();
  if (!value) return field.optional ? null : field.missing;
  if (field.name === "email" && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value)) return "Die E-Mail-Adresse ist ungültig.";
  if (field.name === "phone" && !/^[0-9+()/\-. ]{4,}$/.test(value)) return "Die Telefonnummer ist ungültig.";
  return null;
}

export function setError(form, name, text) {
  const wrapper = form.querySelector(`[data-field="${name}"]`);
  if (!wrapper) return;
  const error = wrapper.querySelector(".shop-field__error");
  error.textContent = text ?? "";
  error.hidden = !text;
  wrapper.querySelector("input").toggleAttribute("aria-invalid", Boolean(text));
}

async function submit(form, section, { api, shop, key, summary, account }) {
  const status = form.querySelector("[data-status]");
  status.hidden = true;
  const problems = FIELDS.map((field) => [field.name, problemOf(form, field)]);
  problems.push(["terms", form.elements.terms.checked ? null : TERMS_MISSING]);
  problems.forEach(([name, text]) => setError(form, name, text));
  const first = problems.find(([, text]) => text);
  if (first) {
    form.elements[first[0]].focus();
    return;
  }
  const button = form.querySelector('[type="submit"]');
  const fieldsets = form.querySelectorAll("fieldset");
  button.disabled = true;
  button.textContent = "Bestellung wird gesendet …";
  form.setAttribute("aria-busy", "true");
  fieldsets.forEach((fieldset) => { fieldset.disabled = true; });
  form.elements.terms.disabled = true;
  const value = (name) => form.elements[name].value.trim();
  try {
    const body = {
      idempotencyKey: key,
      website: form.elements.website.value,
      customer: Object.fromEntries(FIELDS.map((field) => [field.name, value(field.name)])),
      items: summary.available.map((line) => ({ articleId: line.id, quantity: line.quantity })),
    };
    const confirmation = account
      ? await accountRequest(`/shops/${encodeURIComponent(api.kennung)}/orders`, { method: "POST", body })
      : await api.placeOrder(body);
    clearCart(api.kennung);
    renderConfirmation(section, { api, shop, confirmation, account });
  } catch (error) {
    button.disabled = false;
    button.textContent = "Zahlungspflichtig bestellen";
    form.removeAttribute("aria-busy");
    fieldsets.forEach((fieldset) => { fieldset.disabled = false; });
    form.elements.terms.disabled = false;
    status.textContent = error.status === 401 && account
      ? "Ihre Anmeldung ist abgelaufen. Bitte laden Sie die Seite neu und melden Sie sich erneut an oder bestellen Sie als Gast."
      : error.status ? error.serverMessage ?? SEND_FAILED : SEND_FAILED;
    status.hidden = false;
    // The catalogue changed under the visitor: the cart page marks the affected lines.
    if (error.status === 409) {
      status.insertAdjacentHTML("beforeend", ` <a href="${escapeHtml(api.href("warenkorb"))}">Zum Warenkorb</a>`);
    }
  }
}

// ---- Confirmation and receipt ---------------------------------------------------------

function addressLines(person) {
  return [
    [person.firstName, person.lastName ?? person.name].filter(Boolean).join(" "),
    [person.street, person.houseNumber].filter(Boolean).join(" "),
    [person.postalCode, person.city].filter(Boolean).join(" "),
    person.country,
  ].filter(Boolean).map(escapeHtml).join("<br />");
}

function mailNotice(confirmation) {
  if (confirmation.customerEmailNotice === "QUEUED") {
    return `Eine Bestellbestätigung geht an <strong>${escapeHtml(confirmation.customer.email)}</strong>.`;
  }
  return "Eine Bestätigung per E-Mail wird derzeit noch nicht versendet. Bitte drucken oder speichern Sie diesen Beleg.";
}

export function renderConfirmation(section, { api, shop, confirmation, account = null }) {
  document.title = `Bestellung ${confirmation.orderNumber} – ${shop.displayName}`;
  const { contact, seller } = confirmation;
  section.innerHTML = `
    <div class="shop-done" tabindex="-1">
      <span class="shop-done__icon">${icon("check", 26)}</span>
      <h1 class="shop-heading shop-checkout__title">Danke für Ihre Bestellung.</h1>
      <p class="shop-done__number">Bestellnummer <strong>${escapeHtml(confirmation.orderNumber)}</strong></p>
      <p class="shop-muted shop-done__text">${mailNotice(confirmation)}</p>
      <div class="shop-done__actions">
        <button type="button" class="shop-button shop-button--plain" data-print>${icon("print")}Beleg drucken</button>
        ${account ? `<a class="shop-link-back" href="${escapeHtml(api.href("konto"))}">Zu meinen Bestellungen</a>` : ""}
        <a class="shop-link-back" href="${escapeHtml(api.href("artikel"))}">Weiter einkaufen</a>
      </div>
    </div>
    <article class="shop-receipt shop-panel" aria-labelledby="receipt-title">
      <header class="shop-receipt__head">
        <div>
          <h2 class="shop-receipt__title" id="receipt-title">Bestellbeleg</h2>
          <p class="shop-receipt__shop">${escapeHtml(shop.displayName)}</p>
        </div>
        <dl class="shop-receipt__meta">
          <div><dt>Bestellnummer</dt><dd>${escapeHtml(confirmation.orderNumber)}</dd></div>
          <div><dt>Datum</dt><dd>${escapeHtml(formatDate(confirmation.orderDate))}</dd></div>
        </dl>
      </header>
      <table class="shop-receipt__items">
        <caption class="shop-visually-hidden">Bestellte Artikel</caption>
        <thead><tr><th scope="col">Artikel</th><th scope="col">Menge</th><th scope="col">Einzelpreis</th><th scope="col">USt</th><th scope="col">Summe</th></tr></thead>
        <tbody>${confirmation.items.map((item) => `<tr>
          <th scope="row">${escapeHtml(item.name)}</th>
          <td data-label="Menge">${escapeHtml(item.quantity)}</td>
          <td data-label="Einzelpreis">${escapeHtml(formatEuro(item.unitGross))}</td>
          <td data-label="USt">${escapeHtml(rateLabel(Number(item.taxRatePercent)))}</td>
          <td data-label="Summe">${escapeHtml(formatEuro(item.lineGross))}</td></tr>`).join("")}</tbody>
      </table>
      <dl class="shop-sum shop-receipt__sum">
        <div class="shop-sum__row shop-sum__row--minor"><dt>Netto</dt><dd>${escapeHtml(formatEuro(confirmation.netTotal))}</dd></div>
        ${confirmation.taxes.map((entry) => `<div class="shop-sum__row shop-sum__row--minor"><dt>USt ${escapeHtml(rateLabel(Number(entry.ratePercent)))} auf ${escapeHtml(formatEuro(entry.net))}</dt><dd>${escapeHtml(formatEuro(entry.tax))}</dd></div>`).join("")}
        <div class="shop-sum__row shop-sum__row--total"><dt>Gesamt <span>inkl. USt</span></dt><dd>${escapeHtml(formatEuro(confirmation.grossTotal))}</dd></div>
      </dl>
      <div class="shop-receipt__parties">
        <section><h3>Lieferadresse</h3><p>${addressLines(confirmation.customer)}</p>
          <p>${escapeHtml(confirmation.customer.email)}${confirmation.customer.phone ? `<br />${escapeHtml(confirmation.customer.phone)}` : ""}</p></section>
        <section><h3>Ihr Ansprechpartner</h3><p>${addressLines(contact)}</p>
          ${contact.email || contact.phone ? `<p>${[contact.email, contact.phone].filter(Boolean).map(escapeHtml).join("<br />")}</p>` : ""}</section>
        ${seller?.name ? `<section><h3>Verkäufer</h3><p>${escapeHtml(seller.name)}${seller.taxIdentifier ? `<br />UID ${escapeHtml(seller.taxIdentifier)}` : ""}</p></section>` : ""}
      </div>
      <p class="shop-receipt__pay"><strong>Zahlung:</strong> ${escapeHtml(PAYMENT_NOTE)}</p>
    </article>`;
  section.querySelector("[data-print]").addEventListener("click", () => window.print());
  window.scrollTo({ top: 0 });
  section.querySelector(".shop-done").focus({ preventScroll: true });
}
