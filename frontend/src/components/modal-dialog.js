// <modal-dialog title="Partner anlegen"><form>...</form></modal-dialog>
// Call .open() / .close(); dispatches "modal-close" event on backdrop/×/Escape.
class ModalDialog extends HTMLElement {
  static get observedAttributes() {
    return ["title"];
  }

  connectedCallback() {
    const content = this.innerHTML;

    this.innerHTML = `
      <div class="modal-dialog__backdrop">
        <div class="modal-dialog__panel" role="dialog" aria-modal="true">
          <header class="modal-dialog__header">
            <h2></h2>
            <button type="button" class="modal-dialog__close" aria-label="Schließen">×</button>
          </header>
          <div class="modal-dialog__body">${content}</div>
        </div>
      </div>
    `;

    this._titleEl = this.querySelector(".modal-dialog__header h2");
    this._titleEl.textContent = this.getAttribute("title") ?? "";

    this.querySelector(".modal-dialog__backdrop").addEventListener("click", (e) => {
      if (e.target === e.currentTarget) this.close();
    });
    this.querySelector(".modal-dialog__close").addEventListener("click", () => this.close());
    this._escHandler = (e) => {
      if (e.key === "Escape") this.close();
    };

    this.hidden = !this.hasAttribute("open");
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "title" && this._titleEl) {
      this._titleEl.textContent = newValue ?? "";
    }
  }

  open() {
    this.hidden = false;
    document.addEventListener("keydown", this._escHandler);
  }

  close() {
    this.hidden = true;
    document.removeEventListener("keydown", this._escHandler);
    this.dispatchEvent(new CustomEvent("modal-close", { bubbles: true }));
  }
}

customElements.define("modal-dialog", ModalDialog);
