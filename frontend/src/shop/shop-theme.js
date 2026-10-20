// Theme -> CSS custom properties (shop-theme spec). The font ids mirror the backend
// ShopFont registry; the families are self-hosted in shop-fonts.css.
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

// Readable text colour on a filled background (WCAG relative luminance).
function onColor(hex) {
  const channel = (offset) => {
    const value = parseInt(hex.slice(offset, offset + 2), 16) / 255;
    return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
  };
  const luminance = 0.2126 * channel(1) + 0.7152 * channel(3) + 0.0722 * channel(5);
  return luminance > 0.4 ? "#111111" : "#ffffff";
}

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

export function applyTheme(element, theme) {
  applyColors(element, theme.colors);
  element.style.setProperty("--shop-heading-font", SHOP_FONTS[theme.headingFont] ?? SYSTEM_STACK);
  element.style.setProperty("--shop-body-font", SHOP_FONTS[theme.bodyFont] ?? SYSTEM_STACK);
}
