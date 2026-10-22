import { escapeHtml } from "./html.js";

const ICONS = { loading: "loader-circle", empty: "inbox", error: "triangle-alert" };

// Shared loading / empty / error block (portal-visual-design spec). `action` is trusted
// markup from the caller (a button or link), never user data.
export function stateHtml(kind, title, { hint = "", action = "" } = {}) {
  return `
    <div class="state-block state-block--${kind}" role="${kind === "error" ? "alert" : "status"}">
      <i data-lucide="${ICONS[kind]}" aria-hidden="true"></i>
      <p class="state-block__title">${escapeHtml(title)}</p>
      ${hint ? `<p class="state-block__hint">${escapeHtml(hint)}</p>` : ""}
      ${action}
    </div>`;
}
