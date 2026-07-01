// <status-badge status="submitted"></status-badge>
// Known statuses: submitted, reviewed, fulfilled (order), active, inactive (partner).
const LABELS = {
  submitted: "Eingereicht",
  reviewed: "Geprüft",
  fulfilled: "Abgeschlossen",
  active: "Aktiv",
  inactive: "Inaktiv",
};

class StatusBadge extends HTMLElement {
  static get observedAttributes() {
    return ["status"];
  }

  connectedCallback() {
    this.render();
  }

  attributeChangedCallback() {
    this.render();
  }

  render() {
    const status = this.getAttribute("status") ?? "";
    const label = LABELS[status] ?? status;
    this.innerHTML = `<span class="status-badge status-badge--${status}">${label}</span>`;
  }
}

customElements.define("status-badge", StatusBadge);
