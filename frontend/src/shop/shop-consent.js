// Consent for the public shop (shop-consent spec, design decisions 3 and 4).
// Categories: "notwendig" (always on: cart, this decision) and "statistik" (off by default).
// The decision lives only in this browser (localStorage "papo.consent.<version>") with version
// and date; there is no server-side record. Raising CONSENT_VERSION asks every visitor again.
// Optional features (add-shop-statistics) must ask hasConsent("statistik") before storing or
// sending anything and listen for CONSENT_EVENT to stop when consent is withdrawn.
import { escapeHtml } from "../lib/html.js";

export const CONSENT_VERSION = 1;
export const CONSENT_EVENT = "papo-consent-change";
// Storage owned by the "statistik" category; deleted whenever that consent is not given.
export const OPTIONAL_STORAGE_KEYS = ["papo.visitor"];

const KEY_PREFIX = "papo.consent.";
const KEY = `${KEY_PREFIX}${CONSENT_VERSION}`;

// Fallback when localStorage is blocked: the decision holds for this page view only.
let memoryDecision = null;

function storage() {
  try {
    return window.localStorage;
  } catch {
    return null;
  }
}

export function readConsent() {
  try {
    const raw = storage()?.getItem(KEY);
    const stored = raw ? JSON.parse(raw) : null;
    if (stored?.version === CONSENT_VERSION && typeof stored.categories === "object") return stored;
  } catch {
    // unreadable entry = no decision yet
  }
  return memoryDecision;
}

export function hasConsent(category) {
  if (category === "notwendig") return true;
  return readConsent()?.categories?.[category] === true;
}

function removeOptionalStorage() {
  const store = storage();
  for (const key of OPTIONAL_STORAGE_KEYS) {
    try {
      store?.removeItem(key);
    } catch {
      // nothing stored
    }
  }
}

export function saveConsent({ statistik }) {
  const decision = { version: CONSENT_VERSION, date: new Date().toISOString(), categories: { statistik: Boolean(statistik) } };
  memoryDecision = decision;
  const store = storage();
  try {
    store?.setItem(KEY, JSON.stringify(decision));
    // Decisions of older versions are void; keep only the current one.
    for (let index = store.length - 1; index >= 0; index -= 1) {
      const key = store.key(index);
      if (key?.startsWith(KEY_PREFIX) && key !== KEY) store.removeItem(key);
    }
  } catch {
    // blocked storage: memoryDecision covers this page view
  }
  if (!decision.categories.statistik) removeOptionalStorage();
  window.dispatchEvent(new CustomEvent(CONSENT_EVENT, { detail: decision }));
  return decision;
}

const CATEGORIES = [
  {
    id: "notwendig",
    title: "Notwendig",
    text: "Speichert Ihren Warenkorb und diese Auswahl in Ihrem Browser. Ohne diese Speicherung funktioniert der Shop nicht.",
    fixed: true,
  },
  {
    id: "statistik",
    title: "Statistik",
    text: "Erlaubt dem Shop, wiederkehrende Besuche über eine zufällige Kennung in Ihrem Browser zu zählen. Die Auswertung bleibt beim Shop und wird nicht an Dritte weitergegeben.",
  },
];

// <shop-consent>: bottom banner on the first visit plus the "Cookie-Einstellungen" dialog.
// The banner does not block the page; the dialog is a native modal <dialog>, which keeps focus
// inside and closes with Escape. Both offer "Nur notwendige" and "Alle akzeptieren" with the
// same size and weight; nothing is preselected.
class ShopConsent extends HTMLElement {
  #privacyHref = "";
  #observer = null;

