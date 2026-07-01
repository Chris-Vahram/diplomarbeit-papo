// Usage: import { showToast } from "./toast-notice.js"; showToast("Gespeichert.", "success");
const AUTO_DISMISS_MS = 4000;

class ToastNotice extends HTMLElement {
  connectedCallback() {
    const message = this.getAttribute("message") ?? "";
    const type = this.getAttribute("type") ?? "info";
    this.className = `toast-notice toast-notice--${type}`;
    this.textContent = message;
    setTimeout(() => this.remove(), AUTO_DISMISS_MS);
  }
}

customElements.define("toast-notice", ToastNotice);

export function showToast(message, type = "info") {
  const root = document.getElementById("toast-root");
  if (!root) return;
  const toast = document.createElement("toast-notice");
  toast.setAttribute("message", message);
  toast.setAttribute("type", type);
  root.appendChild(toast);
}
