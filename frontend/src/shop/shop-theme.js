// Theme -> CSS custom properties (shop-theme spec). The font ids mirror the backend
// ShopFont registry; the families are self-hosted in shop-fonts.css.
import { onColor } from "./contrast.js";

const SYSTEM_STACK = 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif';

export const SHOP_FONTS = {
  system: SYSTEM_STACK,
  "plus-jakarta-sans": `"Plus Jakarta Sans", ${SYSTEM_STACK}`,
  inter: `"Inter", ${SYSTEM_STACK}`,
  "dm-sans": `"DM Sans", ${SYSTEM_STACK}`,
  "work-sans": `"Work Sans", ${SYSTEM_STACK}`,
  nunito: `"Nunito", ${SYSTEM_STACK}`,
  montserrat: `"Montserrat", ${SYSTEM_STACK}`,
  "source-sans-3": `"Source Sans 3", ${SYSTEM_STACK}`,
  "open-sans": `"Open Sans", ${SYSTEM_STACK}`,
  lora: `"Lora", Georgia, serif`,
  merriweather: `"Merriweather", Georgia, serif`,
  "playfair-display": `"Playfair Display", Georgia, serif`,
};

const COLOR_ROLES = ["primary", "accent", "background", "surface", "text"];
const HEX = /^#[0-9a-f]{6}$/i;

// Sets the given colour roles on an element. Used for the theme (document root) and for
// block overrides (block host); a block without overrides inherits the theme.
// Values are re-checked here because they end up in CSS.
export function applyColors(element, colors = {}) {
  for (const role of COLOR_ROLES) {
    const value = colors[role];
    if (typeof value !== "string" || !HEX.test(value)) continue;
    element.style.setProperty(`--shop-${role}`, value);
    if (role === "primary" || role === "accent") {
      element.style.setProperty(`--shop-on-${role}`, onColor(value));
    }
  }
}

const FORMS = new Set(["klar", "handwerk", "kraeftig"]);

export function applyTheme(element, theme) {
  applyColors(element, theme.colors);
  // Form preset of the theme (shop.css [data-form]); older themes have none and mean "klar".
  element.dataset.form = FORMS.has(theme.form) ? theme.form : "klar";
  element.style.setProperty("--shop-heading-font", SHOP_FONTS[theme.headingFont] ?? SYSTEM_STACK);
  element.style.setProperty("--shop-body-font", SHOP_FONTS[theme.bodyFont] ?? SYSTEM_STACK);
}
