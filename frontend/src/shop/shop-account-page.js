// "Mein Konto" (/shop/<kennung>/konto, shop-customer-accounts spec): sign-in entry for visitors,
// and for logged-in customers their orders in this shop, the delivery data that prefills the
// checkout, the data download and account deletion. Keycloak returns here after every login.
import { escapeHtml } from "../lib/html.js";
import { formatDate, formatEuro } from "../lib/format.js";
import { FIELDS, fieldHtml, icon, problemOf, setError } from "./shop-checkout.js";
import { accountRequest, completeLogin, currentCustomer, endSession, logout, startLogin } from "./shop-account.js";

// Same words as the partner's order list, so customer and partner talk about the same state.
export const STATUS_LABELS = { SUBMITTED: "Eingereicht", REVIEWED: "Geprüft", FULFILLED: "Abgeschlossen" };
const STATUS_HINTS = {
  SUBMITTED: "Ihre Bestellung ist eingegangen.",
  REVIEWED: "Ihre Bestellung wurde geprüft und wird vorbereitet.",
  FULFILLED: "Ihre Bestellung ist abgeschlossen.",
};

// In the profile every field is voluntary; the checkout still asks for what an order needs.
const PROFILE_FIELDS = FIELDS.map((field) => ({ ...field, optional: true, quiet: true, value: "" }));

const LOAD_FAILED = "Ihr Konto konnte nicht geladen werden. Bitte laden Sie die Seite neu.";

function pageHead(title, lead, actions = "") {
  return `<header class="shop-checkout__head shop-account__head">
    <div class="shop-account__title">
      <h1 class="shop-heading shop-checkout__title">${escapeHtml(title)}</h1>
      ${lead ? `<p class="shop-muted">${lead}</p>` : ""}
    </div>
    ${actions}
  </header>`;
}

export async function renderAccountPage(main, { api, shop }) {
  document.title = `Mein Konto – ${shop.displayName}`;
  const section = document.createElement("section");
  section.className = "shop-checkout shop-account shop-inner";
  main.replaceChildren(section);

  const returned = await completeLogin(api.kennung);
  if (returned && !returned.error && returned.returnTo && returned.returnTo !== "konto") {
    location.replace(api.href(returned.returnTo));
    return;
  }

  const customer = currentCustomer();
  if (!customer) {
    renderSignedOut(section, { api, problem: returned?.error });
    return;
  }

  section.innerHTML = `${pageHead("Mein Konto", `Angemeldet als <strong>${escapeHtml(customer.email)}</strong>`)}
    <p class="shop-muted">Wird geladen …</p>`;
  let profile, orders;
  try {
    [profile, orders] = await Promise.all([
      accountRequest("/profile"),
      accountRequest(`/shops/${encodeURIComponent(api.kennung)}/orders`),
    ]);
  } catch (error) {
    if (error.status === 401) {
      renderSignedOut(section, { api, problem: "expired" });
    } else {
      section.querySelector(".shop-muted:last-child").textContent = LOAD_FAILED;
    }
    return;
  }
  renderSignedIn(section, { api, shop, customer, profile, orders });
}

// ---- Signed out -----------------------------------------------------------------------------

const PROBLEMS = {
  cancelled: "Die Anmeldung wurde abgebrochen.",
  failed: "Die Anmeldung hat nicht geklappt. Bitte versuchen Sie es erneut.",
  expired: "Ihre Anmeldung ist abgelaufen. Bitte melden Sie sich erneut an.",
};

