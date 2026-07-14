import { apiFetch } from "../lib/api.js";
import { showToast } from "../components/toast-notice.js";
import { formatAddress, formatDate } from "../lib/format.js";

// Decided (Chris, 2026-07-13): 60 days for now, revisit with the client at the
// next Termin (design.md, decision 5).
const DORMANT_THRESHOLD_DAYS = 60;

// Whole-calendar-day diff, not elapsed milliseconds - avoids boundary
// misclassification across timezones/DST (design.md, decision 5b).
function daysSinceLastOrder(isoDate) {
  const [year, month, day] = isoDate.split("-").map(Number);
  const then = Date.UTC(year, month - 1, day);
  const now = new Date();
  const today = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
  return Math.round((today - then) / 86400000);
}

function isDormant(lastOrderDate) {
  if (!lastOrderDate) return true;
  return daysSinceLastOrder(lastOrderDate) > DORMANT_THRESHOLD_DAYS;
}

class PartnerCustomerListPage extends HTMLElement {
  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <h1>Meine Endkunden</h1>
      <data-table id="customer-table"></data-table>
    `;

    this.table = this.querySelector("#customer-table");
    this.table.columns = [
      { key: "name", label: "Name" },
      { key: "addressLabel", label: "Adresse" },
      { key: "lastOrderCell", label: "Letzte Bestellung", html: true },
    ];
    this.table.filters = [
      {
        key: "aktivitaet",
        type: "select",
        label: "Aktivität",
        options: [
          { value: "inaktiv", label: "Länger keine Bestellung" },
          { value: "aktiv", label: "Kürzlich bestellt" },
        ],
      },
    ];
    this.table.emptyMessage = "Noch keine Endkunden angelegt.";
    this.table.searchKeys = ["name"];
    this.table.pageSize = 20;

    this.load();
  }

  async load() {
    try {
      const kunden = await apiFetch("/partner/customers");
      const rows = kunden
        .map((k) => {
          const dormant = isDormant(k.lastOrderDate);
          const dateText = k.lastOrderDate ? formatDate(k.lastOrderDate) : "Noch keine Bestellung";
          const badge = dormant ? ` <span class="dormant-badge">Inaktiv</span>` : "";
          return {
            name: `${k.firstName ? k.firstName + " " : ""}${k.lastName}`,
            addressLabel: formatAddress(k.address),
            lastOrderCell: `${dateText}${badge}`,
            lastOrderDate: k.lastOrderDate,
            aktivitaet: dormant ? "inaktiv" : "aktiv",
            rowClass: dormant ? "data-table__row--dormant" : "",
          };
        })
        // Oldest-first default: never-ordered (null) Endkunden first, then oldest
        // to newest last order (design.md, decision 6).
        .sort((a, b) => {
          if (!a.lastOrderDate) return b.lastOrderDate ? -1 : 0;
          if (!b.lastOrderDate) return 1;
          return a.lastOrderDate < b.lastOrderDate ? -1 : a.lastOrderDate > b.lastOrderDate ? 1 : 0;
        });
      this.table.rows = rows;
    } catch (error) {
      showToast(error.message, "error");
    }
  }
}

customElements.define("partner-customer-list-page", PartnerCustomerListPage);
