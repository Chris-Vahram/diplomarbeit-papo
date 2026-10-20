import { apiFetch } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { resolveImageUrl } from "../lib/product-images.js";
import { showToast } from "../components/toast-notice.js";
import { BLOCK_TYPES, missingRequired, newBlock } from "./block-schema.js";
import { EditorHistory } from "./editor-history.js";
import { assetObjectUrl } from "./asset-library.js";
import "./asset-library.js";
import "./block-properties.js";
import "./design-panel.js";
import "./page-manager.js";

// Shop builder of "Mein Webshop" (shop-builder spec): block palette, live preview in an
// iframe of shop.html?preview (design.md Decision 3) and properties panel, plus the tabs
// "Seiten", "Design" and "Bilder". Drafts are saved as whole pages with the loaded version
// (Decision 1); undo/redo is a client-side snapshot stack (Decision 2).

const AUTOSAVE_MS = 1500;
const SMALL_SCREEN = window.matchMedia("(max-width: 767px)");
const SAVE_TEXT = {
  saved: "Gespeichert",
  pending: "Wird gespeichert …",
  saving: "Wird gespeichert …",
  error: "Nicht gespeichert – erneut versuchen",
  conflict: "Nicht gespeichert – Seite wurde inzwischen geändert",
};

function blockSummary(block) {
  const config = block.config ?? {};
  return config.title || config.heading || config.name || config.caption || "";
}

function move(list, from, to) {
  const copy = [...list];
  const [item] = copy.splice(from, 1);
  copy.splice(to, 0, item);
  return copy;
}

class ShopBuilder extends HTMLElement {
  #shop = null;
  #pages = null;
  #page = null;
  #blocks = [];
  #theme = null;
  #themeDraft = null;
  #preview = null;
  #imageUrls = {};
  #assetUrls = {};
  #selectedId = null;
  #tab = "edit";
  #device = "desktop";
  #frameReady = false;
  #history = new EditorHistory();
  #pickField = null;
  #small = null;
  #screenTimer = null;

  #saveTimer = null;
  #saving = null;
  #saveAgain = false;
  #changeCount = 0;
  #status = "saved";
  #themeTimer = null;
  #themeSaving = null;

  // PartnerShopDto, for the public link in the small-screen hint.
  set shop(value) {
    this.#shop = value;
    if (SMALL_SCREEN.matches) this.#renderSmallScreenHint();
  }

