// <modal-dialog title="Partner anlegen"><form>...</form></modal-dialog>
// Call .open() / .close(); dispatches "modal-close" event on backdrop/×/Escape.
class ModalDialog extends HTMLElement {
  connectedCallback() {
    const title = this.getAttribute("title") ?? "";
    const content = this.innerHTML;

    this.innerHTML = `
      <div class="modal-dialog__backdrop">
        <div class="modal-dialog__panel" role="dialog" aria-modal="true">
          <header class="modal-dialog__header">
            <h2>${title}</h2>
            <button type="button" class="modal-dialog__close" aria-label="Schließen">×</button>
          </header>
          <div class="modal-dialog__body">${content}</div>
        </div>
      </div>
    `;

    this.querySelector(".modal-dialog__backdrop").addEventListener("click", (e) => {
      if (e.target === e.currentTarget) this.close();
    });
    this.querySelector(".modal-dialog__close").addEventListener("click", () => this.close());
    this._escHandler = (e) => {
      if (e.key === "Escape") this.close();
    };

    this.hidden = !this.hasAttribute("open");
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
