import { apiFetch } from "../lib/api.js";
import { shopStatusLabel, shopPublicUrl } from "../lib/shop-status.js";

// "Mein Webshop" (partner-shop spec): status, public link and the company's offline
// reason. Read-only - identifier and release are company decisions.
class PartnerShopPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Mein Webshop</h1>
      <p class="page-intro">Hier sehen Sie den Status Ihres öffentlichen Shops. Kennung und Freigabe legt das Unternehmen fest.</p>
      <section class="trade-proof-card shop-status-card" aria-labelledby="shop-status-heading">
        <h2 id="shop-status-heading">Status</h2>
        <p id="shop-status" class="trade-proof-card__status" aria-live="polite">Status wird geladen …</p>
        <p id="shop-hint" hidden></p>
        <dl id="shop-details" class="profile-readonly" hidden>
          <dt>Kennung</dt><dd id="shop-kennung"></dd>
          <dt>Öffentlicher Link</dt><dd id="shop-link"></dd>
        </dl>
        <div id="shop-reason" class="shop-status-card__reason" hidden>
          <strong>Grund:</strong> <span id="shop-reason-text"></span>
        </div>
        <p id="shop-error" class="trade-proof-card__error" role="alert" hidden></p>
        <button type="button" id="shop-retry" hidden>Erneut laden</button>
      </section>
    `;
    this.querySelector("#shop-retry").addEventListener("click", () => this.load());
    this.load();
  }

  async load() {
    const status = this.querySelector("#shop-status");
    const error = this.querySelector("#shop-error");
    const retry = this.querySelector("#shop-retry");
    status.textContent = "Status wird geladen …";
    error.hidden = true;
    retry.hidden = true;
    try {
      this.render(await apiFetch("/partner/shop"));
    } catch (loadError) {
      status.textContent = "Status konnte nicht geladen werden.";
      error.textContent = loadError.message;
      error.hidden = false;
      retry.hidden = false;
    }
  }

  render(shop) {
    this.querySelector("#shop-status").textContent = shopStatusLabel(shop.status);
    this.querySelector("#shop-kennung").textContent = shop.kennung;

    const url = shopPublicUrl(shop);
    const linkCell = this.querySelector("#shop-link");
    linkCell.innerHTML = "";
    if (shop.status === "ONLINE") {
      const link = document.createElement("a");
      link.href = url;
      link.target = "_blank";
      link.rel = "noopener";
      link.textContent = url;
      linkCell.append(link);
    } else {
      linkCell.textContent = `${url} (derzeit nicht erreichbar)`;
    }
    this.querySelector("#shop-details").hidden = false;

    const hint = this.querySelector("#shop-hint");
    hint.hidden = shop.status !== "NOT_RELEASED";
    hint.textContent = "Ihr Shop wurde vom Unternehmen noch nicht freigegeben.";

    const reason = this.querySelector("#shop-reason");
    reason.hidden = shop.status !== "OFFLINE_COMPANY" || !shop.offlineReason;
    this.querySelector("#shop-reason-text").textContent = shop.offlineReason ?? "";
  }
}

customElements.define("partner-shop-page", PartnerShopPage);
