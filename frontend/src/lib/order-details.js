// Reusable order-details markup (items, quantities, prices, Endkunde, partner,
// status) — shared by the company's and the partner's order-details overlay
// (order-management spec). Uses data the list endpoints already return, no new fetch.
import { escapeHtml } from "./html.js";
import { formatEuro, formatNumber, formatDate, formatAddress } from "./format.js";
import { itemsTableFragment } from "./order-form.js";
import { orderTotalsHtml } from "./price.js";
import { ORIGIN_LABELS } from "./origin.js";

export function renderOrderDetailsHtml(order, { showPartner = false } = {}) {
  const kundeName = `${order.kunde.firstName ? escapeHtml(order.kunde.firstName) + " " : ""}${escapeHtml(order.kunde.lastName)}`;
  const items = order.items.map((item) => ({
    name: item.productName,
    quantity: item.quantity,
    unitPrice: item.unitPriceSnapshot,
    unitNet: item.unitNetPrice,
    lineTotal: item.lineTotal,
    lineNet: item.lineNetTotal,
  }));

  return `
    <section class="order-details">
      <div class="print-document">
        <header class="print-letterhead print-only">
          <div class="print-letterhead__brand">PAPO</div>
          <div class="print-letterhead__title">Auftragsbeleg</div>
          <div class="print-letterhead__meta">
            <span class="print-letterhead__order-no">Bestellnummer ${order.id}</span>
            <span>${formatDate(order.orderDate)}</span>
          </div>
        </header>
        <dl class="profile-readonly">
          <dt>Bestellnummer</dt><dd>${order.id}</dd>
          <dt>Datum</dt><dd>${formatDate(order.orderDate)}</dd>
          ${showPartner ? `<dt>Partner</dt><dd>${escapeHtml(order.partnerName)}</dd>` : ""}
          <dt>Endkunde</dt><dd>${kundeName}, ${escapeHtml(formatAddress(order.kunde.address))}</dd>
          <dt>Herkunft</dt><dd>${ORIGIN_LABELS[order.origin] ?? "Portal"}${order.origin === "SHOP" ? " (Bestellung im Webshop)" : ""}</dd>
          <dt>Status</dt><dd><status-badge status="${order.status.toLowerCase()}"></status-badge></dd>
          ${order.commissionValue != null ? `<dt>Provision</dt><dd>${formatNumber(order.commissionPercentUsed)}% (${formatEuro(order.commissionValue)})</dd>` : ""}
        </dl>
        ${itemsTableFragment(items)}
        ${orderTotalsHtml({ gross: order.orderValue, net: order.netValue, tax: order.taxValue })}
      </div>
      <button type="button" class="print-button" data-action="print">Beleg drucken</button>
    </section>
  `;
}
