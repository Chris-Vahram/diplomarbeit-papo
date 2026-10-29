// Demo shop viewer (shop-templates spec "Demo shop per template"): full-screen layer above the
// setup dialog or template picker with shop.html?demo inside. The payload comes from the
// builder (demoPayload); viewing saves nothing. "Diese Vorlage wählen" runs the opener's choose
// callback and closes the layer. Escape closes only this layer, not the dialog underneath.

class ShopDemoViewer extends HTMLElement {
  #payload = null;
  #choose = null;
  #returnFocus = null;

  connectedCallback() {
    this.hidden = true;
    this.innerHTML = `
      <div class="demo-viewer" role="dialog" aria-modal="true" aria-labelledby="demo-title">
        <header class="demo-viewer__bar">
          <div class="demo-viewer__text">
            <h2 class="demo-viewer__title" id="demo-title"></h2>
            <p class="demo-viewer__hint">Beispiel mit Ihren Artikeln. Farben, Schriften, Texte und Bilder passen Sie danach an Ihre eigene Marke an.</p>
          </div>
          <div class="builder-device" role="group" aria-label="Vorschaugröße">
            <button type="button" data-device="desktop" aria-pressed="true">Computer</button>
            <button type="button" data-device="mobile" aria-pressed="false">Handy</button>
          </div>
          <div class="demo-viewer__actions">
            <button type="button" class="button-secondary" data-action="close">Schließen</button>
            <button type="button" class="button-primary" data-action="choose">Diese Vorlage wählen</button>
          </div>
        </header>
        <div class="demo-viewer__stage">
          <iframe class="demo-viewer__frame" data-device="desktop" title="Demo-Shop"></iframe>
        </div>
      </div>`;
    this.querySelector("[data-action=close]").addEventListener("click", () => this.close());
    this.querySelector("[data-action=choose]").addEventListener("click", () => {
      const choose = this.#choose;
      this.close();
      choose?.();
    });
    this.querySelector(".builder-device").addEventListener("click", (event) => {
      const button = event.target.closest("[data-device]");
      if (button) this.#setDevice(button.dataset.device);
    });
  }

  // title: e.g. „Handwerk“; payload: see demoPayload in shop-builder.js; choose: selects the
  // template in the opening dialog.
  open({ title, payload, choose }) {
    this.#payload = payload;
    this.#choose = choose;
    this.#returnFocus = document.activeElement;
    this.querySelector("#demo-title").textContent = `Demo-Shop: ${title}`;
    this.#setDevice("desktop");
    this.querySelector(".demo-viewer__frame").src = "/shop.html?demo=1";
    this.hidden = false;
    window.addEventListener("message", this.#onMessage);
    document.addEventListener("keydown", this.#onKeydown, true);
    this.querySelector("[data-action=close]").focus();
  }

  close() {
    if (this.hidden) return;
    this.hidden = true;
    this.querySelector(".demo-viewer__frame").src = "about:blank";
    window.removeEventListener("message", this.#onMessage);
    document.removeEventListener("keydown", this.#onKeydown, true);
    this.#returnFocus?.focus();
  }

  #setDevice(device) {
    this.querySelector(".demo-viewer__frame").dataset.device = device;
    this.querySelectorAll("[data-device]").forEach((button) => {
      if (button.tagName === "BUTTON") button.setAttribute("aria-pressed", String(button.dataset.device === device));
    });
  }

  #onMessage = (event) => {
    const frame = this.querySelector(".demo-viewer__frame");
    if (event.source !== frame.contentWindow || event.origin !== location.origin) return;
    if (event.data?.type === "papo-shop-demo-ready") {
      frame.contentWindow.postMessage({ type: "papo-shop-demo", payload: this.#payload }, location.origin);
    }
  };

  // Captured before the dialog underneath sees it, so Escape closes only the demo.
  #onKeydown = (event) => {
    if (event.key !== "Escape") return;
    event.stopImmediatePropagation();
    event.preventDefault();
    this.close();
  };
}

customElements.define("shop-demo-viewer", ShopDemoViewer);
