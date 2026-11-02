import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { taxIdentifierFieldsHtml, readTaxIdentifier, fillTaxIdentifier } from "../lib/tax-identifier-fields.js";
import { tradeRegisterIdentifierFieldsHtml, readTradeRegisterIdentifier, fillTradeRegisterIdentifier } from "../lib/trade-register-fields.js";
import { escapeHtml } from "../lib/html.js";

// Shop legal texts (shop-legal spec): shown on the fixed system pages of every partner shop.
const LEGAL_TEXTS = [
  { key: "IMPRESSUM", name: "impressum", label: "Impressum", hint: "Firmenname, Anschrift, Kontakt, Firmenbuch- und UID-Nummer." },
  { key: "AGB", name: "agb", label: "AGB", hint: "Allgemeine Geschäftsbedingungen inkl. Zahlungs- und Lieferbedingungen." },
  { key: "DATENSCHUTZ", name: "datenschutz", label: "Datenschutzerklärung", hint: "Welche Daten der Shop verarbeitet, wozu und wie lange." },
  { key: "WIDERRUF", name: "widerruf", label: "Widerrufsbelehrung", hint: "Inklusive Muster-Widerrufsformular." },
];
const LEGAL_MAX = 30000;
const formatCount = (count) => count.toLocaleString("de-AT");

