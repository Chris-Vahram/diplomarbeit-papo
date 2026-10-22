import { apiFetch, apiFetchBlob, apiFetchForm } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { showToast } from "../components/toast-notice.js";

// Shop image library (shop-assets spec): upload with rights confirmation, storage use,
// list and delete. mode="pick" adds "Auswählen" and dispatches "asset-pick" ({ id }).
// Images load with the partner's login, so <img> gets object URLs (as lib/product-images.js).

const objectUrls = new Map();

export async function assetObjectUrl(id) {
  if (!objectUrls.has(id)) {
    objectUrls.set(id, apiFetchBlob(`/partner/shop/assets/${id}`).then((blob) => URL.createObjectURL(blob)));
  }
  try {
    return await objectUrls.get(id);
  } catch (error) {
    objectUrls.delete(id);
    throw error;
  }
}

const sizeFormat = new Intl.NumberFormat("de-AT", { maximumFractionDigits: 1 });

export function formatMegabytes(bytes) {
  const megabytes = bytes / (1024 * 1024);
  // A small image must not read as "0 MB".
  if (bytes > 0 && megabytes < 0.1) return "< 0,1 MB";
  return `${sizeFormat.format(megabytes)} MB`;
}

class ShopAssetLibrary extends HTMLElement {
  #data = null;

  connectedCallback() {
    const pick = this.getAttribute("mode") === "pick";
    this.innerHTML = `
      <div class="asset-library">
        <div class="asset-library__quota">
          <div class="asset-library__quota-text" id="asset-quota-text">Speicherplatz wird geladen …</div>
          <div class="asset-library__bar" role="presentation"><span id="asset-quota-bar"></span></div>
        </div>
        <form class="asset-library__upload" novalidate>
          <label class="asset-library__file">
            <span>Bild hochladen (PNG, JPG oder WebP, max. 5 MB)</span>
            <input type="file" name="file" accept="image/png,image/jpeg,image/webp" />
          </label>
          <label class="asset-library__rights">
            <input type="checkbox" name="rightsConfirmed" />
            <span>Ich habe die Rechte an diesem Bild.</span>
          </label>
          <button type="submit" class="button-primary" disabled>Hochladen</button>
        </form>
        <p class="asset-library__empty" hidden>Noch keine Bilder hochgeladen.</p>
        <ul class="asset-library__grid"></ul>
      </div>`;
    const form = this.querySelector("form");
    const submit = form.querySelector("button");
    // Without the rights confirmation the upload is not even sent (shop-assets spec).
    const updateSubmit = () => {
      submit.disabled = !form.file.files.length || !form.rightsConfirmed.checked;
    };
    form.addEventListener("change", updateSubmit);
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      if (submit.disabled) return;
      submit.disabled = true;
      submit.textContent = "Wird hochgeladen …";
      try {
        const body = new FormData();
        body.append("file", form.file.files[0]);
        body.append("rightsConfirmed", "true");
        const asset = await apiFetchForm("/partner/shop/assets", body);
        form.reset();
        showToast("Bild hochgeladen.", "success");
        await this.load();
        if (pick) this.dispatchEvent(new CustomEvent("asset-pick", { detail: { id: asset.id }, bubbles: true }));
      } catch (error) {
        showToast(error.message, "error");
      } finally {
        submit.textContent = "Hochladen";
        updateSubmit();
      }
    });
    this.querySelector(".asset-library__grid").addEventListener("click", (event) => this.#onGridClick(event));
    this.load();
  }

  get assets() {
    return this.#data?.assets ?? [];
  }

  async load() {
    try {
      this.#data = await apiFetch("/partner/shop/assets");
      this.#render();
    } catch (error) {
      this.querySelector("#asset-quota-text").textContent = `Bilder konnten nicht geladen werden: ${error.message}`;
    }
  }

  #render() {
    const { assets, usedBytes, quotaBytes } = this.#data;
    const pick = this.getAttribute("mode") === "pick";
    const percent = Math.min(100, Math.round((usedBytes / quotaBytes) * 100));
    this.querySelector("#asset-quota-text").textContent =
      `${formatMegabytes(usedBytes)} von ${formatMegabytes(quotaBytes)} belegt – ${formatMegabytes(Math.max(0, quotaBytes - usedBytes))} frei`;
    const bar = this.querySelector("#asset-quota-bar");
    bar.style.width = `${percent}%`;
    bar.classList.toggle("is-full", percent >= 90);
    this.querySelector(".asset-library__empty").hidden = assets.length > 0;
    const grid = this.querySelector(".asset-library__grid");
    grid.innerHTML = assets
      .map((asset) => {
        const used = asset.usedOn.length ? `Verwendet auf: ${asset.usedOn.join(", ")}` : "Nicht verwendet";
        return `<li class="asset-card" data-asset-id="${escapeHtml(asset.id)}">
          <img class="asset-card__image" alt="" />
          <span class="asset-card__name" title="${escapeHtml(asset.filename)}">${escapeHtml(asset.filename)}</span>
          <span class="asset-card__meta">${escapeHtml(formatMegabytes(asset.sizeBytes))} · ${escapeHtml(used)}</span>
          <span class="asset-card__actions">
            ${pick ? '<button type="button" class="button-primary" data-action="pick">Auswählen</button>' : ""}
            <button type="button" class="button-secondary" data-action="delete">Löschen</button>
          </span>
        </li>`;
      })
      .join("");
    for (const card of grid.querySelectorAll(".asset-card")) {
      assetObjectUrl(Number(card.dataset.assetId))
        .then((url) => {
          card.querySelector("img").src = url;
        })
        .catch(() => {});
    }
  }

  async #onGridClick(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.closest("[data-asset-id]").dataset.assetId);
    if (button.dataset.action === "pick") {
      this.dispatchEvent(new CustomEvent("asset-pick", { detail: { id }, bubbles: true }));
      return;
    }
    // Deleting cannot be undone: the first click only asks.
    if (!button.dataset.confirm) {
      button.dataset.confirm = "1";
      button.textContent = "Wirklich löschen?";
      return;
    }
    button.disabled = true;
    try {
      await apiFetch(`/partner/shop/assets/${id}`, { method: "DELETE" });
      showToast("Bild gelöscht.", "success");
      await this.load();
    } catch (error) {
      // e.g. "Das Bild wird noch verwendet auf: Startseite."
      showToast(error.message, "error");
      button.disabled = false;
      delete button.dataset.confirm;
      button.textContent = "Löschen";
    }
  }
}

customElements.define("shop-asset-library", ShopAssetLibrary);