function renderSignedOut(section, { api, problem }) {
  section.innerHTML = `${pageHead("Kundenkonto")}
    <div class="shop-account__welcome shop-panel">
      <span class="shop-empty__icon">${icon("user", 26)}</span>
      <h2 class="shop-account__welcome-title">Schneller bestellen mit Ihrem Konto</h2>
      <ul class="shop-account__benefits">
        <li>Ihre Lieferadresse wird an der Kasse automatisch eingetragen.</li>
        <li>Sie sehen Ihre Bestellungen in diesem Shop und deren Stand.</li>
        <li>Sie können Ihre Daten jederzeit herunterladen oder Ihr Konto löschen.</li>
      </ul>
      ${problem ? `<p class="shop-note shop-note--warn" role="status">${escapeHtml(PROBLEMS[problem] ?? PROBLEMS.failed)}</p>` : ""}
      <div class="shop-account__entry">
        <button type="button" class="shop-button shop-button--plain shop-button--block" data-login>Anmelden</button>
        <button type="button" class="shop-account__secondary" data-register>Neues Konto erstellen</button>
      </div>
      <p class="shop-panel__foot">Anmeldung und Registrierung laufen über das PAPO-Kundenkonto. Nach der Registrierung
        bestätigen Sie Ihre E-Mail-Adresse über den Link, den wir Ihnen senden. Bestellen geht auch ohne Konto.</p>
    </div>`;
  section.querySelector("[data-login]").addEventListener("click", (event) => {
    busy(event.currentTarget, "Weiter zur Anmeldung …");
    startLogin(api.kennung, { returnTo: "konto" });
  });
  section.querySelector("[data-register]").addEventListener("click", (event) => {
    busy(event.currentTarget, "Weiter zur Registrierung …");
    startLogin(api.kennung, { returnTo: "konto", action: "register" });
  });
}

function busy(button, text) {
  button.disabled = true;
  button.textContent = text;
}

// ---- Signed in ------------------------------------------------------------------------------

function renderSignedIn(section, { api, shop, customer, profile, orders }) {
  const actions = `<button type="button" class="shop-account__logout" data-logout>${icon("logout")}<span>Abmelden</span></button>`;
  const greeting = profile.firstName || customer.firstName;
  section.innerHTML = `${pageHead(greeting ? `Hallo, ${greeting}` : "Mein Konto",
      `Angemeldet als <strong>${escapeHtml(profile.loginEmail ?? customer.email)}</strong>`, actions)}
    <div class="shop-checkout__layout shop-account__layout">
      <div class="shop-account__main">
        <section class="shop-account__orders" aria-labelledby="account-orders">
          <h2 class="shop-checkout__legend" id="account-orders">Ihre Bestellungen in ${escapeHtml(shop.displayName)}</h2>
          ${ordersHtml(api, orders)}
        </section>
        ${profileFormHtml(profile)}
      </div>
      <aside class="shop-checkout__aside shop-account__aside" aria-label="Kontoeinstellungen">
        <div class="shop-panel">
          <h2 class="shop-checkout__legend">Anmeldung</h2>
          <dl class="shop-account__login">
            <dt>E-Mail-Adresse</dt><dd>${escapeHtml(profile.loginEmail ?? customer.email)}</dd>
          </dl>
          <div class="shop-account__tools">
            <button type="button" class="shop-account__tool" data-password>${icon("key")}<span>Passwort ändern</span></button>
            <button type="button" class="shop-account__tool" data-export>${icon("download")}<span>Meine Daten herunterladen</span></button>
          </div>
          <p class="shop-note" role="status" data-tools-status hidden></p>
        </div>
        ${deleteHtml()}
      </aside>
    </div>`;

  section.querySelector("[data-logout]").addEventListener("click", (event) => {
    busy(event.currentTarget, "Abmelden …");
    logout(api.kennung);
  });
  section.querySelector("[data-password]").addEventListener("click", () => {
    startLogin(api.kennung, { returnTo: "konto", action: "password" });
  });
  section.querySelector("[data-export]").addEventListener("click", (event) => exportData(section, event.currentTarget));
  bindProfileForm(section.querySelector("[data-profile]"));
  bindDelete(section, api);
}

function ordersHtml(api, orders) {
  if (!orders.length) {
    return `<div class="shop-account__none">
      <span class="shop-account__none-icon">${icon("receipt", 22)}</span>
      <div>
        <p class="shop-account__none-title">Noch keine Bestellungen</p>
        <p class="shop-muted">Bestellungen, die Sie angemeldet aufgeben, erscheinen hier mit ihrem Stand.</p>
      </div>
      <a class="shop-button" href="${escapeHtml(api.href("artikel"))}">Artikel ansehen</a>
    </div>`;
  }
  return `<ol class="shop-orders">${orders.map(orderHtml).join("")}</ol>`;
}

