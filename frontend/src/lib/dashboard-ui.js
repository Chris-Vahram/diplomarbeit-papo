// Shared by both dashboards: monthly revenue chart and the period picker (presets,
// Von/Bis, "Gesamter Zeitraum"). A page provides `period`, `periodPreset` and `render()`.
import { toLocalIsoDate, presetPeriod, PERIOD_PRESETS } from "./dashboard-data.js";
import { showToast } from "../components/toast-notice.js";

const eur = new Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" });
const dateLabel = new Intl.DateTimeFormat("de-AT", { day: "2-digit", month: "2-digit", year: "numeric" });

function formatCompactEur(value) {
  return value >= 1000 ? `${(value / 1000).toFixed(1).replace(".", ",")} T€` : eur.format(value);
}

export function renderMonthChart(title, monthLabels, monthlyValues) {
  const max = Math.max(1, ...monthlyValues);
  const lastIndex = monthlyValues.length - 1;
  const bars = monthlyValues
    .map((value, i) => {
      const pct = Math.max(2, Math.round((value / max) * 100));
      return `
        <div class="month-chart__bar-wrap">
          <div class="month-chart__bar${i === lastIndex ? " month-chart__bar--current" : ""}" style="height:${pct}%" title="${monthLabels[i] ?? ""}: ${eur.format(value)}"></div>
        </div>`;
    })
    .join("");
  const xlabels = monthLabels.map((m) => `<span class="month-chart__xlabel">${m}</span>`).join("");

  return `
    <div class="metric-card metric-card--chart month-chart-card">
      <div class="metric-card__watermark"><i data-lucide="line-chart"></i></div>
      <div class="metric-card__chart-label">${title}</div>
      <div class="month-chart">
        <div class="month-chart__bars">${bars}</div>
        <div class="month-chart__yaxis">
          <span>${formatCompactEur(max)}</span>
          <span>${formatCompactEur(max / 2)}</span>
          <span>0€</span>
        </div>
        <div class="month-chart__xaxis">${xlabels}</div>
      </div>
    </div>`;
}

export function periodPickerHtml() {
  return `
<div class="period-picker">
  <button type="button" class="period-picker__button" id="period-button">
    <i data-lucide="calendar"></i>
    <span id="period-label"></span>
    <i data-lucide="chevron-down" class="period-picker__caret"></i>
  </button>
  <div class="period-picker__panel is-closed" id="period-panel">
    <div class="period-picker__presets">
      ${Object.entries(PERIOD_PRESETS).map(([kind, label]) => `<button type="button" data-preset="${kind}">${label}</button>`).join("")}
    </div>
    <div class="period-picker__field">
      <label for="period-from">Von</label>
      <input type="date" id="period-from" />
    </div>
    <div class="period-picker__field">
      <label for="period-to">Bis</label>
      <input type="date" id="period-to" />
    </div>
    <div class="period-picker__actions">
      <button type="button" class="period-picker__reset" id="period-reset">Gesamter Zeitraum</button>
      <button type="button" class="period-picker__apply" id="period-apply">Anwenden</button>
    </div>
  </div>
</div>`;
}

export function wirePeriodPicker(page) {
  page.periodButton = page.querySelector("#period-button");
  page.periodPanel = page.querySelector("#period-panel");
  page.periodFromInput = page.querySelector("#period-from");
  page.periodToInput = page.querySelector("#period-to");

  updatePeriodUi(page);

  page.periodButton.addEventListener("click", (event) => {
    event.stopPropagation();
    page.periodPanel.classList.toggle("is-closed");
  });
  document.addEventListener("click", (event) => {
    if (!page.contains(event.target)) page.periodPanel.classList.add("is-closed");
  });
  page.periodPanel.addEventListener("click", (event) => event.stopPropagation());

  for (const button of page.periodPanel.querySelectorAll("[data-preset]")) {
    button.addEventListener("click", () => {
      page.periodPreset = button.dataset.preset;
      page.period = presetPeriod(page.periodPreset);
      page.periodPanel.classList.add("is-closed");
      updatePeriodUi(page);
      page.render();
    });
  }

  page.querySelector("#period-apply").addEventListener("click", () => {
    const [fy, fm, fd] = page.periodFromInput.value.split("-").map(Number);
    const [ty, tm, td] = page.periodToInput.value.split("-").map(Number);
    const from = new Date(fy, fm - 1, fd, 0, 0, 0, 0);
    const to = new Date(ty, tm - 1, td, 23, 59, 59, 999);
    if (isNaN(from) || isNaN(to) || from > to) {
      showToast("Bitte einen gültigen Zeitraum wählen.", "error");
      return;
    }
    page.period = { from, to };
    page.periodPreset = null;
    page.periodPanel.classList.add("is-closed");
    updatePeriodUi(page);
    page.render();
  });

  page.querySelector("#period-reset").addEventListener("click", () => {
    page.period = null;
    page.periodPreset = null;
    page.periodPanel.classList.add("is-closed");
    updatePeriodUi(page);
    page.render();
  });
}

function updatePeriodUi(page) {
  for (const button of page.periodPanel.querySelectorAll("[data-preset]")) {
    button.setAttribute("aria-pressed", String(button.dataset.preset === page.periodPreset));
  }
  if (page.period) {
    page.periodFromInput.value = toLocalIsoDate(page.period.from);
    page.periodToInput.value = toLocalIsoDate(page.period.to);
    page.querySelector("#period-label").textContent = page.periodPreset
      ? PERIOD_PRESETS[page.periodPreset]
      : `${dateLabel.format(page.period.from)} – ${dateLabel.format(page.period.to)}`;
  } else {
    page.periodFromInput.value = "";
    page.periodToInput.value = "";
    page.querySelector("#period-label").textContent = "Gesamter Zeitraum";
  }
}