  connectedCallback() {
    SMALL_SCREEN.addEventListener("change", this.#onScreenChange);
    window.addEventListener("message", this.#onFrameMessage);
    window.addEventListener("beforeunload", this.#onBeforeUnload);
    document.addEventListener("keydown", this.#onKeydown);
    this.#mount();
  }

  disconnectedCallback() {
    SMALL_SCREEN.removeEventListener("change", this.#onScreenChange);
    clearTimeout(this.#screenTimer);
    window.removeEventListener("message", this.#onFrameMessage);
    window.removeEventListener("beforeunload", this.#onBeforeUnload);
    document.removeEventListener("keydown", this.#onKeydown);
    // Leaving the page through the portal menu: send what is still pending.
    if (this.#saveTimer) this.#save();
    if (this.#themeTimer) this.#saveTheme();
  }

  // Only a lasting switch between editor and hint re-mounts (debounced: window resizes and
  // device emulation pass through intermediate widths).
  #onScreenChange = () => {
    clearTimeout(this.#screenTimer);
    this.#screenTimer = setTimeout(async () => {
      if (SMALL_SCREEN.matches === this.#small) return;
      await this.#flush();
      this.#mount();
    }, 300);
  };

  #onBeforeUnload = (event) => {
    if (this.#saveTimer || this.#saving || this.#status === "error" || this.#themeTimer) {
      event.preventDefault();
    }
  };

  #mount() {
    this.#small = SMALL_SCREEN.matches;
    if (this.#small) {
      this.#frameReady = false;
      this.#renderSmallScreenHint();
      return;
    }
    this.#renderLayout();
    this.#load();
  }

  // shop-builder spec "Device support": below 768 px only a hint and the public link.
  #renderSmallScreenHint() {
    const link = this.#shop ? `${location.origin}${this.#shop.publicPath}` : "";
    this.innerHTML = `<section class="builder-small">
      <p><strong>Der Shop-Editor ist für Computer und Tablet gedacht.</strong></p>
      <p>Öffnen Sie „Mein Webshop“ auf einem größeren Bildschirm, um Ihren Shop zu gestalten.</p>
      ${link ? `<p>Ihr Shop: <a href="${escapeHtml(link)}" target="_blank" rel="noopener">${escapeHtml(link)}</a></p>` : ""}
    </section>`;
  }

  #renderLayout() {
    this.#frameReady = false;
    this.innerHTML = `
      <div class="builder" data-tab="edit">
        <div class="builder-tabs" role="tablist" aria-label="Shop-Editor">
          <button type="button" role="tab" data-tab="edit" aria-selected="true">Seite bearbeiten</button>
          <button type="button" role="tab" data-tab="pages" aria-selected="false">Seiten</button>
          <button type="button" role="tab" data-tab="design" aria-selected="false">Design</button>
          <button type="button" role="tab" data-tab="images" aria-selected="false">Bilder</button>
        </div>

        <div class="builder-pagebar">
          <label class="builder-pagebar__select">Seite
            <select id="builder-page-select"></select>
          </label>
          <span class="builder-pagebar__state" id="builder-page-state"></span>
          <span class="builder-save" id="builder-save" aria-live="polite"></span>
          <button type="button" class="button-secondary" id="builder-retry" hidden>Erneut versuchen</button>
          <span class="builder-pagebar__spacer"></span>
          <button type="button" class="builder-icon" id="builder-undo" title="Rückgängig (Strg+Z)" aria-label="Rückgängig" disabled>↶</button>
          <button type="button" class="builder-icon" id="builder-redo" title="Wiederholen (Strg+Umschalt+Z)" aria-label="Wiederholen" disabled>↷</button>
          <div class="builder-device" role="group" aria-label="Vorschaugröße">
            <button type="button" data-device="desktop" aria-pressed="true">Computer</button>
            <button type="button" data-device="mobile" aria-pressed="false">Handy</button>
          </div>
          <button type="button" class="button-secondary" id="builder-restore" hidden>Vorherige Version wiederherstellen</button>
          <button type="button" class="button-primary" id="builder-publish">Seite veröffentlichen</button>
        </div>

        <div class="builder-body">
          <aside class="builder-left">
            <div data-panel="edit">
              <h3 class="builder-panel__title">Abschnitt hinzufügen</h3>
              <div class="builder-palette" id="builder-palette"></div>
              <h3 class="builder-panel__title">Abschnitte dieser Seite</h3>
              <ol class="block-list" id="builder-blocks"></ol>
            </div>
            <div data-panel="pages" hidden><shop-page-manager></shop-page-manager></div>
            <div data-panel="design" hidden><shop-design-panel></shop-design-panel></div>
            <div data-panel="images" hidden>
              <h3 class="builder-panel__title">Bilder</h3>
              <p class="builder-hint">Bilder, die Sie hier hochladen, können Sie in den Abschnitten „Hero/Banner“, „Bild“ und „Bild + Text“ verwenden.</p>
              <shop-asset-library></shop-asset-library>
            </div>
          </aside>
          <section class="builder-preview" aria-label="Vorschau">
            <div class="builder-frame" data-device="desktop">
              <iframe id="builder-frame" title="Vorschau des Shops" src="/shop.html?preview=1"></iframe>
            </div>
          </section>
          <aside class="builder-right">
            <shop-block-properties></shop-block-properties>
          </aside>
        </div>

        <modal-dialog title="Seite wurde geändert" id="builder-conflict">
          <p id="builder-conflict-text">Diese Seite wurde inzwischen geändert. Bitte neu laden.</p>
          <p class="builder-hint">Wahrscheinlich ist der Shop in einem zweiten Fenster oder von einer Kollegin bzw. einem Kollegen geöffnet. Ihre letzten Änderungen in diesem Fenster gehen beim Neuladen verloren.</p>
          <div class="entity-form__actions">
            <button type="button" class="button-primary" id="builder-conflict-reload">Neu laden</button>
          </div>
        </modal-dialog>

        <modal-dialog title="Vorherige Version wiederherstellen" id="builder-restore-dialog">
          <p>Die zuletzt davor veröffentlichte Version dieser Seite wird wieder öffentlich und ersetzt Ihren Entwurf.</p>
          <div class="entity-form__actions">
            <button type="button" class="button-secondary" data-close>Abbrechen</button>
            <button type="button" class="button-primary" id="builder-restore-confirm">Wiederherstellen</button>
          </div>
        </modal-dialog>

        <modal-dialog title="Bild wählen" id="builder-image-dialog">
          <shop-asset-library mode="pick"></shop-asset-library>
        </modal-dialog>
      </div>`;
    this.#bindLayout();
  }

  #bindLayout() {
    this.querySelector(".builder-tabs").addEventListener("click", (event) => {
      const tab = event.target.closest("[data-tab]")?.dataset.tab;
      if (tab) this.#showTab(tab);
    });
    this.querySelector(".builder-device").addEventListener("click", (event) => {
      const device = event.target.closest("[data-device]")?.dataset.device;
      if (!device) return;
      this.#device = device;
      this.querySelector(".builder-frame").dataset.device = device;
      this.querySelectorAll(".builder-device button").forEach((button) =>
        button.setAttribute("aria-pressed", String(button.dataset.device === device)));
    });
    this.querySelector("#builder-page-select").addEventListener("change", (event) => this.#openPage(Number(event.target.value)));
    this.querySelector("#builder-retry").addEventListener("click", () => this.#save());
    this.querySelector("#builder-undo").addEventListener("click", () => this.#undo());
    this.querySelector("#builder-redo").addEventListener("click", () => this.#redo());
    this.querySelector("#builder-publish").addEventListener("click", () => this.#publish());
    this.querySelector("#builder-restore").addEventListener("click", () => this.querySelector("#builder-restore-dialog").open());
    this.querySelector("#builder-restore-confirm").addEventListener("click", () => this.#restore());
    this.querySelector("#builder-conflict-reload").addEventListener("click", () => this.#reloadAfterConflict());
    this.querySelectorAll("[data-close]").forEach((button) =>
      button.addEventListener("click", () => button.closest("modal-dialog").close()));

    this.querySelector("#builder-palette").addEventListener("click", (event) => {
      const type = event.target.closest("[data-type]")?.dataset.type;
      if (type) this.#addBlock(type);
    });
    const blockList = this.querySelector("#builder-blocks");
    blockList.addEventListener("click", (event) => this.#onBlockListClick(event));
    this.#bindBlockDragAndDrop(blockList);

    this.addEventListener("block-change", (event) => {
      event.stopPropagation();
      this.#updateBlock(this.#selectedId, event.detail.config, event.detail.key);
    });
    this.addEventListener("pick-image", (event) => {
      this.#pickField = event.detail.field;
      this.querySelector("#builder-image-dialog shop-asset-library").load();
      this.querySelector("#builder-image-dialog").open();
    });
    this.querySelector("#builder-image-dialog").addEventListener("asset-pick", (event) => {
      this.querySelector("#builder-image-dialog").close();
      this.querySelector("shop-block-properties").setImage(this.#pickField, event.detail.id);
    });
    this.addEventListener("page-open", (event) => {
      this.#showTab("edit");
      this.#openPage(event.detail.id);
    });
    this.addEventListener("pages-changed", (event) => this.#onPagesChanged(event.detail));
    this.addEventListener("page-deleted", (event) => {
      if (event.detail.id === this.#page?.id) this.#openPage(this.#pages.pages[0].id);
    });
    this.addEventListener("theme-change", (event) => this.#onThemeChange(event.detail.theme));
    this.addEventListener("theme-publish", () => this.#publishTheme());
  }

  async #load() {
    try {
      const [pages, theme, preview] = await Promise.all([
        apiFetch("/partner/shop/pages"),
        apiFetch("/partner/shop/theme"),
        apiFetch("/partner/shop/preview"),
      ]);
      this.#pages = pages;
      this.#theme = theme;
      this.#themeDraft = theme.draft;
      this.#preview = preview;
      this.querySelector("shop-page-manager").list = pages;
      this.#renderDesignPanel();
      this.#renderPalette();
      await this.#openPage(pages.pages[0].id, { skipFlush: true });
      this.#resolveArticleImages();
    } catch (error) {
      this.querySelector(".builder-body").innerHTML =
        `<p class="builder-error" role="alert">Der Editor konnte nicht geladen werden: ${escapeHtml(error.message)}</p>`;
    }
  }

  // Article images of a not yet public shop come through the portal and need object URLs.
  async #resolveArticleImages() {
    const urls = [...new Set(this.#preview.articles.map((article) => article.imageUrl).filter((url) => url?.startsWith("/product-images/")))];
    await Promise.all(urls.map(async (url) => {
      try {
        this.#imageUrls[url] = await resolveImageUrl(url);
      } catch {
        // Missing image: the card shows its placeholder.
      }
    }));
    this.#sendPreview();
  }

  #showTab(tab) {
    this.#tab = tab;
    this.querySelector(".builder").dataset.tab = tab;
    this.querySelectorAll(".builder-tabs [data-tab]").forEach((button) =>
      button.setAttribute("aria-selected", String(button.dataset.tab === tab)));
    this.querySelectorAll("[data-panel]").forEach((panel) => {
      panel.hidden = panel.dataset.panel !== tab;
    });
    if (tab === "images") this.querySelector("[data-panel=images] shop-asset-library").load();
  }

  // ---- Pages -------------------------------------------------------------------------

  async #openPage(id, { skipFlush = false } = {}) {
    if (!skipFlush && !(await this.#flush())) {
      showToast("Bitte zuerst die Änderungen speichern.", "error");
      this.#renderPageBar();
      return;
    }
    try {
      const detail = await apiFetch(`/partner/shop/pages/${id}`);
      this.#page = detail.page;
      this.#blocks = detail.draft.blocks;
      this.#selectedId = null;
      this.#history.clear();
      this.#setStatus("saved");
      this.#renderAll();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  #onPagesChanged({ list, page }) {
    if (list) {
      this.#pages = list;
      const current = list.pages.find((candidate) => candidate.id === this.#page?.id);
      // Keep the version from the last own save if it is newer than the list's.
      if (current && current.version >= this.#page.version) this.#page = current;
    }
    if (page) this.#replacePage(page);
    this.#renderPageBar();
    this.#sendPreview();
  }

  #replacePage(page) {
    this.#pages = { ...this.#pages, pages: this.#pages.pages.map((candidate) => (candidate.id === page.id ? page : candidate)) };
    if (page.id === this.#page?.id) this.#page = page;
    this.querySelector("shop-page-manager").list = this.#pages;
  }

  async #refreshPages() {
    this.#pages = await apiFetch("/partner/shop/pages");
    this.querySelector("shop-page-manager").list = this.#pages;
    this.#renderPageBar();
  }

  #renderPageBar() {
    if (!this.#pages || !this.#page) return;
    this.querySelector("#builder-page-select").innerHTML = this.#pages.pages
      .map((page) => `<option value="${page.id}"${page.id === this.#page.id ? " selected" : ""}>${escapeHtml(page.menuName)}${page.online ? "" : " (Entwurf)"}</option>`)
      .join("");
    const page = this.#page;
    const changed = page.draftChanged || this.#saveTimer || this.#saving;
    this.querySelector("#builder-page-state").textContent = page.online
      ? changed ? "Online – Änderungen noch nicht veröffentlicht" : "Online – aktuell"
      : "Entwurf – noch nicht online";
    this.querySelector("#builder-restore").hidden = !page.online || !page.restorable;
    this.querySelector("#builder-publish").textContent = page.online ? "Änderungen veröffentlichen" : "Seite veröffentlichen";
    this.querySelector("#builder-undo").disabled = !this.#history.canUndo;
    this.querySelector("#builder-redo").disabled = !this.#history.canRedo;
  }

  async #publish() {
    const problems = missingRequired(this.#blocks);
    if (problems.length) {
      this.#select(problems[0].blockId);
      showToast(`Bitte zuerst ergänzen: ${problems.map((problem) => problem.message).join(" ")}`, "error");
      return;
    }
    if (!(await this.#flush())) {
      showToast("Die Seite konnte nicht gespeichert werden und wurde daher nicht veröffentlicht.", "error");
      return;
    }
    const button = this.querySelector("#builder-publish");
    button.disabled = true;
    try {
      const page = await apiFetch(`/partner/shop/pages/${this.#page.id}/publish`, {
        method: "POST",
        body: JSON.stringify({ version: this.#page.version }),
      });
      this.#replacePage(page);
      await this.#refreshPages();
      showToast(`„${page.menuName}“ ist veröffentlicht.`, "success");
    } catch (error) {
      if (error.status === 409 && /inzwischen geändert/.test(error.message)) this.#showConflict(error.message);
      else showToast(error.message, "error");
    } finally {
      button.disabled = false;
      this.#renderPageBar();
    }
  }

  async #restore() {
    this.querySelector("#builder-restore-dialog").close();
    if (!(await this.#flush())) return;
    try {
      const detail = await apiFetch(`/partner/shop/pages/${this.#page.id}/restore`, {
        method: "POST",
        body: JSON.stringify({ version: this.#page.version }),
      });
      this.#page = detail.page;
      this.#blocks = detail.draft.blocks;
      this.#selectedId = null;
      this.#history.clear();
      this.#replacePage(detail.page);
      this.#renderAll();
      showToast("Die vorherige Version ist wiederhergestellt.", "success");
    } catch (error) {
      if (error.status === 409 && /inzwischen geändert/.test(error.message)) this.#showConflict(error.message);
      else showToast(error.message, "error");
    }
  }

  // ---- Blocks ------------------------------------------------------------------------

  #renderPalette() {
    this.querySelector("#builder-palette").innerHTML = Object.entries(BLOCK_TYPES)
      .filter(([, type]) => !type.fixed)
      .map(([id, type]) => `<button type="button" class="palette-item" data-type="${id}" title="${escapeHtml(type.description)}">
          <span class="palette-item__label">+ ${escapeHtml(type.label)}</span>
          <span class="palette-item__desc">${escapeHtml(type.description)}</span>
        </button>`)
      .join("");
  }

  #renderBlockList() {
    const list = this.querySelector("#builder-blocks");
    if (!this.#blocks.length) {
      list.innerHTML = `<li class="builder-empty">Diese Seite ist noch leer. Fügen Sie oben einen Abschnitt hinzu.</li>`;
      return;
    }
    list.innerHTML = this.#blocks
      .map((block, index) => {
        const type = BLOCK_TYPES[block.type];
        const fixed = type?.fixed;
        const summary = blockSummary(block);
        const selected = block.id === this.#selectedId;
        return `<li class="block-row${selected ? " is-selected" : ""}" draggable="true" data-block-id="${escapeHtml(block.id)}">
          <button type="button" class="block-row__select" data-action="select" aria-pressed="${selected}">
            <span class="block-row__handle" aria-hidden="true">⠿</span>
            <span class="block-row__label">${escapeHtml(type?.label ?? block.type)}</span>
            ${summary ? `<span class="block-row__summary">${escapeHtml(summary)}</span>` : ""}
          </button>
          <span class="block-row__actions">
            <button type="button" class="builder-icon" data-action="up" aria-label="nach oben"${index === 0 ? " disabled" : ""}>↑</button>
            <button type="button" class="builder-icon" data-action="down" aria-label="nach unten"${index === this.#blocks.length - 1 ? " disabled" : ""}>↓</button>
            ${fixed ? "" : `<button type="button" class="builder-icon" data-action="duplicate" aria-label="Duplizieren" title="Duplizieren">⧉</button>
            <button type="button" class="builder-icon" data-action="delete" aria-label="Löschen" title="Löschen">🗑</button>`}
          </span>
        </li>`;
      })
      .join("");
  }

  #onBlockListClick(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = button.closest("[data-block-id]").dataset.blockId;
    const index = this.#blocks.findIndex((block) => block.id === id);
    switch (button.dataset.action) {
      case "select":
        this.#select(id);
        break;
      case "up":
        this.#applyBlocks(move(this.#blocks, index, index - 1));
        break;
      case "down":
        this.#applyBlocks(move(this.#blocks, index, index + 1));
        break;
      case "duplicate": {
        const copy = { ...structuredClone(this.#blocks[index]), id: crypto.randomUUID() };
        const blocks = [...this.#blocks];
        blocks.splice(index + 1, 0, copy);
        this.#selectedId = copy.id;
        this.#applyBlocks(blocks);
        break;
      }
      case "delete":
        if (this.#selectedId === id) this.#selectedId = null;
        this.#applyBlocks(this.#blocks.filter((block) => block.id !== id));
        showToast("Abschnitt gelöscht. „Rückgängig“ holt ihn zurück.", "info");
        break;
      default:
    }
  }

  #bindBlockDragAndDrop(list) {
    let dragged = null;
    list.addEventListener("dragstart", (event) => {
      const row = event.target.closest("[data-block-id]");
      if (!row) return;
      dragged = row.dataset.blockId;
      row.classList.add("is-dragging");
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", dragged);
    });
    list.addEventListener("dragend", () => {
      dragged = null;
      list.querySelectorAll(".is-dragging, .is-drop-target").forEach((row) => row.classList.remove("is-dragging", "is-drop-target"));
    });
    list.addEventListener("dragover", (event) => {
      const row = event.target.closest("[data-block-id]");
      if (!row || !dragged) return;
      event.preventDefault();
      list.querySelectorAll(".is-drop-target").forEach((other) => other !== row && other.classList.remove("is-drop-target"));
      row.classList.add("is-drop-target");
    });
    list.addEventListener("drop", (event) => {
      const row = event.target.closest("[data-block-id]");
      if (!row || !dragged) return;
      event.preventDefault();
      const from = this.#blocks.findIndex((block) => block.id === dragged);
      const to = this.#blocks.findIndex((block) => block.id === row.dataset.blockId);
      dragged = null;
      if (from !== to) this.#applyBlocks(move(this.#blocks, from, to));
    });
  }

  #addBlock(type) {
    const block = newBlock(type);
    const blocks = [...this.#blocks];
    const at = this.#blocks.findIndex((candidate) => candidate.id === this.#selectedId);
    blocks.splice(at === -1 ? blocks.length : at + 1, 0, block);
    this.#selectedId = block.id;
    this.#applyBlocks(blocks);
  }

  #select(id) {
    this.#selectedId = id;
    if (this.#tab !== "edit") this.#showTab("edit");
    this.#renderBlockList();
    this.#renderProperties();
    this.#sendPreview();
    this.querySelector(`#builder-blocks [data-block-id="${CSS.escape(id)}"]`)?.scrollIntoView({ block: "nearest" });
  }

  #updateBlock(id, config, key) {
    this.#applyBlocks(this.#blocks.map((block) => (block.id === id ? { ...block, config } : block)), key);
  }

  // Every draft change goes through here: undo step, autosave, re-render.
  #applyBlocks(blocks, key = null) {
    this.#history.record(JSON.stringify(this.#blocks), key);
    this.#blocks = blocks;
    this.#changed();
  }

  #changed() {
    this.#changeCount += 1;
    this.#setStatus("pending");
    clearTimeout(this.#saveTimer);
    this.#saveTimer = setTimeout(() => this.#save(), AUTOSAVE_MS);
    this.#renderAll();
  }

  #undo() {
    const snapshot = this.#history.undo(JSON.stringify(this.#blocks));
    if (snapshot === null) return;
    this.#blocks = JSON.parse(snapshot);
    this.#changed();
  }

  #redo() {
    const snapshot = this.#history.redo(JSON.stringify(this.#blocks));
    if (snapshot === null) return;
    this.#blocks = JSON.parse(snapshot);
    this.#changed();
  }

  // Strg+Z / Strg+Umschalt+Z outside text fields (inside them the browser's own undo applies).
  #onKeydown = (event) => {
    if (!this.#page || this.#tab !== "edit" || !(event.ctrlKey || event.metaKey)) return;
    if (event.target.closest("input, textarea, select, [contenteditable]")) return;
    const key = event.key.toLowerCase();
    if (key === "z" && !event.shiftKey) {
      event.preventDefault();
      this.#undo();
    } else if ((key === "z" && event.shiftKey) || key === "y") {
      event.preventDefault();
      this.#redo();
    }
  };

  #renderAll() {
    this.#renderBlockList();
    this.#renderProperties();
    this.#renderPageBar();
    this.#sendPreview();
  }

  #renderProperties() {
    const block = this.#blocks.find((candidate) => candidate.id === this.#selectedId) ?? null;
    if (!block) this.#selectedId = null;
    const linkPages = this.#pages.pages
      .filter((page) => page.online && page.slot !== "ARTICLE_DETAIL" && page.id !== this.#page.id)
      .map((page) => ({ segment: page.segment, menuName: page.menuName }));
    this.querySelector("shop-block-properties").data = {
      block,
      theme: this.#themeDraft,
      linkPages,
      articles: this.#preview.articles,
      categories: this.#preview.categories,
    };
  }

  // ---- Autosave ----------------------------------------------------------------------

  #setStatus(status) {
    this.#status = status;
    const el = this.querySelector("#builder-save");
    if (!el) return;
    el.textContent = SAVE_TEXT[status];
    el.dataset.status = status;
    this.querySelector("#builder-retry").hidden = status !== "error";
  }

  #save() {
    if (this.#status === "conflict") return Promise.resolve();
    if (this.#saving) {
      this.#saveAgain = true;
      return this.#saving;
    }
    clearTimeout(this.#saveTimer);
    this.#saveTimer = null;
    const count = this.#changeCount;
    const page = this.#page;
    this.#setStatus("saving");
    this.#saving = apiFetch(`/partner/shop/pages/${page.id}/draft`, {
      method: "PUT",
      body: JSON.stringify({ content: { blocks: this.#blocks }, version: page.version }),
    })
      .then((saved) => {
        this.#replacePage(saved);
        if (count === this.#changeCount && !this.#saveTimer) this.#setStatus("saved");
      })
      .catch((error) => {
        if (error.status === 409 && /inzwischen geändert/.test(error.message)) {
          this.#setStatus("conflict");
          this.#showConflict(error.message);
        } else {
          // Local changes stay in the editor; "Erneut versuchen" sends them again.
          this.#setStatus("error");
          if (error.status === 400) showToast(error.message, "error");
        }
      })
      .finally(() => {
        this.#saving = null;
        this.#renderPageBar();
        if (this.#saveAgain && this.#status !== "conflict") {
          this.#saveAgain = false;
          this.#save();
        }
      });
    return this.#saving;
  }

  // Waits until everything is saved; false if saving failed.
  async #flush() {
    if (this.#themeTimer) await this.#saveTheme();
    while (this.#saveTimer || this.#saving) {
      if (this.#saveTimer) await this.#save();
      else await this.#saving;
      if (this.#status === "error" || this.#status === "conflict") return false;
    }
    return this.#status === "saved";
  }

  #showConflict(message) {
    this.querySelector("#builder-conflict-text").textContent = message;
    this.querySelector("#builder-conflict").open();
  }

  async #reloadAfterConflict() {
    this.querySelector("#builder-conflict").close();
    clearTimeout(this.#saveTimer);
    clearTimeout(this.#themeTimer);
    this.#saveTimer = null;
    this.#themeTimer = null;
    this.#status = "saved";
    this.#mount();
  }

  // ---- Theme -------------------------------------------------------------------------

  #renderDesignPanel() {
    this.querySelector("shop-design-panel").data = {
      draft: this.#themeDraft,
      fonts: this.#theme.fonts,
      draftChanged: this.#theme.draftChanged || JSON.stringify(this.#themeDraft) !== JSON.stringify(this.#theme.draft),
    };
  }

  #onThemeChange(theme) {
    this.#themeDraft = theme;
    clearTimeout(this.#themeTimer);
    this.#themeTimer = setTimeout(() => this.#saveTheme(), AUTOSAVE_MS);
    this.#renderDesignPanel();
    this.#sendPreview();
  }

  async #saveTheme() {
    clearTimeout(this.#themeTimer);
    this.#themeTimer = null;
    if (this.#themeSaving) await this.#themeSaving;
    const draft = this.#themeDraft;
    this.#themeSaving = apiFetch("/partner/shop/theme/draft", {
      method: "PUT",
      body: JSON.stringify({ theme: draft, version: this.#theme.version }),
    })
      .then((theme) => {
        this.#theme = theme;
        this.#renderDesignPanel();
        return true;
      })
      .catch((error) => {
        if (error.status === 409) this.#showConflict(error.message);
        else showToast(`Design nicht gespeichert: ${error.message}`, "error");
        return false;
      })
      .finally(() => {
        this.#themeSaving = null;
      });
    return this.#themeSaving;
  }

  async #publishTheme() {
    if (this.#themeTimer && !(await this.#saveTheme())) return;
    if (this.#themeSaving) await this.#themeSaving;
    try {
      this.#theme = await apiFetch("/partner/shop/theme/publish", {
        method: "POST",
        body: JSON.stringify({ version: this.#theme.version }),
      });
      this.#themeDraft = this.#theme.draft;
      this.#renderDesignPanel();
      showToast("Design veröffentlicht. Alle Online-Seiten zeigen jetzt die neuen Farben und Schriften.", "success");
    } catch (error) {
      if (error.status === 409) this.#showConflict(error.message);
      else showToast(error.message, "error");
    }
  }

  // ---- Preview -----------------------------------------------------------------------

  #onFrameMessage = (event) => {
    const frame = this.querySelector("#builder-frame");
    if (!frame || event.source !== frame.contentWindow || event.origin !== location.origin) return;
    if (event.data?.type === "papo-shop-preview-ready") {
      this.#frameReady = true;
      this.#sendPreview();
    } else if (event.data?.type === "papo-shop-select" && this.#blocks.some((block) => block.id === event.data.blockId)) {
      this.#select(event.data.blockId);
    }
  };

  // Navigation from the editor's own page list, so renamed or reordered pages show at once.
  #navigation() {
    const items = this.#pages.pages
      .filter((page) => page.online && page.slot !== "ARTICLE_DETAIL")
      .map((page) => ({ segment: page.segment, menuName: page.menuName, path: page.slot === "HOME" ? "" : page.segment }));
    return { ...this.#preview.navigation, items };
  }

  #sendPreview() {
    const frame = this.querySelector("#builder-frame");
    if (!this.#frameReady || !frame || !this.#page || !this.#preview) return;
    this.#loadAssetUrls();
    const page = this.#page;
    frame.contentWindow.postMessage({
      type: "papo-shop-preview",
      payload: {
        shop: this.#preview.shop,
        theme: this.#themeDraft,
        navigation: this.#navigation(),
        page: {
          menuName: page.menuName,
          path: page.slot === "HOME" ? "" : page.slot === "ARTICLE_DETAIL" ? "artikel" : page.segment,
          blocks: this.#blocks,
        },
        article: page.slot === "ARTICLE_DETAIL" ? this.#preview.articles[0] ?? null : null,
        articles: this.#preview.articles,
        assetUrls: this.#assetUrls,
        imageUrls: this.#imageUrls,
        selectedBlockId: this.#selectedId,
      },
    }, location.origin);
  }

  // Object URLs for the shop images used on the page; the preview is re-sent once loaded.
  #loadAssetUrls() {
    const missing = this.#blocks
      .map((block) => block.config?.imageId)
      .filter((id) => id && !(id in this.#assetUrls));
    for (const id of new Set(missing)) {
      this.#assetUrls[id] = "";
      assetObjectUrl(id)
        .then((url) => {
          this.#assetUrls[id] = url;
          this.#sendPreview();
        })
        .catch(() => {});
    }
  }
}

customElements.define("shop-builder", ShopBuilder);
