// Editing directly in the editor preview (shop-builder spec "Block editing"): visible texts are
// editable in place, and the selected block gets a toolbar to move, resize, duplicate and
// delete it. Loaded only by shop.html?preview inside the editor; the public shop never runs
// this. Every change goes to the editor as a message; the editor owns the draft, autosave
// and undo, and sends the updated page back.

// Fields that stay on one line (Enter ends the edit instead of adding a line break).
const SINGLE_LINE = new Set(["title", "heading", "buttonLabel", "caption", "name", "role"]);

// "Größe" per block type: which layout field the size buttons change.
const SIZES = {
  image: { field: "width", label: "Breite", options: [["narrow", "S"], ["wide", "M"], ["full", "L"]] },
  spacer: { field: "size", label: "Größe", options: [["small", "S"], ["medium", "M"], ["large", "L"]] },
  "article-grid": { field: "columns", label: "Spalten", options: [["2", "2"], ["3", "3"], ["4", "4"]] },
  "featured-articles": { field: "columns", label: "Spalten", options: [["2", "2"], ["3", "3"], ["4", "4"]] },
};
const SPACING = { field: "spacing", label: "Abstand", options: [["small", "S"], ["medium", "M"], ["large", "L"]] };
// Value a block shows while the field is unset (renderer defaults).
const DEFAULTS = { width: "wide", size: "medium", columns: "3", spacing: "medium" };

// Lucide-style outline icons, inline so the shop loads nothing extra.
const icon = (path) => `<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${path}</svg>`;
const ICONS = {
  grip: icon('<circle cx="9" cy="6" r="1"/><circle cx="15" cy="6" r="1"/><circle cx="9" cy="12" r="1"/><circle cx="15" cy="12" r="1"/><circle cx="9" cy="18" r="1"/><circle cx="15" cy="18" r="1"/>'),
  up: icon('<path d="m18 15-6-6-6 6"/>'),
  down: icon('<path d="m6 9 6 6 6-6"/>'),
  copy: icon('<rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>'),
  trash: icon('<path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>'),
};

const STYLES = `
[data-editable] { outline: none; cursor: text; border-radius: 3px; }
[data-editable]:hover { box-shadow: 0 0 0 2px color-mix(in srgb, var(--shop-accent) 55%, transparent); }
[data-editable]:focus { box-shadow: 0 0 0 2px var(--shop-accent); background: color-mix(in srgb, var(--shop-surface) 35%, transparent); }
.papo-tools {
  position: absolute; z-index: 20; display: flex; align-items: center; gap: 2px; padding: 4px;
  background: #1f2328; color: #fff; border-radius: 8px; box-shadow: 0 6px 18px rgba(0,0,0,.28);
  font: 600 12px/1 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; transform: translateY(-100%);
}
.papo-tools[hidden] { display: none; }
.papo-tools button, .papo-tools__grip {
  display: inline-flex; align-items: center; justify-content: center; min-width: 28px; height: 28px; padding: 0 6px;
  border: 0; border-radius: 5px; background: none; color: inherit; font: inherit; cursor: pointer;
}
.papo-tools button:hover:not(:disabled), .papo-tools__grip:hover { background: rgba(255,255,255,.16); }
.papo-tools button:disabled { opacity: .35; cursor: default; }
.papo-tools button[aria-pressed="true"] { background: #fff; color: #1f2328; }
.papo-tools__grip { cursor: grab; touch-action: none; }
.papo-tools__sep { width: 1px; height: 18px; margin: 0 4px; background: rgba(255,255,255,.25); }
.papo-tools__label { padding: 0 4px 0 2px; opacity: .75; font-weight: 500; }
.papo-tools [data-action="delete"]:hover { background: #c62828; }
.papo-drop-line {
  position: absolute; z-index: 19; left: 0; right: 0; height: 6px; margin-top: -3px; pointer-events: none;
  background: #1f2328; box-shadow: 0 0 0 3px rgba(255,255,255,.95), 0 4px 12px rgba(0,0,0,.35);
}
.papo-drop-line[hidden] { display: none; }
.shop-block.is-dragging { opacity: .4; }
.shop-root--preview .shop-block { cursor: grab; }
.papo-dragging, .papo-dragging * { cursor: grabbing !important; user-select: none !important; }
`;

