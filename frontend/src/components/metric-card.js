// <metric-card label="Provision" value="1.234 €" icon="percent" tone="positive"
//   value-tone="brand" trend="up" trend-label="+12,4% ggü. Vorperiode"
//   sparkline="40,65,30,90,55,70" ring="68" ring-caption="27 von 40 Produkten"></metric-card>
//
// icon: optional Lucide icon name — shown as a large, near-invisible watermark
//   in the card's corner.
// tone: "brand" (default) | "positive" — tints the watermark only.
// value-tone: "brand" (default, role accent) | "positive" (semantic success) —
//   the BIG number's color. Keep "brand" when the small badge/arrow already
//   carries the semantic (up/down) meaning, so the two don't fight for it.
// trend: "up" | "down" — draws a small colored arrow badge (green/red) instead
//   of a plain dot. trend-label is the small caption shown under the label
//   explaining what the arrow compares against (e.g. previous period).
// sparkline: optional comma-separated relative values (0-100) rendered as a
//   tiny bar strip — a real-data visual, not more text.
// ring: optional 0-100 — a small "part of whole" completion ring (e.g. enabled
//   vs. total catalog). ring-caption is the tiny label under it. Mutually
//   exclusive with sparkline (ring takes priority if both are set).
// ring-layout: "aside" (default, small ring in the corner) | "full" — a large
//   ring filling the card's full height on the left, with the value/label
//   stack to its right. Use "full" when the ring IS the card's main figure,
//   not a secondary annotation (icon watermark/badge are omitted in this mode).
class MetricCard extends HTMLElement {
  static get observedAttributes() {
    return ["label", "value", "icon", "tone", "value-tone", "trend", "trend-label", "sparkline", "ring", "ring-caption", "ring-layout"];
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
    const icon = this.getAttribute("icon");
    const tone = this.getAttribute("tone") === "positive" ? "positive" : "brand";
    const valueTone = this.getAttribute("value-tone") === "positive" ? "positive" : "brand";
    const trend = this.getAttribute("trend");
    const trendLabel = this.getAttribute("trend-label");
    const sparkline = this.getAttribute("sparkline");
    const ring = this.getAttribute("ring");
    const ringCaption = this.getAttribute("ring-caption");
    const ringLayout = this.getAttribute("ring-layout") === "full" ? "full" : "aside";

    if (ring && ringLayout === "full") {
      this.innerHTML = `
        <div class="metric-card metric-card--ring-full">
          <div class="metric-card__ring-full-wrap">
            <div class="metric-card__ring-full" style="--pct:${ring}">
              <span class="metric-card__ring-full-label">${ring}%</span>
            </div>
          </div>
          <div class="metric-card__body">
            <div class="metric-card__value metric-card__value--${valueTone}">${value}</div>
            <div class="metric-card__label">${label}</div>
            ${ringCaption ? `<div class="metric-card__ring-caption metric-card__ring-caption--full">${ringCaption}</div>` : ""}
          </div>
        </div>
      `;
      return;
    }

    let badge = `<div class="metric-card__icon"></div>`;
    if (trend === "up") {
      badge = `<div class="metric-card__icon metric-card__icon--up"><i data-lucide="arrow-up-right"></i></div>`;
    } else if (trend === "down") {
      badge = `<div class="metric-card__icon metric-card__icon--down"><i data-lucide="arrow-down-right"></i></div>`;
    } else if (tone === "positive") {
      badge = `<div class="metric-card__icon metric-card__icon--up"><i data-lucide="arrow-up-right"></i></div>`;
    }

    let extra = "";
    if (ring) {
      extra = `
        <div class="metric-card__aside">
          <div class="metric-card__ring" style="--pct:${ring}"></div>
          ${ringCaption ? `<span class="metric-card__ring-caption">${ringCaption}</span>` : ""}
        </div>`;
    } else if (sparkline) {
      const bars = sparkline
        .split(",")
        .map((n) => Number(n.trim()) || 0)
        .map((n, i, arr) => `<span style="height:${Math.max(8, n)}%" class="${i === arr.length - 1 ? "is-current" : ""}"></span>`)
        .join("");
      extra = `<div class="metric-card__aside"><div class="metric-card__sparkline">${bars}</div></div>`;
    }

    this.innerHTML = `
      <div class="metric-card metric-card--${tone}">
        ${icon ? `<div class="metric-card__watermark"><i data-lucide="${icon}"></i></div>` : ""}
        ${badge}
        <div class="metric-card__body">
          <div class="metric-card__value metric-card__value--${valueTone}">${value}</div>
          <div class="metric-card__label">${label}</div>
          ${trendLabel ? `<div class="metric-card__trend metric-card__trend--${trend === "down" ? "down" : "up"}">${trendLabel}</div>` : ""}
        </div>
        ${extra}
      </div>
    `;
    window.lucide?.createIcons();
  }
}

customElements.define("metric-card", MetricCard);
