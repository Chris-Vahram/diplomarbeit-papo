// Gross/net price markup (article-tax spec): prices are stored gross; the portal shows the
// gross value with "inkl. USt" and the net value in small type below. One helper so catalog,
// cart, lists, order details and the printed document all read the same way.
import { formatEuro, formatNumber } from "./format.js";
import { netCentsHalfUp, toCents } from "./money.js";

export function netOf(gross, ratePercent) {
  return netCentsHalfUp([{ grossCents: toCents(gross), ratePercent }]) / 100;
}

// `euro: false` for table cells whose header already carries "(€)".
export function priceStackHtml(gross, net, { euro = true, taxLabel = false } = {}) {
  const format = euro ? formatEuro : formatNumber;
  return `<span class="price-stack"><span class="price-stack__gross">${format(gross)}${
    taxLabel ? ` <span class="price-stack__tax">inkl. USt</span>` : ""
  }</span><span class="price-stack__net">netto ${format(net)}</span></span>`;
}

// Totals block under item lists: gross is the figure that counts, net and USt follow.
export function orderTotalsHtml({ gross, net, tax }, { live = false } = {}) {
  return `
    <dl class="price-totals">
      <dt class="price-totals__label">Gesamtwert <span class="price-stack__tax">inkl. USt</span></dt>
      <dd class="price-totals__gross"${live ? ` aria-live="polite"` : ""}>${formatEuro(gross)}</dd>
      <dt>Netto</dt><dd>${formatEuro(net)}</dd>
      <dt>USt</dt><dd>${formatEuro(tax)}</dd>
    </dl>`;
}

// Totals of not yet stored lines [{ product, quantity }] — same rounding as the backend.
export function cartTotals(lines) {
  const grossCents = lines.reduce((sum, l) => sum + toCents(l.product.price) * l.quantity, 0);
  const netCents = netCentsHalfUp(lines.map((l) => ({
    grossCents: toCents(l.product.price) * l.quantity,
    ratePercent: l.product.taxRatePercent,
  })));
  return { grossCents, netCents, gross: grossCents / 100, net: netCents / 100, tax: (grossCents - netCents) / 100 };
}
