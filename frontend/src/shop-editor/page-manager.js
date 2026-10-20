import { apiFetch } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { showToast } from "../components/toast-notice.js";
import { suggestSegment } from "./block-schema.js";

// Page list of "Mein Webshop" (shop-builder spec "Page management", "SEO fields with
// explanation"): create, settings, navigation order by drag & drop or buttons, take offline,
// delete free pages, limits. The builder owns the page list; every server answer goes back
// to it as "pages-changed" ({ list } or { page }). "page-open" ({ id }) switches the editor.

const KIND_LABEL = { MANDATORY: "Pflichtseite", FREE: "Eigene Seite" };

class ShopPageManager extends HTMLElement {
  #list = null;
  // Set by the builder: waits until the open page is saved; resolves false if that failed.
  beforeChange = null;
  #dragId = null;
  #settingsPage = null;

  set list(value) {
    this.#list = value;
    this.#renderList();
  }

  connectedCallback() {
    this.innerHTML = `
      <div class="page-manager">
        <div class="page-manager__head">
          <p class="page-manager__limits" id="page-limits"></p>
          <button type="button" class="button-primary" data-action="new">Neue Seite</button>
        </div>
        <p class="builder-hint">Die Reihenfolge bestimmt das Menü des Shops. Ziehen Sie eine Seite an die gewünschte Stelle oder verwenden Sie die Pfeile.</p>
        <ul class="page-manager__list" id="page-list"></ul>
      </div>

      <modal-dialog title="Neue Seite" id="page-new-dialog">
        <form class="entity-form" id="page-new-form" novalidate>
          <label class="form-field"><span class="form-field__label">Seitentitel (Menüname)</span>
            <input name="menuName" maxlength="40" required />
          </label>
          <label class="form-field"><span class="form-field__label">Seitenadresse</span>
            <input name="segment" maxlength="60" pattern="[a-z0-9]+(-[a-z0-9]+)*" required />
          </label>
          <p class="builder-hint" id="page-new-url"></p>
          <fieldset class="page-manager__templates">
            <legend>Vorlage</legend>
            <label><input type="radio" name="template" value="leer" checked /> Leere Seite</label>
            <label><input type="radio" name="template" value="ueber-uns" /> Über uns</label>
            <label><input type="radio" name="template" value="kontakt" /> Kontakt</label>
          </fieldset>
          <p class="builder-error" id="page-new-error" role="alert" hidden></p>
          <div class="entity-form__actions">
            <button type="button" class="button-secondary" data-close>Abbrechen</button>
            <button type="submit" class="button-primary">Seite anlegen</button>
          </div>
        </form>
      </modal-dialog>

      <modal-dialog title="Seiteneinstellungen" id="page-settings-dialog">
        <form class="entity-form" id="page-settings-form" novalidate>
          <label class="form-field"><span class="form-field__label">Menüname</span>
            <input name="menuName" maxlength="40" required />
          </label>
          <label class="form-field"><span class="form-field__label">Seitenadresse</span>
            <input name="segment" maxlength="60" pattern="[a-z0-9]+(-[a-z0-9]+)*" />
          </label>
          <p class="builder-hint" id="page-settings-segment-hint"></p>
          <div class="seo-explainer">
            <h4>Gefunden werden (SEO)</h4>
            <p>Suchmaschinen wie Google zeigen den <strong>SEO-Titel</strong> als blaue Überschrift und die <strong>Beschreibung</strong> als kurzen Text darunter. Gute Angaben helfen Kundinnen und Kunden, Ihren Shop zu finden und zu wählen.</p>
            <p>Beides ist freiwillig. Solange Ihr Shop nicht als Live-Shop freigegeben ist, steht er auf „noindex“: Suchmaschinen nehmen ihn dann nicht auf.</p>
          </div>
          <label class="form-field"><span class="form-field__label">SEO-Titel <span class="builder-counter" data-counter="seoTitle"></span></span>
            <input name="seoTitle" maxlength="60" />
          </label>
          <label class="form-field"><span class="form-field__label">SEO-Beschreibung <span class="builder-counter" data-counter="seoDescription"></span></span>
            <textarea name="seoDescription" maxlength="160" rows="3"></textarea>
          </label>
          <p class="builder-error" id="page-settings-error" role="alert" hidden></p>
          <div class="entity-form__actions">
            <button type="button" class="button-secondary" data-close>Abbrechen</button>
            <button type="submit" class="button-primary">Speichern</button>
          </div>
        </form>
      </modal-dialog>

      <modal-dialog title="Seite löschen" id="page-delete-dialog">
        <p id="page-delete-text"></p>
        <div class="entity-form__actions">
          <button type="button" class="button-secondary" data-close>Abbrechen</button>
          <button type="button" class="button-primary button-danger" id="page-delete-confirm">Endgültig löschen</button>
        </div>
      </modal-dialog>`;

    this.querySelectorAll("[data-close]").forEach((button) =>
      button.addEventListener("click", () => button.closest("modal-dialog").close()));
    this.querySelector("#page-list").addEventListener("click", (event) => this.#onListClick(event));
    this.querySelector("[data-action=new]").addEventListener("click", () => this.#openNew());
    this.#bindNewForm();
    this.#bindSettingsForm();
    this.#bindDragAndDrop();
    this.#renderList();
  }

  #renderList() {
    const listEl = this.querySelector("#page-list");
    if (!listEl || !this.#list) return;
    const { pages, onlineCount, maxOnline, pageCount, maxPages } = this.#list;
    this.querySelector("#page-limits").innerHTML =
      `<strong>${onlineCount} von ${maxOnline}</strong> Seiten online · ${pageCount} von ${maxPages} Seiten insgesamt`;
    this.querySelector("[data-action=new]").disabled = pageCount >= maxPages;
    listEl.innerHTML = pages
      .map((page, index) => {
        // Plain status text, never only a colour.
        const state = page.online
          ? `<span class="page-state page-state--online">Online</span>${page.draftChanged ? '<span class="page-state">Änderungen noch nicht veröffentlicht</span>' : ""}`
          : '<span class="page-state page-state--draft">Entwurf – nicht online</span>';
        const free = page.kind === "FREE";
        return `<li class="page-row" draggable="true" data-page-id="${page.id}">
          <span class="page-row__handle" aria-hidden="true">⠿</span>
          <span class="page-row__main">
            <span class="page-row__name">${escapeHtml(page.menuName)}</span>
            <span class="page-row__meta">/${escapeHtml(page.slot === "HOME" ? "" : page.segment)} · ${KIND_LABEL[page.kind]}</span>
            <span class="page-row__state">${state}</span>
          </span>
          <span class="page-row__actions">
            <button type="button" class="builder-icon" data-action="up" aria-label="${escapeHtml(page.menuName)} nach oben"${index === 0 ? " disabled" : ""}>↑</button>
            <button type="button" class="builder-icon" data-action="down" aria-label="${escapeHtml(page.menuName)} nach unten"${index === pages.length - 1 ? " disabled" : ""}>↓</button>
            <button type="button" class="button-secondary" data-action="open">Bearbeiten</button>
            <button type="button" class="button-secondary" data-action="settings">Einstellungen</button>
            ${free && page.online ? '<button type="button" class="button-secondary" data-action="offline">Offline nehmen</button>' : ""}
            ${free ? '<button type="button" class="button-secondary" data-action="delete">Löschen</button>' : ""}
          </span>
        </li>`;
      })
      .join("");
  }

  #page(id) {
    return this.#list.pages.find((page) => page.id === id);
  }

  async #onListClick(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.closest("[data-page-id]").dataset.pageId);
    const page = this.#page(id);
    const ids = this.#list.pages.map((candidate) => candidate.id);
    const index = ids.indexOf(id);
    switch (button.dataset.action) {
      case "open":
        this.dispatchEvent(new CustomEvent("page-open", { detail: { id }, bubbles: true }));
        break;
      case "settings":
        this.#openSettings(page);
        break;
      case "up":
      case "down": {
        const target = button.dataset.action === "up" ? index - 1 : index + 1;
        [ids[index], ids[target]] = [ids[target], ids[index]];
        await this.#saveOrder(ids);
        break;
      }
      case "offline":
        if (!(await this.#saved(id))) break;
        await this.#call(`/partner/shop/pages/${id}/unpublish`, { method: "POST" }, `„${page.menuName}“ ist jetzt offline.`, true);
        break;
      case "delete":
        this.#openDelete(page);
        break;
      default:
    }
  }

  // Returns the response body (null for 204), or false after showing the error.
  async #call(path, options, message, reloadList = false) {
    try {
      const result = await apiFetch(path, options);
      if (message) showToast(message, "success");
      if (reloadList) {
        this.#changed({ list: await apiFetch("/partner/shop/pages") });
      }
      return result;
    } catch (error) {
      showToast(error.message, "error");
      return false;
    }
  }

  async #saved(pageId) {
    if (!this.beforeChange || (await this.beforeChange(pageId))) return true;
    showToast("Bitte zuerst die offenen Änderungen dieser Seite speichern.", "error");
    return false;
  }

