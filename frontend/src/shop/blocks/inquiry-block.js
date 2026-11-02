import { escapeHtml } from "../../lib/html.js";
import { ShopBlock } from "./shop-block.js";
import { textField } from "./text-field.js";

// "Anfrageformular" (shop-inquiries spec): fixed fields, only heading and intro come from the
// config. The shop is resolved by the server from the route, never from the form. Spam guard:
// honeypot "website" (hidden from people) and a signed render timestamp fetched on render.

const MESSAGE_MAX = 2000;
const CONTACT_MISSING = "Bitte E-Mail oder Telefonnummer angeben.";

let formCount = 0;

class InquiryFormBlock extends ShopBlock {
  #renderId = 0;
  #token = null;

  render(config) {
    const renderId = ++this.#renderId;
    const { api, article, shopName } = this.context;
    const id = `inq-${++formCount}`;
    const live = Boolean(api?.live);
    this.innerHTML = `<div class="shop-inner shop-inquiry">
      <div class="shop-inquiry__intro">
        ${textField(api, "h2", "shop-heading", "heading", config.heading)}
        ${textField(api, "p", "shop-text", "text", config.text)}
        <p class="shop-inquiry__recipient">Ihre Anfrage geht direkt an <strong>${escapeHtml(shopName ?? "uns")}</strong>.</p>
      </div>
      <div class="shop-inquiry__panel" data-panel>
        <form class="shop-inquiry__form" novalidate>
          <fieldset ${live ? "" : "disabled"}>
            <legend class="shop-visually-hidden">Anfrage</legend>
            ${live ? "" : '<p class="shop-inquiry__preview">Vorschau – hier wird nichts gesendet.</p>'}
            ${this.#field(id, "name", "Name", '<input type="text" name="name" autocomplete="name" maxlength="120" required aria-required="true" />')}
            <div class="shop-inquiry__pair" role="group" aria-labelledby="${id}-contact-hint">
              ${this.#field(id, "email", "E-Mail", '<input type="email" name="email" autocomplete="email" inputmode="email" maxlength="254" />')}
              ${this.#field(id, "phone", "Telefon", '<input type="tel" name="phone" autocomplete="tel" inputmode="tel" maxlength="40" />')}
              <p class="shop-inquiry__hint" id="${id}-contact-hint">E-Mail oder Telefon – eines genügt.</p>
              <p class="shop-inquiry__error" id="${id}-contact-error" data-error="contact" hidden></p>
            </div>
            ${this.#field(id, "articleId", "Artikel (optional)", `<select name="articleId"><option value="">Kein bestimmter Artikel</option>${
              article ? `<option value="${escapeHtml(article.id)}" selected>${escapeHtml(article.name)}</option>` : ""
            }</select>`)}
            ${this.#field(id, "message", "Nachricht", `<textarea name="message" rows="5" maxlength="${MESSAGE_MAX}" required aria-required="true"></textarea>`,
              `<span class="shop-inquiry__count" id="${id}-count" aria-live="polite" data-count>0 / ${MESSAGE_MAX}</span>`)}
            <div class="shop-inquiry__trap" aria-hidden="true">
              <label for="${id}-website">Website</label>
              <input id="${id}-website" type="text" name="website" tabindex="-1" autocomplete="off" />
            </div>
            <div class="shop-inquiry__consent">
              <input id="${id}-consent" type="checkbox" name="consent" required aria-required="true" aria-describedby="${id}-consent-error" />
              <label for="${id}-consent">Ich stimme zu, dass meine Angaben zur Bearbeitung der Anfrage gespeichert werden.
                <a href="${escapeHtml(api?.href?.("datenschutz") ?? "#")}">Datenschutzerklärung</a></label>
              <p class="shop-inquiry__error" id="${id}-consent-error" data-error="consent" hidden></p>
            </div>
            <p class="shop-inquiry__status" role="alert" data-status hidden></p>
            <button type="submit" class="shop-button shop-inquiry__submit">Anfrage senden</button>
          </fieldset>
        </form>
      </div>
    </div>`;
    this.#wire(renderId, live, article);
  }

