import { escapeHtml } from "../../lib/html.js";
import { countBySeverity, SEVERITIES } from "./assistant.js";

// Markup of the design assistant's findings (shop-design-assistant spec "Plain-language
// explanations"), shared by the inspector, the Design tab and the publish dialog. Every item
// has severity as icon plus word, never colour alone; the "Zum Problem springen" buttons carry
// the finding id and are handled by the builder ("assistant-jump").

const PLURAL = { blocker: ["Blocker", "Blocker"], warning: ["Warnung", "Warnungen"], tip: ["Tipp", "Tipps"] };

export function countText(severity, count) {
  return `${count} ${PLURAL[severity][count === 1 ? 0 : 1]}`;
}

// "1 Blocker · 2 Warnungen" - Tipps only when nothing else is open.
export function summaryText(findings) {
  const counts = countBySeverity(findings);
  const serious = ["blocker", "warning"].filter((severity) => counts[severity]);
  if (serious.length) return serious.map((severity) => countText(severity, counts[severity])).join(" · ");
  return counts.tip ? countText("tip", counts.tip) : "Alles in Ordnung";
}

export function worstSeverity(findings) {
  return findings[0]?.severity ?? null;
}

export function badgeHtml(severity) {
  const meta = SEVERITIES[severity];
  return `<span class="assistant-badge assistant-badge--${severity}"><i data-lucide="${meta.icon}" aria-hidden="true"></i>${meta.label}</span>`;
}

// where: optional (finding) => "Abschnitt 2 · Bild" for findings shown away from their block.
function itemHtml(finding, { where, jump }) {
  const place = where?.(finding);
  const canJump = jump && finding.target;
  return `<li class="assistant-item assistant-item--${finding.severity}${finding.emphasis ? " is-emphasis" : ""}">
    <div class="assistant-item__head">${badgeHtml(finding.severity)}${place ? `<span class="assistant-item__place">${escapeHtml(place)}</span>` : ""}</div>
    <p class="assistant-item__title">${escapeHtml(finding.title)}</p>
    <p class="assistant-item__why">${escapeHtml(finding.why)}</p>
    <p class="assistant-item__fix">${escapeHtml(finding.fix)}</p>
    ${canJump ? `<button type="button" class="assistant-item__jump" data-jump="${escapeHtml(finding.id)}">Zum Problem springen<i data-lucide="arrow-right" aria-hidden="true"></i></button>` : ""}
  </li>`;
}

// Blocker and Warnung open, Tipps folded away (design.md risk "Too many hints annoy").
// tipsOpen: the partner's last choice, so re-rendering while editing keeps it.
export function findingsHtml(findings, { where = null, jump = true, tipsOpen = false } = {}) {
  const serious = findings.filter((finding) => finding.severity !== "tip");
  const tips = findings.filter((finding) => finding.severity === "tip");
  const list = (items) => `<ul class="assistant-list">${items.map((finding) => itemHtml(finding, { where, jump })).join("")}</ul>`;
  return `${serious.length ? list(serious) : ""}
    ${tips.length ? `<details class="assistant-tips" data-assistant-tips${tipsOpen ? " open" : ""}>
      <summary><i data-lucide="${SEVERITIES.tip.icon}" aria-hidden="true"></i><span>${countText("tip", tips.length)} für einen besseren Shop</span></summary>
      ${list(tips)}
    </details>` : ""}`;
}
