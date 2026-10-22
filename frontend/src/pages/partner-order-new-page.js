import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { resolveImageUrls } from "../lib/product-images.js";
import { renderOrderConfirmationHtml, kundeDetailsHtml } from "../lib/order-form.js";
import { addressFieldsHtml, readAddress, fillAddress } from "../lib/address-fields.js";
import {
  catalogStepHtml,
  wireCatalogStep,
  cartStepHtml,
  wireCartStep,
  cartSummaryHtml,
  wireCartSummaryThumbs,
} from "../lib/order-cart.js";
import { escapeHtml } from "../lib/html.js";

const STEPS = ["kunde", "neuerKunde", "produkte", "uebersicht", "erfolg"];
const STEP_LABELS = {
  kunde: "Endkunde wählen",
  neuerKunde: "Neuer Endkunde",
  produkte: "Artikel wählen",
  uebersicht: "Übersicht",
  erfolg: "Erfolg",
};
const STEP_DESCRIPTIONS = {
  kunde: "Wählen Sie den Endkunden dieser Bestellung aus oder legen Sie einen neuen an.",
  neuerKunde: "Erfassen Sie die Daten des neuen Endkunden.",
  produkte: "Wählen Sie die Artikel für diese Bestellung aus und legen Sie sie in den Warenkorb.",
  uebersicht:
    "Prüfen Sie Ihre Bestellung und geben Sie sie auf. Über die Schritte oben können Sie jederzeit zurückgehen, um etwas zu ändern.",
  erfolg: "Ihre Bestellung wurde erfolgreich aufgegeben.",
};

class PartnerOrderNewPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <a href="#/partner/orders" class="back-link">&laquo; Zurück zu Meine Bestellungen</a>
      <h1 id="order-heading">Neue Bestellung</h1>
      <div id="order-content"></div>
    `;
    this.content = this.querySelector("#order-content");
    this.load();
  }

  resetState() {
    this.state = {
      step: "kunde",
      maxStepReached: 0,
      kundeMode: null,
      kundeId: null,
      newKunde: null,
      newKundeDirty: false,
      cart: [],
      order: null,
    };
    this.justCompletedStep = null;
    this.submitting = false;
  }

  async load() {
    this.resetState();
    try {
      const [products, customers, profile] = await Promise.all([
        apiFetch("/partner/products"),
        apiFetch("/partner/customers"),
        apiFetch("/partner/profile"),
      ]);
      this.customers = customers;
      this.commissionPercent = profile.commissionPercent;
      if (products.length === 0) {
        this.content.innerHTML = `<p>Für Sie sind noch keine Artikel freigeschaltet. Eine Bestellung ist erst möglich, sobald die Firma Artikel für Sie freischaltet.</p>`;
        return;
      }
      this.products = await resolveImageUrls(products);
      this.renderStep();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  // Allowed only for already-visited steps (index <= maxStepReached); no back-nav
  // from "Erfolg" at all (partner-order-wizard spec).
  goToStep(step) {
    if (this.state.step === "erfolg") return;
    const targetIndex = STEPS.indexOf(step);
    if (targetIndex > this.state.maxStepReached) return;
    if (targetIndex >= STEPS.indexOf("produkte") && !this.hasConfirmedKunde()) return;
    this.state.step = step;
    this.renderStep();
  }

  hasConfirmedKunde() {
    return this.state.kundeMode === "existing"
      ? this.customers.some((kunde) => kunde.id === this.state.kundeId)
      : this.state.kundeMode === "new" && this.state.newKunde != null && !this.state.newKundeDirty;
  }

  // Advancing past the current step raises maxStepReached (unlocking that
  // checkpoint) and remembers which step just completed, for its checkmark animation.
  advanceTo(step) {
    const targetIndex = STEPS.indexOf(step);
    if (targetIndex > this.state.maxStepReached) {
      this.justCompletedStep = this.state.step;
      this.state.maxStepReached = targetIndex;
    }
    this.state.step = step;
    this.renderStep();
  }

  newKundeDisplayName() {
    const k = this.state.newKunde;
    if (!k) return "";
    return `${k.firstName ? k.firstName + " " : ""}${k.lastName}`;
  }

  kundeDisplayName() {
    const kunde = this.selectedKunde();
    return kunde ? `${kunde.firstName ? `${kunde.firstName} ` : ""}${kunde.lastName}` : "";
  }

  // The Endkunde this order is for, resolved from wizard state - an existing
  // Kunde looked up by id, or the new-Kunde draft itself. Used to show full
  // Endkunde details on "Übersicht" (kundeDetailsHtml expects this shape).
  selectedKunde() {
    if (this.state.kundeMode === "existing") {
      return this.customers.find((c) => c.id === this.state.kundeId) ?? null;
    }
    if (this.state.kundeMode === "new") {
      return this.state.newKunde;
    }
    return null;
  }

  // Whether `step`'s own data is still valid right now — not just "was this step
  // reached before". A step reached earlier can become invalid later (e.g. the
  // partner goes back to "Artikel wählen" and removes every item from the
  // cart) - its checkpoint must stop showing as completed once that happens.
  isStepDataValid(step) {
    switch (step) {
      case "kunde":
        return this.hasConfirmedKunde();
      case "neuerKunde":
        // "existing" here means the partner deliberately bypassed this step —
        // that counts as resolved (see the "skipped" checkpoint label handling
        // in checkpointBarHtml), not incomplete.
        return this.state.kundeMode === "new" ? this.state.newKunde != null && !this.state.newKundeDirty : this.state.kundeMode === "existing";
      case "produkte":
      case "uebersicht":
        return this.state.cart.length > 0;
      case "erfolg":
        return this.state.order != null;
      default:
        return false;
    }
  }

  checkpointBarHtml() {
    const currentIndex = STEPS.indexOf(this.state.step);
    const items = STEPS.map((step, i) => {
      // "Neuer Endkunde" is only truly unreachable (greyed) before the partner has
      // decided how to pick an Endkunde at all; once "bestehenden Kunden wählen" is
      // chosen it counts as a (skipped) completed step — filled in like any other
      // done checkpoint, just labelled in parentheses to show it was bypassed.
      const isSkippedNeuerKunde = step === "neuerKunde" && this.state.kundeMode === "existing";
      let state;
      if (step === "neuerKunde" && currentIndex === 0 && this.state.kundeMode !== "new") state = "unused";
      else if (i === currentIndex) state = "current";
      else if (i <= this.state.maxStepReached && this.isStepDataValid(step)) state = "done";
      else state = "upcoming";

      const clickable =
        state === "done" &&
        this.state.step !== "erfolg" &&
        (i < STEPS.indexOf("produkte") || this.hasConfirmedKunde());
      const label =
        i === 0 && this.state.kundeMode === "new" && this.state.newKunde
          ? `Endkunde: ${escapeHtml(this.newKundeDisplayName())}`
          : isSkippedNeuerKunde
            ? `(${STEP_LABELS[step]})`
            : STEP_LABELS[step];
      const marker = state === "done" ? "✓" : String(i + 1);
      const justDone = this.justCompletedStep === step ? " wizard-checkpoint--just-done" : "";

      return `
        <li class="wizard-checkpoint wizard-checkpoint--${state}${justDone}">
          <button
            type="button"
            class="wizard-checkpoint__btn"
            data-step="${step}"
            title="${escapeHtml(STEP_LABELS[step])}"
            aria-current="${state === "current" ? "step" : "false"}"
            ${clickable ? "" : "disabled"}
          >
            <span class="wizard-checkpoint__marker" aria-hidden="true">${marker}</span>
            <span class="wizard-checkpoint__label">${label}</span>
          </button>
        </li>`;
    }).join("");
    // Text step indicator next to the markers (decision 4B) - progress never by colour alone.
    return `<div class="wizard-progress">
        <ol class="wizard-checkpoints">${items}</ol>
        <p class="wizard-progress__text">Schritt <strong>${currentIndex + 1} von ${STEPS.length}</strong> · ${escapeHtml(STEP_LABELS[this.state.step])}</p>
      </div>`;
  }

  wireCheckpointBar() {
    this.content.querySelectorAll(".wizard-checkpoint__btn").forEach((btn) => {
      btn.addEventListener("click", () => this.goToStep(btn.dataset.step));
    });
  }

  refreshCheckpointBar() {
    const bar = this.content.querySelector(".wizard-progress");
    if (!bar) return;
    bar.outerHTML = this.checkpointBarHtml();
    this.wireCheckpointBar();
  }

  // The step before `step` in the actual (per-partner) flow — skips "neuerKunde"
  // when it was never used, so "Zurück" from "Artikel wählen" never lands
  // on a step the partner didn't go through.
  prevStepFor(step) {
    let prevIndex = STEPS.indexOf(step) - 1;
    if (STEPS[prevIndex] === "neuerKunde" && this.state.kundeMode !== "new") prevIndex -= 1;
    return STEPS[prevIndex] ?? null;
  }

  wirePrevButton() {
    const btn = this.content.querySelector("#wizard-prev-btn");
    if (!btn) return;
    const prevStep = this.prevStepFor(this.state.step);
    if (prevStep) btn.addEventListener("click", () => this.goToStep(prevStep));
  }

  renderStep() {
    this.refreshOrderHeading();
    this.content.innerHTML = `
      ${this.checkpointBarHtml()}
      <div class="wizard-step" id="wizard-step">
        <p class="wizard-step__description">${STEP_DESCRIPTIONS[this.state.step]}</p>
        ${this.stepBodyHtml()}
      </div>
    `;
    this.wireCheckpointBar();
    this.wireStepBody();
    this.wirePrevButton();
    this.justCompletedStep = null;
  }

  refreshOrderHeading() {
    const heading = this.querySelector("#order-heading");
    if (heading) heading.textContent = this.hasConfirmedKunde() ? `Neue Bestellung für ${this.kundeDisplayName()}` : "Neue Bestellung";
  }

  stepBodyHtml() {
    switch (this.state.step) {
      case "kunde":
        return this.kundeStepHtml();
      case "neuerKunde":
        return this.neuerKundeStepHtml();
      case "produkte":
        return this.produkteStepHtml();
      case "uebersicht":
        return this.uebersichtStepHtml();
      case "erfolg":
        return this.erfolgStepHtml();
      default:
        return "";
    }
  }

  wireStepBody() {
    switch (this.state.step) {
      case "kunde":
        return this.wireKundeStep();
      case "neuerKunde":
        return this.wireNeuerKundeStep();
      case "produkte":
        return this.wireProdukteStep();
      case "uebersicht":
        return this.wireUebersichtStep();
      case "erfolg":
        return this.wireErfolgStep();
    }
  }

  // ---- Step: Endkunde wählen ----

  kundeStepHtml() {
    return `
      <form id="kunde-form">
        <div class="entity-form--grid">
          <form-field label="Endkunden suchen" name="kundeSearch">
            <input type="search" name="kundeSearch" autocomplete="off" placeholder="Name oder Adresse" />
          </form-field>
        </div>
        <div class="kunde-list-field">
          <span class="kunde-list-field__label" id="kunde-list-label">Endkunde</span>
          <ul class="kunde-list" role="listbox" aria-labelledby="kunde-list-label" id="kunde-list"></ul>
        </div>
        <p id="kunde-search-empty" hidden>Keine Endkunden gefunden.</p>
        <div class="wizard-step__actions">
          <button type="button" class="button-secondary" id="new-kunde-btn">Neuen Kunden anlegen</button>
          ${this.state.newKunde ? '<button type="button" class="button-secondary" id="resume-kunde-btn">Kundenentwurf fortsetzen</button>' : ""}
          <button type="submit" class="button-primary">Weiter</button>
        </div>
      </form>`;
  }

  wireKundeStep() {
    const form = this.content.querySelector("#kunde-form");
    const search = form.elements.kundeSearch;
    const list = this.content.querySelector("#kunde-list");
    const empty = form.querySelector("#kunde-search-empty");

    const renderOptions = () => {
      const needle = search.value.trim().toLocaleLowerCase("de-AT");
      const matches = this.customers.filter((kunde) =>
        [kunde.firstName, kunde.lastName, ...Object.values(kunde.address ?? {})]
          .filter(Boolean).join(" ").toLocaleLowerCase("de-AT").includes(needle),
      );

      list.innerHTML = matches.map((kunde) => {
        const name = `${kunde.firstName ? `${kunde.firstName} ` : ""}${kunde.lastName}`;
        const address = Object.values(kunde.address ?? {}).filter(Boolean).join(" ");
        const selected = this.state.kundeMode === "existing" && this.state.kundeId === kunde.id;
        return `
          <li>
            <button type="button" role="option" class="kunde-list__row${selected ? " is-selected" : ""}"
              data-id="${kunde.id}" aria-selected="${selected}">${escapeHtml(`${name} – ${address}`)}</button>
          </li>`;
      }).join("");

      empty.hidden = matches.length > 0;

      if (this.state.kundeMode === "existing" && this.state.kundeId != null
          && !matches.some((kunde) => kunde.id === this.state.kundeId)) {
        this.state.kundeId = null;
        this.refreshCheckpointBar();
        this.refreshOrderHeading();
      }
    };

    list.addEventListener("click", (event) => {
      const row = event.target.closest(".kunde-list__row");
      if (!row) return;
      this.state.kundeMode = "existing";
      this.state.kundeId = Number(row.dataset.id);
      renderOptions();
      this.refreshCheckpointBar();
      this.refreshOrderHeading();
    });

    renderOptions();
    search.addEventListener("input", renderOptions);

    this.content.querySelector("#new-kunde-btn").addEventListener("click", () => {
      this.state.kundeMode = "new";
      this.state.kundeId = null;
      this.advanceTo("neuerKunde");
    });
    this.content.querySelector("#resume-kunde-btn")?.addEventListener("click", () => {
      this.state.kundeMode = "new";
      this.advanceTo("neuerKunde");
    });

    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (this.state.kundeMode !== "existing" || this.state.kundeId == null) return;
      if (!this.customers.some((kunde) => kunde.id === this.state.kundeId)) return;
      this.advanceTo("produkte");
    });
  }

  // ---- Step: Neuer Endkunde ----

  neuerKundeStepHtml() {
    return `
      <form id="neuer-kunde-form" class="entity-form">
        <div class="entity-form--grid">
          <form-field label="Nachname" name="lastName" required></form-field>
          <form-field label="Vorname" name="firstName"></form-field>
          ${addressFieldsHtml("address")}
          <form-field label="E-Mail" name="email" type="email"></form-field>
          <form-field label="Telefon" name="phone"></form-field>
        </div>
        <div class="wizard-step__actions">
          <button type="button" class="button-secondary" id="wizard-prev-btn">Zurück</button>
          <button type="submit" class="button-primary">Weiter</button>
        </div>
      </form>`;
  }

  wireNeuerKundeStep() {
    const form = this.content.querySelector("#neuer-kunde-form");
    if (this.state.newKunde) {
      form.elements.lastName.value = this.state.newKunde.lastName ?? "";
      form.elements.firstName.value = this.state.newKunde.firstName ?? "";
      fillAddress(form, "address", this.state.newKunde.address);
      form.elements.email.value = this.state.newKunde.email ?? "";
      form.elements.phone.value = this.state.newKunde.phone ?? "";
    }

    const saveDraft = () => {
      this.state.newKunde = {
        lastName: form.elements.lastName.value,
        firstName: form.elements.firstName.value || null,
        address: readAddress(form, "address"),
        email: form.elements.email.value || null,
        phone: form.elements.phone.value || null,
      };
    };
    form.addEventListener("input", () => {
      saveDraft();
      this.state.newKundeDirty = true;
      this.refreshCheckpointBar();
      this.refreshOrderHeading();
    });

    form.addEventListener("submit", (event) => {
      event.preventDefault();
      saveDraft();
      this.state.kundeMode = "new";
      this.state.kundeId = null;
      this.state.newKundeDirty = false;
      this.advanceTo("produkte");
    });
  }

  // ---- Step: Artikel wählen ----

  produkteStepHtml() {
    const kundeName = escapeHtml(this.kundeDisplayName());
    return `
      <div class="produkte-step-layout">
        <div class="produkte-catalog-panel">
          ${catalogStepHtml(this.products)}
        </div>
        <div class="cart cart--preview">
          <h2 class="cart-preview__heading">Bestellung für ${kundeName}</h2>
          <div class="cart-step-body" id="produkte-cart-step-root">${cartStepHtml(this.state.cart, this.commissionPercent, { showSubmit: false })}</div>
        </div>
      </div>
      <div class="wizard-step__actions">
        <button type="button" class="button-secondary" id="wizard-prev-btn">Zurück</button>
        <button type="button" id="produkte-weiter-btn" class="button-primary" ${this.state.cart.length === 0 ? "disabled" : ""}>
          Weiter
        </button>
      </div>`;
  }

  wireProdukteStep() {
    const catalogRoot = this.content.querySelector(".catalog");
    const cartRoot = this.content.querySelector("#produkte-cart-step-root");

    // Adding a product from the catalog mutates state.cart directly but doesn't
    // go through wireCartStep's own re-render - refresh the preview panel here so
    // the just-added line shows up (wireCartStep's delegated listeners, bound to
    // cartRoot itself rather than its children, keep working after this innerHTML swap).
    const refreshCartPanel = () => {
      cartRoot.innerHTML = cartStepHtml(this.state.cart, this.commissionPercent, { showSubmit: false });
    };

    wireCatalogStep(catalogRoot, this.products, this.state.cart, () => {
      refreshCartPanel();
      this.updateProdukteWeiterBtn();
    });
    wireCartStep(cartRoot, this.state.cart, this.commissionPercent, () => this.updateProdukteWeiterBtn(), {
      showSubmit: false,
    });

    this.content.querySelector("#produkte-weiter-btn").addEventListener("click", () => {
      if (this.state.cart.length === 0) return;
      this.advanceTo("uebersicht");
    });
  }

  updateProdukteWeiterBtn() {
    const weiterBtn = this.content.querySelector("#produkte-weiter-btn");
    if (weiterBtn) weiterBtn.disabled = this.state.cart.length === 0;
  }

  // ---- Step: Übersicht ----

  uebersichtStepHtml() {
    const kunde = this.selectedKunde();
    return `
      <div class="wizard-step--uebersicht">
        <p>Bitte Bestellung überprüfen und kostenpflichtig aufgeben</p>
        <div class="uebersicht-layout">
          ${kunde ? kundeDetailsHtml(kunde) : "<div></div>"}
          <div class="cart cart--summary" id="cart-step-root">${cartSummaryHtml(this.state.cart, this.commissionPercent, { showSubmit: false })}</div>
        </div>
        <div class="wizard-step__actions">
          <button type="button" class="button-secondary" id="wizard-prev-btn">Zurück</button>
          <button type="button" id="cart-submit-btn" class="button-primary">Bestellung aufgeben</button>
        </div>
      </div>`;
  }

  wireUebersichtStep() {
    // Read-only review - no quantity/remove wiring here (per the "don't edit in
    // place" change); changing anything means going back via the checkpoint bar.
    wireCartSummaryThumbs(this.content.querySelector("#cart-step-root"));
    this.content.querySelector(".wizard-step--uebersicht").addEventListener("click", (event) => {
      if (event.target.closest("#cart-submit-btn")) this.handleSubmit();
    });
  }

  async handleSubmit() {
    if (this.submitting || this.state.cart.length === 0) return;
    this.submitting = true;
    const submitBtn = this.content.querySelector("#cart-submit-btn");
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "Wird gesendet…";
    }

    const payload = {
      kundeId: this.state.kundeMode === "existing" ? this.state.kundeId : null,
      newKunde: this.state.kundeMode === "new" ? this.state.newKunde : null,
      items: this.state.cart.map((i) => ({ productId: i.product.id, quantity: i.quantity })),
    };

    try {
      const order = await apiFetch("/partner/orders", { method: "POST", body: JSON.stringify(payload) });
      showToast("Bestellung aufgegeben.", "success");
      this.state.order = order;
      this.advanceTo("erfolg");
    } catch (error) {
      showToast(error.message, "error");
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = "Bestellung aufgeben";
      }
    } finally {
      this.submitting = false;
    }
  }

  // ---- Step: Erfolg ----

  reducedMotion() {
    return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  // A full-screen burst, not a thin trickle from the top edge: many pieces,
  // randomized size/delay/duration/horizontal drift, falling well past 100vh
  // (not a fixed rem distance, which stopped short on taller screens) so it
  // reads as a proper celebratory pop rather than a few specks sliding down.
  confettiHtml() {
    const pieces = Array.from({ length: 80 }, (_, i) => {
      const left = Math.round(Math.random() * 100);
      const delay = (Math.random() * 0.6).toFixed(2);
      const duration = (1.4 + Math.random() * 1.2).toFixed(2);
      const drift = Math.round((Math.random() - 0.5) * 16);
      const size = (0.4 + Math.random() * 0.5).toFixed(2);
      return `<span
        class="confetti-piece confetti-piece--${i % 5}"
        style="left:${left}%; width:${size}rem; height:${(size * 1.4).toFixed(2)}rem; --confetti-drift:${drift}rem; animation-delay:${delay}s; animation-duration:${duration}s;"
      ></span>`;
    }).join("");
    return `<div class="confetti" aria-hidden="true">${pieces}</div>`;
  }

  erfolgStepHtml() {
    // No separate Endkunde-details card here (unlike "Übersicht") - the
    // confirmation card below already shows the Endkunde on the simulated bill.
    return `
      <div class="wizard-step--erfolg">
        ${renderOrderConfirmationHtml(this.state.order)}
        ${this.reducedMotion() ? "" : this.confettiHtml()}
        <div class="wizard-step__actions">
          <button type="button" class="button-secondary" id="new-order-btn">Neue Bestellung</button>
          <a href="#/partner/orders" class="button-primary">Fertig</a>
        </div>
      </div>`;
  }

  wireErfolgStep() {
    this.content.querySelector("#new-order-btn").addEventListener("click", () => {
      this.resetState();
      this.renderStep();
    });
    if (!this.reducedMotion()) {
      // Longest possible piece lifetime is delay(0.6s) + duration(2.6s); give it
      // a margin so nothing gets cut off mid-fall.
      const confetti = this.content.querySelector(".confetti");
      if (confetti) setTimeout(() => confetti.remove(), 3500);
    }
  }
}

customElements.define("partner-order-new-page", PartnerOrderNewPage);
