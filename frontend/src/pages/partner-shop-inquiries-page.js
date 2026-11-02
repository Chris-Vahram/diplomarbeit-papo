import { apiFetch } from "../lib/api.js";
import { escapeHtml } from "../lib/html.js";
import { stateHtml } from "../lib/states.js";
import { refreshInquiriesBadge } from "../lib/router.js";
import { showToast } from "../components/toast-notice.js";

// Inbox "Anfragen" under "Mein Webshop" (shop-inquiries spec). Opening an inquiry marks it as
// read; the partner marks it as done or deletes it for good (inline confirmation, no dialog).

const VIEWS = [
  ["open", "Offen"],
  ["done", "Erledigt"],
  ["all", "Alle"],
];

const dateTime = new Intl.DateTimeFormat("de-AT", { dateStyle: "medium", timeStyle: "short" });
const shortDate = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

function received(iso) {
  return dateTime.format(new Date(iso));
}

class PartnerShopInquiriesPage extends HTMLElement {
  #inquiries = [];
  #view = "open";
  #openId = null;

  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <div class="list-header">
        <h1>Anfragen</h1>
        <a class="button-secondary" href="#/partner/shop">Zu „Mein Webshop“</a>
      </div>
      <p class="page-intro">Fragen und Angebotswünsche aus dem Anfrageformular Ihres Webshops. Nur Sie sehen diese Anfragen.</p>
      <div class="inquiry-views" role="group" aria-label="Anfragen anzeigen">
        ${VIEWS.map(([key, label]) => `<button type="button" class="inquiry-views__button" data-view="${key}" aria-pressed="${key === this.#view}">
          ${label}<span class="inquiry-views__count" data-count="${key}"></span></button>`).join("")}
      </div>
      <div id="inquiry-list" aria-live="polite"></div>
    `;
    this.addEventListener("click", (event) => this.#onClick(event));
    this.load();
  }

  async load() {
    const list = this.querySelector("#inquiry-list");
    list.innerHTML = stateHtml("loading", "Anfragen werden geladen …");
    window.lucide?.createIcons();
    try {
      this.#inquiries = await apiFetch("/partner/shop/inquiries");
      this.#render();
    } catch (error) {
      list.innerHTML = stateHtml("error", "Anfragen konnten nicht geladen werden.", {
        hint: error.message,
        action: '<button type="button" class="button-secondary" data-action="reload">Erneut laden</button>',
      });
      window.lucide?.createIcons();
    }
  }

  #visible() {
    if (this.#view === "open") return this.#inquiries.filter((inquiry) => !inquiry.doneAt);
    if (this.#view === "done") return this.#inquiries.filter((inquiry) => inquiry.doneAt);
    return this.#inquiries;
  }

  #render() {
    const open = this.#inquiries.filter((inquiry) => !inquiry.doneAt).length;
    const counts = { open, done: this.#inquiries.length - open, all: this.#inquiries.length };
    this.querySelectorAll("[data-count]").forEach((element) => {
      element.textContent = String(counts[element.dataset.count]);
    });
    this.querySelectorAll("[data-view]").forEach((button) => {
      button.setAttribute("aria-pressed", String(button.dataset.view === this.#view));
    });

    const list = this.querySelector("#inquiry-list");
    const visible = this.#visible();
    if (!visible.length) {
      const [title, hint] = this.#inquiries.length === 0
        ? ["Noch keine Anfragen.", "Fügen Sie in „Mein Webshop“ einer Seite den Abschnitt „Anfrageformular“ hinzu, zum Beispiel auf der Kontaktseite."]
        : this.#view === "open"
          ? ["Keine offenen Anfragen.", "Alles erledigt."]
          : ["Keine erledigten Anfragen.", ""];
      list.innerHTML = stateHtml("empty", title, { hint });
      window.lucide?.createIcons();
      return;
    }
    list.innerHTML = `<ul class="inquiry-list">${visible.map((inquiry) => this.#itemHtml(inquiry)).join("")}</ul>`;
    window.lucide?.createIcons();
  }

  #itemHtml(inquiry) {
    const open = inquiry.id === this.#openId;
    const unread = !inquiry.readAt;
    const detailId = `inquiry-${inquiry.id}`;
    const subject = `Ihre Anfrage${inquiry.articleName ? ` zu ${inquiry.articleName}` : ""}`;
    const phoneDigits = (inquiry.phone ?? "").replace(/[^\d+]/g, "");
    const contact = [
      inquiry.email
        ? `<dt>E-Mail</dt><dd><a href="mailto:${escapeHtml(inquiry.email)}">${escapeHtml(inquiry.email)}</a></dd>`
        : "",
      inquiry.phone ? `<dt>Telefon</dt><dd><a href="tel:${escapeHtml(phoneDigits)}">${escapeHtml(inquiry.phone)}</a></dd>` : "",
      inquiry.articleName ? `<dt>Artikel</dt><dd>${escapeHtml(inquiry.articleName)}</dd>` : "",
      `<dt>Eingegangen</dt><dd>${escapeHtml(received(inquiry.createdAt))}</dd>`,
    ].join("");
    const reply = inquiry.email
      ? `<a class="button-primary" href="mailto:${escapeHtml(inquiry.email)}?subject=${encodeURIComponent(subject)}">
          <i data-lucide="reply" aria-hidden="true"></i>Per E-Mail antworten</a>`
      : phoneDigits
        ? `<a class="button-primary" href="tel:${escapeHtml(phoneDigits)}"><i data-lucide="phone" aria-hidden="true"></i>Anrufen</a>`
        : "";
    return `<li class="inquiry${unread ? " inquiry--unread" : ""}${inquiry.doneAt ? " inquiry--done" : ""}" data-id="${inquiry.id}">
      <button type="button" class="inquiry__summary" data-action="toggle" aria-expanded="${open}" aria-controls="${detailId}">
        <span class="inquiry__head">
          <span class="inquiry__name">${escapeHtml(inquiry.name)}</span>
          ${unread ? '<span class="status-badge status-badge--submitted">Neu</span>' : ""}
          ${inquiry.doneAt ? '<span class="status-badge status-badge--fulfilled">Erledigt</span>' : ""}
          <time class="inquiry__date" datetime="${escapeHtml(inquiry.createdAt)}">${escapeHtml(shortDate.format(new Date(inquiry.createdAt)))}</time>
        </span>
        <span class="inquiry__preview">${inquiry.articleName ? `<span class="inquiry__article">${escapeHtml(inquiry.articleName)}</span> · ` : ""}${escapeHtml(inquiry.message)}</span>
        <i data-lucide="chevron-down" class="inquiry__chevron" aria-hidden="true"></i>
      </button>
      <div class="inquiry__detail" id="${detailId}" ${open ? "" : "hidden"}>
        <p class="inquiry__message">${escapeHtml(inquiry.message)}</p>
        <dl class="inquiry__meta">${contact}</dl>
        <div class="inquiry__actions">
          ${reply}
          <button type="button" class="button-secondary" data-action="done">${inquiry.doneAt ? "Wieder öffnen" : "Als erledigt markieren"}</button>
          <button type="button" class="button-ghost inquiry__delete" data-action="ask-delete"><i data-lucide="trash-2" aria-hidden="true"></i>Löschen</button>
        </div>
        <div class="inquiry__confirm" role="group" aria-label="Löschen bestätigen" hidden>
          <p>Diese Anfrage endgültig löschen? Das lässt sich nicht rückgängig machen.</p>
          <div class="inquiry__actions">
            <button type="button" class="button-danger" data-action="delete">Endgültig löschen</button>
            <button type="button" class="button-secondary" data-action="cancel-delete">Abbrechen</button>
          </div>
        </div>
      </div>
    </li>`;
  }

