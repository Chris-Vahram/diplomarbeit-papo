// <metric-card label="Provision" value="1.234 €"></metric-card>
class MetricCard extends HTMLElement {
  static get observedAttributes() {
    return ["label", "value"];
  }

  connectedCallback() {
    this.render();
  }

  attributeChangedCallback() {
    this.render();
  }

  render() {
    const label = this.getAttribute("label") ?? "";
    const value = this.getAttribute("value") ?? "";
    this.innerHTML = `
      <div class="metric-card">
        <div class="metric-card__value">${value}</div>
        <div class="metric-card__label">${label}</div>
      </div>
    `;
  }
}

customElements.define("metric-card", MetricCard);
