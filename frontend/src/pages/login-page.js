import { login } from "../lib/auth.js";

class LoginPage extends HTMLElement {
  connectedCallback() {
    this.innerHTML = `
      <form class="login-form">
        <h1>Anmeldung</h1>
        <label>
          Benutzername
          <input name="username" type="text" required autocomplete="username" />
        </label>
        <label>
          Passwort
          <input name="password" type="password" required autocomplete="current-password" />
        </label>
        <p class="login-form__hint">Demo-Logins: <code>company-demo</code> / <code>partner-demo</code> (Passwort = Benutzername)</p>
        <p class="login-form__error" hidden></p>
        <button type="submit">Anmelden</button>
      </form>
    `;

    const form = this.querySelector("form");
    const errorEl = this.querySelector(".login-form__error");

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
  }
}

customElements.define("login-page", LoginPage);
