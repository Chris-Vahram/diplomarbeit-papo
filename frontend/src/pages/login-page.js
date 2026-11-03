import { login } from "../lib/auth.js";

// Dismissal of the bookmark hint; no personal data, named in /datenschutz.
const BOOKMARK_HINT_KEY = "papo.bookmarkHintDismissed";

function isBookmarkHintDismissed() {
  try {
    return localStorage.getItem(BOOKMARK_HINT_KEY) === "1";
  } catch {
    return false;
  }
}

function dismissBookmarkHint() {
  try {
    localStorage.setItem(BOOKMARK_HINT_KEY, "1");
  } catch {
    // Storage blocked: the hint simply shows again next time.
  }
}

class LoginPage extends HTMLElement {
  connectedCallback() {
    this.innerHTML = `
      <form class="login-form">
        <h1>Anmeldung</h1>
        <p class="login-form__intro">Ihre Zugangsdaten erhalten Sie von Ihrem Unternehmen. Eine Selbstregistrierung gibt es nicht.</p>
        <label>
          Benutzername
          <input name="username" type="text" required autocomplete="username" />
        </label>
        <label>
          Passwort
          <input name="password" type="password" required autocomplete="current-password" />
        </label>
        <p class="login-form__error" hidden></p>
        <button type="submit">Anmelden</button>
        <p class="login-form__bookmark" ${isBookmarkHintDismissed() ? "hidden" : ""}>
          <span>Tipp: Speichern Sie <strong>paportal.at/portal</strong> als Lesezeichen, dann kommen Sie direkt hierher.</span>
          <button type="button" class="login-form__bookmark-close" aria-label="Hinweis ausblenden">
            <i data-lucide="x" aria-hidden="true"></i>
          </button>
        </p>
        <a class="login-form__about" href="/">Was ist PAPO?</a>
      </form>
    `;

    const form = this.querySelector("form");
    const errorEl = this.querySelector(".login-form__error");
    const hint = this.querySelector(".login-form__bookmark");

    this.querySelector(".login-form__bookmark-close").addEventListener("click", () => {
      dismissBookmarkHint();
      hint.hidden = true;
      form.querySelector('[name="username"]').focus();
    });

    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      errorEl.hidden = true;

      const data = new FormData(form);
      try {
        const session = await login(data.get("username"), data.get("password"));
        location.hash = session.isCompany ? "#/company/dashboard" : "#/partner/dashboard";
      } catch (error) {
        errorEl.textContent = error.message;
        errorEl.hidden = false;
      }
    });

    window.lucide?.createIcons();
  }
}

customElements.define("login-page", LoginPage);
