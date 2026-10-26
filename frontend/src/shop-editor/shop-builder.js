import { apiFetch } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { showToast } from "../components/toast-notice.js";
import { BLOCK_TYPES, missingRequired, newBlock } from "./block-schema.js";
import { EditorHistory } from "./editor-history.js";
import { assetObjectUrl } from "./asset-library.js";
import { articleImageUrls, imageIds, postPreview, previewPayload } from "./preview-payload.js";
import "./asset-library.js";
import { BLOCK_DRAG_TYPE } from "./block-palette.js";
import "./block-properties.js";
import "./design-panel.js";
import "./page-manager.js";
import "./template-picker.js";
import "./setup-dialog.js";
import "./demo-viewer.js";
import { stateHtml } from "../lib/states.js";

// Shop builder (shop-builder spec), opened full screen from "Mein Webshop"
// (#/partner/shop/editor[?page=<id>]): block palette, live preview in an
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

const BACK_BAR = `<header class="builder-header">
  <a class="builder-back" href="#/partner/shop" data-action="back"><i data-lucide="arrow-left"></i><span>Zurück</span></a>
  <h1 class="builder-header__title">Shop bearbeiten</h1>
</header>`;

// Page to open first: ?page=<id> from a thumbnail on "Mein Webshop".
function requestedPageId() {
  const query = location.hash.split("?")[1] ?? "";
  return Number(new URLSearchParams(query).get("page")) || null;
}

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
  #templates = [];
  #pickField = null;
  #collapsedSides = { left: false, right: false };
  #small = null;
  #screenTimer = null;

  #saveTimer = null;
  #saving = null;
  #saveAgain = false;
  #changeCount = 0;
  #status = "saved";
  #themeTimer = null;
  #themeFailed = false;
  // Bumped when pending saves of a page are discarded (page deleted); late answers are ignored.
  #saveGeneration = 0;
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
    if (this.#saveTimer || this.#saving || this.#status === "error" || this.#themeTimer || this.#themeFailed) {
      event.preventDefault();
    }
  };

  #mount() {
    this.#small = SMALL_SCREEN.matches;
    if (this.#small) {
      this.#frameReady = false;
      this.#renderSmallScreenHint();
    } else {
      this.#renderLayout();
      this.#load();
    }
    this.querySelector("[data-action=back]").addEventListener("click", (event) => this.#back(event));
    window.lucide?.createIcons();
  }

  // Back to "Mein Webshop" only once everything is saved; otherwise stay with the message.
  async #back(event) {
    event.preventDefault();
    if (!(await this.#flush())) {
      showToast("Ihre letzten Änderungen sind noch nicht gespeichert. Bitte „Erneut versuchen“ wählen.", "error");
      return;
    }
    location.hash = "#/partner/shop";
  }

  // shop-builder spec "Device support": below 768 px only a hint and the public link.
  #renderSmallScreenHint() {
    const link = this.#shop ? `${location.origin}${this.#shop.publicPath}` : "";
    this.innerHTML = `${BACK_BAR}<section class="builder-small">
      <p><strong>Der Shop-Editor ist für Computer und Tablet gedacht.</strong></p>
      <p>Öffnen Sie „Mein Webshop“ auf einem größeren Bildschirm, um Ihren Shop zu gestalten.</p>
      ${link ? `<p>Ihr Shop: <a href="${escapeHtml(link)}" target="_blank" rel="noopener">${escapeHtml(link)}</a></p>` : ""}
    </section>`;
  }

  #renderLayout() {
    this.#frameReady = false;
    // Row 1: back, editor tabs, preview size. Row 2 (page bar): only for "Seite bearbeiten".
    this.innerHTML = `
      <header class="builder-header">
        <a class="builder-back" href="#/partner/shop" data-action="back"><i data-lucide="arrow-left"></i><span>Zurück</span></a>
        <div class="builder-tabs" role="tablist" aria-label="Shop bearbeiten">
          <button type="button" role="tab" data-tab="edit" aria-selected="true">Seite bearbeiten</button>
          <button type="button" role="tab" data-tab="pages" aria-selected="false">Seiten</button>
          <button type="button" role="tab" data-tab="design" aria-selected="false">Design</button>
          <button type="button" role="tab" data-tab="images" aria-selected="false">Bilder</button>
        </div>
        <span class="builder-header__spacer"></span>
        <div class="builder-device" role="group" aria-label="Vorschaugröße">
          <button type="button" data-device="desktop" aria-pressed="true">Computer</button>
          <button type="button" data-device="mobile" aria-pressed="false">Handy</button>
        </div>
      </header>
      <div class="builder" data-tab="edit">

        <div class="builder-pagebar">
          <label class="builder-pagebar__select">Seite
            <select id="builder-page-select"></select>
          </label>
          <span class="builder-pagebar__state" id="builder-page-state"></span>
          <span class="builder-save" id="builder-save" aria-live="polite"></span>
          <button type="button" class="button-secondary" id="builder-retry" hidden>Erneut versuchen</button>
          <span class="builder-pagebar__spacer"></span>
          <button type="button" class="button-secondary" id="builder-template">Vorlage wählen</button>
          <button type="button" class="builder-icon" id="builder-undo" title="Rückgängig (Strg+Z)" aria-label="Rückgängig" disabled>↶</button>
          <button type="button" class="builder-icon" id="builder-redo" title="Wiederholen (Strg+Umschalt+Z)" aria-label="Wiederholen" disabled>↷</button>
          <button type="button" class="button-secondary" id="builder-restore" title="Vorherige veröffentlichte Version wiederherstellen" hidden>Wiederherstellen</button>
          <button type="button" class="button-primary" id="builder-publish">Seite veröffentlichen</button>
        </div>

        <div class="builder-body">
          <aside class="builder-left" aria-label="Seitenleiste">
            <button type="button" class="builder-collapse" data-collapse="left"></button>
            <div data-panel="edit">
              <div class="side-tabs" role="tablist" aria-label="Abschnitte">
                <button type="button" role="tab" data-side="add" aria-selected="true">Hinzufügen</button>
                <button type="button" role="tab" data-side="blocks" aria-selected="false">Abschnitte <span class="side-tabs__count" id="builder-block-count"></span></button>
              </div>
              <div data-side-panel="add">
                <p class="builder-hint">Klicken Sie auf einen Abschnitt oder ziehen Sie ihn an die gewünschte Stelle in der Vorschau.</p>
                <shop-block-palette></shop-block-palette>
              </div>
              <div data-side-panel="blocks" hidden>
                <ol class="block-list" id="builder-blocks"></ol>
              </div>
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
              <div class="builder-frame__bar" aria-hidden="true">
                <span class="builder-frame__dots"><i></i><i></i><i></i></span>
                <span class="builder-frame__url" id="builder-frame-url"></span>
              </div>
              <div class="builder-frame__screen">
                <iframe id="builder-frame" title="Vorschau des Shops" src="/shop.html?preview=1"></iframe>
                <div class="builder-drop-layer" hidden><span class="builder-drop-line" hidden></span></div>
              </div>
            </div>
          </section>
          <aside class="builder-right" aria-label="Eigenschaften">
            <button type="button" class="builder-collapse" data-collapse="right"></button>
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

        <shop-template-picker></shop-template-picker>
        <shop-setup-dialog></shop-setup-dialog>
        <shop-demo-viewer></shop-demo-viewer>

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
    this.querySelector("#builder-template").addEventListener("click", () => this.querySelector("shop-template-picker").open(this.#page));
    this.addEventListener("template-apply", (event) => this.#applyTemplate(event.detail));
    this.addEventListener("shop-setup", (event) => this.#setup(event.detail.style));
    this.addEventListener("template-demo", (event) => this.#openDemo(event.detail));
    this.querySelector("#builder-undo").addEventListener("click", () => this.#undo());
    this.querySelector("#builder-redo").addEventListener("click", () => this.#redo());
    this.querySelector("#builder-publish").addEventListener("click", () => this.#publish());
    this.querySelector("#builder-restore").addEventListener("click", () => this.querySelector("#builder-restore-dialog").open());
    this.querySelector("#builder-restore-confirm").addEventListener("click", () => this.#restore());
    this.querySelector("#builder-conflict-reload").addEventListener("click", () => this.#reloadAfterConflict());
    this.querySelectorAll("[data-close]").forEach((button) =>
      button.addEventListener("click", () => button.closest("modal-dialog").close()));

    this.addEventListener("palette-add", (event) => this.#addBlock(event.detail.type));
    // Shown after dragstart returns: changing the layout inside dragstart can cancel the drag.
    this.addEventListener("palette-drag", () => setTimeout(() => {
      this.querySelector(".builder-drop-layer").hidden = false;
    }));
    this.addEventListener("palette-drag-end", () => this.#endDrop());
    this.#bindDropLayer(this.querySelector(".builder-drop-layer"));
    this.querySelector(".side-tabs").addEventListener("click", (event) => {
      const side = event.target.closest("[data-side]")?.dataset.side;
      if (side) this.#showSideTab(side);
    });
    this.querySelectorAll("[data-collapse]").forEach((button) =>
      button.addEventListener("click", () => this.#toggleCollapse(button.dataset.collapse)));
    this.#applyCollapse();
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
    // The open page was deleted: its pending changes are gone with it, so nothing is flushed.
    this.addEventListener("page-deleted", (event) => {
      if (event.detail.id !== this.#page?.id) return;
      this.#saveGeneration += 1;
      clearTimeout(this.#saveTimer);
      this.#saveTimer = null;
      this.#saveAgain = false;
      this.#setStatus("saved");
      this.#openPage(this.#pages.pages[0].id, { skipFlush: true });
    });
    // Page settings and "Offline nehmen" wait for a running autosave of the same page, so the
    // partner's own action never shows up as a conflict.
    this.querySelector("shop-page-manager").beforeChange = (pageId) =>
      (pageId === this.#page?.id ? this.#flush() : Promise.resolve(true));
    this.addEventListener("theme-change", (event) => this.#onThemeChange(event.detail.theme));
    this.addEventListener("theme-publish", () => this.#publishTheme());
  }

  async #load() {
    try {
      const [pages, theme, preview, templates] = await Promise.all([
        apiFetch("/partner/shop/pages"),
        apiFetch("/partner/shop/theme"),
        apiFetch("/partner/shop/preview"),
        apiFetch("/partner/shop/templates"),
      ]);
      this.#pages = pages;
      this.#theme = theme;
      this.#themeDraft = theme.draft;
      this.#preview = preview;
      this.querySelector("shop-page-manager").list = pages;
      this.querySelector("shop-page-manager").templates = templates;
      this.querySelector("shop-template-picker").templates = templates;
      this.#templates = templates;
      this.#renderDesignPanel();
      const start = pages.pages.find((page) => page.id === requestedPageId()) ?? pages.pages[0];
      await this.#openPage(start.id, { skipFlush: true });
      this.#resolveArticleImages();
      if (pages.setupNeeded) this.querySelector("shop-setup-dialog").open(templates);
    } catch (error) {
      const body = this.querySelector(".builder-body");
      body.innerHTML = stateHtml("error", "Der Editor konnte nicht geladen werden.", {
        hint: error.message,
        action: `<button type="button" class="button-secondary" data-builder-retry>Erneut laden</button>`,
      });
      body.querySelector("[data-builder-retry]").addEventListener("click", () => location.reload());
      window.lucide?.createIcons();
    }
  }

  async #resolveArticleImages() {
    this.#imageUrls = await articleImageUrls(this.#preview.articles);
    this.#sendPreview();
  }

  #showTab(tab) {
    this.#tab = tab;
    this.querySelector(".builder").dataset.tab = tab;
    // Preview size only matters where a preview is shown.
    this.querySelector(".builder-header .builder-device").hidden = tab === "pages" || tab === "images";
    this.querySelectorAll(".builder-tabs [data-tab]").forEach((button) =>
      button.setAttribute("aria-selected", String(button.dataset.tab === tab)));
    this.querySelectorAll("[data-panel]").forEach((panel) => {
      panel.hidden = panel.dataset.panel !== tab;
    });
    if (tab === "images") this.querySelector("[data-panel=images] shop-asset-library").load();
    this.#sendPreview();
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
      ? changed ? "Online · Änderungen offen" : "Online · aktuell"
      : "Entwurf · nicht online";
    this.querySelector("#builder-restore").hidden = !page.online || !page.restorable;
    // Address line of the preview window, as the page will appear publicly.
    const path = page.slot === "HOME" ? "" : page.slot === "ARTICLE_DETAIL" ? "/artikel/…" : `/${page.segment}`;
    this.querySelector("#builder-frame-url").textContent = `${location.host}/shop/${this.#preview?.shop.kennung ?? ""}${path}`;
    this.querySelector("#builder-publish").textContent = "Veröffentlichen";
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

  // ---- Sidebars ----------------------------------------------------------------------

  #showSideTab(side) {
    this.querySelectorAll(".side-tabs [data-side]").forEach((button) =>
      button.setAttribute("aria-selected", String(button.dataset.side === side)));
    this.querySelectorAll("[data-side-panel]").forEach((panel) => {
      panel.hidden = panel.dataset.sidePanel !== side;
    });
  }

  // Both sidebars start expanded each time the editor opens and only the buttons change that.
  #toggleCollapse(side) {
    this.#collapsedSides[side] = !this.#collapsedSides[side];
    this.#applyCollapse();
  }

  #applyCollapse(state = this.#collapsedSides) {
    const builder = this.querySelector(".builder");
    for (const side of ["left", "right"]) {
      const collapsed = Boolean(state[side]);
      builder.toggleAttribute(`data-${side}-collapsed`, collapsed);
      const button = this.querySelector(`[data-collapse="${side}"]`);
      const label = side === "left" ? "Seitenleiste" : "Eigenschaften";
      const icon = side === "left" ? (collapsed ? "panel-left-open" : "panel-left-close") : (collapsed ? "panel-right-open" : "panel-right-close");
      button.setAttribute("aria-expanded", String(!collapsed));
      button.setAttribute("aria-label", `${label} ${collapsed ? "ausklappen" : "einklappen"}`);
      button.title = button.getAttribute("aria-label");
      button.innerHTML = `<i data-lucide="${icon}"></i>`;
    }
    window.lucide?.createIcons();
  }

  // ---- Drag from the palette into the preview -----------------------------------------

  // The layer covers the preview while a palette entry is dragged; the iframe is same-origin,
  // so the insertion point comes from the rendered blocks' positions.
  #bindDropLayer(layer) {
    const line = layer.querySelector(".builder-drop-line");
    layer.addEventListener("dragover", (event) => {
      if (!event.dataTransfer.types.includes(BLOCK_DRAG_TYPE)) return;
      event.preventDefault();
      event.dataTransfer.dropEffect = "copy";
      const { y } = this.#dropTarget(event.clientY);
      line.style.top = `${y}px`;
      line.hidden = false;
    });
    layer.addEventListener("dragleave", (event) => {
      if (event.target === layer) line.hidden = true;
    });
    layer.addEventListener("drop", (event) => {
      const type = event.dataTransfer.getData(BLOCK_DRAG_TYPE);
      if (!type || !BLOCK_TYPES[type]) return;
      event.preventDefault();
      const { index } = this.#dropTarget(event.clientY);
      this.#endDrop();
      this.#addBlock(type, index);
    });
  }

  #endDrop() {
    const layer = this.querySelector(".builder-drop-layer");
    if (!layer) return;
    layer.hidden = true;
    layer.querySelector(".builder-drop-line").hidden = true;
  }

  // index in the draft where the block goes, y of the insertion line in the frame.
  #dropTarget(clientY) {
    const frame = this.querySelector("#builder-frame");
    const doc = frame.contentDocument;
    const frameTop = frame.getBoundingClientRect().top;
    const y = clientY - frameTop;
    // Scroll the preview when dragging near its upper or lower edge.
    if (y < 48) frame.contentWindow.scrollBy(0, -24);
    else if (y > frame.clientHeight - 48) frame.contentWindow.scrollBy(0, 24);
    const rendered = [...(doc?.querySelectorAll("shop-page-view > [data-block-id]") ?? [])];
    for (const element of rendered) {
      const rect = element.getBoundingClientRect();
      if (y < rect.top + rect.height / 2) {
        const index = this.#blocks.findIndex((block) => block.id === element.dataset.blockId);
        return { index, y: rect.top };
      }
    }
    const last = rendered.at(-1)?.getBoundingClientRect();
    const main = doc?.querySelector(".shop-main")?.getBoundingClientRect();
    return { index: this.#blocks.length, y: last ? last.bottom : main?.top ?? 0 };
  }

  #renderBlockList() {
    const list = this.querySelector("#builder-blocks");
    this.querySelector("#builder-block-count").textContent = `(${this.#blocks.length})`;
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
    if (button.dataset.action === "select") this.#select(id);
    else this.#blockAction(button.dataset.action, id);
  }

  // Shared by the block list and the toolbar in the preview. value: target index for "move",
  // option for "size" (field = the layout field).
  #blockAction(action, id, { field, value } = {}) {
    const index = this.#blocks.findIndex((block) => block.id === id);
    if (index === -1) return;
    const block = this.#blocks[index];
    const fixed = BLOCK_TYPES[block.type]?.fixed;
    switch (action) {
      case "up":
        if (index > 0) this.#applyBlocks(move(this.#blocks, index, index - 1));
        break;
      case "down":
        if (index < this.#blocks.length - 1) this.#applyBlocks(move(this.#blocks, index, index + 1));
        break;
      case "move": {
        // value = id of the block it goes in front of (null = end); ids, because the preview
        // may skip blocks it cannot render, so its positions are not draft positions.
        const before = value ? this.#blocks.findIndex((candidate) => candidate.id === value) : this.#blocks.length;
        if (before === -1) break;
        const to = before > index ? before - 1 : before;
        if (to !== index) this.#applyBlocks(move(this.#blocks, index, to));
        break;
      }
      case "size":
        if (["spacing", "width", "size", "columns"].includes(field)) {
          this.#updateBlock(id, { ...block.config, [field]: value });
        }
        break;
      case "duplicate": {
        if (fixed) break;
        const copy = { ...structuredClone(block), id: crypto.randomUUID() };
        const blocks = [...this.#blocks];
        blocks.splice(index + 1, 0, copy);
        this.#selectedId = copy.id;
        this.#applyBlocks(blocks);
        break;
      }
      case "delete":
        if (fixed) break;
        if (this.#selectedId === id) this.#selectedId = null;
        this.#applyBlocks(this.#blocks.filter((candidate) => candidate.id !== id));
        showToast("Abschnitt gelöscht. „Rückgängig“ holt ihn zurück.", "info");
        break;
      default:
    }
  }

  // Text typed directly in the preview. The preview already shows it; the draft follows.
  #inlineEdit(id, field, value) {
    const block = this.#blocks.find((candidate) => candidate.id === id);
    const schema = BLOCK_TYPES[block?.type]?.fields.find((candidate) => candidate.name === field);
    if (!schema || schema.kind !== "text" || typeof value !== "string") return;
    this.#updateBlock(id, { ...block.config, [field]: value.slice(0, schema.maxLength) }, `${id}:${field}`);
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

  // index: from a drop into the preview; a click inserts after the selected block.
  #addBlock(type, index = null) {
    const block = newBlock(type);
    const blocks = [...this.#blocks];
    const selected = this.#blocks.findIndex((candidate) => candidate.id === this.#selectedId);
    const at = index ?? (selected === -1 ? blocks.length : selected + 1);
    blocks.splice(at, 0, block);
    this.#selectedId = block.id;
    this.#applyBlocks(blocks);
  }

  // Demo shop for one template (shop-templates "Demo shop per template"). A mandatory-page
  // template shows its whole style ("<page>-<style>" for all three pages, starting on its own
  // page); a free-page template shows that page in the current design. Uses the partner's
  // articles; nothing is saved.
  #openDemo({ template, choose }) {
    const withIds = (blocks) => blocks.map((block, index) => ({ ...structuredClone(block), id: `demo-${index}` }));
    const articles = this.#preview.articles;
    const pages = {};
    let items;
    let theme = this.#themeDraft;
    let start = "";
    if (template.page === "frei") {
      pages.seite = { menuName: template.name, blocks: withIds(template.blocks) };
      items = [{ segment: "seite", menuName: template.name, path: "seite" }];
      start = "seite";
    } else {
      const style = template.id.slice(template.page.length + 1);
      const ofStyle = (page) => this.#templates.find((candidate) => candidate.id === `${page}-${style}`);
      for (const [page, key, menuName] of [["startseite", "", "Startseite"], ["artikel", "artikel", "Artikel"],
        ["artikeldetail", "artikeldetail", "Artikeldetail"]]) {
        const found = ofStyle(page);
        if (found) pages[key] = { menuName, blocks: withIds(found.blocks) };
      }
      items = [{ segment: "startseite", menuName: "Startseite", path: "" }, { segment: "artikel", menuName: "Artikel", path: "artikel" }]
        .filter((item) => item.path in pages);
      theme = ofStyle("startseite")?.theme ?? template.theme ?? theme;
      if (template.page === "artikel") start = "artikel";
      if (template.page === "artikeldetail") start = `artikel/${articles[0]?.id ?? ""}`;
    }
    this.querySelector("shop-demo-viewer").open({
      title: template.name,
      choose,
      payload: {
        shop: this.#preview.shop,
        theme,
        navigation: { items, legal: [], sellerName: this.#preview.navigation.sellerName },
        pages,
        start,
        articles,
        imageUrls: this.#imageUrls,
        assetUrls: {},
      },
    });
  }

  // First-time setup: the server sets pages and design, then the editor reloads everything.
  async #setup(style) {
    const dialog = this.querySelector("shop-setup-dialog");
    try {
      await apiFetch("/partner/shop/setup", { method: "POST", body: JSON.stringify({ style }) });
      dialog.close();
      if (style) showToast("Ihr Shop ist eingerichtet. Passen Sie die Seiten jetzt nach Ihren Wünschen an.", "success");
      this.#mount();
    } catch (error) {
      showToast(error.message, "error");
      dialog.failed();
    }
  }

  // Replaces the draft blocks as one undo step; the theme suggestion only goes into the design
  // draft when the partner ticked "Farben und Schriften übernehmen", and is then undone with
  // the blocks. Nothing is published.
  #applyTemplate({ template, applyTheme }) {
    this.#selectedId = null;
    const blocks = template.blocks.map((block) => ({ ...structuredClone(block), id: crypto.randomUUID() }));
    if (applyTheme) {
      this.#history.record(JSON.stringify({ blocks: this.#blocks, theme: this.#themeDraft }));
      this.#blocks = blocks;
      this.#changed();
      this.#onThemeChange(structuredClone(template.theme));
    } else {
      this.#applyBlocks(blocks);
    }
    showToast(applyTheme
      ? `Vorlage „${template.name}“ mit Farben und Schriften übernommen.`
      : `Vorlage „${template.name}“ übernommen.`, "success");
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
    const snapshot = this.#history.undo(this.#snapshot(this.#history.nextUndo));
    if (snapshot !== null) this.#restoreSnapshot(snapshot);
  }

  #redo() {
    const snapshot = this.#history.redo(this.#snapshot(this.#history.nextRedo));
    if (snapshot !== null) this.#restoreSnapshot(snapshot);
  }

  // Snapshots are the block list ("[...]"); a template applied with its theme is one step for
  // blocks and theme ("{blocks, theme}"), so the current state is taken in the same shape.
  #snapshot(counterpart = null) {
    return JSON.stringify(counterpart?.startsWith("{") ? { blocks: this.#blocks, theme: this.#themeDraft } : this.#blocks);
  }

  #restoreSnapshot(snapshot) {
    const state = JSON.parse(snapshot);
    this.#blocks = Array.isArray(state) ? state : state.blocks;
    this.#changed();
    if (!Array.isArray(state)) this.#onThemeChange(state.theme);
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
    const generation = this.#saveGeneration;
    this.#setStatus("saving");
    this.#saving = apiFetch(`/partner/shop/pages/${page.id}/draft`, {
      method: "PUT",
      body: JSON.stringify({ content: { blocks: this.#blocks }, version: page.version }),
    })
      .then((saved) => {
        if (generation !== this.#saveGeneration) return;
        this.#replacePage(saved);
        if (count === this.#changeCount && !this.#saveTimer) this.#setStatus("saved");
      })
      .catch((error) => {
        if (generation !== this.#saveGeneration) return;
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

  // Waits until everything is saved; false if saving failed. A failed theme save is retried
  // once here, so "Zurück" never leaves unsaved design changes behind.
  async #flush() {
    if (this.#themeTimer || this.#themeFailed) {
      if (!(await this.#saveTheme())) return false;
    } else if (this.#themeSaving && !(await this.#themeSaving)) {
      return false;
    }
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
        this.#themeFailed = false;
        this.#renderDesignPanel();
        return true;
      })
      .catch((error) => {
        this.#themeFailed = true;
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
      if (event.data.blockId !== this.#selectedId) this.#select(event.data.blockId);
    } else if (event.data?.type === "papo-shop-edit") {
      this.#inlineEdit(event.data.blockId, event.data.field, event.data.value);
    } else if (event.data?.type === "papo-shop-action") {
      this.#blockAction(event.data.action, event.data.blockId, event.data);
    }
  };

  #sendPreview() {
    const palette = this.querySelector("shop-block-palette");
    if (palette && this.#preview && this.#pages) {
      palette.context = { preview: this.#preview, theme: this.#themeDraft, pages: this.#pages.pages, imageUrls: this.#imageUrls };
    }
    const frame = this.querySelector("#builder-frame");
    if (!this.#frameReady || !frame || !this.#page || !this.#preview) return;
    this.#loadAssetUrls();
    postPreview(frame, previewPayload({
      preview: this.#preview,
      theme: this.#themeDraft,
      pages: this.#pages.pages,
      page: this.#page,
      blocks: this.#blocks,
      assetUrls: this.#assetUrls,
      imageUrls: this.#imageUrls,
      // Selection and block toolbar belong to "Seite bearbeiten" only.
      selectedBlockId: this.#tab === "edit" ? this.#selectedId : null,
    }));
  }

  // Object URLs for the shop images used on the page; the preview is re-sent once loaded.
  #loadAssetUrls() {
    for (const id of imageIds(this.#blocks).filter((candidate) => !(candidate in this.#assetUrls))) {
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
