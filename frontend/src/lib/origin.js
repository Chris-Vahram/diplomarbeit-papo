// Order and Endkunde origin (order-management / customer-management "origin"): entered in the
// portal or created by a guest order in the partner's shop. Only shop records get a badge, so
// the lists stay quiet for the usual portal case; the filter offers both.
import { escapeHtml } from "./html.js";

export const ORIGIN_LABELS = { INTERNAL: "Portal", PORTAL: "Portal", SHOP: "Shop" };

export function originBadgeHtml(origin) {
  return origin === "SHOP" ? ` <span class="origin-badge" title="Über den Webshop bestellt">Shop</span>` : "";
}

// Name cell with badge; the raw name stays a separate row field for search and filters.
export function nameWithOriginHtml(name, origin) {
  return `${escapeHtml(name)}${originBadgeHtml(origin)}`;
}

// Filter values: "SHOP" or the portal value of the respective entity.
export function originFilter(portalValue) {
  return {
    key: "origin",
    type: "select",
    label: "Herkunft",
    options: [
      { value: portalValue, label: "Portal" },
      { value: "SHOP", label: "Shop" },
    ],
  };
}
