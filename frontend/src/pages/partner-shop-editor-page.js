import { apiFetch } from "../lib/api.js";
import "../shop-editor/shop-builder.js";

// Full-screen shop builder (#/partner/shop/editor): the router hides the portal navigation;
// the builder brings its own "Zurück" to "Mein Webshop".
class PartnerShopEditorPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    const builder = document.createElement("shop-builder");
    this.append(builder);
    // Only for the public link in the small-screen hint.
    apiFetch("/partner/shop")
      .then((shop) => {
        builder.shop = shop;
      })
      .catch(() => {});
  }
}

customElements.define("partner-shop-editor-page", PartnerShopEditorPage);