  #changed(detail) {
    if (detail.list) this.list = detail.list;
    this.dispatchEvent(new CustomEvent("pages-changed", { detail, bubbles: true }));
  }

  async #saveOrder(ids) {
    const list = await this.#call("/partner/shop/pages/order", { method: "PUT", body: JSON.stringify({ pageIds: ids }) });
    if (list) this.#changed({ list });
    else this.list = this.#list;
  }

  #bindDragAndDrop() {
    const listEl = this.querySelector("#page-list");
    listEl.addEventListener("dragstart", (event) => {
      const row = event.target.closest("[data-page-id]");
      if (!row) return;
      this.#dragId = Number(row.dataset.pageId);
      row.classList.add("is-dragging");
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", String(this.#dragId));
    });
    listEl.addEventListener("dragend", (event) => {
      event.target.closest("[data-page-id]")?.classList.remove("is-dragging");
      listEl.querySelectorAll(".is-drop-target").forEach((row) => row.classList.remove("is-drop-target"));
    });
    listEl.addEventListener("dragover", (event) => {
      const row = event.target.closest("[data-page-id]");
      if (!row || this.#dragId === null) return;
      event.preventDefault();
      listEl.querySelectorAll(".is-drop-target").forEach((other) => other !== row && other.classList.remove("is-drop-target"));
      row.classList.add("is-drop-target");
    });
    listEl.addEventListener("drop", async (event) => {
      const row = event.target.closest("[data-page-id]");
      if (!row || this.#dragId === null) return;
      event.preventDefault();
      const dragged = this.#dragId;
      this.#dragId = null;
      const ids = this.#list.pages.map((page) => page.id);
      // The dropped page takes the target row's place: above it when moved up, below when moved down.
      const to = ids.indexOf(Number(row.dataset.pageId));
      if (to === ids.indexOf(dragged)) return;
      ids.splice(ids.indexOf(dragged), 1);
      ids.splice(to, 0, dragged);
      await this.#saveOrder(ids);
    });
  }

  #openNew() {
    const form = this.querySelector("#page-new-form");
    form.reset();
    delete form.segment.dataset.touched;
    this.querySelector("#page-new-error").hidden = true;
    this.querySelector("#page-new-url").textContent = "";
    this.querySelector("#page-new-dialog").open();
    form.menuName.focus();
  }

  #bindNewForm() {
    const form = this.querySelector("#page-new-form");
    form.menuName.addEventListener("input", () => {
      if (!form.segment.dataset.touched) form.segment.value = suggestSegment(form.menuName.value);
      this.#showUrl(form.segment.value);
    });
    form.segment.addEventListener("input", () => {
      form.segment.dataset.touched = "1";
      this.#showUrl(form.segment.value);
    });
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const error = this.querySelector("#page-new-error");
      try {
        const detail = await apiFetch("/partner/shop/pages", {
          method: "POST",
          body: JSON.stringify({
            menuName: form.menuName.value,
            segment: form.segment.value,
            template: form.template.value,
          }),
        });
        this.querySelector("#page-new-dialog").close();
        showToast(`Seite „${detail.page.menuName}“ angelegt.`, "success");
        this.#changed({ list: await apiFetch("/partner/shop/pages") });
        this.dispatchEvent(new CustomEvent("page-open", { detail: { id: detail.page.id }, bubbles: true }));
      } catch (failure) {
        error.textContent = failure.message;
        error.hidden = false;
      }
    });
  }

  #showUrl(segment) {
    this.querySelector("#page-new-url").textContent = segment ? `Adresse im Shop: …/${segment}` : "";
  }

  #openSettings(page) {
    this.#settingsPage = page;
    const form = this.querySelector("#page-settings-form");
    form.menuName.value = page.menuName;
    form.segment.value = page.segment;
    // Mandatory pages keep their address; the public routes depend on it.
    form.segment.disabled = page.kind !== "FREE";
    this.querySelector("#page-settings-segment-hint").textContent = page.kind === "FREE"
      ? "Nur Kleinbuchstaben, Ziffern und Bindestriche, z. B. ueber-uns."
      : "Die Adresse einer Pflichtseite ist fest.";
    form.seoTitle.value = page.seoTitle ?? "";
    form.seoDescription.value = page.seoDescription ?? "";
    this.#updateCounters(form);
    this.querySelector("#page-settings-error").hidden = true;
    this.querySelector("#page-settings-dialog").open();
  }

  #updateCounters(form) {
    for (const name of ["seoTitle", "seoDescription"]) {
      this.querySelector(`[data-counter="${name}"]`).textContent = `${form[name].value.length} / ${form[name].maxLength}`;
    }
  }

  #bindSettingsForm() {
    const form = this.querySelector("#page-settings-form");
    form.addEventListener("input", () => this.#updateCounters(form));
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const error = this.querySelector("#page-settings-error");
      if (!(await this.#saved(this.#settingsPage.id))) {
        error.textContent = "Bitte zuerst die offenen Änderungen dieser Seite speichern.";
        error.hidden = false;
        return;
      }
      // Fresh version: the builder may have autosaved this page since the list was drawn.
      const page = this.#page(this.#settingsPage.id);
      try {
        const saved = await apiFetch(`/partner/shop/pages/${page.id}/settings`, {
          method: "PUT",
          body: JSON.stringify({
            menuName: form.menuName.value,
            segment: form.segment.disabled ? null : form.segment.value,
            seoTitle: form.seoTitle.value,
            seoDescription: form.seoDescription.value,
            version: page.version,
          }),
        });
        this.querySelector("#page-settings-dialog").close();
        showToast("Einstellungen gespeichert.", "success");
        this.#changed({ page: saved });
      } catch (failure) {
        error.textContent = failure.message;
        error.hidden = false;
      }
    });
  }

  #openDelete(page) {
    this.querySelector("#page-delete-text").textContent =
      `Die Seite „${page.menuName}“ wird gelöscht${page.online ? " und verschwindet sofort aus dem öffentlichen Shop" : ""}. Das kann nicht rückgängig gemacht werden.`;
    const confirm = this.querySelector("#page-delete-confirm");
    confirm.onclick = async () => {
      confirm.disabled = true;
      const done = await this.#call(`/partner/shop/pages/${page.id}`, { method: "DELETE" }, `„${page.menuName}“ wurde gelöscht.`, true);
      confirm.disabled = false;
      this.querySelector("#page-delete-dialog").close();
      if (done !== false) this.dispatchEvent(new CustomEvent("page-deleted", { detail: { id: page.id }, bubbles: true }));
    };
    this.querySelector("#page-delete-dialog").open();
  }
}

customElements.define("shop-page-manager", ShopPageManager);
