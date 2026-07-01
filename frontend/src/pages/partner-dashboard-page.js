class PartnerDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Partner-Dashboard</h1>
      <p>Platzhalter – Provision, Umsatz, Bestellungen, freigegebene Produkte folgen in Phase 4–5.</p>
    `;
  }
}

customElements.define("partner-dashboard-page", PartnerDashboardPage);