function orderHtml(order) {
  const count = order.items.reduce((sum, item) => sum + item.quantity, 0);
  const status = STATUS_LABELS[order.status] ?? order.status;
  const delivery = order.delivery ?? {};
  return `<li><details class="shop-order">
    <summary class="shop-order__summary">
      <span class="shop-order__id">
        <span class="shop-order__number">Bestellung ${escapeHtml(order.orderNumber)}</span>
        <span class="shop-order__meta">${escapeHtml(formatDate(order.orderDate))} · ${count} Artikel</span>
      </span>
      <span class="shop-status shop-status--${escapeHtml(order.status.toLowerCase())}">${escapeHtml(status)}</span>
      <span class="shop-order__total">${escapeHtml(formatEuro(order.grossTotal))}</span>
      <span class="shop-order__chevron">${icon("chevron")}</span>
    </summary>
    <div class="shop-order__body">
      <p class="shop-order__hint">${escapeHtml(STATUS_HINTS[order.status] ?? "")}</p>
      <ul class="shop-mini">${order.items.map((item) => `<li>
        <span class="shop-mini__qty">${escapeHtml(item.quantity)} ×</span>
        <span class="shop-mini__name">${escapeHtml(item.name)}</span>
        <span class="shop-mini__price">${escapeHtml(formatEuro(item.lineGross))}</span></li>`).join("")}</ul>
      <div class="shop-order__foot">
        <p><span class="shop-order__label">Lieferadresse</span>${[
          [delivery.firstName, delivery.lastName].filter(Boolean).join(" "),
          [delivery.street, delivery.houseNumber].filter(Boolean).join(" "),
          [delivery.postalCode, delivery.city].filter(Boolean).join(" "),
        ].filter(Boolean).map(escapeHtml).join("<br />")}</p>
        <p class="shop-order__sum"><span class="shop-order__label">Gesamt inkl. USt</span>${escapeHtml(formatEuro(order.grossTotal))}</p>
      </div>
    </div>
  </details></li>`;
}

// ---- Delivery data --------------------------------------------------------------------------

function profileFormHtml(profile) {
  const fields = PROFILE_FIELDS.map((field) => ({ ...field, value: profile[field.name] ?? (field.name === "country" ? "Österreich" : "") }));
  return `<form class="shop-panel shop-checkout__form shop-account__profile" data-profile novalidate>
    <fieldset>
      <legend class="shop-checkout__legend">Lieferdaten</legend>
      <p class="shop-checkout__hint">Damit füllen wir die Kasse für Sie aus. Alle Angaben sind freiwillig und
        lassen sich bei jeder Bestellung noch ändern.</p>
      <div class="shop-fields">${fields.map((field) => fieldHtml(field, "pf")).join("")}</div>
    </fieldset>
    <div class="shop-account__save">
      <p class="shop-note" role="status" data-profile-status></p>
      <button type="submit" class="shop-button shop-button--plain shop-button--submit">Speichern</button>
    </div>
  </form>`;
}

function bindProfileForm(form) {
  const status = form.querySelector("[data-profile-status]");
  const button = form.querySelector('[type="submit"]');
  form.addEventListener("input", (event) => {
    setError(form, event.target.name, null);
    status.textContent = "";
    status.className = "shop-note";
  });
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const problems = PROFILE_FIELDS.map((field) => [field.name, problemOf(form, field)]);
    problems.forEach(([name, text]) => setError(form, name, text));
    const first = problems.find(([, text]) => text);
    if (first) {
      form.elements[first[0]].focus();
      return;
    }
    button.disabled = true;
    button.textContent = "Wird gespeichert …";
    const body = Object.fromEntries(PROFILE_FIELDS.map((field) => [field.name, form.elements[field.name].value.trim()]));
    try {
      await accountRequest("/profile", { method: "PUT", body });
      status.className = "shop-note shop-note--saved";
      status.innerHTML = `${icon("check", 16)}Gespeichert`;
    } catch (error) {
      status.className = "shop-note shop-note--error";
      status.textContent = error.serverMessage ?? "Die Angaben konnten nicht gespeichert werden. Bitte versuchen Sie es erneut.";
    } finally {
      button.disabled = false;
      button.textContent = "Speichern";
    }
  });
}

// ---- Export ---------------------------------------------------------------------------------

