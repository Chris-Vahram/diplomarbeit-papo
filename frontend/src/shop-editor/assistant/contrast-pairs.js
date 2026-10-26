import { contrast, mix, onColor } from "../../shop/contrast.js";

// Text/background pairs as shop.css draws them (shop-design-assistant spec "Contrast rule").
// Mirrored in the backend by ShopDesignRules; keep both in step with shop.css.
//
// A pair: { key, label, foreground, background, min, role } - role is the theme colour that
// fixes it (the text colour, or the fill when the text colour is chosen automatically).

export const TEXT_MIN = 4.5;
export const UI_MIN = 3;

// Blocker below 3:1 for every pair, Warnung below the pair's own minimum.
export function severityOf(ratio, min = TEXT_MIN) {
  if (ratio < UI_MIN) return "blocker";
  if (ratio < min) return "warning";
  return null;
}

const pair = (key, label, foreground, background, min, role) => ({ key, label, foreground, background, min, role });

// --shop-muted: text mixed into the page background.
const muted = (colors) => mix(colors.text, colors.background, 0.68);

// Pairs that use the theme colours alone; every page shows them.
export function themePairs(colors) {
  return [
    pair("body", "Text auf dem Seitenhintergrund", colors.text, colors.background, TEXT_MIN, "text"),
    pair("body-muted", "Nebentext auf dem Seitenhintergrund", muted(colors), colors.background, TEXT_MIN, "text"),
    pair("surface", "Text auf Flächen (Kopfzeile, Fußzeile, Karten)", colors.text, colors.surface, TEXT_MIN, "text"),
    pair("surface-muted", "Nebentext auf Flächen", muted(colors), colors.surface, TEXT_MIN, "text"),
    pair("navigation", "Markierung des aktuellen Menüpunkts", colors.primary, colors.surface, UI_MIN, "primary"),
    pair("price", "Preise auf Artikelkarten", colors.primary, colors.surface, TEXT_MIN, "primary"),
    pair("on-primary", "Text auf der Hauptfarbe", onColor(colors.primary), colors.primary, TEXT_MIN, "primary"),
    pair("on-accent", "Text auf der Akzentfarbe und auf Schaltflächen", onColor(colors.accent), colors.accent, TEXT_MIN, "accent"),
  ];
}

const ARTICLE_TYPES = new Set(["article-grid", "featured-articles", "article-detail"]);

// Pairs one block draws with its effective colours (theme plus its own overrides).
export function blockPairs(block, colors, form = "klar") {
  const config = block.config ?? {};
  const pairs = [];
  const button = () => {
    if (config.buttonLabel) pairs.push(pair("button", "Schaltfläche", onColor(colors.accent), colors.accent, TEXT_MIN, "accent"));
  };
  // A hero photo changes the ground: "handwerk" sets the text on a surface panel, "kraeftig"
  // on a primary scrim over the photo; "klar" keeps it beside the photo on the block background.
  if (block.type === "hero" && config.imageId && form !== "klar") {
    if (form === "handwerk") pairs.push(pair("panel", "Text im Textfeld über dem Foto", colors.text, colors.surface, TEXT_MIN, "text"));
    else pairs.push(pair("scrim", "Text auf dem Foto", onColor(colors.primary), colors.primary, TEXT_MIN, "primary"));
    button();
    return pairs;
  }
  switch (config.background ?? "none") {
    case "primary":
      pairs.push(pair("on-primary", "Text auf der Hauptfarbe", onColor(colors.primary), colors.primary, TEXT_MIN, "primary"));
      break;
    case "accent":
      pairs.push(pair("on-accent", "Text auf der Akzentfarbe", onColor(colors.accent), colors.accent, TEXT_MIN, "accent"));
      break;
    case "surface":
      pairs.push(pair("surface", "Text auf der Fläche", colors.text, colors.surface, TEXT_MIN, "text"));
      pairs.push(pair("surface-muted", "Nebentext auf der Fläche", muted(colors), colors.surface, TEXT_MIN, "text"));
      break;
    default:
      pairs.push(pair("body", "Text", colors.text, colors.background, TEXT_MIN, "text"));
      pairs.push(pair("body-muted", "Nebentext", muted(colors), colors.background, TEXT_MIN, "text"));
  }
  if (ARTICLE_TYPES.has(block.type)) {
    pairs.push(pair("card", "Text auf Artikelkarten", colors.text, colors.surface, TEXT_MIN, "text"));
    pairs.push(pair("card-muted", "Kategorie auf Artikelkarten", muted(colors), colors.surface, TEXT_MIN, "text"));
    pairs.push(pair("price", "Preise", colors.primary, colors.surface, TEXT_MIN, "primary"));
  }
  button();
  return pairs;
}

export function effectiveColors(theme, block) {
  return { ...theme.colors, ...(block.config?.colors ?? {}) };
}

// Failing pairs with ratio and severity.
export function failing(pairs) {
  return pairs
    .map((candidate) => ({ ...candidate, ratio: contrast(candidate.foreground, candidate.background) }))
    .map((candidate) => ({ ...candidate, severity: severityOf(candidate.ratio, candidate.min) }))
    .filter((candidate) => candidate.severity);
}

// A block pair that is the same as a theme pair is the theme's problem, reported once there.
export const pairIdentity = (candidate) =>
  `${candidate.foreground.toLowerCase()}|${candidate.background.toLowerCase()}|${candidate.min}`;
