import { apiFetch } from "../lib/api.js";
import { presetPeriod, toLocalIsoDate, PERIOD_PRESETS } from "../lib/dashboard-data.js";
import { formatEuro } from "../lib/format.js";
import { escapeHtml } from "../lib/html.js";
import { shopStatusLabel } from "../lib/shop-status.js";
import { stateHtml } from "../lib/states.js";

// "Webshop-Statistik" under "Mein Webshop" (shop-statistics spec). Period Woche/Monat/Jahr
// (Monat by default, Protokoll 12.10.); the server returns the period and the previous one of
// equal length, so every KPI shows its change. Unique/returning visitors appear only when
// consenting visitors were counted.

const MONTHS = ["Jän", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"];
const WEEKDAYS = ["So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"];
const DEVICES = ["Desktop", "Mobil", "Tablet"];
const count = new Intl.NumberFormat("de-AT");
const oneDecimal = new Intl.NumberFormat("de-AT", { minimumFractionDigits: 1, maximumFractionDigits: 1 });
const dayMonth = new Intl.DateTimeFormat("de-AT", { day: "numeric", month: "numeric" });
const fullDate = new Intl.DateTimeFormat("de-AT", { day: "numeric", month: "numeric", year: "numeric" });
const longDay = new Intl.DateTimeFormat("de-AT", { weekday: "short", day: "numeric", month: "numeric" });

function parseDay(iso) {
  const [year, month, day] = iso.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function rangeLabel(fromIso, toIso) {
  const from = parseDay(fromIso);
  const to = parseDay(toIso);
  if (fromIso === toIso) return fullDate.format(to);
  return `${from.getFullYear() === to.getFullYear() ? dayMonth.format(from) : fullDate.format(from)}–${fullDate.format(to)}`;
}

// Change against the previous period. kind "points" compares percentages in percentage points.
function delta(current, previous, kind = "relative") {
  if (current == null || previous == null) return { tone: "none", text: "–", spoken: "kein Vergleich möglich" };
  if (kind === "points") {
    const diff = current - previous;
    if (Math.abs(diff) < 0.05) return { tone: "flat", text: "±0,0 Pkt.", spoken: "unverändert gegenüber der Vorperiode" };
    const text = `${diff > 0 ? "+" : "−"}${oneDecimal.format(Math.abs(diff))} Pkt.`;
    return { tone: diff > 0 ? "up" : "down", text, spoken: `${text.replace("Pkt.", "Prozentpunkte")} gegenüber der Vorperiode` };
  }
  if (previous === 0) {
    return current === 0
      ? { tone: "flat", text: "±0 %", spoken: "unverändert gegenüber der Vorperiode" }
      : { tone: "up", text: "neu", spoken: "neu, in der Vorperiode keine" };
  }
  const pct = ((current - previous) / previous) * 100;
  if (Math.abs(pct) < 0.05) return { tone: "flat", text: "±0 %", spoken: "unverändert gegenüber der Vorperiode" };
  const text = `${pct > 0 ? "+" : "−"}${oneDecimal.format(Math.abs(pct))} %`;
  return { tone: pct > 0 ? "up" : "down", text, spoken: `${text} gegenüber der Vorperiode` };
}

const DELTA_ICON = { up: "arrow-up-right", down: "arrow-down-right", flat: "minus" };

function kpiHtml(label, value, change) {
  const icon = DELTA_ICON[change.tone];
  return `
    <div class="stat-kpi">
      <dt class="stat-kpi__label">${label}</dt>
      <dd class="stat-kpi__value">${value}</dd>
      <dd class="stat-kpi__delta stat-kpi__delta--${change.tone}">
        ${icon ? `<i data-lucide="${icon}" aria-hidden="true"></i>` : ""}
        <span aria-hidden="true">${change.text}</span>
        <span class="visually-hidden">${change.spoken}</span>
      </dd>
    </div>`;
}

function percent(value) {
  return value == null ? "–" : `${oneDecimal.format(Number(value))} %`;
}

// Day buckets for week/month, month buckets for the year.
function buckets(days, byMonth) {
  if (!byMonth) {
    return days.map((day) => {
      const date = parseDay(day.day);
      return { label: date.getDate(), full: longDay.format(date), weekday: WEEKDAYS[date.getDay()], visits: day.visits, orders: day.orders };
    });
  }
  const months = new Map();
  for (const day of days) {
    const key = day.day.slice(0, 7);
    const entry = months.get(key) ?? { label: MONTHS[Number(key.slice(5)) - 1], full: MONTHS[Number(key.slice(5)) - 1], visits: 0, orders: 0 };
    entry.visits += day.visits;
    entry.orders += day.orders;
    months.set(key, entry);
  }
  return [...months.values()];
}

function chartHtml(stats, preset) {
  const bars = buckets(stats.days, preset === "year");
  const max = Math.max(...bars.map((bar) => bar.visits), 0);
  const unit = preset === "year" ? "Monat" : "Tag";
  if (max === 0) {
    return `<section class="chart-card stat-chart" aria-labelledby="stat-chart-title">
      <h2 class="chart-card__title" id="stat-chart-title">Besuche pro ${unit}</h2>
      ${stateHtml("empty", "Noch keine Besuche in diesem Zeitraum.", { hint: "Sobald Besucher Ihren Webshop öffnen, erscheinen sie hier." })}
    </section>`;
  }
  // Label every bar for a week or a year, about weekly for a month.
  const showLabel = (index) => bars.length <= 12 || index % 7 === 0 || index === bars.length - 1;
  const columns = bars.map((bar, index) => {
    const height = bar.visits === 0 ? 0 : Math.max(3, (bar.visits / max) * 100);
    const tip = `${bar.full}: ${count.format(bar.visits)} ${bar.visits === 1 ? "Besuch" : "Besuche"}, ${count.format(bar.orders)} ${bar.orders === 1 ? "Bestellung" : "Bestellungen"}`;
    return `<div class="stat-chart__col${index === bars.length - 1 ? " stat-chart__col--current" : ""}" title="${tip}">
      <div class="stat-chart__bar" style="--h:${height.toFixed(1)}%">${bar.orders > 0 ? `<span class="stat-chart__order" aria-hidden="true"></span>` : ""}</div>
      <span class="stat-chart__x" aria-hidden="true">${showLabel(index) ? (preset === "week" ? bar.weekday : bar.label) : ""}</span>
    </div>`;
  }).join("");
  const rows = bars.map((bar) => `<tr><th scope="row">${bar.full}</th><td>${bar.visits}</td><td>${bar.orders}</td></tr>`).join("");
  return `<section class="chart-card stat-chart" aria-labelledby="stat-chart-title">
    <div class="stat-chart__head">
      <h2 class="chart-card__title" id="stat-chart-title">Besuche pro ${unit}</h2>
      <p class="stat-chart__legend" aria-hidden="true">
        <span><span class="stat-chart__swatch"></span>Besuche</span>
        <span><span class="stat-chart__order stat-chart__order--legend"></span>mit Bestellung</span>
      </p>
    </div>
    <div class="stat-chart__plot" style="--cols:${bars.length}" aria-hidden="true">
      <span class="stat-chart__max">${count.format(max)}</span>
      ${columns}
    </div>
    <table class="visually-hidden">
      <caption>Besuche und Bestellungen pro ${unit}</caption>
      <thead><tr><th scope="col">${unit}</th><th scope="col">Besuche</th><th scope="col">Bestellungen</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
  </section>`;
}

function topArticlesHtml(articles) {
  if (!articles.length) {
    return stateHtml("empty", "Noch keine Artikelaufrufe.", { hint: "Hier stehen die zehn meistbesuchten Artikel." });
  }
  return `<table class="stat-table">
    <thead><tr><th scope="col">Artikel</th><th scope="col">Aufrufe</th><th scope="col">In den Warenkorb</th></tr></thead>
    <tbody>${articles.map((article, index) => `<tr>
      <th scope="row"><span class="stat-table__cell"><span class="stat-table__rank" aria-hidden="true">${index + 1}</span><span class="stat-table__name">${escapeHtml(article.name)}</span></span></th>
      <td>${count.format(article.views)}</td>
      <td>${count.format(article.addToCart)}</td>
    </tr>`).join("")}</tbody>
  </table>`;
}

// Share bars for referrers and devices; the number stays the primary information.
function shareListHtml(rows, emptyText) {
  const total = rows.reduce((sum, row) => sum + row.count, 0);
  if (total === 0) return `<p class="stat-empty">${emptyText}</p>`;
  return `<ul class="stat-shares">${rows.map((row) => {
    const share = (row.count / total) * 100;
    return `<li class="stat-shares__row">
      <span class="stat-shares__label" title="${escapeHtml(row.label)}">${escapeHtml(row.label)}</span>
      <span class="stat-shares__value">${count.format(row.count)}<span class="stat-shares__pct"> · ${Math.round(share)} %</span></span>
      <span class="stat-shares__track" aria-hidden="true"><span style="width:${share.toFixed(1)}%"></span></span>
    </li>`;
  }).join("")}</ul>`;
}

function visitorsHtml(current, previous) {
  if (current.uniqueVisitors == null) {
    return `<p class="stat-consent-note"><i data-lucide="shield-check" aria-hidden="true"></i>
      <span>Eindeutige und wiederkehrende Besucher werden nur gezählt, wenn Besucher der Statistik zustimmen. In diesem Zeitraum hat noch niemand zugestimmt.</span></p>`;
  }
  return `<section class="chart-card stat-visitors" aria-labelledby="stat-visitors-title">
    <h2 class="chart-card__title" id="stat-visitors-title">Besucher mit Zustimmung</h2>
    <dl class="stat-kpis stat-kpis--pair">
      ${kpiHtml("Eindeutige Besucher", count.format(current.uniqueVisitors), delta(current.uniqueVisitors, previous.uniqueVisitors ?? 0))}
      ${kpiHtml("Davon wiederkehrend", count.format(current.returningVisitors), delta(current.returningVisitors, previous.returningVisitors ?? 0))}
    </dl>
    <p class="stat-visitors__note">Enthält nur Besucher, die der Statistik zugestimmt haben. Ohne Zustimmung wird niemand wiedererkannt.</p>
  </section>`;
}

class PartnerShopStatisticsPage extends HTMLElement {
  #preset = "month";
  #request = 0;

  connectedCallback() {
    this.classList.add("area--partner");
    this.innerHTML = `
      <div class="list-header">
        <h1>Webshop-Statistik</h1>
        <a class="button-secondary" href="#/partner/shop">Zu „Mein Webshop“</a>
      </div>
      <p class="page-intro">So wird Ihr Webshop genutzt. Gezählt wird anonym und ohne IP-Adressen. Bestellungen und Umsatz umfassen alle Bestellungen aus dem Webshop, unabhängig vom Status.</p>
      <div class="stat-toolbar">
        <div class="segmented" role="group" aria-label="Zeitraum">
          ${Object.entries(PERIOD_PRESETS).map(([kind, label]) => `<button type="button" class="segmented__button" data-preset="${kind}" aria-pressed="${kind === this.#preset}">${label}</button>`).join("")}
        </div>
        <p class="stat-toolbar__range" id="stat-range"></p>
      </div>
      <div id="stat-shop-notice"></div>
      <div class="stat-body" id="stat-body" aria-live="polite"></div>
    `;
    this.body = this.querySelector("#stat-body");
    this.querySelector(".segmented").addEventListener("click", (event) => {
      const button = event.target.closest("[data-preset]");
      if (!button || button.dataset.preset === this.#preset) return;
      this.#preset = button.dataset.preset;
      for (const other of this.querySelectorAll("[data-preset]")) other.setAttribute("aria-pressed", String(other === button));
      this.load();
    });
    this.body.addEventListener("click", (event) => {
      if (event.target.closest("[data-action='reload']")) this.load();
    });
    this.body.innerHTML = stateHtml("loading", "Statistik wird geladen …");
    window.lucide?.createIcons();
    this.load();
    this.loadShopStatus();
  }

  // Without an online shop no new visits arrive; say so instead of letting zeros look like a problem.
  async loadShopStatus() {
    try {
      const shop = await apiFetch("/partner/shop");
      if (!this.isConnected || (shop.status === "ONLINE" && shop.publicEnabled)) return;
      this.querySelector("#stat-shop-notice").innerHTML = `<p class="stat-notice" role="status">
        <i data-lucide="info" aria-hidden="true"></i>
        <span>Ihr Webshop ist derzeit nicht öffentlich erreichbar (${escapeHtml(shopStatusLabel(shop.status))}). Solange kommen keine neuen Besuche hinzu.</span></p>`;
      window.lucide?.createIcons();
    } catch {
      // the notice is optional
    }
  }

  async load() {
    const request = ++this.#request;
    const period = presetPeriod(this.#preset);
    this.body.setAttribute("aria-busy", "true");
    try {
      const stats = await apiFetch(`/partner/shop/statistics?from=${toLocalIsoDate(period.from)}&to=${toLocalIsoDate(period.to)}`);
      if (request !== this.#request || !this.isConnected) return;
      this.render(stats);
    } catch (error) {
      if (request !== this.#request || !this.isConnected) return;
      this.body.innerHTML = stateHtml("error", "Die Statistik konnte nicht geladen werden.", {
        hint: error.message,
        action: '<button type="button" class="button-secondary" data-action="reload">Erneut laden</button>',
      });
      window.lucide?.createIcons();
    } finally {
      if (request === this.#request) this.body.removeAttribute("aria-busy");
    }
  }

  render(stats) {
    const { current, previous } = stats;
    this.querySelector("#stat-range").textContent =
      `${rangeLabel(stats.from, stats.to)} · Vergleich mit ${rangeLabel(stats.previousFrom, stats.previousTo)}`;
    const devices = DEVICES.map((label) => ({ label, count: stats.devices.find((row) => row.label === label)?.count ?? 0 }));
    const referrers = stats.referrers.map((row) => ({ label: row.label || "Direkt oder unbekannt", count: row.count }));

    this.body.innerHTML = `
      <dl class="stat-kpis" aria-label="Kennzahlen im Zeitraum">
        ${kpiHtml("Besuche", count.format(current.visits), delta(current.visits, previous.visits))}
        ${kpiHtml("Artikelaufrufe", count.format(current.articleViews), delta(current.articleViews, previous.articleViews))}
        ${kpiHtml("In den Warenkorb", count.format(current.addToCart), delta(current.addToCart, previous.addToCart))}
        ${kpiHtml("Bestellungen", count.format(current.orders), delta(current.orders, previous.orders))}
        ${kpiHtml("Umsatz brutto", formatEuro(Number(current.revenue)), delta(Number(current.revenue), Number(previous.revenue)))}
        ${kpiHtml("Conversion-Rate", percent(current.conversionRate),
          delta(current.conversionRate == null ? null : Number(current.conversionRate),
            previous.conversionRate == null ? null : Number(previous.conversionRate), "points"))}
      </dl>
      ${chartHtml(stats, this.#preset)}
      <div class="stat-grid">
        <section class="chart-card" aria-labelledby="stat-articles-title">
          <h2 class="chart-card__title" id="stat-articles-title">Meistbesuchte Artikel</h2>
          ${topArticlesHtml(stats.topArticles)}
        </section>
        <div class="stat-grid__side">
          <section class="chart-card" aria-labelledby="stat-referrers-title">
            <h2 class="chart-card__title" id="stat-referrers-title">Herkunft der Besuche</h2>
            ${shareListHtml(referrers, "Noch keine Besuche in diesem Zeitraum.")}
          </section>
          <section class="chart-card" aria-labelledby="stat-devices-title">
            <h2 class="chart-card__title" id="stat-devices-title">Geräte</h2>
            ${shareListHtml(devices, "Noch keine Besuche in diesem Zeitraum.")}
          </section>
        </div>
      </div>
      ${visitorsHtml(current, previous)}
    `;
    window.lucide?.createIcons();
  }
}

customElements.define("partner-shop-statistics-page", PartnerShopStatisticsPage);