  #find(id) {
    return this.#inquiries.find((inquiry) => inquiry.id === id);
  }

  #replace(updated) {
    this.#inquiries = this.#inquiries.map((inquiry) => (inquiry.id === updated.id ? updated : inquiry));
  }

  async #onClick(event) {
    const target = event.target.closest("[data-action], [data-view]");
    if (!target) return;
    if (target.dataset.view) {
      this.#view = target.dataset.view;
      this.#render();
      return;
    }
    const action = target.dataset.action;
    if (action === "reload") {
      this.load();
      return;
    }
    const item = target.closest(".inquiry");
    const id = Number(item?.dataset.id);
    if (!item) return;

    if (action === "toggle") {
      this.#toggle(item, id);
    } else if (action === "ask-delete" || action === "cancel-delete") {
      const confirm = item.querySelector(".inquiry__confirm");
      confirm.hidden = action === "cancel-delete";
      item.querySelector(".inquiry__detail > .inquiry__actions").hidden = !confirm.hidden;
      item.querySelector(confirm.hidden ? '[data-action="ask-delete"]' : '[data-action="delete"]').focus();
    } else if (action === "done") {
      await this.#run(target, async () => {
        const inquiry = this.#find(id);
        this.#replace(await apiFetch(`/partner/shop/inquiries/${id}/done`, {
          method: "PUT",
          body: JSON.stringify({ done: !inquiry.doneAt }),
        }));
        showToast(inquiry.doneAt ? "Anfrage wieder geöffnet." : "Anfrage als erledigt markiert.", "success");
        refreshInquiriesBadge();
      });
    } else if (action === "delete") {
      await this.#run(target, async () => {
        await apiFetch(`/partner/shop/inquiries/${id}`, { method: "DELETE" });
        this.#inquiries = this.#inquiries.filter((inquiry) => inquiry.id !== id);
        if (this.#openId === id) this.#openId = null;
        showToast("Anfrage gelöscht.", "success");
        refreshInquiriesBadge();
      });
    }
  }

  #toggle(item, id) {
    const opening = this.#openId !== id;
    this.querySelectorAll('.inquiry__summary[aria-expanded="true"]').forEach((summary) => {
      summary.setAttribute("aria-expanded", "false");
      summary.nextElementSibling.hidden = true;
    });
    this.#openId = opening ? id : null;
    if (!opening) return;
    item.querySelector(".inquiry__summary").setAttribute("aria-expanded", "true");
    item.querySelector(".inquiry__detail").hidden = false;
    const inquiry = this.#find(id);
    if (!inquiry.readAt) this.#markRead(item, id);
  }

  // Read is a side effect of opening; a failure keeps the item unread and stays quiet.
  async #markRead(item, id) {
    try {
      this.#replace(await apiFetch(`/partner/shop/inquiries/${id}/read`, { method: "POST" }));
      item.classList.remove("inquiry--unread");
      item.querySelector(".status-badge--submitted")?.remove();
      refreshInquiriesBadge();
    } catch {
      // Stays marked as new; the next opening tries again.
    }
  }

  async #run(button, task) {
    button.disabled = true;
    try {
      await task();
      this.#render();
    } catch (error) {
      button.disabled = false;
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-shop-inquiries-page", PartnerShopInquiriesPage);
