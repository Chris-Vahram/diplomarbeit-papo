import { apiFetch, apiFetchForm } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { resolveImageUrl } from "../lib/product-images.js";
import { showToast } from "./toast-notice.js";

// <image-chooser name="imageUrl"></image-chooser>
// Lets the company set a product image by uploading a file, picking a previously
// uploaded image from the gallery, or pasting an external URL. Renders a real
// <input type="hidden" name="..."> in its light DOM so it participates in FormData
// like <form-field> (form-field.js) - `form.elements.<name>` resolves to that inner
// input, but reading/writing the *resolved* value with preview updates must go
// through the .value getter/setter on <image-chooser> itself, not the raw input.
// Call .reset() to clear the chooser (form.reset() alone only clears the raw input).
class ImageChooser extends HTMLElement {
  connectedCallback() {
    const name = this.getAttribute("name") ?? "";
    this._gallery = null;

    this.innerHTML = `
      <div class="image-chooser">
        <span class="form-field__label">Bild</span>
        <input type="hidden" name="${escapeHtml(name)}" />
        <div class="image-chooser__body">
          <span class="data-table__thumb data-table__thumb--empty image-chooser__preview" data-preview></span>
          <div class="image-chooser__controls">
            <div class="image-chooser__tabs" role="tablist">
              <button type="button" data-mode="upload" class="image-chooser__tab image-chooser__tab--active">Hochladen</button>
              <button type="button" data-mode="gallery" class="image-chooser__tab">Aus Galerie wählen</button>
              <button type="button" data-mode="url" class="image-chooser__tab">Externe URL</button>
            </div>
            <div class="image-chooser__panel" data-panel="upload">
              <input type="file" accept="image/png,image/jpeg,image/webp" data-upload-input />
              <p class="image-chooser__hint">PNG, JPEG oder WEBP, maximal 2 MB.</p>
            </div>
            <div class="image-chooser__panel" data-panel="gallery" hidden>
              <div class="image-chooser__gallery" data-gallery></div>
            </div>
            <div class="image-chooser__panel" data-panel="url" hidden>
              <input type="text" placeholder="https://…" data-url-input />
            </div>
          </div>
        </div>
      </div>
    `;

    this._hiddenInput = this.querySelector("input[type=hidden]");
    this._urlInput = this.querySelector("[data-url-input]");

    this.querySelectorAll("[data-mode]").forEach((btn) =>
      btn.addEventListener("click", () => this._switchMode(btn.dataset.mode)),
    );
    this.querySelector("[data-upload-input]").addEventListener("change", (event) => this._handleUpload(event));
    this._urlInput.addEventListener("input", () => this._setValue(this._urlInput.value.trim()));
  }

  get value() {
    return this._hiddenInput.value;
  }

  set value(newValue) {
    this._setValue(newValue ?? "", { fromExternal: true });
  }

  reset() {
    this._gallery = null;
    this._urlInput.value = "";
    this._switchMode("upload");
    this._setValue("");
  }

  async _setValue(value, { fromExternal = false } = {}) {
    this._hiddenInput.value = value;
    if (fromExternal) {
      this._urlInput.value = value.startsWith("/product-images/") ? "" : value;
    }
    await this._updatePreview(value);
  }

  async _updatePreview(value) {
    const preview = this.querySelector("[data-preview]");
    if (!value) {
      preview.outerHTML = `<span class="data-table__thumb data-table__thumb--empty image-chooser__preview" data-preview></span>`;
      return;
    }
    const src = await resolveImageUrl(value);
    // Re-query: awaiting resolveImageUrl may run after a newer _updatePreview call
    // already replaced the node: apply to whatever [data-preview] currently is.
    const current = this.querySelector("[data-preview]");
    if (current) current.outerHTML = `<img class="data-table__thumb image-chooser__preview" src="${escapeHtml(src)}" alt="" data-preview />`;
  }

  _switchMode(mode) {
    this.querySelectorAll("[data-mode]").forEach((btn) =>
      btn.classList.toggle("image-chooser__tab--active", btn.dataset.mode === mode),
    );
    this.querySelectorAll("[data-panel]").forEach((panel) => {
      panel.hidden = panel.dataset.panel !== mode;
    });
    if (mode === "gallery" && !this._gallery) this._loadGallery();
  }

  async _handleUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    try {
      const result = await apiFetchForm("/product-images", formData);
      await this._setValue(result.url);
      showToast("Bild hochgeladen.", "success");
      this._gallery = null;
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      event.target.value = "";
    }
  }

  async _loadGallery() {
    const container = this.querySelector("[data-gallery]");
    container.innerHTML = `<p class="image-chooser__hint">Galerie wird geladen…</p>`;
    try {
      this._gallery = await apiFetch("/product-images");
      if (this._gallery.length === 0) {
        container.innerHTML = `<p class="image-chooser__hint">Noch keine Bilder hochgeladen.</p>`;
        return;
      }
      const items = await Promise.all(
        this._gallery.map(async (image) => {
          const src = await resolveImageUrl(image.url);
          return `<button type="button" class="image-chooser__gallery-item" data-gallery-url="${escapeHtml(image.url)}" title="${escapeHtml(image.filename)}">
            <img src="${escapeHtml(src)}" alt="${escapeHtml(image.filename)}" />
          </button>`;
        }),
      );
      container.innerHTML = items.join("");
      container.querySelectorAll("[data-gallery-url]").forEach((btn) =>
        btn.addEventListener("click", () => this._setValue(btn.dataset.galleryUrl)),
      );
    } catch (error) {
      container.innerHTML = `<p class="image-chooser__hint">Galerie konnte nicht geladen werden.</p>`;
      showToast(error.message, "error");
    }
  }
}

customElements.define("image-chooser", ImageChooser);
