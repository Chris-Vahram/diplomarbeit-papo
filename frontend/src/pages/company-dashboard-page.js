class CompanyDashboardPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--company");
    this.innerHTML = `
      <h1>Firmen-Dashboard</h1>
      <p>Platzhalter – Partnerverwaltung, Produktverwaltung, Bestellübersicht folgen in Phase 1–4.</p>
    `;
  }
}

customElements.define("company-dashboard-page", CompanyDashboardPage);