  // control: the input's markup without id; extra: markup after it (the message counter).
  #field(id, name, label, control, extra = "") {
    const describedBy = `${id}-${name}-error`;
    const counter = extra ? ` ${id}-count` : "";
    const withIds = control.replace(/^<(\w+)/, `<$1 id="${id}-${name}" aria-describedby="${describedBy}${counter}"`);
    return `<div class="shop-inquiry__field" data-field-name="${name}">
      <label for="${id}-${name}">${escapeHtml(label)}</label>
      ${withIds}
      ${extra}
      <p class="shop-inquiry__error" id="${describedBy}" data-error="${name}" hidden></p>
    </div>`;
  }

  #wire(renderId, live, article) {
    const form = this.querySelector("form");
    const { api } = this.context;
    const message = form.elements.message;
    const count = this.querySelector("[data-count]");
    message.addEventListener("input", () => {
      count.textContent = `${message.value.length} / ${MESSAGE_MAX}`;
    });

    // Catalogue for the optional article reference; a failed load leaves "Kein bestimmter Artikel".
    api?.articles?.().then((articles) => {
      if (renderId !== this.#renderId) return;
      const select = form.elements.articleId;
      const chosen = select.value;
      select.innerHTML = `<option value="">Kein bestimmter Artikel</option>${(articles ?? [])
        .map((entry) => `<option value="${escapeHtml(entry.id)}">${escapeHtml(entry.name)}</option>`)
        .join("")}`;
      select.value = chosen || (article ? String(article.id) : "");
    }).catch(() => {});

    if (!live) {
      form.addEventListener("submit", (event) => event.preventDefault());
      return;
    }
    this.#loadToken(renderId);
    // Errors clear as soon as the field is corrected, never only on the next submit.
    form.addEventListener("input", (event) => {
      const name = event.target.name;
      if (name === "email" || name === "phone") this.#setError("contact", null);
      else if (name) this.#setError(name, null);
    });
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      this.#submit(form, renderId);
    });
  }

  #loadToken(renderId) {
    this.#token = null;
    this.context.api.inquiryToken()
      .then((token) => {
        if (renderId === this.#renderId) this.#token = token;
      })
      .catch(() => {});
  }

  #setError(name, text) {
    const error = this.querySelector(`[data-error="${name}"]`);
    if (!error) return;
    error.textContent = text ?? "";
    error.hidden = !text;
    const controls = name === "contact"
      ? [this.querySelector('[name="email"]'), this.querySelector('[name="phone"]')]
      : [this.querySelector(`[name="${name}"]`)];
    controls.forEach((control) => control?.toggleAttribute("aria-invalid", Boolean(text)));
  }

  #validate(form) {
    const value = (name) => form.elements[name].value.trim();
    const problems = [];
    if (!value("name")) problems.push(["name", "Bitte geben Sie Ihren Namen an."]);
    if (!value("email") && !value("phone")) problems.push(["contact", CONTACT_MISSING]);
    else if (value("email") && !form.elements.email.checkValidity()) problems.push(["email", "Die E-Mail-Adresse ist ungültig."]);
    if (!value("message")) problems.push(["message", "Bitte schreiben Sie eine Nachricht."]);
    if (!form.elements.consent.checked) problems.push(["consent", "Bitte stimmen Sie der Datenschutzerklärung zu."]);
    return problems;
  }

  async #submit(form, renderId) {
    const status = this.querySelector("[data-status]");
    status.hidden = true;
    ["name", "contact", "email", "phone", "message", "consent"].forEach((name) => this.#setError(name, null));
    const problems = this.#validate(form);
    if (problems.length) {
      problems.forEach(([name, text]) => this.#setError(name, text));
      const first = problems[0][0] === "contact" ? "email" : problems[0][0];
      form.elements[first].focus();
      return;
    }
    const button = form.querySelector('[type="submit"]');
    const fieldset = form.querySelector("fieldset");
    button.textContent = "Wird gesendet …";
    fieldset.disabled = true;
    form.setAttribute("aria-busy", "true");
    const articleId = form.elements.articleId.value;
    try {
      await this.context.api.sendInquiry({
        name: form.elements.name.value,
        email: form.elements.email.value,
        phone: form.elements.phone.value,
        message: form.elements.message.value,
        articleId: articleId ? Number(articleId) : null,
        consent: form.elements.consent.checked,
        website: form.elements.website.value,
        token: this.#token,
      });
      if (renderId !== this.#renderId) return;
      this.#showSent();
    } catch (error) {
      if (renderId !== this.#renderId) return;
      fieldset.disabled = false;
      form.removeAttribute("aria-busy");
      button.textContent = "Anfrage senden";
      status.textContent = error.serverMessage
        ?? "Die Anfrage konnte nicht gesendet werden. Bitte versuchen Sie es später erneut.";
      status.hidden = false;
      // A spent or expired timestamp is replaced, so the next try can pass.
      this.#loadToken(renderId);
    }
  }

  #showSent() {
    const panel = this.querySelector("[data-panel]");
    const shopName = this.context.shopName;
    panel.innerHTML = `<div class="shop-inquiry__sent" tabindex="-1">
      <span class="shop-inquiry__sent-icon" aria-hidden="true"><svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg></span>
      <h3 class="shop-inquiry__sent-title">Danke, Ihre Anfrage ist angekommen.</h3>
      <p class="shop-muted">Sie wurde an ${escapeHtml(shopName ?? "den Shop")} übermittelt.</p>
      <button type="button" class="shop-inquiry__again" data-again>Weitere Anfrage senden</button>
    </div>`;
    const sent = panel.firstElementChild;
    sent.focus();
    panel.querySelector("[data-again]").addEventListener("click", () => this.render(this.config));
  }
}

customElements.define("shop-block-inquiry-form", InquiryFormBlock);
