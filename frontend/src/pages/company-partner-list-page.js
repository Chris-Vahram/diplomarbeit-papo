import { apiFetch, apiDownload } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatDate } from "../lib/format.js";
import { priceStackHtml } from "../lib/price.js";
import { escapeHtml } from "../lib/html.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";
import { taxIdentifierFieldsHtml, readTaxIdentifier, fillTaxIdentifier } from "../lib/tax-identifier-fields.js";
import { renderShopStatus, shopActionButton } from "../lib/shop-status.js";

// Commission dropdown presets per FSD FR-19/partner-management spec: 5/10/15 or a custom value.
const COMMISSION_PRESETS = ["5", "10", "15"];

class CompanyPartnerListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <div class="list-header">
        <h1>Partnerverwaltung</h1>
        <button type="button" class="button-primary" id="new-partner-btn">Neuer Partner</button>
      </div>
      <data-table id="partner-table"></data-table>
      <modal-dialog id="partner-modal" title="Neuer Partner">
        <form id="partner-form" class="entity-form entity-form--grid">
          <input type="hidden" name="id" />
          <form-field label="Partnernummer" name="partnerNumber" required></form-field>
          <p class="partner-form__hint" id="partner-number-hint" hidden>
            Anmelden kann sich ein Partner nur über ein verknüpftes Login (Schaltfläche „Logins“). Eine Änderung der Partnernummer ändert daran nichts.
          </p>
          <form-field label="Name" name="name" required></form-field>
          ${addressFieldsHtml("address")}
          ${taxIdentifierFieldsHtml()}
          <form-field label="E-Mail" name="email" type="email"></form-field>
          <form-field label="Telefon" name="phone"></form-field>
          <form-field class="form-field--wide" label="Interne Notiz – nur für das Unternehmen" name="internalNote">
            <textarea name="internalNote" maxlength="4000" rows="5"></textarea>
          </form-field>
          <form-field label="Provision" name="commissionPercentPreset">
            <select name="commissionPercentPreset">
              <option value="5">5%</option>
              <option value="10">10%</option>
              <option value="15">15%</option>
              <option value="custom">Individuell</option>
            </select>
          </form-field>
          <form-field label="Individueller Wert (%)" name="commissionPercentCustom" type="number" step="0.01" min="0" max="100"></form-field>
          <label class="checkbox-field">
            <input type="checkbox" name="active" checked />
            Aktiv
          </label>
          <div class="entity-form__actions">
            <button type="submit">Speichern</button>
          </div>
        </form>
      </modal-dialog>
      <modal-dialog id="logins-modal" title="Logins">
        <p>Verknüpft einen Keycloak-Benutzer mit diesem Partner. Die Keycloak-Benutzer-ID steht in der Keycloak-Administration beim Benutzer unter „ID“.</p>
        <ul class="login-list" id="login-list"></ul>
        <form id="login-form" class="entity-form">
          <form-field label="Keycloak-Benutzer-ID" name="oidcSubject" required></form-field>
          <div class="entity-form__actions">
            <button type="submit">Verknüpfen</button>
          </div>
        </form>
      </modal-dialog>
      <modal-dialog id="enablement-modal" title="Artikel freischalten">
        <data-table id="enablement-table"></data-table>
      </modal-dialog>
      <modal-dialog id="proof-modal" title="Gewerbeberechtigungsnachweis prüfen">
        <div id="proof-content">
          <p id="proof-status" aria-live="polite">Nachweis wird geladen …</p>
          <p id="proof-error" class="trade-proof-card__error" role="alert" hidden></p>
          <button type="button" id="proof-retry" hidden>Erneut laden</button>
          <dl id="proof-metadata" class="profile-readonly" hidden></dl>
          <div class="entity-form__actions">
            <button type="button" id="proof-download" hidden>Nachweis herunterladen</button>
          </div>
          <form id="proof-review-form" class="entity-form" hidden>
            <input type="hidden" name="version" />
            <form-field label="Prüfstatus" name="status" required>
              <select name="status">
                <option value="ACCEPTED">Akzeptieren</option>
                <option value="REJECTED">Ablehnen</option>
              </select>
            </form-field>
            <form-field label="Ablehnungsgrund" name="rejectionReason">
              <textarea name="rejectionReason" maxlength="1000" rows="4"></textarea>
            </form-field>
            <div class="entity-form__actions">
              <button type="submit">Prüfung speichern</button>
            </div>
          </form>
        </div>
      </modal-dialog>
      <modal-dialog id="shop-modal" title="Webshop">
        <div class="shop-panel shop-panel--dialog">
          <div class="shop-panel__head">
            <h3 id="shop-partner-name">Webshop</h3>
            <span id="shop-badge" aria-live="polite"></span>
          </div>
          <p id="shop-loading">Webshop wird geladen …</p>
          <p id="shop-hint" class="shop-panel__hint" hidden></p>
          <p id="shop-env-notice" class="shop-panel__env" hidden>Öffentliche Shops sind auf dieser Umgebung noch deaktiviert.</p>
          <div id="shop-reason" class="shop-panel__reason" hidden>
            <strong>Grund:</strong> <span id="shop-reason-text"></span>
          </div>
          <p id="shop-error" class="trade-proof-card__error" role="alert" hidden></p>
          <dl id="shop-details" class="profile-readonly shop-panel__details" hidden>
            <dt>Öffentlicher Link</dt><dd id="shop-link"></dd>
          </dl>

          <section id="shop-release-section" class="shop-panel__section" hidden>
            <h4>Freigabe</h4>
            <div class="shop-panel__actions">
              <button type="button" id="shop-release" hidden>Freigeben</button>
              <button type="button" id="shop-takedown-open" class="button-danger" hidden>Offline nehmen</button>
            </div>
            <p id="shop-legal-hint" class="shop-panel__legal" hidden></p>
            <form id="shop-takedown-form" class="entity-form" hidden>
              <form-field label="Grund (für den Partner sichtbar)" name="reason" required>
                <textarea name="reason" minlength="10" maxlength="500" rows="3"></textarea>
              </form-field>
              <div class="shop-panel__actions">
                <button type="submit" class="button-danger">Shop offline nehmen</button>
                <button type="button" id="shop-takedown-cancel" class="row-actions__secondary">Abbrechen</button>
              </div>
            </form>
          </section>

          <section id="shop-kennung-section" class="shop-panel__section" hidden>
            <h4>Kennung</h4>
            <form id="shop-kennung-form" class="shop-panel__kennung">
              <input name="kennung" required maxlength="40" aria-label="Kennung" autocomplete="off" spellcheck="false" />
              <button type="submit">Speichern</button>
            </form>
            <p class="partner-form__hint">3–40 Zeichen: Kleinbuchstaben a–z, Ziffern und einzelne Bindestriche.</p>
            <p class="shop-panel__warning" id="shop-kennung-warning" hidden>Bisherige Links funktionieren dann nicht mehr.</p>
          </section>
        </div>
      </modal-dialog>
    `;

    this.table = this.querySelector("#partner-table");
    this.table.columns = [
      { key: "partnerNumber", label: "Nr.", mobile: "meta" },
      { key: "name", label: "Name", mobile: "title" },
      { key: "commissionLabel", label: "Provision", mobile: "amount" },
      { key: "statusBadge", label: "Status", html: true, mobile: "status" },
      { key: "proofStatus", label: "Nachweis" },
      { key: "actions", label: "", html: true },
    ];
    this.table.emptyMessage = "Noch keine Partner angelegt.";
    this.table.retry = () => this.load();
    this.table.searchKeys = ["name", "partnerNumber"];
    this.table.pageSize = 20;
    this.table.filters = [
      {
        key: "activeLabel",
        type: "select",
        label: "Status",
        options: [
          { value: "active", label: "Aktiv" },
          { value: "inactive", label: "Inaktiv" },
        ],
      },
    ];

    this.modal = this.querySelector("#partner-modal");
    this.form = this.querySelector("#partner-form");
    this.partnerNumberHint = this.querySelector("#partner-number-hint");
    this.customField = this.querySelector('form-field[name="commissionPercentCustom"]');
    this.presetSelect = this.form.elements.commissionPercentPreset;

    this.enablementModal = this.querySelector("#enablement-modal");
    this.enablementTable = this.querySelector("#enablement-table");
    this.enablementTable.columns = [
      { key: "name", label: "Name", mobile: "title" },
      { key: "priceLabel", label: "Preis inkl. USt", mobile: "amount", html: true },
      { key: "category", label: "Artikelkategorie", mobile: "meta" },
      { key: "enabledCheckbox", label: "Freigeschaltet", html: true, mobile: "status" },
    ];
    this.enablementTable.emptyMessage = "Noch keine Artikel angelegt.";
    this.proofModal = this.querySelector("#proof-modal");
    this.proofStatus = this.querySelector("#proof-status");
    this.proofError = this.querySelector("#proof-error");
    this.proofRetry = this.querySelector("#proof-retry");
    this.proofMetadata = this.querySelector("#proof-metadata");
    this.proofDownload = this.querySelector("#proof-download");
    this.proofReviewForm = this.querySelector("#proof-review-form");
    this.proofRetry.addEventListener("click", () => this.loadProof());
    this.proofDownload.addEventListener("click", () => this.downloadProof());
    this.proofReviewForm.addEventListener("submit", (event) => this.reviewProof(event));
    this.shopModal = this.querySelector("#shop-modal");
    this.shopKennungForm = this.querySelector("#shop-kennung-form");
    this.shopTakedownForm = this.querySelector("#shop-takedown-form");
    this.shopKennungForm.addEventListener("submit", (event) => this.changeShopKennung(event));
    this.shopTakedownForm.addEventListener("submit", (event) => this.takeShopOffline(event));
    this.querySelector("#shop-release").addEventListener("click", () => this.releaseShop());
    this.querySelector("#shop-takedown-open").addEventListener("click", () => {
      this.querySelector("#shop-takedown-open").hidden = true;
      this.shopTakedownForm.hidden = false;
      this.shopTakedownForm.elements.reason.focus();
    });
    this.querySelector("#shop-takedown-cancel").addEventListener("click", () => this.renderShop(this.shop));
    this.shopKennungForm.elements.kennung.addEventListener("input", () => {
      this.querySelector("#shop-kennung-warning").hidden =
        !this.shop?.releasedAt || this.shopKennungForm.elements.kennung.value === this.shop.kennung;
    });

    this.presetSelect.addEventListener("change", () => this.toggleCustomField());

    this.querySelector("#new-partner-btn").addEventListener("click", () => this.openCreate());
    this.table.addEventListener("click", (event) => {
      const editButton = event.target.closest("[data-edit-id]");
      if (editButton) this.openEdit(editButton.dataset.editId);
      const manageButton = event.target.closest("[data-manage-id]");
      if (manageButton) this.openEnablement(manageButton.dataset.manageId);
      const proofButton = event.target.closest("[data-proof-id]");
      if (proofButton) this.openProof(proofButton.dataset.proofId);
      const shopButton = event.target.closest("[data-shop-id]");
      if (shopButton) this.openShop(shopButton.dataset.shopId);
      const loginsButton = event.target.closest("[data-logins-id]");
      if (loginsButton) this.openLogins(loginsButton.dataset.loginsId);
    });
    this.loginsModal = this.querySelector("#logins-modal");
    this.loginList = this.querySelector("#login-list");
    this.loginForm = this.querySelector("#login-form");
    this.loginForm.addEventListener("submit", (event) => this.addLogin(event));
    this.loginList.addEventListener("click", (event) => {
      const button = event.target.closest("[data-revoke-login-id]");
      if (button) this.revokeLogin(button);
    });
    this.form.addEventListener("submit", (event) => this.handleSubmit(event));
    this.enablementTable.addEventListener("change", (event) => {
      const checkbox = event.target.closest("[data-toggle-product-id]");
      if (checkbox) this.toggleEnablement(checkbox.dataset.toggleProductId, checkbox.checked);
    });

    this.toggleCustomField();
    this.load();
  }

  toggleCustomField() {
    const isCustom = this.presetSelect.value === "custom";
    this.customField.hidden = !isCustom;
    // Required only while visible, so an empty custom value can no longer
    // silently submit as 0% (Number("") === 0) — the browser blocks submit instead.
    const customInput = this.customField.querySelector("input");
    if (customInput) customInput.required = isCustom;
  }

  async load() {
    try {
      // The shop overview is an add-on: if it fails, the partner list still loads and the
      // "Webshop" buttons show a neutral "Status unbekannt".
      const [partners, shops] = await Promise.all([
        apiFetch("/company/partners"),
        apiFetch("/company/shops").catch(() => null),
      ]);
      this.partners = partners;
      const shopStatusByPartner = new Map((shops ?? []).map((shop) => [shop.partnerId, shop.status]));
      // No shop record yet means the company has never released one.
      const shopStatusOf = (partnerId) => (shops ? shopStatusByPartner.get(partnerId) ?? "NOT_RELEASED" : null);
      this.table.rows = this.partners.map((p) => ({
        partnerNumber: p.partnerNumber,
        name: p.name,
        commissionLabel: `${p.commissionPercent}%`,
        activeLabel: p.active ? "active" : "inactive",
        statusBadge: `<status-badge status="${p.active ? "active" : "inactive"}"></status-badge>`,
        proofStatus: this.proofStatusLabel(p.tradeProof),
        actions: `<div class="row-actions">
          <button type="button" data-edit-id="${p.id}">Partnerdaten Bearbeiten</button>
          <button type="button" class="row-actions__secondary" data-proof-id="${p.id}">Nachweis</button>
          <button type="button" class="row-actions__secondary" data-manage-id="${p.id}">Artikel</button>
          <button type="button" class="row-actions__secondary" data-logins-id="${p.id}">Logins</button>
          ${shopActionButton(p.id, shopStatusOf(p.id))}
        </div>`,
      }));
      const requestedPartnerId = new URLSearchParams(location.hash.split("?")[1] ?? "").get("enablement");
      if (requestedPartnerId) await this.openEnablement(requestedPartnerId);
    } catch (error) {
      this.table.error = error.message;
    }
  }

  async openProof(partnerId) {
    this.proofPartnerId = partnerId;
    this.proofModal.open();
    await this.loadProof();
  }

  async loadProof() {
    this.proofStatus.textContent = "Nachweis wird geladen …";
    this.proofError.hidden = true;
    this.proofRetry.hidden = true;
    this.proofMetadata.hidden = true;
    this.proofDownload.hidden = true;
    this.proofReviewForm.hidden = true;
    try {
      const proof = await apiFetch(`/company/partners/${this.proofPartnerId}/trade-proof`);
      if (!proof.present) {
        this.proofStatus.textContent = "Für diesen Partner ist noch kein Nachweis hinterlegt.";
        return;
      }
      this.proofStatus.textContent = this.proofStatusLabel(proof);
      this.proofMetadata.innerHTML = "";
      for (const [label, value] of [["Datei", proof.filename], ["Register", `${proof.registerIdentifier.country} / ${proof.registerIdentifier.type}`], ["Kennzeichen", proof.registerIdentifier.value]]) {
        const term = document.createElement("dt");
        term.textContent = label;
        const description = document.createElement("dd");
        description.textContent = value ?? "–";
        this.proofMetadata.append(term, description);
      }
      this.proofMetadata.hidden = false;
      this.proofDownload.hidden = false;
      this.proofReviewForm.hidden = false;
      this.proofReviewForm.elements.version.value = proof.version;
      this.proofReviewForm.elements.status.value = proof.status === "REJECTED" ? "REJECTED" : "ACCEPTED";
      this.proofReviewForm.elements.rejectionReason.value = proof.rejectionReason ?? "";
    } catch (error) {
      this.proofStatus.textContent = "Nachweis konnte nicht geladen werden.";
      this.proofError.textContent = error.message;
      this.proofError.hidden = false;
      this.proofRetry.hidden = false;
    }
  }

  async downloadProof() {
    try {
      const proof = await apiFetch(`/company/partners/${this.proofPartnerId}/trade-proof`);
      await apiDownload(`/company/partners/${this.proofPartnerId}/trade-proof/download`, proof.filename);
    } catch (error) {
      this.proofError.textContent = error.message;
      this.proofError.hidden = false;
    }
  }

  async reviewProof(event) {
    event.preventDefault();
    if (this.proofReviewing) return;
    this.proofReviewing = true;
    const formData = new FormData(this.proofReviewForm);
    try {
      await apiFetch(`/company/partners/${this.proofPartnerId}/trade-proof/review`, {
        method: "PUT",
        body: JSON.stringify({
          version: formData.get("version"),
          status: formData.get("status"),
          rejectionReason: formData.get("rejectionReason") || null,
        }),
      });
      showToast("Prüfung gespeichert.", "success");
      this.proofModal.close();
      await this.load();
    } catch (error) {
      this.proofError.textContent = error.message;
      this.proofError.hidden = false;
    } finally {
      this.proofReviewing = false;
    }
  }

  async openShop(partnerId) {
    // A new dialog session: responses still in flight for a previously opened partner
    // are ignored (shopAction), so they can never fill or act on this partner's dialog.
    this.shopSession = (this.shopSession ?? 0) + 1;
    this.shopBusy = false;
    this.shopPartnerId = partnerId;
    this.shop = null;
    const partner = this.partners?.find((p) => String(p.id) === String(partnerId));
    this.querySelector("#shop-partner-name").textContent = partner?.name ?? "Webshop";
    this.querySelector("#shop-badge").innerHTML = "";
    this.querySelector("#shop-loading").hidden = false;
    for (const id of ["#shop-hint", "#shop-details", "#shop-reason", "#shop-release-section", "#shop-kennung-section"]) {
      this.querySelector(id).hidden = true;
    }
    this.shopModal.open();
    await this.shopAction(() => apiFetch(`/company/partners/${partnerId}/shop`));
  }

  // Runs one shop request for the current dialog session, then re-renders the dialog or
  // shows the German error inline. One request at a time per session (double-submit guard).
  async shopAction(request, successMessage) {
    if (this.shopBusy) return;
    const session = this.shopSession;
    this.shopBusy = true;
    const error = this.querySelector("#shop-error");
    error.hidden = true;
    try {
      const shop = await request();
      if (successMessage) {
        showToast(successMessage, "success");
        this.load();
      }
      if (session === this.shopSession) this.renderShop(shop);
    } catch (requestError) {
      if (session === this.shopSession) {
        error.textContent = requestError.message;
        error.hidden = false;
      }
    } finally {
      if (session === this.shopSession) this.shopBusy = false;
    }
  }

  renderShop(shop) {
    this.shop = shop;
    this.querySelector("#shop-loading").hidden = true;
    renderShopStatus(this.shopModal, shop);

    const release = this.querySelector("#shop-release");
    release.hidden = shop.status !== "NOT_RELEASED" && shop.status !== "OFFLINE_COMPANY";
    release.textContent = shop.status === "OFFLINE_COMPANY" ? "Erneut freigeben" : "Freigeben";
    this.querySelector("#shop-takedown-open").hidden = shop.status === "NOT_RELEASED" || shop.status === "OFFLINE_COMPANY";
    this.shopTakedownForm.hidden = true;
    this.shopTakedownForm.reset();
    this.querySelector("#shop-release-section").hidden = false;
    this.renderLegalHint(shop);

    this.shopKennungForm.elements.kennung.value = shop.kennung;
    this.querySelector("#shop-kennung-warning").hidden = true;
    this.querySelector("#shop-kennung-section").hidden = false;
  }

  // Release gate (shop-legal spec): production refuses release while legal texts are missing;
  // staging releases with marked placeholders, which the dialog says plainly.
  renderLegalHint(shop) {
    const hint = this.querySelector("#shop-legal-hint");
    const missing = shop.missingLegalTexts ?? 0;
    hint.hidden = missing === 0;
    this.querySelector("#shop-release").disabled = Boolean(shop.legalGate) && missing > 0;
    if (!missing) return;
    const count = missing === 1 ? "1 Rechtstext fehlt" : `${missing} Rechtstexte fehlen`;
    hint.innerHTML = shop.legalGate
      ? `${count} im <a href="#/company/profile">Firmenprofil</a>. Erst danach lässt sich der Shop freigeben.`
      : `${count} im <a href="#/company/profile">Firmenprofil</a>. Der Shop zeigt dafür markierte Platzhalter; auf dieser Testumgebung ist die Freigabe trotzdem möglich.`;
  }

  changeShopKennung(event) {
    event.preventDefault();
    const kennung = this.shopKennungForm.elements.kennung.value.trim();
    this.shopAction(() => apiFetch(`/company/partners/${this.shopPartnerId}/shop/kennung`, {
      method: "PUT",
      body: JSON.stringify({ kennung }),
    }), "Kennung gespeichert.");
  }

  releaseShop() {
    this.shopAction(() => apiFetch(`/company/partners/${this.shopPartnerId}/shop/release`, { method: "POST" }),
      "Webshop freigegeben.");
  }

  takeShopOffline(event) {
    event.preventDefault();
    const reason = this.shopTakedownForm.elements.reason.value.trim();
    this.shopAction(() => apiFetch(`/company/partners/${this.shopPartnerId}/shop/takedown`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    }), "Webshop offline genommen.");
  }

  proofStatusLabel(proof) {
    if (!proof?.present) return "Fehlt";
    return { PENDING: "Ausstehend", ACCEPTED: "Akzeptiert", REJECTED: "Abgelehnt" }[proof.status] ?? "Unbekannt";
  }

  openCreate() {
    this.form.reset();
    this.form.elements.id.value = "";
    this.presetSelect.value = "5";
    this.toggleCustomField();
    this.partnerNumberHint.hidden = true;
    this.modal.setAttribute("title", "Neuer Partner");
    this.modal.open();
  }

  openEdit(id) {
    const partner = this.partners.find((p) => String(p.id) === String(id));
    if (!partner) return;
    this.form.elements.id.value = partner.id;
    this.form.elements.partnerNumber.value = partner.partnerNumber;
    this.form.elements.name.value = partner.name;
    fillAddress(this.form, "address", partner.address);
    fillTaxIdentifier(this.form, partner.taxIdentifier);
    this.form.elements.email.value = partner.email ?? "";
    this.form.elements.phone.value = partner.phone ?? "";
    this.form.elements.internalNote.value = partner.internalNote ?? "";
    this.form.elements.active.checked = partner.active;
    this.partnerNumberHint.hidden = false;

    const commissionStr = String(partner.commissionPercent);
    if (COMMISSION_PRESETS.includes(commissionStr)) {
      this.presetSelect.value = commissionStr;
    } else {
      this.presetSelect.value = "custom";
      this.customField.value = commissionStr;
    }
    this.toggleCustomField();

    this.modal.setAttribute("title", "Partner bearbeiten");
    this.modal.open();
  }

  // Login bindings (bind-partner-login-to-oidc-subject): separate from the partner form.
  async openLogins(partnerId) {
    this.loginsPartnerId = partnerId;
    this.loginForm.reset();
    this.loginList.innerHTML = "<li>Lade Logins…</li>";
    this.loginsModal.open();
    await this.loadLogins();
  }

  async loadLogins() {
    const partnerId = this.loginsPartnerId;
    try {
      const logins = await apiFetch(`/company/partners/${partnerId}/logins`);
      // Ignore a late response for a dialog that was meanwhile opened for another partner.
      if (partnerId !== this.loginsPartnerId) return;
      this.loginList.innerHTML = logins.length === 0
        ? "<li>Noch kein Login verknüpft.</li>"
        : logins.map((l) => `
          <li class="login-list__item${l.revokedAt ? " login-list__item--revoked" : ""}">
            <code>${escapeHtml(l.oidcSubject)}</code>
            <span>${l.revokedAt ? `entfernt am ${formatDate(l.revokedAt.slice(0, 10))}` : `seit ${formatDate(l.createdAt.slice(0, 10))}`}</span>
            ${l.revokedAt ? "" : `<button type="button" class="button-danger" data-revoke-login-id="${l.id}">Entfernen</button>`}
          </li>`).join("");
    } catch (error) {
      if (partnerId !== this.loginsPartnerId) return;
      this.loginList.innerHTML = "<li>Logins konnten nicht geladen werden.</li>";
      showToast(error.message, "error");
    }
  }

  async addLogin(event) {
    event.preventDefault();
    if (this.loginPending) return;
    this.loginPending = true;
    const submit = this.loginForm.querySelector("button[type=submit]");
    submit.disabled = true;
    try {
      await apiFetch(`/company/partners/${this.loginsPartnerId}/logins`, {
        method: "POST",
        body: JSON.stringify({ oidcSubject: this.loginForm.elements.oidcSubject.value }),
      });
      showToast("Login verknüpft.", "success");
      this.loginForm.reset();
      await this.loadLogins();
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      this.loginPending = false;
      submit.disabled = false;
    }
  }

  async revokeLogin(button) {
    button.disabled = true;
    try {
      await apiFetch(`/company/partners/${this.loginsPartnerId}/logins/${button.dataset.revokeLoginId}`, { method: "DELETE" });
      showToast("Login entfernt.", "success");
    } catch (error) {
      showToast(error.message, "error");
    }
    await this.loadLogins();
  }

  async openEnablement(partnerId) {
    const partner = this.partners.find((p) => String(p.id) === String(partnerId));
    if (!partner) return;
    this.enablementPartnerId = partnerId;
    this.enablementModal.setAttribute("title", `Artikel freischalten – ${partner.name}`);
    this.enablementModal.open();
    await this.loadEnablement();
  }

  async loadEnablement() {
    try {
      const products = await apiFetch(`/company/partners/${this.enablementPartnerId}/products`);
      this.enablementTable.rows = products.map((p) => ({
        name: p.name,
        priceLabel: priceStackHtml(p.price, p.netPrice),
        category: p.category ?? "–",
        enabledCheckbox: `<input type="checkbox" data-toggle-product-id="${p.productId}" ${p.enabled ? "checked" : ""} />`,
      }));
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  async toggleEnablement(productId, enabled) {
    try {
      await apiFetch(`/company/partners/${this.enablementPartnerId}/products/${productId}`, {
        method: enabled ? "PUT" : "DELETE",
      });
      showToast(enabled ? "Artikel freigeschaltet." : "Artikel gesperrt.", "success");
    } catch (error) {
      showToast(error.message, "error");
      await this.loadEnablement();
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    if (this.submitting) return;
    this.submitting = true;
    const data = new FormData(this.form);
    const id = data.get("id");
    const preset = data.get("commissionPercentPreset");
    const commissionPercent = preset === "custom" ? Number(data.get("commissionPercentCustom")) : Number(preset);

    const payload = {
      partnerNumber: data.get("partnerNumber"),
      name: data.get("name"),
      address: readAddress(this.form, "address"),
      taxIdentifier: readTaxIdentifier(this.form),
      email: data.get("email") || null,
      phone: data.get("phone") || null,
      internalNote: data.get("internalNote"),
      commissionPercent,
      active: this.form.elements.active.checked,
    };

    try {
      if (id) {
        await apiFetch(`/company/partners/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        showToast("Partner aktualisiert.", "success");
      } else {
        await apiFetch("/company/partners", { method: "POST", body: JSON.stringify(payload) });
        showToast("Partner angelegt.", "success");
      }
      this.modal.close();
      this.load();
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      this.submitting = false;
    }
  }
}

customElements.define("company-partner-list-page", CompanyPartnerListPage);
