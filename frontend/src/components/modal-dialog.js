// <modal-dialog title="Partner anlegen"><form>...</form></modal-dialog>
// Call .open() / .close(); dispatches "modal-close" event on backdrop/×/Escape.
// Locks background scroll while any modal-dialog is open (counted, so nested/
// sequential opens across different instances don't unlock each other early).
// Uses position:fixed + a negative top offset (not plain overflow:hidden) so the
// page doesn't visibly snap to the top when opened mid-scroll, and restores the
// exact scroll position on close.
let openModalCount = 0;
let savedScrollY = 0;

function lockBackgroundScroll() {
  savedScrollY = window.scrollY;
  document.body.classList.add("modal-open");
  document.body.style.top = `-${savedScrollY}px`;
}

function unlockBackgroundScroll() {
  document.body.classList.remove("modal-open");
  document.body.style.top = "";
  window.scrollTo(0, savedScrollY);
}

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
    if (!this.hidden) return;
    this.hidden = false;
    document.addEventListener("keydown", this._escHandler);
    if (openModalCount === 0) lockBackgroundScroll();
    openModalCount += 1;
  }

  close() {
    if (this.hidden) return;
    this.hidden = true;
    document.removeEventListener("keydown", this._escHandler);
    openModalCount = Math.max(0, openModalCount - 1);
    if (openModalCount === 0) unlockBackgroundScroll();
    this.dispatchEvent(new CustomEvent("modal-close", { bubbles: true }));
  }
}

customElements.define("modal-dialog", ModalDialog);
