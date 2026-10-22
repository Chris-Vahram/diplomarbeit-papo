import { escapeHtml } from "../../lib/html.js";

const PLACEHOLDERS = {
  title: "Titel",
  heading: "Überschrift",
  text: "Text",
  caption: "Bildunterschrift",
  name: "Name",
  role: "Funktion",
};

// One text of a block. data-field names its config field (inline editing in the editor
// preview). Empty texts render nothing publicly; in the editor preview (api.editing) they
// render empty with a placeholder, so they can still be filled in place.
export function textField(api, tag, className, field, value) {
  if (value) {
    return `<${tag} class="${className}" data-field="${field}">${escapeHtml(value)}</${tag}>`;
  }
  return api?.editing
    ? `<${tag} class="${className}" data-field="${field}" data-empty data-placeholder="${escapeHtml(PLACEHOLDERS[field] ?? "Text")} eingeben"></${tag}>`
    : "";
}