class CompanyProfilePage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Firmenprofil</h1>
      <p class="page-intro">Diese Angaben gelten für Ihr Unternehmen im Partnerportal.</p>
      <form id="company-profile-form" class="entity-form entity-form--grid">
        <form-field class="form-field--wide" label="Firmenname" name="businessName" required></form-field>
        ${taxIdentifierFieldsHtml()}
        ${tradeRegisterIdentifierFieldsHtml()}
        <form-field class="form-field--wide" label="E-Mail für Bestellhinweise" name="orderNoticeEmail" type="email"></form-field>
        <div class="entity-form__actions">
          <button type="submit">Speichern</button>
        </div>
      </form>
      <section class="legal-texts" aria-labelledby="legal-texts-heading">
        <h2 id="legal-texts-heading">Rechtstexte für Webshops</h2>
        <p class="page-intro">Diese Texte erscheinen in allen Webshops Ihrer Partner. Partner können sie nicht ändern.
          Fehlt ein Text, zeigt der Shop einen markierten Platzhalter.</p>
        <p class="legal-texts__summary" id="legal-texts-summary" aria-live="polite">Rechtstexte werden geladen …</p>
        <details class="legal-texts__format">
          <summary>Formatierung</summary>
          <ul>
            <li>Leerzeile = neuer Absatz, Zeilenumbruch bleibt erhalten</li>
            <li><code># Überschrift</code>, <code>## Unterüberschrift</code></li>
            <li><code>- Punkt</code> oder <code>1. Punkt</code> für Listen</li>
            <li><code>[Linktext](https://…)</code>; E-Mail-Adressen und Webadressen werden automatisch verlinkt</li>
            <li>HTML wird nicht ausgeführt, sondern als Text angezeigt.</li>
          </ul>
        </details>
        <form id="legal-texts-form" class="entity-form legal-texts__form" novalidate>
          ${LEGAL_TEXTS.map((text) => `
          <div class="form-field legal-texts__field" data-legal="${text.key}">
            <div class="legal-texts__label-row">
              <label class="form-field__label" for="legal-${text.name}">${escapeHtml(text.label)}</label>
              <span class="legal-texts__state" data-state></span>
            </div>
            <p class="legal-texts__hint" id="legal-${text.name}-hint">${escapeHtml(text.hint)}</p>
            <textarea id="legal-${text.name}" name="${text.name}" rows="8" maxlength="${LEGAL_MAX}"
              aria-describedby="legal-${text.name}-hint legal-${text.name}-count"></textarea>
            <span class="legal-texts__count" id="legal-${text.name}-count" data-count></span>
          </div>`).join("")}
          <div class="entity-form__actions">
            <button type="submit">Rechtstexte speichern</button>
          </div>
        </form>
      </section>
    `;
    this.form = this.querySelector("#company-profile-form");
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.legalForm = this.querySelector("#legal-texts-form");
    this.legalForm.addEventListener("submit", (event) => this.handleLegalSubmit(event));
    this.legalForm.addEventListener("input", (event) => {
      if (event.target.matches("textarea")) this.updateCount(event.target);
    });
    this.load();
    this.loadLegal();
  }

  async loadLegal() {
    try {
      this.fillLegal(await apiFetch("/company/profile/legal-texts"));
    } catch (error) {
      this.querySelector("#legal-texts-summary").textContent = "Die Rechtstexte konnten nicht geladen werden.";
      showToast(error.message, "error");
    }
  }

  fillLegal(texts) {
    for (const text of LEGAL_TEXTS) {
      const area = this.legalForm.elements[text.name];
      area.value = texts[text.name] ?? "";
      this.updateCount(area);
      const missing = texts.missing.includes(text.key);
      const state = this.legalForm.querySelector(`[data-legal="${text.key}"] [data-state]`);
      state.textContent = missing ? "Fehlt – Platzhalter" : "Hinterlegt";
      state.className = `legal-texts__state legal-texts__state--${missing ? "missing" : "ok"}`;
    }
    const missingLabels = LEGAL_TEXTS.filter((text) => texts.missing.includes(text.key)).map((text) => text.label);
    const summary = this.querySelector("#legal-texts-summary");
    summary.className = `legal-texts__summary legal-texts__summary--${missingLabels.length ? "missing" : "ok"}`;
    summary.textContent = missingLabels.length
      ? `Noch offen: ${missingLabels.join(", ")}. Ohne alle Texte lassen sich Shops im Echtbetrieb nicht freigeben.`
      : "Alle Rechtstexte sind hinterlegt.";
  }

  updateCount(area) {
    const counter = area.parentElement.querySelector("[data-count]");
    counter.textContent = `${formatCount(area.value.length)} / ${formatCount(LEGAL_MAX)} Zeichen`;
    counter.classList.toggle("legal-texts__count--near", area.value.length > LEGAL_MAX * 0.95);
  }

  async handleLegalSubmit(event) {
    event.preventDefault();
    if (this.legalSubmitting) return;
    this.legalSubmitting = true;
    const button = this.legalForm.querySelector('[type="submit"]');
    button.disabled = true;
    const payload = Object.fromEntries(LEGAL_TEXTS.map((text) => [text.name, this.legalForm.elements[text.name].value]));
    try {
      this.fillLegal(await apiFetch("/company/profile/legal-texts", { method: "PUT", body: JSON.stringify(payload) }));
      showToast("Rechtstexte gespeichert.", "success");
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      this.legalSubmitting = false;
      button.disabled = false;
    }
  }

  async load() {
    try {
      const profile = await apiFetch("/company/profile");
      this.form.elements.businessName.value = profile.businessName ?? "";
      fillTaxIdentifier(this.form, profile.taxIdentifier);
      fillTradeRegisterIdentifier(this.form, profile.tradeRegisterIdentifier);
      this.form.elements.orderNoticeEmail.value = profile.orderNoticeEmail ?? "";
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    if (this.submitting) return;
    this.submitting = true;
    const payload = {
      businessName: this.form.elements.businessName.value,
      taxIdentifier: readTaxIdentifier(this.form),
      tradeRegisterIdentifier: readTradeRegisterIdentifier(this.form),
      // Empty string clears the address; the backend keeps it only when the field is absent.
      orderNoticeEmail: this.form.elements.orderNoticeEmail.value.trim(),
    };
    try {
      await apiFetch("/company/profile", { method: "PUT", body: JSON.stringify(payload) });
      showToast("Firmenprofil aktualisiert.", "success");
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      this.submitting = false;
    }
  }
}

customElements.define("company-profile-page", CompanyProfilePage);
