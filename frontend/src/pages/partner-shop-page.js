import { apiFetch } from "../lib/api.js";
import { renderShopStatus } from "../lib/shop-status.js";
import "../shop-editor/page-thumbnails.js";

// "Mein Webshop" (partner-shop spec): status, public link and the company's offline
// reason (identifier and release are company decisions), then miniatures of the shop's
// pages and the entry into the full-screen builder (shop-builder spec).
class PartnerShopPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Mein Webshop</h1>
      <p class="page-intro">Hier gestalten Sie Ihren öffentlichen Shop und sehen seinen Status. Kennung und Freigabe legt das Unternehmen fest.</p>
      <section class="shop-panel" aria-labelledby="shop-status-heading">
        <div class="shop-panel__head">
          <h2 id="shop-status-heading">Status</h2>
          <span id="shop-badge" aria-live="polite"></span>
        </div>
        <p id="shop-loading">Status wird geladen …</p>
        <p id="shop-hint" class="shop-panel__hint" hidden></p>
        <p id="shop-env-notice" class="shop-panel__env" hidden>Öffentliche Shops sind auf dieser Umgebung noch deaktiviert.</p>
        <div id="shop-reason" class="shop-panel__reason" hidden>
          <strong>Grund:</strong> <span id="shop-reason-text"></span>
        </div>
        <dl id="shop-details" class="profile-readonly shop-panel__details" hidden>
          <dt>Kennung</dt><dd id="shop-kennung"></dd>
          <dt>Öffentlicher Link</dt><dd id="shop-link"></dd>
        </dl>
        <p id="shop-error" class="trade-proof-card__error" role="alert" hidden></p>
        <button type="button" class="button-secondary" id="shop-retry" hidden>Erneut laden</button>
      </section>
      <shop-page-thumbnails></shop-page-thumbnails>
    `;
    this.querySelector("#shop-retry").addEventListener("click", () => this.load());
    this.load();
  }

  async load() {
    const loading = this.querySelector("#shop-loading");
    const error = this.querySelector("#shop-error");
    const retry = this.querySelector("#shop-retry");
    loading.hidden = false;
    loading.textContent = "Status wird geladen …";
    error.hidden = true;
    retry.hidden = true;
    try {
      this.render(await apiFetch("/partner/shop"));
      loading.hidden = true;
    } catch (loadError) {
      loading.textContent = "Status konnte nicht geladen werden.";
      error.textContent = loadError.message;
      error.hidden = false;
      retry.hidden = false;
    }
  }

  render(shop) {
    this.querySelector("#shop-kennung").textContent = shop.kennung;
    renderShopStatus(this, shop);
  }
}

customElements.define("partner-shop-page", PartnerShopPage);
