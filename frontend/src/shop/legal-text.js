// Restricted Markdown for the company's shop legal texts (shop-legal spec, design decision 1).
// Supported: paragraphs (blank line), line breaks inside a paragraph, "#"/"##"/"###" headings,
// "- " or "* " lists, "1. " lists, [Text](https://…) links and bare https:// / mailto / e-mail
// addresses. Everything else, HTML included, is shown as text: every piece of input passes
// through escapeHtml, and only the tags written here are ever produced.
import { escapeHtml } from "../lib/html.js";

const SAFE_URL = /^(https?:\/\/|mailto:|tel:)/i;
// [text](url) | https://… | name@domain.tld
const INLINE = /\[([^\]\n]+)\]\(([^)\s"']+)\)|(https?:\/\/[^\s<>()"']+[^\s<>()"'.,;:!?])|([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,})/g;

function link(href, label) {
  const external = /^https?:/i.test(href);
  return `<a href="${escapeHtml(href)}"${external ? ' rel="noopener noreferrer" target="_blank"' : ""}>${escapeHtml(label)}</a>`;
}

function inline(text) {
  let html = "";
  let last = 0;
  for (const match of text.matchAll(INLINE)) {
    html += escapeHtml(text.slice(last, match.index));
    const [whole, label, href, url, email] = match;
    if (label !== undefined) {
      // A link with a scheme we do not allow (javascript:, data:, …) stays visible as typed.
      html += SAFE_URL.test(href) ? link(href, label) : escapeHtml(whole);
    } else if (url) {
      html += link(url, url);
    } else {
      html += link(`mailto:${email}`, email);
    }
    last = match.index + whole.length;
  }
  return html + escapeHtml(text.slice(last));
}

const HEADING = /^(#{1,3})\s+(.+)$/;
const BULLET = /^[-*]\s+(.+)$/;
const NUMBERED = /^\d{1,3}[.)]\s+(.+)$/;

// skipTitle: a leading heading equal to the page title is dropped, the page shows its own h1.
export function renderLegalText(source, { skipTitle } = {}) {
  const blocks = String(source ?? "").replace(/\r\n?/g, "\n").split(/\n\s*\n/);
  let html = "";
  blocks.forEach((block, index) => {
    const lines = block.split("\n").map((line) => line.trim()).filter(Boolean);
    if (!lines.length) return;
    let rest = lines;
    // Headings are single lines; text directly below one starts the next paragraph.
    const heading = rest[0].match(HEADING);
    if (heading) {
      const title = heading[2].trim();
      const isPageTitle = index === 0 && skipTitle && title.toLowerCase() === skipTitle.toLowerCase();
      if (!isPageTitle) {
        const level = heading[1].length + 1; // "#" -> h2, the page h1 is the title
        html += `<h${level}>${inline(title)}</h${level}>`;
      }
      rest = rest.slice(1);
    }
    if (!rest.length) return;
    if (rest.every((line) => BULLET.test(line))) {
      html += `<ul>${rest.map((line) => `<li>${inline(line.match(BULLET)[1])}</li>`).join("")}</ul>`;
    } else if (rest.every((line) => NUMBERED.test(line))) {
      html += `<ol>${rest.map((line) => `<li>${inline(line.match(NUMBERED)[1])}</li>`).join("")}</ol>`;
    } else {
      html += `<p>${rest.map(inline).join("<br />")}</p>`;
    }
  });
  return html;
}