  connectedCallback() {
    this.#privacyHref = this.getAttribute("privacy-href") ?? "";
    this.innerHTML = `
      <section class="shop-consent" role="region" aria-labelledby="consent-title" hidden data-banner>
        <div class="shop-consent__body">
          <h2 class="shop-consent__title" id="consent-title">Ihre Privatsphäre</h2>
          <p class="shop-consent__text">Dieser Shop speichert nur, was er braucht, etwa Ihren Warenkorb.
            Mit Ihrer Zustimmung zählt er zusätzlich wiederkehrende Besuche.
            ${this.#privacyLink()}</p>
        </div>
        <div class="shop-consent__actions">
          <button type="button" class="shop-consent__choice" data-choice="necessary">Nur notwendige</button>
          <button type="button" class="shop-consent__choice" data-choice="all">Alle akzeptieren</button>
          <button type="button" class="shop-consent__more" data-open-settings>Einstellungen</button>
        </div>
      </section>
      <dialog class="shop-consent-dialog" aria-labelledby="consent-dialog-title">
        <form method="dialog" class="shop-consent-dialog__panel">
          <header class="shop-consent-dialog__head">
            <h2 class="shop-consent__title" id="consent-dialog-title">Cookie-Einstellungen</h2>
            <button type="submit" class="shop-consent-dialog__close" value="cancel" aria-label="Schließen">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
          </header>
          <p class="shop-consent__text">Sie können Ihre Auswahl hier jederzeit ändern oder widerrufen. ${this.#privacyLink()}</p>
          <ul class="shop-consent-dialog__list">${CATEGORIES.map((category) => this.#categoryHtml(category)).join("")}</ul>
          <p class="shop-consent__date" data-date hidden></p>
          <div class="shop-consent__actions shop-consent__actions--dialog">
            <button type="button" class="shop-consent__choice" data-choice="necessary">Nur notwendige</button>
            <button type="button" class="shop-consent__choice" data-choice="selection">Auswahl speichern</button>
            <button type="button" class="shop-consent__choice" data-choice="all">Alle akzeptieren</button>
          </div>
        </form>
      </dialog>`;
    this.banner = this.querySelector("[data-banner]");
    this.dialog = this.querySelector("dialog");
    this.addEventListener("click", (event) => this.#onClick(event));
    if (!readConsent()) this.#showBanner();
  }

  #privacyLink() {
    return this.#privacyHref
      ? `<a href="${escapeHtml(this.#privacyHref)}">Mehr in der Datenschutzerklärung</a>`
      : "";
  }

  #categoryHtml(category) {
    const id = `consent-${category.id}`;
    return `<li class="shop-consent-dialog__item">
      <div class="shop-consent-dialog__label">
        <label for="${id}">${escapeHtml(category.title)}</label>
        <p id="${id}-text">${escapeHtml(category.text)}</p>
      </div>
      ${category.fixed
        ? `<span class="shop-consent-dialog__always">Immer aktiv</span>
           <input id="${id}" class="shop-switch" type="checkbox" role="switch" checked disabled aria-describedby="${id}-text" />`
        : `<input id="${id}" class="shop-switch" type="checkbox" role="switch" name="${category.id}" aria-describedby="${id}-text" />`}
    </li>`;
  }

  #onClick(event) {
    if (event.target.closest("[data-open-settings]")) {
      this.openSettings();
      return;
    }
    const choice = event.target.closest("[data-choice]")?.dataset.choice;
    if (!choice) return;
    const statistik = choice === "all" || (choice === "selection" && this.dialog.querySelector('[name="statistik"]').checked);
    saveConsent({ statistik });
    const fromDialog = this.dialog.open;
    if (fromDialog) this.dialog.close();
    this.#hideBanner({ restoreFocus: !fromDialog });
  }

  // Called by the footer link "Cookie-Einstellungen"; shows the current decision, never a preset.
  openSettings() {
    const decision = readConsent();
    this.dialog.querySelector('[name="statistik"]').checked = decision?.categories?.statistik === true;
    const date = this.dialog.querySelector("[data-date]");
    date.hidden = !decision;
    if (decision) {
      date.textContent = `Ihre Auswahl vom ${new Date(decision.date).toLocaleDateString("de-AT", { day: "numeric", month: "long", year: "numeric" })}.`;
    }
    this.dialog.showModal();
  }

  #showBanner() {
    this.banner.hidden = false;
    // Keeps the page's last content (e.g. the checkout button) reachable above the banner.
    this.#observer = new ResizeObserver(() => {
      document.documentElement.style.setProperty("--shop-consent-space", `${this.banner.offsetHeight + 16}px`);
    });
    this.#observer.observe(this.banner);
    requestAnimationFrame(() => this.banner.classList.add("shop-consent--in"));
  }

  #hideBanner({ restoreFocus }) {
    if (this.banner.hidden) return;
    const hadFocus = this.banner.contains(document.activeElement);
    this.#observer?.disconnect();
    document.documentElement.style.removeProperty("--shop-consent-space");
    this.banner.classList.remove("shop-consent--in");
    const done = () => { this.banner.hidden = true; };
    const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (reduced) done();
    else this.banner.addEventListener("transitionend", done, { once: true });
    setTimeout(done, 400); // in case no transition runs (hidden tab)
    // Focus would otherwise fall back to <body>; the main content is the natural next stop.
    if (restoreFocus && hadFocus) document.getElementById("shop-main")?.focus({ preventScroll: true });
  }
}

customElements.define("shop-consent", ShopConsent);