async function exportData(section, button) {
  const status = section.querySelector("[data-tools-status]");
  const label = button.querySelector("span");
  button.disabled = true;
  label.textContent = "Wird vorbereitet …";
  try {
    const response = await accountRequest("/export", { raw: true });
    const blob = await response.blob();
    const name = /filename="([^"]+)"/.exec(response.headers.get("Content-Disposition") ?? "")?.[1]
      ?? `kundenkonto-${new Date().toISOString().slice(0, 10)}.json`;
    const url = URL.createObjectURL(blob);
    const link = Object.assign(document.createElement("a"), { href: url, download: name });
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    status.hidden = false;
    status.className = "shop-note";
    status.textContent = `Die Datei ${name} wurde heruntergeladen.`;
  } catch (error) {
    status.hidden = false;
    status.className = "shop-note shop-note--error";
    status.textContent = error.serverMessage ?? "Der Download hat nicht geklappt. Bitte versuchen Sie es erneut.";
  } finally {
    button.disabled = false;
    label.textContent = "Meine Daten herunterladen";
  }
}

// ---- Deletion -------------------------------------------------------------------------------

function deleteHtml() {
  return `<div class="shop-panel shop-account__delete" data-delete>
    <h2 class="shop-checkout__legend">Konto löschen</h2>
    <p class="shop-panel__foot">Ihre Anmeldung und Ihre gespeicherten Lieferdaten werden entfernt. Bereits aufgegebene
      Bestellungen bleiben beim Verkäufer erhalten, mit den Angaben zum Bestellzeitpunkt.</p>
    <button type="button" class="shop-account__danger-link" data-delete-open aria-expanded="false" aria-controls="account-delete-confirm">Konto löschen …</button>
    <div class="shop-account__confirm" id="account-delete-confirm" data-open="false" inert>
      <div class="shop-account__confirm-inner">
        <div class="shop-check">
          <input id="delete-sure" type="checkbox" />
          <label for="delete-sure">Ich möchte mein Kundenkonto endgültig löschen.</label>
        </div>
        <p class="shop-note shop-note--error" role="alert" data-delete-status hidden></p>
        <div class="shop-account__confirm-actions">
          <button type="button" class="shop-account__danger" data-delete-confirm disabled>Endgültig löschen</button>
          <button type="button" class="shop-account__secondary" data-delete-cancel>Abbrechen</button>
        </div>
      </div>
    </div>
  </div>`;
}

function bindDelete(section, api) {
  const panel = section.querySelector("[data-delete]");
  const opener = panel.querySelector("[data-delete-open]");
  const confirm = panel.querySelector(".shop-account__confirm");
  const sure = panel.querySelector("#delete-sure");
  const go = panel.querySelector("[data-delete-confirm]");
  const status = panel.querySelector("[data-delete-status]");
  const toggle = (open) => {
    confirm.dataset.open = String(open);
    confirm.inert = !open;
    opener.setAttribute("aria-expanded", String(open));
    opener.hidden = open;
    if (open) sure.focus({ preventScroll: true });
    else {
      sure.checked = false;
      go.disabled = true;
      status.hidden = true;
      opener.focus({ preventScroll: true });
    }
  };
  opener.addEventListener("click", () => toggle(true));
  panel.querySelector("[data-delete-cancel]").addEventListener("click", () => toggle(false));
  sure.addEventListener("change", () => { go.disabled = !sure.checked; });
  go.addEventListener("click", async () => {
    go.disabled = true;
    go.textContent = "Wird gelöscht …";
    status.hidden = true;
    try {
      await accountRequest("/account", { method: "DELETE" });
      endSession();
      renderDeleted(section, api);
    } catch (error) {
      go.disabled = false;
      go.textContent = "Endgültig löschen";
      status.textContent = error.serverMessage ?? "Das Konto konnte nicht gelöscht werden. Bitte versuchen Sie es erneut.";
      status.hidden = false;
    }
  });
}

function renderDeleted(section, api) {
  section.innerHTML = `<div class="shop-done" tabindex="-1">
      <span class="shop-done__icon">${icon("check", 26)}</span>
      <h1 class="shop-heading shop-checkout__title">Ihr Konto wurde gelöscht.</h1>
      <p class="shop-muted shop-done__text">Ihre Anmeldung und Ihre Lieferdaten sind entfernt. Sie können weiterhin
        als Gast bestellen.</p>
      <div class="shop-done__actions">
        <a class="shop-button" href="${escapeHtml(api.href())}">Zur Startseite</a>
      </div>
    </div>`;
  window.scrollTo({ top: 0 });
  section.querySelector(".shop-done").focus({ preventScroll: true });
}