export function installPreviewTools(root, post) {
  const style = document.createElement("style");
  style.textContent = STYLES;
  document.head.append(style);
  const toolbar = document.createElement("div");
  toolbar.className = "papo-tools";
  toolbar.hidden = true;
  toolbar.setAttribute("role", "toolbar");
  toolbar.setAttribute("aria-label", "Abschnitt bearbeiten");
  const dropLine = document.createElement("div");
  dropLine.className = "papo-drop-line";
  dropLine.hidden = true;
  document.body.append(toolbar, dropLine);

  let view = null;
  let selected = null;
  let dragged = null;
  let pending = false;

  // Blocks may fill in later (article lists): keep texts editable and the toolbar in place.
  new MutationObserver(() => {
    if (pending || !view) return;
    pending = true;
    requestAnimationFrame(() => {
      pending = false;
      makeEditable();
      placeToolbar();
    });
  }).observe(root, { childList: true, subtree: true });

  const blocks = () => [...(view?.children ?? [])];
  const typeOf = (element) => element.localName.replace(/^shop-block-/, "");

  // ---- Inline text --------------------------------------------------------------------

  function makeEditable() {
    for (const field of view.querySelectorAll("[data-field]")) {
      if (field.hasAttribute("data-editable")) continue;
      field.setAttribute("data-editable", "");
      field.setAttribute("contenteditable", "plaintext-only");
      field.spellcheck = true;
    }
  }

  root.addEventListener("input", (event) => {
    const field = event.target.closest("[data-editable]");
    const element = field?.closest("[data-block-id]");
    if (!element) return;
    const name = field.dataset.field;
    const value = SINGLE_LINE.has(name) ? field.innerText.replace(/\n/g, " ") : field.innerText;
    // The text is already on screen; re-rendering would move the caret.
    view.adopt(element, { ...element.config, [name]: value });
    post({ type: "papo-shop-edit", blockId: element.dataset.blockId, field: name, value });
    placeToolbar();
  });

  root.addEventListener("keydown", (event) => {
    const field = event.target.closest("[data-editable]");
    if (!field) return;
    if (event.key === "Escape" || (event.key === "Enter" && SINGLE_LINE.has(field.dataset.field))) {
      event.preventDefault();
      field.blur();
    }
  });

  // ---- Toolbar ------------------------------------------------------------------------

  function renderToolbar() {
    if (!selected) {
      toolbar.hidden = true;
      return;
    }
    const list = blocks();
    const index = list.indexOf(selected);
    const type = typeOf(selected);
    const fixed = type === "article-detail";
    const size = SIZES[type] ?? SPACING;
    const current = String(selected.config[size.field] ?? DEFAULTS[size.field]);
    toolbar.innerHTML = `
      <span class="papo-tools__grip" title="Ziehen zum Verschieben" aria-hidden="true">${ICONS.grip}</span>
      <button type="button" data-action="up" title="Nach oben" aria-label="Nach oben"${index <= 0 ? " disabled" : ""}>${ICONS.up}</button>
      <button type="button" data-action="down" title="Nach unten" aria-label="Nach unten"${index === list.length - 1 ? " disabled" : ""}>${ICONS.down}</button>
      <span class="papo-tools__sep"></span>
      <span class="papo-tools__label">${size.label}</span>
      ${size.options.map(([value, label]) => `<button type="button" data-action="size" data-field="${size.field}" data-value="${value}"
        aria-pressed="${current === value}" aria-label="${size.label} ${label}">${label}</button>`).join("")}
      ${fixed ? "" : `<span class="papo-tools__sep"></span>
      <button type="button" data-action="duplicate" title="Duplizieren" aria-label="Duplizieren">${ICONS.copy}</button>
      <button type="button" data-action="delete" title="Löschen" aria-label="Löschen">${ICONS.trash}</button>`}`;
    toolbar.hidden = false;
    placeToolbar();
  }

  // Above the block's top edge; inside the block when there is no room above (first block).
  function placeToolbar() {
    if (!selected || toolbar.hidden) return;
    const rect = selected.getBoundingClientRect();
    const top = rect.top + window.scrollY;
    const height = toolbar.offsetHeight;
    toolbar.style.top = `${top < height + 8 ? top + height + 8 : top - 6}px`;
    toolbar.style.left = `${Math.max(8, rect.left + window.scrollX + 8)}px`;
  }

  toolbar.addEventListener("mousedown", (event) => event.preventDefault()); // keep text focus
  toolbar.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button || !selected) return;
    // Ends a text edit, so the block may be re-rendered with the change.
    if (document.activeElement?.isContentEditable) document.activeElement.blur();
    post({
      type: "papo-shop-action",
      action: button.dataset.action,
      blockId: selected.dataset.blockId,
      field: button.dataset.field,
      value: button.dataset.value,
    });
  });
  window.addEventListener("resize", placeToolbar);

  // ---- Reorder by dragging ------------------------------------------------------------
  // Pointer events instead of HTML5 drag & drop: the toolbar must keep the text focus
  // (mousedown default prevented), which would block a native drag start. A block can be
  // grabbed at the grip or anywhere outside its texts; 6 px of movement starts the drag.

  const DRAG_THRESHOLD = 6;
  let suppressClick = false;

  function dropIndex(clientY) {
    const list = blocks();
    for (let i = 0; i < list.length; i++) {
      const rect = list[i].getBoundingClientRect();
      if (clientY < rect.top + rect.height / 2) return { index: i, y: rect.top };
    }
    const last = list.at(-1)?.getBoundingClientRect();
    return { index: list.length, y: last ? last.bottom : 0 };
  }

  function beginDrag(event, element) {
    if (event.button !== 0 || !element) return;
    dragged = { element, startY: event.clientY, active: false, pointerId: event.pointerId };
  }

  toolbar.addEventListener("pointerdown", (event) => {
    if (event.target.closest(".papo-tools__grip")) beginDrag(event, selected);
  });
  root.addEventListener("pointerdown", (event) => {
    if (event.target.closest("[data-editable], button, input, select, textarea")) return;
    beginDrag(event, event.target.closest("shop-page-view > [data-block-id]"));
  });
  // Images and links would otherwise start the browser's own drag.
  root.addEventListener("dragstart", (event) => event.preventDefault());

  document.addEventListener("pointermove", (event) => {
    if (!dragged || event.pointerId !== dragged.pointerId) return;
    if (!dragged.active) {
      if (Math.abs(event.clientY - dragged.startY) < DRAG_THRESHOLD) return;
      dragged.active = true;
      dragged.element.classList.add("is-dragging");
      document.documentElement.classList.add("papo-dragging");
      getSelection()?.removeAllRanges();
    }
    event.preventDefault();
    const { y } = dropIndex(event.clientY);
    dropLine.style.top = `${y + window.scrollY}px`;
    dropLine.hidden = false;
    if (event.clientY < 48) window.scrollBy(0, -20);
    else if (event.clientY > window.innerHeight - 48) window.scrollBy(0, 20);
  });

  function endDrag(event, drop) {
    if (!dragged || event.pointerId !== dragged.pointerId) return;
    if (dragged.active) {
      if (drop) {
        post({ type: "papo-shop-action", action: "move", blockId: dragged.element.dataset.blockId,
          value: dropIndex(event.clientY).index });
      }
      dragged.element.classList.remove("is-dragging");
      document.documentElement.classList.remove("papo-dragging");
      dropLine.hidden = true;
      // The click that ends a drag must not select the block under the pointer.
      suppressClick = true;
      setTimeout(() => {
        suppressClick = false;
      });
    }
    dragged = null;
  }
  document.addEventListener("pointerup", (event) => endDrag(event, true));
  document.addEventListener("pointercancel", (event) => endDrag(event, false));
  root.addEventListener("click", (event) => {
    if (!suppressClick) return;
    event.stopImmediatePropagation();
    event.preventDefault();
  }, true);

  return {
    // After every preview render.
    afterRender(pageView, selectedBlockId) {
      view = pageView;
      makeEditable();
      selected = selectedBlockId
        ? blocks().find((element) => element.dataset.blockId === selectedBlockId) ?? null
        : null;
      renderToolbar();
    },
  };
}
